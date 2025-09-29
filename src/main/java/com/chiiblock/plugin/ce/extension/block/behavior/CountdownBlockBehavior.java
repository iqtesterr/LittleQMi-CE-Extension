package com.chiiblock.plugin.ce.extension.block.behavior;

import com.chiiblock.plugin.ce.extension.block.entity.BlockEntityTypes;
import com.chiiblock.plugin.ce.extension.block.entity.CountdownBlockEntity;
import net.momirealms.craftengine.bukkit.block.behavior.BukkitBlockBehavior;
import net.momirealms.craftengine.core.block.BlockBehavior;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.block.behavior.EntityBlockBehavior;
import net.momirealms.craftengine.core.block.entity.BlockEntity;
import net.momirealms.craftengine.core.block.entity.BlockEntityType;
import net.momirealms.craftengine.core.plugin.context.PlayerOptionalContext;
import net.momirealms.craftengine.core.plugin.context.event.EventFunctions;
import net.momirealms.craftengine.core.plugin.context.function.Function;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.world.BlockPos;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@SuppressWarnings("all")
public class CountdownBlockBehavior extends BukkitBlockBehavior implements EntityBlockBehavior {
    public static final Factory FACTORY = new Factory();
    private final CountdownType countdownType;
    private LocalDateTime date;
    private int countdown;

    public enum CountdownType {
        ABSOLUTE, LOADED_ONLY
    }

    private final TreeMap<Long, List<Function<PlayerOptionalContext>>> functionsMap;

    public CountdownBlockBehavior(CustomBlock customBlock, LocalDateTime date, TreeMap<Long, List<Function<PlayerOptionalContext>>> functionsMap) {
        super(customBlock);
        this.countdownType = CountdownType.ABSOLUTE;
        this.date = date;
        this.functionsMap = functionsMap;
    }

    public CountdownBlockBehavior(CustomBlock customBlock, int countdown, TreeMap<Long, List<Function<PlayerOptionalContext>>> functionsMap) {
        super(customBlock);
        this.countdownType = CountdownType.LOADED_ONLY;
        this.countdown = countdown;
        this.functionsMap = functionsMap;
    }

    public CountdownType countdownType() {
        return countdownType;
    }

    public LocalDateTime date() {
        return date;
    }

    public int countdown() {
        return countdown;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos blockPos, ImmutableBlockState immutableBlockState) {
        return new CountdownBlockEntity(blockPos, immutableBlockState);
    }

    @Override
    public <T extends BlockEntity> BlockEntityType<T> blockEntityType() {
        return (BlockEntityType<T>) BlockEntityTypes.COUNTDOWN;
    }

    public static class Factory implements BlockBehaviorFactory {

        @Override
        public BlockBehavior create(CustomBlock block, Map<String, Object> args) {
            CountdownType mode = CountdownType.valueOf(args.getOrDefault("mode", "loaded_only").toString());
            //在这读取 execute 里面的list
            TreeMap<Long, List<Function<PlayerOptionalContext>>> functionsMap = parseFunctions(args.get("functions"), mode);
            if (mode == CountdownType.ABSOLUTE) {
                String input = args.getOrDefault("date", "").toString();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime date = LocalDateTime.parse(input, formatter);
                return new CountdownBlockBehavior(block, date, functionsMap);
            }
            int countdown = ResourceConfigUtils.getAsInt(args.getOrDefault("duration_ticks", 20 * 60 * 5), "duration_ticks");
            return new CountdownBlockBehavior(block, countdown, functionsMap);
        }

        private TreeMap<Long, List<Function<PlayerOptionalContext>>> parseFunctions(Object functionsObj, CountdownType mode) {
            TreeMap<Long, List<Function<PlayerOptionalContext>>> functionsMap = new TreeMap<>();

            if (functionsObj instanceof List<?> functionsList) {
                for (Object item : functionsList) {
                    if (item instanceof Map<?, ?> functionMap) {
                        Map<String, Object> funcConfig = MiscUtils.castToMap(functionMap, false);

                        // 解析时间字段
                        Object timeObj = funcConfig.get("time");
                        if (timeObj == null) continue;

                        Long timeKey = parseTimeBasedOnMode(timeObj, mode);

                        // 使用 CraftEngine 的 EventFunctions 解析 function
                        Function<PlayerOptionalContext> function = EventFunctions.fromMap(funcConfig);

                        functionsMap.computeIfAbsent(timeKey, k -> new ArrayList<>()).add(function);
                    }
                }
            }

            return functionsMap;
        }

        private Long parseTimeBasedOnMode(Object timeObj, CountdownType mode) {
            if (mode == CountdownType.LOADED_ONLY) {
                // tick 模式：期望整数
                if (timeObj instanceof Number) {
                    return ((Number) timeObj).longValue();
                } else {
                    throw new LocalizedResourceConfigException("Time must be an integer for LOADED_ONLY mode");
                }
            } else {
                // date 模式：期望日期字符串
                if (timeObj instanceof String) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    LocalDateTime dateTime = LocalDateTime.parse((String) timeObj, formatter);
                    return dateTime.toEpochSecond(ZoneOffset.UTC);
                } else {
                    throw new LocalizedResourceConfigException("Time must be a date string for ABSOLUTE mode");
                }
            }
        }
    }
}

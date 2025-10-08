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
import net.momirealms.craftengine.core.block.entity.tick.BlockEntityTicker;
import net.momirealms.craftengine.core.plugin.context.PlayerOptionalContext;
import net.momirealms.craftengine.core.plugin.context.event.EventFunctions;
import net.momirealms.craftengine.core.plugin.context.function.Function;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.CEWorld;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;

@SuppressWarnings("all")
public class CountdownBlockBehavior extends BukkitBlockBehavior implements EntityBlockBehavior {
    public static final Factory FACTORY = new Factory();
    private final CountdownType countdownType;
    private LocalDateTime date;
    private int countdown;

    public enum CountdownType {
        ABSOLUTE, LOADED_ONLY
    }
    //private final TreeMap<Long, List<Function<PlayerOptionalContext>>> functionsMap;

    private static final ZoneId TORONTO_ZONE = ZoneId.of("America/Toronto");
    private long lastEpochSecond = -1L;


    public CountdownBlockBehavior(CustomBlock customBlock, LocalDateTime date) {
        super(customBlock);
        this.countdownType = CountdownType.ABSOLUTE;
        this.date = date;
    }

    public CountdownBlockBehavior(CustomBlock customBlock, int countdown) {
        super(customBlock);
        this.countdownType = CountdownType.LOADED_ONLY;
        this.countdown = countdown;
    }

    @Override
    public void tick(Object thisBlock, Object[] args, Callable<Object> superMethod) throws Exception {
        long current = ZonedDateTime.now(TORONTO_ZONE).toEpochSecond();
        if (lastEpochSecond < 0) {
            lastEpochSecond = current;
            return;
        }

        long passed = current - lastEpochSecond;
        if (passed <= 0) return;

        String nowStr = java.time.ZonedDateTime.now(TORONTO_ZONE)
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        System.out.println("[Countdown] Now (Toronto): " + nowStr + " | passed=" + passed);

        lastEpochSecond = current;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos blockPos, ImmutableBlockState immutableBlockState) {
        return new CountdownBlockEntity(blockPos, immutableBlockState);
    }

    @Override
    public <T extends BlockEntity> BlockEntityType<T> blockEntityType() {
        return (BlockEntityType<T>) BlockEntityTypes.COUNTDOWN;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> createSyncBlockEntityTicker(CEWorld level, ImmutableBlockState state, BlockEntityType<T> blockEntityType) {
        return EntityBlockBehavior.super.createSyncBlockEntityTicker(level, state, blockEntityType);
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

    public static class Factory implements BlockBehaviorFactory {

        @Override
        public BlockBehavior create(CustomBlock block, Map<String, Object> args) {
            //CountdownType mode = CountdownType.valueOf(args.getOrDefault("mode", "loaded_only").toString());
            //TreeMap<Long, List<Function<PlayerOptionalContext>>> functionsMap = parseFunctions(args.get("functions"), mode);
            //if (true) {
                String input = args.getOrDefault("date", "").toString();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime date = LocalDateTime.parse(input, formatter).now(TORONTO_ZONE);
                return new CountdownBlockBehavior(block, date);
            //}
            /*int countdown = ResourceConfigUtils.getAsInt(args.getOrDefault("duration_ticks", 20 * 60 * 5), "duration_ticks");
            return new CountdownBlockBehavior(block, countdown, null);*/
        }

        private TreeMap<Long, List<Function<PlayerOptionalContext>>> parseFunctions(Object functionsObj, CountdownType mode) {
            TreeMap<Long, List<Function<PlayerOptionalContext>>> functionsMap = new TreeMap<>();

            if (functionsObj instanceof List<?> functionsList) {
                for (Object item : functionsList) {
                    if (item instanceof Map<?, ?> functionMap) {
                        Map<String, Object> funcConfig = MiscUtils.castToMap(functionMap, false);

                        Object timeObj = funcConfig.get("time");
                        if (timeObj == null) continue;

                        Long timeKey = parseTimeBasedOnMode(timeObj, mode);


                        Function<PlayerOptionalContext> function = EventFunctions.fromMap(funcConfig);

                        functionsMap.computeIfAbsent(timeKey, k -> new ArrayList<>()).add(function);
                    }
                }
            }

            return functionsMap;
        }

        private Long parseTimeBasedOnMode(Object timeObj, CountdownType mode) {
            if (mode == CountdownType.LOADED_ONLY) {
                // tick
                if (timeObj instanceof Number) {
                    return ((Number) timeObj).longValue();
                } else {
                    throw new LocalizedResourceConfigException("Time must be an integer for LOADED_ONLY mode");
                }
            } else {
                // date
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

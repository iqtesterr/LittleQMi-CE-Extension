package com.chiiblock.plugin.ce.extension.block.behavior;

import com.chiiblock.plugin.ce.extension.block.entity.CountdownBlockEntity;
import net.momirealms.craftengine.bukkit.block.behavior.BukkitBlockBehavior;
import net.momirealms.craftengine.core.block.BlockBehavior;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.block.behavior.EntityBlockBehavior;
import net.momirealms.craftengine.core.block.entity.BlockEntity;
import net.momirealms.craftengine.core.block.entity.BlockEntityType;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.world.BlockPos;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@SuppressWarnings("all")
public class CountdownBlockBehavior extends BukkitBlockBehavior implements EntityBlockBehavior {
    public static final Factory FACTORY = new Factory();
    private final CountdownType countdownType;
    private LocalDateTime date;
    private int countdown;

    public enum CountdownType {
        ABSOLUTE, LOADED_ONLY
    }

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
    public <T extends BlockEntity> BlockEntityType<T> blockEntityType() {
        return (BlockEntityType<T>) CountdownBlockEntity.COUNTDOWN;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos blockPos, ImmutableBlockState immutableBlockState) {
        return new CountdownBlockEntity(blockPos, immutableBlockState);
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
            CountdownType mode = CountdownType.valueOf(args.getOrDefault("mode", "loaded_only").toString());
            if (mode == CountdownType.ABSOLUTE) {
                String input = args.getOrDefault("date", "").toString();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime date = LocalDateTime.parse(input, formatter);
                return new CountdownBlockBehavior(block, date);
            }
            int countdown = ResourceConfigUtils.getAsInt(args.getOrDefault("duration_ticks", 20 * 60 * 5), "duration_ticks");
            return new CountdownBlockBehavior(block, countdown);
        }
    }
}

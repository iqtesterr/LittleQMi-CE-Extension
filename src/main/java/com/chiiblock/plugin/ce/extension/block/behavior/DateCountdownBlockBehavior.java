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
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.CEWorld;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Let us apart date_countdown and tick_countdown in next refactor
 */
@SuppressWarnings("all")
public class DateCountdownBlockBehavior extends BukkitBlockBehavior implements EntityBlockBehavior {
    public static final Factory FACTORY = new Factory();
    public static final ZoneId TORONTO_ZONE = ZoneId.of("America/Toronto");
    public long targetEpochSecond;

    public DateCountdownBlockBehavior(CustomBlock customBlock, long targetEpochSecond) {
        super(customBlock);
        this.targetEpochSecond = targetEpochSecond;
    }

    @Override
    public <T extends BlockEntity> BlockEntityType<T> blockEntityType() {
        return EntityBlockBehavior.blockEntityTypeHelper(BlockEntityTypes.COUNTDOWN);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos blockPos, ImmutableBlockState state) {
        return new CountdownBlockEntity(blockPos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> createSyncBlockEntityTicker(CEWorld level, ImmutableBlockState state, BlockEntityType<T> blockEntityType) {
        return EntityBlockBehavior.createTickerHelper(CountdownBlockEntity::tick);
    }

    public static class Factory implements BlockBehaviorFactory {

        @Override
        public BlockBehavior create(CustomBlock block, Map<String, Object> args) {
            String input = args.getOrDefault("date", "").toString();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime date = LocalDateTime.parse(input, formatter);
            ZonedDateTime zoned = date.atZone(TORONTO_ZONE);
            return new DateCountdownBlockBehavior(block, zoned.toEpochSecond());
        }
    }
}

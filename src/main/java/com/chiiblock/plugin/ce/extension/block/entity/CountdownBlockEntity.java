package com.chiiblock.plugin.ce.extension.block.entity;

import com.chiiblock.plugin.ce.extension.block.behavior.CountdownBlockBehavior;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.entity.BlockEntity;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.libraries.nbt.CompoundTag;

import java.util.Optional;

/**
 * Due to variety issue that dynamic updating the behavior such as countdown, mode etc.
 * This entity will following its previous behavior instance.
 * When it being reconstruct, it will follow the new countdown, and maintain how long it elapsed.
 * If the new countdown is shorter than it elapsed, then the correspond timing action will be trigger.
 */
public class CountdownBlockEntity extends BlockEntity {
    private final CountdownBlockBehavior behavior;

    private boolean active;

    // Tick
    private int elapsed;
    private long lastScheduleTicks;

    public CountdownBlockEntity(BlockPos pos, ImmutableBlockState blockState) {
        super(BlockEntityTypes.COUNTDOWN, pos, blockState);
        this.behavior = super.blockState.behavior().getAs(CountdownBlockBehavior.class).orElseThrow();
    }

    public void execute(long currentTick) {

    }

    @Override
    public void loadCustomData(CompoundTag tag) {
        if (behavior.countdownType() == CountdownBlockBehavior.CountdownType.LOADED_ONLY) {
            elapsed = Optional.of(tag.getInt("elapsed")).orElse(0);
        }
    }

    @Override
    protected void saveCustomData(CompoundTag tag) {
        if (behavior.countdownType() == CountdownBlockBehavior.CountdownType.LOADED_ONLY) {
            // Todo Gather Latest Data
            tag.putInt("elapsed", elapsed);
        }
    }

    @Override
    public void preRemove() {

    }
}

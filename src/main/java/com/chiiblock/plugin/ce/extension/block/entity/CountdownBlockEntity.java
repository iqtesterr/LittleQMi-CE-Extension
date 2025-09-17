package com.chiiblock.plugin.ce.extension.block.entity;

import com.chiiblock.plugin.ce.extension.block.behavior.CountdownBlockBehavior;
import net.momirealms.craftengine.bukkit.block.entity.BukkitBlockEntityTypes;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.entity.BlockEntity;
import net.momirealms.craftengine.core.block.entity.BlockEntityType;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.libraries.nbt.CompoundTag;

public class CountdownBlockEntity extends BlockEntity {
    public static final BlockEntityType<CountdownBlockEntity> COUNTDOWN = BukkitBlockEntityTypes.register(Key.of("xiaoqmi:countdown"), CountdownBlockEntity::new);
    private final CountdownBlockBehavior behavior;

    private CountdownBlockEntity(BlockPos pos, ImmutableBlockState blockState) {
        super(COUNTDOWN, pos, blockState);
        this.behavior = super.blockState.behavior().getAs(CountdownBlockBehavior.class).orElseThrow();
    }

    @Override
    public void loadCustomData(CompoundTag tag) {
        super.loadCustomData(tag);
    }

    @Override
    protected void saveCustomData(CompoundTag tag) {
        super.saveCustomData(tag);
    }
}

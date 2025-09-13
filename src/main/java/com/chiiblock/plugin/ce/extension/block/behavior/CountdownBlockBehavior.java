package com.chiiblock.plugin.ce.extension.block.behavior;

import net.momirealms.craftengine.bukkit.block.behavior.BukkitBlockBehavior;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.behavior.EntityBlockBehavior;
import net.momirealms.craftengine.core.block.entity.BlockEntity;
import net.momirealms.craftengine.core.block.entity.BlockEntityType;
import net.momirealms.craftengine.core.block.entity.tick.BlockEntityTicker;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.CEWorld;

public class CountdownBlockBehavior extends BukkitBlockBehavior implements EntityBlockBehavior {

    public CountdownBlockBehavior(CustomBlock customBlock) {
        super(customBlock);
    }

    @Override
    public <T extends BlockEntity> BlockEntityType<T> blockEntityType() {
        return null;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos blockPos, ImmutableBlockState immutableBlockState) {
        return null;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> createBlockEntityTicker(CEWorld level, ImmutableBlockState state, BlockEntityType<T> blockEntityType) {
        return EntityBlockBehavior.super.createBlockEntityTicker(level, state, blockEntityType);
    }
}

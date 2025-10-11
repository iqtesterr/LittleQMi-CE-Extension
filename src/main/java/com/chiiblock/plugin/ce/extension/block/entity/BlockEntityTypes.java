package com.chiiblock.plugin.ce.extension.block.entity;

import net.momirealms.craftengine.core.block.entity.BlockEntityType;

public class BlockEntityTypes extends net.momirealms.craftengine.core.block.entity.BlockEntityTypes {
    public static final BlockEntityType<CountdownBlockEntity> COUNTDOWN = register(BlockEntityTypeKeys.COUNTDOWN, CountdownBlockEntity::new);
    public static final BlockEntityType<FanBlockEntity> FAN = register(BlockEntityTypeKeys.FAN, FanBlockEntity::new);
}

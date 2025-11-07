package com.chiiblock.plugin.ce.extension.block.entity;

import net.momirealms.craftengine.core.block.entity.BlockEntityType;

public class BlockEntityTypes extends net.momirealms.craftengine.core.block.entity.BlockEntityTypes {
    public static final BlockEntityType<CountdownBlockEntity> COUNTDOWN = register(BlockEntityTypeKeys.COUNTDOWN);
    public static final BlockEntityType<FanBlockEntity> FAN = register(BlockEntityTypeKeys.FAN);
    public static final BlockEntityType<HoloBlockEntity> HOLO = register(BlockEntityTypeKeys.HOLO);
}

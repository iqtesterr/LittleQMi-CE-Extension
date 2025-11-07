package com.chiiblock.plugin.ce.extension.block.behavior;

import net.momirealms.craftengine.core.util.Key;

public class BlockBehaviors extends net.momirealms.craftengine.core.block.behavior.BlockBehaviors {
    public static final Key COUNTDOWN_BLOCK = Key.of("littleqmi:date_countdown_block");
    public static final Key SAFE_FENCE_BLOCK = Key.of("littleqmi:safe_fence_block");
    public static final Key FAN_BLOCK = Key.of("littleqmi:fan_block");
    public static final Key CAKE_BLOCK = Key.of("littleqmi:cake_block");
    public static final Key HOLO_BLOCK = Key.of("littleqmi:holo_block");

    public static void init() {
        register(COUNTDOWN_BLOCK, DateCountdownBlockBehavior.FACTORY);
        register(SAFE_FENCE_BLOCK, SafeFenceBlockBehavior.FACTORY);
        register(FAN_BLOCK, FanBlockBehavior.FACTORY);
        register(CAKE_BLOCK, CakeBlockBehaviours.FACTORY);
        register(HOLO_BLOCK, HoloBlockBehavior.FACTORY);
    }
}

package com.chiiblock.plugin.ce.extension.block.behavior;

import net.momirealms.craftengine.core.util.Key;

public class BlockBehaviors extends net.momirealms.craftengine.core.block.behavior.BlockBehaviors {
    public static final Key COUNTDOWN_BLOCK = Key.of("littleqmi:countdown_block");
    public static final Key SAFE_FENCE_BLOCK = Key.of("littleqmi:safe_fence_block");

    public static void init() {
        register(COUNTDOWN_BLOCK, CountdownBlockBehavior.FACTORY);
        register(SAFE_FENCE_BLOCK, SafeFenceBlockBehavior.FACTORY);
    }
}

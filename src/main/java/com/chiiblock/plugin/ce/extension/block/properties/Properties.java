package com.chiiblock.plugin.ce.extension.block.properties;

import com.chiiblock.plugin.ce.extension.block.behavior.SafeFenceBlockBehavior;
import net.momirealms.craftengine.core.block.properties.EnumProperty;
import net.momirealms.craftengine.core.util.Key;

public class Properties {
    public static final Key FENCE_SHAPE = Key.of("littleqmi:fence_shape");

    public static void init() {
        net.momirealms.craftengine.core.block.properties.Properties.register(FENCE_SHAPE, new EnumProperty.Factory<>(SafeFenceBlockBehavior.SafeFenceShape.class));
    }
}

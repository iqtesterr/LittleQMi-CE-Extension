package com.chiiblock.plugin.ce.extension.furniture.multihit;

import com.chiiblock.plugin.ce.extension.furniture.FurnitureManager;
import net.momirealms.craftengine.core.plugin.Manageable;

public class MultiHitModule implements Manageable {
    private final FurnitureManager manager;

    public MultiHitModule(FurnitureManager manager) {
        this.manager = manager;
    }

    @Override
    public void load() {
        if (!manager.multiHitModule()) return;
    }
}

package com.chiiblock.plugin.ce.extension.block;

import com.chiiblock.plugin.ce.extension.CEExtension;
import com.chiiblock.plugin.ce.extension.block.behavior.BlockBehaviors;
import com.chiiblock.plugin.ce.extension.block.countdown.CountdownModule;
import com.chiiblock.plugin.ce.extension.block.entity.BlockEntityTypes;
import net.momirealms.craftengine.core.plugin.Manageable;

public class BlockManager implements Manageable {
    private final CEExtension plugin;
    private final CountdownModule countdownModule;

    public BlockManager(CEExtension plugin) {
        this.plugin = plugin;
        this.countdownModule = new CountdownModule(this);
    }

    @Override
    public void load() {
        BlockBehaviors.init();
    }

    @Override
    public void unload() {

    }

    @Override
    public void disable() {
        this.unload();
        this.countdownModule.disable();
    }

    public CountdownModule countdownModule() {
        return countdownModule;
    }
}

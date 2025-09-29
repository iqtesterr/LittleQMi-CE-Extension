package com.chiiblock.plugin.ce.extension.block.countdown;

import com.chiiblock.plugin.ce.extension.CEExtension;
import com.chiiblock.plugin.ce.extension.block.BlockManager;
import net.momirealms.craftengine.core.plugin.Manageable;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;

public class CountdownModule implements Manageable {
    private final BlockManager manager;
    private final TickWheel tickWheel;
    private final TimeWheel timeWheel;
    private long tick;
    private CountdownTicker ticker;

    public CountdownModule(BlockManager manager) {
        this.manager = manager;
        this.tickWheel = new TickWheel(this);
        this.timeWheel = new TimeWheel();
        this.ticker = new CountdownTicker();
        Bukkit.getPluginManager().registerEvents(ticker, CEExtension.instance());
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(ticker);
        this.ticker = null;
        // wheel stop
    }

    public long currentTick() {
        return tick;
    }

    public void tick() {
        tick++;
        tickWheel.tick();
    }
}

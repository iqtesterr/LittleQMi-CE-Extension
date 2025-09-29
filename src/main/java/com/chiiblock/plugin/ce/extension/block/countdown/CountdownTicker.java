package com.chiiblock.plugin.ce.extension.block.countdown;

import com.chiiblock.plugin.ce.extension.CEExtension;
import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class CountdownTicker implements Listener {

    @EventHandler
    public void onTick(ServerTickEndEvent event) {
        CEExtension.instance().blockManager().countdownModule().tick();
    }
}

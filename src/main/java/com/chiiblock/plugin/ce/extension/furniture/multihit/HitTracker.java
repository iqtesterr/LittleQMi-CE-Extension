package com.chiiblock.plugin.ce.extension.furniture.multihit;

import net.momirealms.craftengine.bukkit.api.event.FurnitureAttemptBreakEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// Todo 管理计数，破坏
public class HitTracker implements Listener {
    private final Map<UUID, HitData> tracks = new HashMap<>();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onFurnitureBreak(FurnitureAttemptBreakEvent event) {
        int tick1 = Bukkit.getCurrentTick();
        int tick2 = Bukkit.getServer().getCurrentTick();
        event.getPlayer().sendMessage(tick1+"");
        event.getPlayer().sendMessage(tick2+"");
        event.setCancelled(true);
    }

    record HitData(int furnitureId, int hits, int time) {

    }
}

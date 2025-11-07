package com.chiiblock.plugin.ce.extension.plugin.gui;

import com.chiiblock.plugin.ce.extension.CEExtension;
import net.momirealms.craftengine.core.plugin.Manageable;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class GuiManager implements Listener, Manageable {
    private static GuiManager instance;
    private final CEExtension plugin;

    private final Map<UUID, ArrayList<Runnable>> tracking = new HashMap<>();

    public GuiManager(CEExtension plugin) {
        instance = this;
        this.plugin = plugin;
    }

    @Override
    public void load() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
        tracking.forEach((uuid, list) -> list.forEach(Runnable::run));
        tracking.clear();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        tracking.get(uuid).forEach(Runnable::run);
        tracking.remove(uuid);
    }

    public void register(UUID uuid, Runnable callback) {
        if (!instance.tracking.containsKey(uuid)) {
            ArrayList<Runnable> list = new ArrayList<>();
            list.add(callback);
            instance.tracking.put(uuid, list);
        } else {
            ArrayList<Runnable> list = instance.tracking.get(uuid);
            list.add(callback);
        }
    }

    public void unregister(UUID uuid, Runnable callback) {
        ArrayList<Runnable> list = instance.tracking.get(uuid);
        list.remove(callback);
        if (list.isEmpty()) {
            instance.tracking.remove(uuid);
        }
    }

    public static GuiManager getInstance() {
        return instance;
    }
}

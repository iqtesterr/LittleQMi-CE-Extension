package com.chiiblock.plugin.ce.extension.furniture.multihit;

import net.momirealms.craftengine.bukkit.api.event.FurnitureAttemptBreakEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HitTracker implements Listener {
    private final Map<UUID, HitData> tracks = new HashMap<>();
    private final MultiHitModule module;

    public HitTracker(MultiHitModule module) {
        this.module = module;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onFurnitureBreak(FurnitureAttemptBreakEvent event) {
        int tick = Bukkit.getCurrentTick();
        String id = event.furniture().id().asString();
        int furnitureId = event.furniture().baseEntityId();
        Player player = event.getPlayer();
        HitData hitData = tracks.get(player.getUniqueId());

        // First Hit
        if (hitData == null || hitData.furnitureId() != furnitureId) {
            HitRule rule = resolveRule(id);
            if (rule == null || rule.hits() <= 1) return;
            tracks.put(player.getUniqueId(), new HitData(furnitureId, 1, tick, rule));
            event.setCancelled(true);
            return;
        }

        HitRule rule = hitData.rule();
        int hit = hitData.hits();
        int interval = rule.intervals(hit);
        int tickBefore = hitData.tick();
        if (tick - tickBefore > interval) {
            // Overtime
            tracks.put(player.getUniqueId(), new HitData(furnitureId, 1, tick, rule));
            event.setCancelled(true);
            return;
        }

        int nextHit = hitData.hits() + 1;
        if (nextHit >= rule.hits()) {
            tracks.remove(player.getUniqueId());
        } else {
            tracks.put(player.getUniqueId(), new HitData(furnitureId, nextHit, tick, rule));
            event.setCancelled(true);
        }
    }

    private HitRule resolveRule(String furnitureId) {
        return module.ruleById(furnitureId).orElseGet(() -> module.global() ? module.globalHitRule() : null);
    }

    record HitData(int furnitureId, int hits, int tick, HitRule rule) { }
}

package com.chiiblock.plugin.ce.extension.furniture.multihit;

import com.chiiblock.plugin.ce.extension.CEExtension;
import com.chiiblock.plugin.ce.extension.furniture.FurnitureManager;
import net.momirealms.craftengine.core.plugin.Manageable;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MultiHitModule implements Manageable {
    private final FurnitureManager manager;
    private HitTracker hitTracker;

    private boolean global;
    private HitRule globalHitRule;

    private final Map<String, HitRule> rulesByIds = new HashMap<>();

    public MultiHitModule(FurnitureManager manager) {
        this.manager = manager;
    }

    @Override
    public void load() {
        if (!manager.multiHitModule()) return;
        loadConfig();
        hitTracker = new HitTracker(this);
        Bukkit.getPluginManager().registerEvents(hitTracker, CEExtension.instance());
    }

    @Override
    public void unload() {
        HandlerList.unregisterAll(hitTracker);
        hitTracker = null;
        rulesByIds.clear();
    }

    private void loadConfig() {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(manager.file());
        // Global
        global = config.contains("multi-hit.global");
        if (global) {
            int globalHits = config.getInt("multi-hit.global.hits", 2);
            Object globalIntervals = config.get("multi-hit.global.intervals");
            List<Integer> globalIntervalsList;
            if (globalIntervals instanceof List) {
                globalIntervalsList = ((List<?>) globalIntervals).stream()
                        .map(o -> Integer.parseInt(o.toString()))
                        .toList();
            } else {
                globalIntervalsList = List.of(config.getInt("multi-hit.global.intervals", 10));
            }
            globalHitRule = HitRule.builder().hits(globalHits).intervals(globalIntervalsList).build();
        }

        // Override
        if (!config.contains("multi-hit.override")) return;
        ConfigurationSection overrideSec = config.getConfigurationSection("multi-hit.override");
        assert overrideSec != null;
        for (String key : overrideSec.getKeys(false)) {
            int hits = overrideSec.getInt(key + ".hits", 1);
            Object intervals = overrideSec.get(key + ".intervals");
            List<Integer> intervalsList;
            if (intervals instanceof List) {
                intervalsList = ((List<?>) intervals).stream()
                        .map(o -> Integer.parseInt(o.toString()))
                        .toList();
            } else {
                intervalsList = List.of(overrideSec.getInt(key + ".intervals", 10));
            }
            List<String> furnitureIds = overrideSec.getStringList(key + ".list");
            HitRule rule = HitRule.builder().hits(hits).intervals(intervalsList).build();
            for (String id : furnitureIds) {
                rulesByIds.put(id, rule);
            }
        }
    }

    public boolean global() {
        return global;
    }

    public HitRule globalHitRule() {
        return globalHitRule;
    }

    public Optional<HitRule> ruleById(String id) {
        return Optional.ofNullable(rulesByIds.get(id));
    }
}

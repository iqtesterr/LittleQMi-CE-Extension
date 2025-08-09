package com.chiiblock.plugin.ce.extension.furniture;

import com.chiiblock.plugin.ce.extension.CEExtension;
import com.chiiblock.plugin.ce.extension.furniture.multihit.MultiHitModule;
import net.momirealms.craftengine.core.plugin.Manageable;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class FurnitureManager implements Manageable {
    private final CEExtension plugin;
    private final MultiHitModule multiHitModule;

    private boolean multiHit;

    public FurnitureManager(CEExtension plugin) {
        this.plugin = plugin;
        this.multiHitModule = new MultiHitModule(this);
    }

    @Override
    public void load() {
        loadConfig();
        multiHitModule.load();
    }

    private void loadConfig() {
        File file = new File(plugin.getDataFolder(), "configs/furniture.yml");
        if (!file.exists()) plugin.saveResource("configs/furniture.yml", false);
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        multiHit = config.getBoolean("multi-hit.enabled", true);
    }

    @Override
    public void unload() {
        multiHitModule.unload();
    }

    public boolean multiHitModule() {
        return multiHit;
    }
}

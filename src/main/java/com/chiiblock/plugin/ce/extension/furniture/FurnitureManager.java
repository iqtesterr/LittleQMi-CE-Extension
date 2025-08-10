package com.chiiblock.plugin.ce.extension.furniture;

import com.chiiblock.plugin.ce.extension.CEExtension;
import com.chiiblock.plugin.ce.extension.furniture.multihit.MultiHitModule;
import net.momirealms.craftengine.core.plugin.Manageable;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class FurnitureManager implements Manageable {
    private final CEExtension plugin;
    private final File file;
    private final MultiHitModule multiHitModule;

    private boolean multiHit;

    public FurnitureManager(CEExtension plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "configs/furniture.yml");
        this.multiHitModule = new MultiHitModule(this);
    }

    @Override
    public void load() {
        loadConfig();
        multiHitModule.load();
    }

    private void loadConfig() {
        if (!file.exists()) plugin.saveResource("configs/furniture.yml", false);
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        multiHit = config.getBoolean("multi-hit.enable", true);
    }

    @Override
    public void unload() {
        multiHitModule.unload();
    }

    public File file() {
        return file;
    }

    public boolean multiHitModule() {
        return multiHit;
    }
}

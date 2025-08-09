package com.chiiblock.plugin.ce.extension;

import org.bukkit.plugin.java.JavaPlugin;

public final class CEExtension extends JavaPlugin {
    private static CEExtension instance;

    @Override
    public void onEnable() {
        instance = this;
        this.getLogger().info("CE Extension Enabled");
    }

    @Override
    public void onDisable() {
        this.getLogger().info("CE Extension Disabled");
    }

    public static CEExtension instance() {
        return instance;
    }
}

package com.chiiblock.plugin.ce.extension;

import com.chiiblock.plugin.ce.extension.block.BlockManager;
import com.chiiblock.plugin.ce.extension.furniture.FurnitureManager;
import com.mojang.brigadier.Command;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;

public final class CEExtension extends JavaPlugin {
    private static CEExtension instance;

    private BlockManager blockManager;
    private FurnitureManager furnitureManager;

    @Override
    public void onEnable() {
        instance = this;
        this.blockManager = new BlockManager(this);
        this.furnitureManager = new FurnitureManager(this);
        this.registerCommands();
        this.reload();
        this.getLogger().info("CE Extension Enabled");
    }

    @Override
    public void onDisable() {
        this.blockManager.disable();
        this.furnitureManager.disable();
        this.getLogger().info("CE Extension Disabled");
    }

    public void reload() {
        this.blockManager.reload();
        this.furnitureManager.reload();
    }

    @SuppressWarnings("UnstableApiUsage")
    private void registerCommands() {
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(Commands.literal("littleqmi-ce-extension")
                    .then(Commands.literal("reload")
                            .executes(ctx -> {
                                reload();
                                ctx.getSource().getSender().sendMessage("CE Extension reloaded");
                                return Command.SINGLE_SUCCESS;
                            }).build())
                    .requires(src -> src.getSender().hasPermission("littleqmi-ce-extension.reload"))
                    .build());
        });
    }

    public static CEExtension instance() {
        return instance;
    }

    public FurnitureManager furnitureManager() {
        return furnitureManager;
    }

    public BlockManager blockManager() {
        return blockManager;
    }
}

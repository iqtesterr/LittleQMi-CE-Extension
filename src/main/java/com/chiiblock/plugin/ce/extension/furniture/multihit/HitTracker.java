package com.chiiblock.plugin.ce.extension.furniture.multihit;

import net.momirealms.craftengine.bukkit.api.event.FurnitureAttemptBreakEvent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
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


    /*@EventHandler
    public void onVanillaBlockInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null) return;
        if (CraftEngineBlocks.isCustomBlock(block)) return;
        BlockData blockData = block.getBlockData();
        if (!(blockData instanceof Door door)) return;
        CustomBlock customBlock = CraftEngineBlocks.byId(Key.of("default", "palm_door"));
        if (customBlock == null) return;
        event.setCancelled(true);

        Block otherHalf = door.getHalf() == Bisected.Half.BOTTOM
                ? block.getRelative(BlockFace.UP)
                : block.getRelative(BlockFace.DOWN);
        if (!(otherHalf.getBlockData() instanceof Door relativeDoor)) return;

        block.setType(Material.AIR, false);
        otherHalf.setType(Material.AIR, false);
        replaceDoorHalf(block, door, customBlock);
        replaceDoorHalf(otherHalf, relativeDoor, customBlock);
    }

    private void replaceDoorHalf(Block block, Door doorData, CustomBlock customBlock) {
        CompoundTag properties = createDoorProperties(doorData);
        ImmutableBlockState newState = customBlock.getBlockState(properties);
        CraftEngineBlocks.place(block.getLocation(), newState, UpdateOption.UPDATE_NONE, false);
    }

    private CompoundTag createDoorProperties(Door door) {
        CompoundTag properties = new CompoundTag();
        properties.putString("facing", door.getFacing().name().toLowerCase());
        properties.putString("half", door.getHalf() == Bisected.Half.BOTTOM ? "lower" : "upper" );
        properties.putString("hinge", door.getHinge().name().toLowerCase());
        properties.putBoolean("open", door.isOpen());
        properties.putBoolean("powered", door.isPowered());
        return properties;
    }*/

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onFurnitureAttemptToBreak(FurnitureAttemptBreakEvent event) {
        Player player = event.getPlayer();
        if (module.creative()) {
            if (player.getGameMode() == GameMode.CREATIVE) return;
        }

        int tick = Bukkit.getCurrentTick();
        String id = event.furniture().id().asString();
        int furnitureId = event.furniture().baseEntityId();
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

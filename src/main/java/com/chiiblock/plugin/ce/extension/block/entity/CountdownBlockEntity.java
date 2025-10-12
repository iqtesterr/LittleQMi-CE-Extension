package com.chiiblock.plugin.ce.extension.block.entity;

import com.chiiblock.plugin.ce.extension.block.behavior.DateCountdownBlockBehavior;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.entity.BlockEntity;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.CEWorld;
import net.momirealms.craftengine.libraries.nbt.CompoundTag;
import org.bukkit.World;

import java.time.ZonedDateTime;
import java.util.UUID;

import static com.chiiblock.plugin.ce.extension.block.behavior.DateCountdownBlockBehavior.TORONTO_ZONE;

/**
 * Due to variety issue that dynamic updating the behavior such as countdown, mode etc.
 * This entity will following its previous behavior instance.
 * When it being reconstruct, it will follow the new countdown, and maintain how long it elapsed.
 * If the new countdown is shorter than it elapsed, then the correspond timing action will be trigger.
 */
public class CountdownBlockEntity extends BlockEntity {
    private final DateCountdownBlockBehavior behavior;
    private ActiveMob activeMob;
    private UUID uuid;
    private long lastEpochSecond = -1L;
    private boolean activeCountdown = false;
    private boolean finish = false;

    public CountdownBlockEntity(BlockPos pos, ImmutableBlockState blockState) {
        super(BlockEntityTypes.COUNTDOWN, pos, blockState);
        this.behavior = super.blockState.behavior().getAs(DateCountdownBlockBehavior.class).orElseThrow();
    }

    @Override
    public void loadCustomData(CompoundTag tag) {
        uuid = tag.getUUID("entity");
    }

    @Override
    protected void saveCustomData(CompoundTag tag) {
        if (uuid != null) {
            tag.putUUID("entity", uuid);
        }
    }

    @Override
    public void preRemove() {
        if(activeMob != null) activeMob.despawn();
    }

    public static void tick(CEWorld ceWorld, BlockPos pos, ImmutableBlockState state, CountdownBlockEntity countdown) {
        MythicBukkit mythic = MythicBukkit.inst();
        if (countdown.uuid == null) {
            MythicMob mob = mythic.getMobManager().getMythicMob("countdown").orElseThrow();
            countdown.activeMob = mob.spawn(new AbstractLocation(BukkitAdapter.adapt((World) ceWorld.world().platformWorld()), pos.x() + 0.5, pos.y() + 1.5, pos.z() + 0.5), 0);
            countdown.uuid = countdown.activeMob.getUniqueId();
        } else if (countdown.activeMob == null) {
            mythic.getMobManager().getActiveMob(countdown.uuid).ifPresent(oldMob -> countdown.activeMob = oldMob);
            mythic.getAPIHelper().castSkill(countdown.activeMob.getEntity().getBukkitEntity(), "countdown_model");
        }
        if (countdown.finish) return;

        long current = ZonedDateTime.now(TORONTO_ZONE).toEpochSecond();
        if (countdown.lastEpochSecond < 0) {
            countdown.lastEpochSecond = current;
            return;
        }

        long remaining = countdown.behavior.targetEpochSecond - current;
        if (remaining <= 0) {
            mythic.getAPIHelper().castSkill(countdown.activeMob.getEntity().getBukkitEntity(), "cover_swap");
            countdown.finish = true;
            return;
        }

        // 24h
        if (remaining > 86400) return;
        if (current == countdown.lastEpochSecond) return;
        countdown.lastEpochSecond = current;

        if (!countdown.activeCountdown) {
            mythic.getAPIHelper().castSkill(countdown.activeMob.getEntity().getBukkitEntity(), "cover_flip");
            countdown.activeCountdown = true;
        }

        boolean lessThanHour = remaining < 3600;
        int hours = (int) (remaining / 3600);
        int minutes = (int) ((remaining % 3600) / 60);
        int seconds = (int) (remaining % 60);

        int d0, d1, d2, d3;
        if (!lessThanHour) {
            d0 = hours / 10;
            d1 = hours % 10;
            d2 = minutes / 10;
            d3 = minutes % 10;
        } else {
            d0 = minutes / 10;
            d1 = minutes % 10;
            d2 = seconds / 10;
            d3 = seconds % 10;
        }

        int[] digits = {d0, d1, d2, d3};
        for (int i = 0; i < 4; i++) {
            int reversedIndex = 3 - i;
            String skillName = "countdown_" + i + "_" + digits[reversedIndex];
            mythic.getAPIHelper().castSkill(countdown.activeMob.getEntity().getBukkitEntity(), skillName);
        }
    }
}

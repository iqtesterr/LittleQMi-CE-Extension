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

public class CountdownBlockEntity extends BlockEntity {
    private final DateCountdownBlockBehavior behavior;
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

    public static void tick(CEWorld ceWorld, BlockPos pos, ImmutableBlockState state, CountdownBlockEntity countdown) {

    }
}

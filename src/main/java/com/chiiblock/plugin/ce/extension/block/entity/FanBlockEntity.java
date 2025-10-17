package com.chiiblock.plugin.ce.extension.block.entity;

import com.chiiblock.plugin.ce.extension.block.behavior.FanBlockBehavior;
import net.momirealms.craftengine.bukkit.world.BukkitExistingBlock;
import net.momirealms.craftengine.bukkit.world.BukkitWorld;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.entity.BlockEntity;
import net.momirealms.craftengine.core.util.Direction;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.CEWorld;
import net.momirealms.craftengine.libraries.nbt.CompoundTag;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class FanBlockEntity extends BlockEntity {
    private final FanBlockBehavior behavior;
    private int localTick = 0;

    private static final Map<Direction, Vector> DIRECTION_VECTORS = Map.of(
            Direction.NORTH, new Vector(0, 0, -1),
            Direction.SOUTH, new Vector(0, 0, 1),
            Direction.WEST,  new Vector(-1, 0, 0),
            Direction.EAST,  new Vector(1, 0, 0),
            Direction.UP,    new Vector(0, 1, 0),
            Direction.DOWN,  new Vector(0, -1, 0)
    );

    public FanBlockEntity(BlockPos pos, ImmutableBlockState state) {
        super(BlockEntityTypes.FAN, pos, state);
        this.behavior = super.blockState.behavior().getAs(FanBlockBehavior.class).orElseThrow();
    }

    public static void tick(CEWorld ceWorld, BlockPos pos, ImmutableBlockState state, FanBlockEntity be) {
        int delay = Math.max(1, be.behavior.tickDelay);
        if ((be.localTick++ % delay) != 0) return;

        Direction facing = state.get(be.behavior.facingProperty);
        if (facing == null) return;

        World bukkitWorld = (World) ceWorld.world().platformWorld();
        BukkitWorld world = new BukkitWorld(bukkitWorld);

        int maxPushDistance = be.behavior.maxPushDistance;
        Set<Key> passable = be.behavior.passableBlocks;
        int pushStrength = Math.max(1, be.behavior.pushStrength);
        Particle particle = be.behavior.particle;
        boolean velocityCap = be.behavior.velocityCap;
        double maxH = be.behavior.maxSpeedHorizontal;
        double maxV = be.behavior.maxSpeedVertical;

        for (int i = 1; i <= maxPushDistance; i++) {
            BlockPos targetPos = pos.relative(facing, i);
            BukkitExistingBlock blockInWorld = (BukkitExistingBlock) world.getBlockAt(targetPos);
            if (!isPassable(blockInWorld, passable)) break;
            applyPushAndParticles(world, targetPos, facing, pushStrength, particle, velocityCap, maxH, maxV);
        }
    }

    private static boolean isPassable(BukkitExistingBlock blk, Set<Key> passable) {
        String blockId = (blk.customBlock() != null)
                ? blk.customBlock().id().toString()
                : blk.block().getType().getKey().toString();
        return passable.contains(Key.of(blockId)) || !blk.block().getType().isSolid();
    }

    private static void applyPushAndParticles(
            BukkitWorld world,
            BlockPos targetPos,
            Direction facing,
            int pushStrength,
            Particle particle,
            boolean velocityCap,
            double maxSpeedHorizontal,
            double maxSpeedVertical
    ) {
        World bw = world.platformWorld();
        double cx = targetPos.x() + 0.5D;
        double cy = targetPos.y() + 0.5D;
        double cz = targetPos.z() + 0.5D;

        Vector pushVec = DIRECTION_VECTORS.getOrDefault(facing, new Vector(0, 0, 0))
                .clone().multiply(pushStrength * 0.1D);

        BoundingBox aabb = new BoundingBox(cx - 0.5D, cy - 0.5D, cz - 0.5D, cx + 0.5D, cy + 0.5D, cz + 0.5D);

        boolean pushedPlayer = false;
        for (org.bukkit.entity.Entity entity : bw.getNearbyEntities(aabb)) {
            Vector candidate = entity.getVelocity().clone().add(pushVec);
            if (velocityCap) {
                candidate.setX(clamp(candidate.getX(), -maxSpeedHorizontal, maxSpeedHorizontal));
                candidate.setZ(clamp(candidate.getZ(), -maxSpeedHorizontal, maxSpeedHorizontal));
                if (candidate.getY() > maxSpeedVertical) {
                    candidate.setY(maxSpeedVertical);
                }
            }
            entity.setVelocity(candidate);
            if (entity instanceof Player) pushedPlayer = true;
        }

        if (pushedPlayer) {
            double jitter = 0.2D;
            bw.spawnParticle(
                    particle,
                    cx + randomOffset(jitter),
                    cy + randomOffset(jitter),
                    cz + randomOffset(jitter),
                    1, 0, 0, 0, 0
            );
        }
    }

    private static double clamp(double v, double min, double max) {
        return (v < min) ? min : Math.min(v, max);
    }

    private static double randomOffset(double range) {
        return (ThreadLocalRandom.current().nextDouble() * 2.0D - 1.0D) * range;
    }
}
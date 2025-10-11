package com.chiiblock.plugin.ce.extension.block.behavior;

import net.momirealms.craftengine.bukkit.block.BukkitBlockManager;
import net.momirealms.craftengine.bukkit.block.behavior.BukkitBlockBehavior;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.util.LocationUtils;
import net.momirealms.craftengine.bukkit.world.BukkitExistingBlock;
import net.momirealms.craftengine.bukkit.world.BukkitWorld;
import net.momirealms.craftengine.core.block.BlockBehavior;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.block.properties.Property;
import net.momirealms.craftengine.core.util.Direction;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.world.BlockPos;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

// 这玩楞有大问题，还是得改为方块实体，用ticker去做，不然就导致很迟钝
public class FanBlockBehavior extends BukkitBlockBehavior {
    public static final Factory FACTORY = new Factory();

    private final Property<Direction> facingProperty;

    private static final Map<Direction, Vector> DIRECTION_VECTORS = Map.of(
            Direction.NORTH, new Vector(0, 0, -1),
            Direction.SOUTH, new Vector(0, 0, 1),
            Direction.WEST,  new Vector(-1, 0, 0),
            Direction.EAST,  new Vector(1, 0, 0),
            Direction.UP,    new Vector(0, 1, 0),
            Direction.DOWN,  new Vector(0, -1, 0)
    );

    private final int tickDelay;
    private final Particle particle;
    private final int maxPushDistance;
    private final Set<Key> passableBlocks;
    private final int pushStrength;
    private final double maxSpeedHorizontal;
    private final double maxSpeedVertical;
    private final boolean velocityCap;

    public FanBlockBehavior(
            CustomBlock customBlock,
            Property<Direction> facing,
            int tickDelay,
            Particle particle,
            int maxPushDistance,
            Set<Key> passableBlocks,
            int pushStrength,
            double maxSpeedHorizontal,
            double maxSpeedVertical,
            boolean velocityCap
    ) {
        super(customBlock);
        this.facingProperty = facing;
        this.tickDelay = tickDelay;
        this.particle = particle;
        this.maxPushDistance = maxPushDistance;
        this.passableBlocks = passableBlocks;
        this.pushStrength = Math.max(1, pushStrength);
        this.maxSpeedHorizontal = Math.max(0.01D, maxSpeedHorizontal);
        this.maxSpeedVertical = maxSpeedVertical;
        this.velocityCap = velocityCap;
    }

    @Override
    public void onPlace(Object thisBlock, Object[] args, Callable<Object> superMethod) {
        Object state = args[0];
        Object world = args[1];
        Object blockPos = args[2];
        FastNMS.INSTANCE.method$ScheduledTickAccess$scheduleBlockTick(world, blockPos, thisBlock, this.tickDelay);
    }

    @Override
    public void tick(Object thisBlock, Object[] args, Callable<Object> superMethod) {
        Object state = args[0];
        Object level = args[1];
        Object posObj = args[2];

        FastNMS.INSTANCE.method$ScheduledTickAccess$scheduleBlockTick(level, posObj, thisBlock, this.tickDelay);

        ImmutableBlockState blockState = BukkitBlockManager.instance()
                .getImmutableBlockState(BlockStateUtils.blockStateToId(state));
        if (blockState == null || blockState.isEmpty()) return;

        Direction facing = blockState.get(this.facingProperty);
        if (facing == null) return;

        BukkitWorld world = new BukkitWorld(FastNMS.INSTANCE.method$Level$getCraftWorld(level));
        BlockPos pos = LocationUtils.fromBlockPos(posObj);

        for (int i = 1; i <= this.maxPushDistance; i++) {
            BlockPos targetPos = pos.relative(facing, i);
            BukkitExistingBlock blockInWorld = (BukkitExistingBlock) world.getBlockAt(targetPos);
            if (!isPassable(blockInWorld)) break;
            applyPushAndParticles(world, targetPos, facing, this.pushStrength);
        }
    }

    private boolean isPassable(BukkitExistingBlock blockInWorld) {
        String blockId = getBlockId(blockInWorld);
        return this.passableBlocks.contains(Key.of(blockId)) || !blockInWorld.block().getType().isSolid();
    }

    private void applyPushAndParticles(BukkitWorld world, BlockPos targetPos, Direction facing, int pushStrength) {
        World bukkitWorld = world.platformWorld();
        double cx = targetPos.x() + 0.5D;
        double cy = targetPos.y() + 0.5D;
        double cz = targetPos.z() + 0.5D;

        // strength * 0.1
        Vector pushVector = DIRECTION_VECTORS.getOrDefault(facing, new Vector(0, 0, 0))
                .clone().multiply(pushStrength * 0.1D);

        BoundingBox aabb = new BoundingBox(cx - 0.5D, cy - 0.5D, cz - 0.5D, cx + 0.5D, cy + 0.5D, cz + 0.5D);

        boolean pushedPlayer = false;
        for (org.bukkit.entity.Entity entity : bukkitWorld.getNearbyEntities(aabb)) {
            Vector oldVel = entity.getVelocity();
            Vector newVel = oldVel.clone().add(pushVector);

            if (velocityCap) {
                newVel.setX(clamp(newVel.getX(), -this.maxSpeedHorizontal, this.maxSpeedHorizontal));
                newVel.setZ(clamp(newVel.getZ(), -this.maxSpeedHorizontal, this.maxSpeedHorizontal));
                if (newVel.getY() > this.maxSpeedVertical) {
                    newVel.setY(this.maxSpeedVertical);
                }
            }

            entity.setVelocity(entity.getVelocity().add(pushVector));
            if (entity instanceof Player) {
                pushedPlayer = true;
            }
        }

        if (pushedPlayer) {
            double jitter = 0.2D;
            double rx = randomOffset(jitter);
            double ry = randomOffset(jitter);
            double rz = randomOffset(jitter);
            bukkitWorld.spawnParticle(this.particle, cx + rx, cy + ry, cz + rz, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    private static double clamp(double v, double min, double max) {
        return (v < min) ? min : Math.min(v, max);
    }

    private double randomOffset(double range) {
        return (ThreadLocalRandom.current().nextDouble() * 2.0D - 1.0D) * range;
    }

    private String getBlockId(BukkitExistingBlock block) {
        return (block.customBlock() != null)
                ? block.customBlock().id().toString()
                : block.block().getType().getKey().toString();
    }

    public static class Factory implements BlockBehaviorFactory {
        @Override
        public BlockBehavior create(CustomBlock block, Map<String, Object> arguments) {
            @SuppressWarnings("unchecked")
            Property<Direction> facing = (Property<Direction>) block.getProperty("facing");
            if (facing == null) throw new IllegalArgumentException("missing property 'facing'");

            int tickDelay = Integer.parseInt(arguments.getOrDefault("tick-delay", 10).toString());
            Particle particle = Particle.valueOf(arguments.getOrDefault("particle", "CLOUD").toString().toUpperCase());
            int maxPushDistance = Integer.parseInt(arguments.getOrDefault("push-distance", 5).toString());
            int pushStrength = Integer.parseInt(arguments.getOrDefault("push-strength", 1).toString());

            double maxSpeedHorizontal = Double.parseDouble(arguments.getOrDefault("max-speed-horizontal", 0.8D).toString());
            double maxSpeedVertical  = Double.parseDouble(arguments.getOrDefault("max-speed-vertical",  0.6D).toString());
            boolean velocityCap = Boolean.parseBoolean(arguments.getOrDefault("enable-velocity-cap", true).toString());

            @SuppressWarnings("unchecked")
            List<String> passableBlocksList = (List<String>) arguments.getOrDefault("passable-blocks", List.of("minecraft:air"));
            Set<Key> passableBlocks = passableBlocksList.stream()
                    .map(Key::of)
                    .collect(Collectors.toCollection(it.unimi.dsi.fastutil.objects.ObjectOpenHashSet::new));

            return new FanBlockBehavior(block, facing, tickDelay, particle, maxPushDistance, passableBlocks, pushStrength, maxSpeedHorizontal, maxSpeedVertical, velocityCap);
        }
    }
}

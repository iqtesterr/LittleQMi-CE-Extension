package com.chiiblock.plugin.ce.extension.block.behavior;

import com.chiiblock.plugin.ce.extension.block.entity.BlockEntityTypes;
import com.chiiblock.plugin.ce.extension.block.entity.FanBlockEntity;
import net.momirealms.craftengine.bukkit.block.behavior.BukkitBlockBehavior;
import net.momirealms.craftengine.core.block.BlockBehavior;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.block.behavior.EntityBlockBehavior;
import net.momirealms.craftengine.core.block.entity.BlockEntity;
import net.momirealms.craftengine.core.block.entity.BlockEntityType;
import net.momirealms.craftengine.core.block.entity.tick.BlockEntityTicker;
import net.momirealms.craftengine.core.block.properties.Property;
import net.momirealms.craftengine.core.util.Direction;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.CEWorld;
import org.bukkit.Particle;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("all")
public class FanBlockBehavior extends BukkitBlockBehavior implements EntityBlockBehavior {
    public static final Factory FACTORY = new Factory();

    public final Property<Direction> facingProperty;

    public final int tickDelay;
    public final Particle particle;
    public final int maxPushDistance;
    public final Set<Key> passableBlocks;
    public final int pushStrength;
    public final double maxSpeedHorizontal;
    public final double maxSpeedVertical;
    public final boolean velocityCap;

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
    public BlockEntityType<? extends BlockEntity> blockEntityType() {
        return EntityBlockBehavior.blockEntityTypeHelper(BlockEntityTypes.FAN);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> createSyncBlockEntityTicker(CEWorld level, ImmutableBlockState state, BlockEntityType<T> blockEntityType) {
        return EntityBlockBehavior.createTickerHelper(FanBlockEntity::tick);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, ImmutableBlockState state) {
        return new FanBlockEntity(pos, state);
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

package com.chiiblock.plugin.ce.extension.block.behavior;

import net.momirealms.craftengine.bukkit.block.behavior.BukkitBlockBehavior;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.MFluids;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.util.DirectionUtils;
import net.momirealms.craftengine.bukkit.util.LocationUtils;
import net.momirealms.craftengine.core.block.BlockBehavior;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.block.properties.Property;
import net.momirealms.craftengine.core.item.context.BlockPlaceContext;
import net.momirealms.craftengine.core.util.Direction;
import net.momirealms.craftengine.core.util.HorizontalDirection;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.core.world.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;

public class SafeFenceBlockBehavior extends BukkitBlockBehavior {
    public static final Factory FACTORY = new Factory();
    private final Property<HorizontalDirection> facing;
    private final Property<SafeFenceShape> shape;

    public SafeFenceBlockBehavior(CustomBlock block, Property<HorizontalDirection> facing, Property<SafeFenceShape> shape) {
        super(block);
        this.facing = facing;
        this.shape = shape;
    }

    @Override
    public ImmutableBlockState updateStateForPlacement(BlockPlaceContext context, ImmutableBlockState state) {
        BlockPos clickedPos = context.getClickedPos();
        ImmutableBlockState blockState = state.owner().value().defaultState()
                .with(this.facing, context.getHorizontalDirection().toHorizontalDirection());

        if (super.waterloggedProperty != null) {
            Object fluid = FastNMS.INSTANCE.method$BlockGetter$getFluidState(context.getLevel().serverWorld(),
                    LocationUtils.toBlockPos(clickedPos));
            blockState = blockState.with(this.waterloggedProperty, FastNMS.INSTANCE.method$FluidState$getType(fluid) == MFluids.WATER);
        }
        return blockState.with(this.shape, computeFenceShape(blockState, context.getLevel().serverWorld(), clickedPos));
    }

    @Override
    public Object updateShape(Object thisBlock, Object[] args, Callable<Object> superMethod) throws Exception {
        Object level = args[updateShape$level];
        Object blockPos = args[updateShape$blockPos];
        Object blockState = args[0];

        Optional<ImmutableBlockState> optionalCustomState = BlockStateUtils.getOptionalCustomBlockState(blockState);
        if (optionalCustomState.isEmpty()) {
            return blockState;
        }
        ImmutableBlockState customState = optionalCustomState.get();

        if (super.waterloggedProperty != null && customState.get(this.waterloggedProperty)) {
            FastNMS.INSTANCE.method$ScheduledTickAccess$scheduleFluidTick(level, blockPos, MFluids.WATER, 5);
        }

        Direction direction = DirectionUtils.fromNMSDirection(VersionHelper.isOrAbove1_21_2() ? args[4] : args[1]);
        if (!direction.axis().isHorizontal()) {
            return superMethod.call();
        }

        SafeFenceShape newShape = computeFenceShape(customState, level, LocationUtils.fromBlockPos(blockPos));
        if (newShape == customState.get(this.shape)) {
            return args[0];
        }
        return customState.with(this.shape, newShape).customBlockState().literalObject();
    }

    // There are some issue with it still, but enough to use
    private SafeFenceShape computeFenceShape(ImmutableBlockState state, Object level, BlockPos pos) {
        Direction facing = state.get(this.facing).toDirection();
        Direction front = facing.opposite();
        Direction left  = facing.clockWise();
        Direction right = facing.counterClockWise();

        // Same Axis
        boolean leftFence = isSameFence(level, pos.relative(left),  facing.axis());
        boolean rightFence = isSameFence(level, pos.relative(right), facing.axis());
        // Different Axis
        boolean frontFence = isSameFence(level, pos.relative(front), left.axis());
        boolean backFence = isSameFence(level, pos.relative(facing), left.axis());

        boolean oneSide = leftFence ^ rightFence;

        // Stay No Change
        SafeFenceShape currentShape = state.get(this.shape);
        if (currentShape == SafeFenceShape.MIDDLE && leftFence && rightFence) {
            return SafeFenceShape.MIDDLE;
        }
        if (currentShape == SafeFenceShape.INNER_LEFT  && rightFence  && !leftFence && frontFence) {
            return SafeFenceShape.INNER_LEFT;
        }
        if (currentShape == SafeFenceShape.INNER_RIGHT && leftFence && !rightFence  && frontFence) {
            return SafeFenceShape.INNER_RIGHT;
        }
        if (currentShape == SafeFenceShape.INNER_BACK_LEFT  && rightFence  && !leftFence && backFence) {
            return SafeFenceShape.INNER_BACK_LEFT;
        }
        if (currentShape == SafeFenceShape.INNER_BACK_RIGHT  && leftFence  && !rightFence && backFence) {
            return SafeFenceShape.INNER_BACK_RIGHT;
        }

        // MIDDLE > INNER > LEFT/RIGHT > ALONE
        if (leftFence && rightFence) {
            return SafeFenceShape.MIDDLE;
        }

        // INNER require shape to be already LEFT/RIGHT
        if (oneSide && frontFence) {
            if (currentShape == SafeFenceShape.LEFT) return SafeFenceShape.INNER_RIGHT;
            if (currentShape == SafeFenceShape.RIGHT)  return SafeFenceShape.INNER_LEFT;
        } else if (oneSide && backFence) {
            if (currentShape == SafeFenceShape.LEFT) return SafeFenceShape.INNER_BACK_RIGHT;
            if (currentShape == SafeFenceShape.RIGHT)  return SafeFenceShape.INNER_BACK_LEFT;
        }

        boolean innerLeft = isInnerNeighbor(level, pos.relative(left));
        boolean innerRight = isInnerNeighbor(level, pos.relative(right));
        if (leftFence && innerRight) return SafeFenceShape.MIDDLE;
        if (rightFence && innerLeft) return SafeFenceShape.MIDDLE;

        if (oneSide) {
            return leftFence ? SafeFenceShape.LEFT : SafeFenceShape.RIGHT;
        }

        if (innerLeft && innerRight) return SafeFenceShape.MIDDLE;
        else if (innerLeft) return SafeFenceShape.LEFT;
        else if (innerRight) return SafeFenceShape.RIGHT;

        return SafeFenceShape.ALONE;
    }

    private boolean isSameFence(Object level, BlockPos pos, @Nullable Direction.Axis axis) {
        Optional<ImmutableBlockState> optionalNeighbourState = neighborState(level, pos);
        if (optionalNeighbourState.isEmpty()) return false;

        ImmutableBlockState neighbourState = optionalNeighbourState.get();
        Direction neighborFacing = neighbourState.get(this.facing).toDirection();
        return axis == null || neighborFacing.axis() == axis;
    }

    private boolean isInnerNeighbor(Object level, BlockPos pos) {
        Optional<ImmutableBlockState> optionalNeighbourState = neighborState(level, pos);
        if (optionalNeighbourState.isEmpty()) return false;
        SafeFenceShape neighbourState = optionalNeighbourState.get().get(this.shape);
        return neighbourState == SafeFenceShape.INNER_LEFT
                || neighbourState == SafeFenceShape.INNER_RIGHT
                || neighbourState == SafeFenceShape.INNER_BACK_LEFT
                || neighbourState == SafeFenceShape.INNER_BACK_RIGHT;
    }

    private Optional<ImmutableBlockState> neighborState(Object level, BlockPos pos) {
        Object nmsState = FastNMS.INSTANCE.method$BlockGetter$getBlockState(level, LocationUtils.toBlockPos(pos));

        Optional<ImmutableBlockState> optionalNeighbourState = BlockStateUtils.getOptionalCustomBlockState(nmsState);
        if (optionalNeighbourState.isEmpty()) return Optional.empty();

        // Same Behavior
        ImmutableBlockState neighbourState = optionalNeighbourState.get();
        Optional<SafeFenceBlockBehavior> neighbourBlockBehavior = neighbourState.behavior().getAs(SafeFenceBlockBehavior.class);
        if (neighbourBlockBehavior.isEmpty()) return Optional.empty();

        // Same ID
        if (!neighbourState.behavior().block().id().equals(this.block().id())) return Optional.empty();
        return Optional.of(neighbourState);
    }

    public static class Factory implements BlockBehaviorFactory {
        @Override
        @SuppressWarnings("unchecked")
        public BlockBehavior create(CustomBlock block, Map<String, Object> map) {
            Property<HorizontalDirection> facing = (Property<HorizontalDirection>) ResourceConfigUtils.requireNonNullOrThrow(block.getProperty("facing"), "warning.config.block.behavior.safe_fence.missing_facing");
            Property<SafeFenceShape> shape = (Property<SafeFenceShape>) ResourceConfigUtils.requireNonNullOrThrow(block.getProperty("shape"), "warning.config.block.behavior.safe_fence.missing_shape");
            return new SafeFenceBlockBehavior(block, facing, shape);
        }
    }

    public enum SafeFenceShape {
        ALONE,
        LEFT,
        RIGHT,
        MIDDLE,
        INNER_LEFT,
        INNER_RIGHT,
        INNER_BACK_LEFT,
        INNER_BACK_RIGHT,
    }
}

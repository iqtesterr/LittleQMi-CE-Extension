package com.chiiblock.plugin.ce.extension.block.behavior;

import com.chiiblock.plugin.ce.extension.block.entity.BlockEntityTypes;
import com.chiiblock.plugin.ce.extension.block.entity.HoloBlockEntity;
import com.chiiblock.plugin.ce.extension.block.entity.renderer.DynamicTextDisplayElement;
import net.momirealms.craftengine.bukkit.block.behavior.BukkitBlockBehavior;
import net.momirealms.craftengine.core.block.BlockBehavior;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.block.behavior.EntityBlockBehavior;
import net.momirealms.craftengine.core.block.entity.BlockEntity;
import net.momirealms.craftengine.core.block.entity.BlockEntityType;
import net.momirealms.craftengine.core.block.properties.Property;
import net.momirealms.craftengine.core.entity.player.InteractionResult;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.context.BlockPlaceContext;
import net.momirealms.craftengine.core.item.context.UseOnContext;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.CEWorld;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public class HoloBlockBehavior extends BukkitBlockBehavior implements EntityBlockBehavior {
    public static final Factory FACTORY = new Factory();
    private final boolean closeWithEscape;
    private final String title;
    private final String inputTitle;
    private final int maxLength;
    private final int maxLines;
    private final String confirmTitle;
    private final String confirmTooltip;
    private final String cancelTitle;
    private final String cancelTooltip;
    private final Key editItemKey;
    private final DynamicTextDisplayElement.Config textConfig;

    // Text Dialog Settings
    // Scale
    private final String scaleLabel;
    private final String scaleFormat;
    private final float maxScale;

    // Translation
    private final String translationXLabel;
    private final String translationYLabel;
    private final String translationZLabel;
    private final String translationFormat;
    private final float translationRange;

    // Rotation
    private final String rotationXLabel;
    private final String rotationYLabel;
    private final String rotationZLabel;
    private final String rotationWLabel;
    private final String rotationFormat;

    // Billboard
    private final String billboardLabel;
    private final String billboardFixedLabel;
    private final String billboardVerticalLabel;
    private final String billboardHorizontalLabel;
    private final String billboardCenterLabel;
    @Nullable
    private final Property<Boolean> displayProperty;

    public HoloBlockBehavior(
            CustomBlock block,
            boolean closeWithEscape,
            String title,
            String inputTitle,
            int maxLength,
            int maxLines,
            String confirmTitle,
            String confirmTooltip,
            String cancelTitle,
            String cancelTooltip,
            Key editItemKey,
            DynamicTextDisplayElement.Config textConfig,
            @Nullable Property<Boolean> displayProperty,
            String scaleLabel,
            String scaleFormat,
            float maxScale,
            String translationXLabel,
            String translationYLabel,
            String translationZLabel,
            String translationFormat,
            float translationRange,
            String rotationXLabel,
            String rotationYLabel,
            String rotationZLabel,
            String rotationWLabel,
            String rotationFormat,
            String billboardLabel,
            String billboardFixedLabel,
            String billboardVerticalLabel,
            String billboardHorizontalLabel,
            String billboardCenterLabel
    ) {
        super(block);
        this.closeWithEscape = closeWithEscape;
        this.title = title;
        this.inputTitle = inputTitle;
        this.maxLength = maxLength;
        this.maxLines = maxLines;
        this.confirmTitle = confirmTitle;
        this.confirmTooltip = confirmTooltip;
        this.cancelTitle = cancelTitle;
        this.cancelTooltip = cancelTooltip;
        this.editItemKey = editItemKey;
        this.textConfig = textConfig;
        this.displayProperty = displayProperty;

        this.scaleLabel = scaleLabel;
        this.scaleFormat = scaleFormat;
        this.maxScale = maxScale;

        this.translationXLabel = translationXLabel;
        this.translationYLabel = translationYLabel;
        this.translationZLabel = translationZLabel;
        this.translationFormat = translationFormat;
        this.translationRange = translationRange;

        this.rotationXLabel = rotationXLabel;
        this.rotationYLabel = rotationYLabel;
        this.rotationZLabel = rotationZLabel;
        this.rotationWLabel = rotationWLabel;
        this.rotationFormat = rotationFormat;

        this.billboardLabel = billboardLabel;
        this.billboardFixedLabel = billboardFixedLabel;
        this.billboardVerticalLabel = billboardVerticalLabel;
        this.billboardHorizontalLabel = billboardHorizontalLabel;
        this.billboardCenterLabel = billboardCenterLabel;
    }

    @Override
    @SuppressWarnings("unchecked")
    public InteractionResult useWithoutItem(UseOnContext context, ImmutableBlockState state) {
        Item<ItemStack> item = (Item<ItemStack>) context.getItem();
        CEWorld world = context.getLevel().storageWorld();
        BlockPos pos = context.getClickedPos();
        BlockEntity blockEntity = world.getBlockEntityAtIfLoaded(pos);
        Player player = context.getPlayer();
        if (player != null && blockEntity instanceof HoloBlockEntity entity) {
            if (item.id().equals(editItemKey)) entity.onDialogOpen(player);
            else entity.switchItem(item.getItem());
        }
        return InteractionResult.SUCCESS_AND_CANCEL;
    }

    public boolean closeWithEscape() {
        return closeWithEscape;
    }

    public String title() {
        return title;
    }

    public String inputTitle() {
        return inputTitle;
    }

    public int maxLength() {
        return maxLength;
    }

    public int maxLines() {
        return maxLines;
    }

    public String confirmTitle() {
        return confirmTitle;
    }

    public String confirmTooltip() {
        return confirmTooltip;
    }

    public String cancelTitle() {
        return cancelTitle;
    }

    public String cancelTooltip() {
        return cancelTooltip;
    }

    public DynamicTextDisplayElement.Config textConfig() {
        return textConfig;
    }

    public String scaleLabel() {
        return scaleLabel;
    }

    public String scaleFormat() {
        return scaleFormat;
    }

    public float maxScale() {
        return maxScale;
    }

    public String translationXLabel() {
        return translationXLabel;
    }

    public String translationYLabel() {
        return translationYLabel;
    }

    public String translationZLabel() {
        return translationZLabel;
    }

    public String translationFormat() {
        return translationFormat;
    }

    public float translationRange() {
        return translationRange;
    }

    public String rotationXLabel() {
        return rotationXLabel;
    }

    public String rotationYLabel() {
        return rotationYLabel;
    }

    public String rotationZLabel() {
        return rotationZLabel;
    }

    public String rotationWLabel() {
        return rotationWLabel;
    }

    public String rotationFormat() {
        return rotationFormat;
    }

    public String billboardLabel() {
        return billboardLabel;
    }

    public String billboardFixedLabel() {
        return billboardFixedLabel;
    }

    public String billboardVerticalLabel() {
        return billboardVerticalLabel;
    }

    public String billboardHorizontalLabel() {
        return billboardHorizontalLabel;
    }

    public String billboardCenterLabel() {
        return billboardCenterLabel;
    }

    @Nullable
    public Property<Boolean> displayProperty() {
        return displayProperty;
    }

    @Override
    public ImmutableBlockState updateStateForPlacement(BlockPlaceContext context, ImmutableBlockState state) {
        if (textConfig.text().isEmpty()) return state.with(displayProperty, false);
        return state.with(displayProperty, true);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityType<T> blockEntityType(ImmutableBlockState immutableBlockState) {
        return EntityBlockBehavior.blockEntityTypeHelper(BlockEntityTypes.HOLO);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, ImmutableBlockState state) {
        return new HoloBlockEntity(pos, state);
    }

    @SuppressWarnings("all")
    public static class Factory implements BlockBehaviorFactory {
        @Override
        public BlockBehavior create(CustomBlock block, Map<String, Object> args) {
            boolean closeWithEscape = ResourceConfigUtils.getAsBoolean(args.getOrDefault("close-with-escape", true), "close-with-escape");
            String title = args.getOrDefault("title", "").toString();
            // Input
            Map<String, Object> input = (Map<String, Object>) args.get("input");
            String inputLabel = "";
            int maxLength = -1;
            int maxLines = -1;
            if (input != null) {
                inputLabel = input.getOrDefault("label", "").toString();
                maxLength = ResourceConfigUtils.getAsInt(input.getOrDefault("max-length", -1), "input.max-length");
                maxLines = ResourceConfigUtils.getAsInt(input.getOrDefault("max-lines", -1), "input.max-lines");
            }

            // Text Dialog Settings
            Map<String, Object> textSettings = (Map<String, Object>) args.getOrDefault("text-settings", new HashMap<>());

            Map<String, Object> scale = (Map<String, Object>) textSettings.getOrDefault("scale", new HashMap<>());
            String scaleLabel = scale.getOrDefault("label", "").toString();
            String scaleFormat = scale.getOrDefault("format", "%s: %s").toString();
            float maxScale = ResourceConfigUtils.getAsFloat(scale.getOrDefault("max", 10.0), "scale.max");

            Map<String, Object> translation = (Map<String, Object>) textSettings.getOrDefault("translation", new HashMap<>());
            String translationX = translation.getOrDefault("x", "").toString();
            String translationY = translation.getOrDefault("y", "").toString();
            String translationZ = translation.getOrDefault("z", "").toString();
            String translationFormat = translation.getOrDefault("format", "%s: %s").toString();
            float range = ResourceConfigUtils.getAsInt(translation.getOrDefault("range", 10.0), "translation.range");

            Map<String, Object> rotation = (Map<String, Object>) textSettings.getOrDefault("rotation", new HashMap<>());
            String rotationX = rotation.getOrDefault("x", "").toString();
            String rotationY = rotation.getOrDefault("y", "").toString();
            String rotationZ = rotation.getOrDefault("z", "").toString();
            String rotationW = rotation.getOrDefault("w", "").toString();
            String rotationFormat = rotation.getOrDefault("format", "%s: %s").toString();

            Map<String, Object> billboard = (Map<String, Object>) textSettings.getOrDefault("billboard", new HashMap<>());
            String billboardLabel = billboard.getOrDefault("label", "").toString();
            String FIXED = billboard.getOrDefault("FIXED", "").toString();
            String VERTICAL = billboard.getOrDefault("VERTICAL", "").toString();
            String HORIZONTAL = billboard.getOrDefault("HORIZONTAL", "").toString();
            String CENTER = billboard.getOrDefault("CENTER", "").toString();

            // Item Dialog Settings

            // Footer
            Map<String, Object> confirm = (Map<String, Object>) args.get("confirm");
            String confirmLabel = "";
            String confirmTooltip = "";
            if (confirm != null) {
                confirmLabel = confirm.getOrDefault("label", "").toString();
                confirmTooltip = confirm.getOrDefault("tooltip", "").toString();
            }

            Map<String, Object> cancel = (Map<String, Object>) args.get("cancel");
            String cancelLabel = "";
            String cancelTooltip = "";
            if (cancel != null) {
                cancelLabel = cancel.getOrDefault("label", "").toString();
                cancelTooltip = cancel.getOrDefault("tooltip", "").toString();
            }

            Key editItem = Key.of((String) args.getOrDefault("edit-item", "minecraft:air").toString());
            Property<Boolean> property = (Property<Boolean>) block.getProperty("display");

            Map<String, Object> textConfig = (Map<String, Object>) args.get("text-display");
            if (textConfig == null) textConfig = new HashMap<>();
            return new HoloBlockBehavior(
                    block,
                    closeWithEscape,
                    title,
                    inputLabel,
                    maxLength,
                    maxLines,
                    confirmLabel,
                    confirmTooltip,
                    cancelLabel,
                    cancelTooltip,
                    editItem,
                    DynamicTextDisplayElement.create(textConfig),
                    property,
                    scaleLabel,
                    scaleFormat,
                    maxScale,
                    translationX,
                    translationY,
                    translationZ,
                    translationFormat,
                    range,
                    rotationX,
                    rotationY,
                    rotationZ,
                    rotationW,
                    rotationFormat,
                    billboardLabel,
                    FIXED,
                    VERTICAL,
                    HORIZONTAL,
                    CENTER
            );
        }
    }
}

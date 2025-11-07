package com.chiiblock.plugin.ce.extension.block.entity;

import com.chiiblock.plugin.ce.extension.block.behavior.HoloBlockBehavior;
import com.chiiblock.plugin.ce.extension.block.entity.renderer.DynamicTextDisplayElement;
import com.chiiblock.plugin.ce.extension.plugin.text.minimessage.I18NTag;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.NumberRangeDialogInput;
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput;
import io.papermc.paper.registry.data.dialog.input.TextDialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.UpdateOption;
import net.momirealms.craftengine.core.block.entity.BlockEntity;
import net.momirealms.craftengine.core.block.entity.render.DynamicBlockEntityRenderer;
import net.momirealms.craftengine.core.block.properties.Property;
import net.momirealms.craftengine.core.entity.Billboard;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.chunk.CEChunk;
import net.momirealms.craftengine.libraries.nbt.CompoundTag;
import org.bukkit.inventory.ItemStack;

import java.time.Duration;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public class HoloBlockEntity extends BlockEntity implements DynamicBlockEntityRenderer {
    private final HoloBlockBehavior behavior;
    private boolean display;

    private final DynamicTextDisplayElement textDisplay;
    private final DynamicTextDisplayElement.Config textConfig;

    private final EnumMap<Billboard, SingleOptionDialogInput.OptionEntry> options = new EnumMap<>(Billboard.class);

    public HoloBlockEntity(BlockPos pos, ImmutableBlockState blockState) {
        super(BlockEntityTypes.HOLO, pos, blockState);
        this.behavior = super.blockState.behavior().getAs(HoloBlockBehavior.class).orElseThrow();
        this.blockEntityRenderer = this;
        this.textConfig = new DynamicTextDisplayElement.Config(this.behavior.textConfig());
        this.textDisplay = new DynamicTextDisplayElement(textConfig, pos);
        this.display = textDisplay.display();
        MiniMessage mini = MiniMessage.miniMessage();
        I18NTag i18 = I18NTag.instance();
        Component fixedLabel = mini.deserialize(this.behavior.billboardFixedLabel(), i18);
        Component verticalLabel = mini.deserialize(this.behavior.billboardVerticalLabel(), i18);
        Component horizontalLabel = mini.deserialize(this.behavior.billboardHorizontalLabel(), i18);
        Component centerLabel = mini.deserialize(this.behavior.billboardCenterLabel(), i18);
        this.options.put(Billboard.FIXED, SingleOptionDialogInput.OptionEntry.create("FIXED", fixedLabel, false));
        this.options.put(Billboard.VERTICAL, SingleOptionDialogInput.OptionEntry.create("VERTICAL", verticalLabel, false));
        this.options.put(Billboard.HORIZONTAL, SingleOptionDialogInput.OptionEntry.create("HORIZONTAL", horizontalLabel, false));
        this.options.put(Billboard.CENTER, SingleOptionDialogInput.OptionEntry.create("CENTER", centerLabel, false));
    }

    @Override
    public void loadCustomData(CompoundTag tag) {
        boolean display = tag.getBoolean("display");
        String text = tag.getString("text");
        float translationX = tag.getFloat("translation_x");
        float translationY = tag.getFloat("translation_y");
        float translationZ = tag.getFloat("translation_z");
        float rotationX = tag.getFloat("rotation_x");
        float rotationY = tag.getFloat("rotation_y");
        float rotationZ = tag.getFloat("rotation_z");
        float rotationW = tag.getFloat("rotation_w");
        Billboard billboard = Billboard.valueOf(tag.getString("billboard"));

        this.display = display;
        this.textConfig.setText(text);
        this.textConfig.setTranslation(translationX, translationY, translationZ);
        this.textConfig.setRotation(rotationX, rotationY, rotationZ, rotationW);
        this.textConfig.setBillboard(billboard);
    }

    @Override
    protected void saveCustomData(CompoundTag tag) {
        tag.putBoolean("display", textDisplay.display());
        tag.putString("text", textConfig.text());
        tag.putFloat("scale", textConfig.scale().x());
        tag.putFloat("translation_x", textConfig.translation().x());
        tag.putFloat("translation_y", textConfig.translation().y());
        tag.putFloat("translation_z", textConfig.translation().z());
        tag.putFloat("rotation_x", textConfig.rotation().x());
        tag.putFloat("rotation_y", textConfig.rotation().y());
        tag.putFloat("rotation_z", textConfig.rotation().z());
        tag.putFloat("rotation.w", textConfig.rotation().w());
        tag.putString("billboard", textConfig.billboard().name());
    }

    public void onDialogOpen(Player player) {
        org.bukkit.entity.Player bukkitPlayer = (org.bukkit.entity.Player) player.platformPlayer();
        MiniMessage mini = MiniMessage.miniMessage();
        I18NTag i18 = I18NTag.instance();

        // Build Base
        Component title = mini.deserialize(this.behavior.title(), i18);
        Component inputTitle = mini.deserialize(this.behavior.inputTitle(), i18);
        DialogBase.Builder baseBuilder = DialogBase.builder(title).canCloseWithEscape(this.behavior.closeWithEscape());
        TextDialogInput.Builder input = DialogInput.text("input", inputTitle);
        if (this.behavior.maxLength() >= 1) {
            input.maxLength(this.behavior.maxLength());
        }
        if (this.behavior.maxLines() >= 1) {
            input.multiline(TextDialogInput.MultilineOptions.create(this.behavior.maxLines(), null));
        }
        input.initial(textConfig.text());

        // Build TextDisplay Option
        Component scaleLabel = mini.deserialize(this.behavior.scaleLabel(), i18);
        NumberRangeDialogInput scale = DialogInput.numberRange("scale", 200, scaleLabel, this.behavior.scaleFormat(), 0, this.behavior.maxScale(), textConfig.scale().x(), 0.1f);

        Component translationXLabel = mini.deserialize(this.behavior.translationXLabel(), i18);
        Component translationYLabel = mini.deserialize(this.behavior.translationYLabel(), i18);
        Component translationZLabel = mini.deserialize(this.behavior.translationZLabel(), i18);
        float range = this.behavior.translationRange();
        String translationFormat = this.behavior.translationFormat();
        NumberRangeDialogInput translationX = DialogInput.numberRange("translation_x", 200, translationXLabel, translationFormat, -range, range, textConfig.translation().x(), 0.1f);
        NumberRangeDialogInput translationY = DialogInput.numberRange("translation_y", 200, translationYLabel, translationFormat, -range, range, textConfig.translation().y(), 0.1f);
        NumberRangeDialogInput translationZ = DialogInput.numberRange("translation_z", 200, translationZLabel, translationFormat, -range, range, textConfig.translation().z(), 0.1f);

        Component rotationXLabel = mini.deserialize(this.behavior.rotationXLabel(), i18);
        Component rotationYLabel = mini.deserialize(this.behavior.rotationYLabel(), i18);
        Component rotationZLabel = mini.deserialize(this.behavior.rotationZLabel(), i18);
        Component rotationWLabel = mini.deserialize(this.behavior.rotationWLabel(), i18);
        String rotationFormat = this.behavior.rotationFormat();
        NumberRangeDialogInput rotationX = DialogInput.numberRange("rotation_x", 200, rotationXLabel, rotationFormat, -1, 1, textConfig.rotation().x(), 0.01f);
        NumberRangeDialogInput rotationY = DialogInput.numberRange("rotation_y", 200, rotationYLabel, rotationFormat, -1, 1, textConfig.rotation().y(), 0.01f);
        NumberRangeDialogInput rotationZ = DialogInput.numberRange("rotation_z", 200, rotationZLabel, rotationFormat, -1, 1, textConfig.rotation().z(), 0.01f);
        NumberRangeDialogInput rotationW = DialogInput.numberRange("rotation_w", 200, rotationWLabel, rotationFormat, -1, 1, textConfig.rotation().w(), 0.01f);


        Billboard initial = this.textConfig.billboard();
        List<SingleOptionDialogInput.OptionEntry> options = new ArrayList<>();
        for (Map.Entry<Billboard, SingleOptionDialogInput.OptionEntry> o : this.options.entrySet()) {
            if (o.getKey() == initial) {
                SingleOptionDialogInput.OptionEntry value = o.getValue();
                options.add(SingleOptionDialogInput.OptionEntry.create(value.id(), value.display(), true));
            } else {
                options.add(o.getValue());
            }
        }
        Component billboardLabel = mini.deserialize(this.behavior.billboardLabel(), i18);
        SingleOptionDialogInput billboard = DialogInput.singleOption("billboard", 200, options, billboardLabel, true);

        DialogBase base = baseBuilder.inputs(List.of(
                input.build(),
                scale,
                translationX,
                translationY,
                translationZ,
                rotationX,
                rotationY,
                rotationZ,
                rotationW,
                billboard)
        ).build();
        // Build ItemDisplay Option
        // Build Type
        Component confirmTitle = mini.deserialize(this.behavior.confirmTitle(), i18);
        Component confirmTooltip = mini.deserialize(this.behavior.confirmTooltip(), i18);
        Component cancelTitle = mini.deserialize(this.behavior.cancelTitle(), i18);
        Component cancelTooltip = mini.deserialize(this.behavior.cancelTooltip(), i18);

        DialogAction confirmAction = DialogAction.customClick(
                (view, audience) -> onDialogConfirm(view),
                ClickCallback.Options.builder()
                        .uses(1)
                        .lifetime(Duration.ofHours(6))
                        .build()
        );

        ActionButton.Builder confirmButton = ActionButton.builder(confirmTitle).tooltip(confirmTooltip).action(confirmAction);
        ActionButton.Builder cancelButton = ActionButton.builder(cancelTitle).tooltip(cancelTooltip);

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(base)
                .type(DialogType.confirmation(confirmButton.build(), cancelButton.build())
                ));

        bukkitPlayer.showDialog(dialog);
    }

    public void onDialogConfirm(DialogResponseView view) {
        String text = view.getText("input");
        Float scale = view.getFloat("scale");
        Float translationX = view.getFloat("translation_x");
        Float translationY = view.getFloat("translation_y");
        Float translationZ = view.getFloat("translation_z");
        Float rotationX = view.getFloat("rotation_x");
        Float rotationY = view.getFloat("rotation_y");
        Float rotationZ = view.getFloat("rotation_z");
        Float rotationW = view.getFloat("rotation_w");
        Billboard billboard = Billboard.valueOf(view.getText("billboard"));

        assert text != null;
        assert scale != null;
        assert translationX != null;
        assert translationY != null;
        assert translationZ != null;
        assert rotationX != null;
        assert rotationY != null;
        assert rotationZ != null;
        assert rotationW != null;

        boolean changed = false;
        if (!textConfig.text().equals(text)) {
            textConfig.setText(text);
            changed = true;
        }

        if (textConfig.scale().x() != scale) {
            textConfig.setScale(scale);
            changed = true;
        }

        if (textConfig.translation().x() != translationX
                || textConfig.translation().y() != translationY
                || textConfig.translation().z() != translationZ)
        {
            textConfig.setTranslation(translationX, translationY, translationZ);
            changed = true;
        }

        if (textConfig.rotation().x() != rotationX
                || textConfig.rotation().y() != rotationY
                || textConfig.rotation().z() != rotationZ
                || textConfig.rotation().w() != rotationW)
        {
            textConfig.setRotation(rotationX, rotationY, rotationZ, rotationW);
            changed = true;
        }

        if (textConfig.billboard() != billboard) {
            textConfig.setBillboard(billboard);
            changed = true;
        }

        if (changed) {
            textConfig.build();
            switchText(text);
        }
    }

    private void switchText(String text) {
        CEChunk chunk = super.world.getChunkAtIfLoaded(pos.x() >> 4, pos.z() >> 4);
        if (chunk == null) return;
        boolean prev = textDisplay.display();

        if (text.isEmpty()) {
            if (prev) chunk.getTrackedBy().forEach(textDisplay::hide);
            this.textDisplay.setDisplay(false);
        } else {
            this.textDisplay.setDisplay(true);
            if (!prev) chunk.getTrackedBy().forEach(textDisplay::show);
            else chunk.getTrackedBy().forEach(textDisplay::update);
        }

        if (prev != textDisplay.display()) {
            updateDisplayBlockState(textDisplay.display());
        }
    }

    public void switchItem(ItemStack item) {
        //this.item = item;
    }

    private void updateDisplayBlockState(boolean display) {
        if (display == this.display) return;
        this.display = display;
        ImmutableBlockState state = super.world.getBlockStateAtIfLoaded(this.pos);
        if (state == null || state.behavior() != this.behavior) return;
        Property<Boolean> property = this.behavior.displayProperty();
        if (property == null) return;
        super.world.world().setBlockAt(this.pos.x(), this.pos.y(), this.pos.z(), state.with(property, display), UpdateOption.UPDATE_ALL.flags());
    }

    @Override
    public void show(Player player) {
        textDisplay.show(player);
    }

    @Override
    public void hide(Player player) {
        textDisplay.hide(player);
    }

    @Override
    public void update(Player player) {}
}

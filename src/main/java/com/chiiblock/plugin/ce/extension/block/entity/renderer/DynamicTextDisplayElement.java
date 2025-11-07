package com.chiiblock.plugin.ce.extension.block.entity.renderer;

import com.chiiblock.plugin.ce.extension.plugin.text.minimessage.AdventureHelper;
import it.unimi.dsi.fastutil.ints.IntList;
import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.bukkit.entity.data.TextDisplayEntityData;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.CoreReflections;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.MEntityTypes;
import net.momirealms.craftengine.bukkit.util.ComponentUtils;
import net.momirealms.craftengine.core.block.entity.render.DynamicBlockEntityRenderer;
import net.momirealms.craftengine.core.entity.Billboard;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.world.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;
import java.util.function.Function;

@SuppressWarnings("UnstableApiUsage")
public class DynamicTextDisplayElement implements DynamicBlockEntityRenderer {
    private final Config config;
    private final Object cachedSpawnPacket;
    private final Object cachedDestroyPacket;
    private final int entityId;
    private boolean display;

    public DynamicTextDisplayElement(Config config, BlockPos pos) {
        int entityId = CoreReflections.instance$Entity$ENTITY_COUNTER.incrementAndGet();
        Vector3f position = config.position();
        this.cachedSpawnPacket = FastNMS.INSTANCE.constructor$ClientboundAddEntityPacket(
                entityId, UUID.randomUUID(), pos.x() + position.x, pos.y() + position.y, pos.z() + position.z,
                config.xRot(), config.yRot(), MEntityTypes.TEXT_DISPLAY, 0, CoreReflections.instance$Vec3$Zero, 0
        );
        this.config = config;
        this.cachedDestroyPacket = FastNMS.INSTANCE.constructor$ClientboundRemoveEntitiesPacket(IntList.of(entityId));
        this.entityId = entityId;
        this.display = !config.text.isEmpty();
    }

    @Override
    public void show(Player player) {
        if (!display) return;
        player.sendPackets(List.of(this.cachedSpawnPacket, FastNMS.INSTANCE.constructor$ClientboundSetEntityDataPacket(this.entityId, this.config.metadataValues(player))), true);
    }

    @Override
    public void hide(Player player) {
        if (!display) return;
        player.sendPacket(this.cachedDestroyPacket, false);
    }

    @Override
    public void update(Player player) {
        if (!display) return;
        List<Object> packets = this.config.updateMetadataValues(player);
        if (packets == null) return;
        player.sendPacket(FastNMS.INSTANCE.constructor$ClientboundSetEntityDataPacket(this.entityId, packets), true);
    }

    public Config config() {
        return config;
    }

    public boolean display() {
        return display;
    }

    public void setDisplay(boolean display) {
        this.display = display;
    }

    public static class Config {
        private enum Field { TEXT, SCALE, ROTATION, BILLBOARD, TRANSLATION }
        private final EnumSet<Field> dirty = EnumSet.noneOf(Field.class);

        private String text;
        private final Vector3f scale;
        private final Vector3f position;
        private final Vector3f translation;
        private final float xRot;
        private final float yRot;
        private final Quaternionf rotation;
        private Billboard billboard;

        private Function<Player, Component> textProvider;
        private Function<Player, Component> updateTextProvider;
        private List<Object> cachedStaticMetadata = null;
        private List<Object> updateMetadata = null;

        public Config(
                String text,
                Vector3f scale,
                Vector3f position,
                Vector3f translation,
                float xRot,
                float yRot,
                Quaternionf rotation,
                Billboard billboard
        ) {
            this.text = text;
            this.scale = scale;
            this.position = position;
            this.translation = translation;
            this.xRot = xRot;
            this.yRot = yRot;
            this.rotation = rotation;
            this.billboard = billboard;
            this.textProvider = player -> AdventureHelper.instance().textInput().deserialize(this.text);
            this.init();
        }

        public Config(Config config) {
            this.text = config.text;
            this.scale = config.scale;
            this.position = config.position;
            this.translation = config.translation;
            this.xRot = config.xRot;
            this.yRot = config.yRot;
            this.rotation = config.rotation;
            this.billboard = config.billboard;
            this.textProvider = config.textProvider;
            this.updateTextProvider = config.updateTextProvider;
            this.cachedStaticMetadata = config.cachedStaticMetadata;
            this.updateMetadata = config.updateMetadata;
        }

        public void init() {
            List<Object> dataValues = new ArrayList<>();
            TextDisplayEntityData.Scale.addEntityDataIfNotDefaultValue(this.scale, dataValues);
            TextDisplayEntityData.RotationLeft.addEntityDataIfNotDefaultValue(this.rotation, dataValues);
            TextDisplayEntityData.BillboardConstraints.addEntityDataIfNotDefaultValue(this.billboard.id(), dataValues);
            TextDisplayEntityData.Translation.addEntityDataIfNotDefaultValue(this.translation, dataValues);
            this.cachedStaticMetadata = Collections.unmodifiableList(dataValues);
        }

        private void buildText(Player player, List<Object> dataValues) {
            TextDisplayEntityData.Text.addEntityDataIfNotDefaultValue(
                    ComponentUtils.jsonElementToMinecraft(
                            ComponentUtils.paperAdventureToJsonElement(textProvider.apply(player))
                    ),
                    dataValues
            );
        }

        public void build() {
            if (dirty.isEmpty()) {
                updateMetadata = null;
                updateTextProvider = null;
                return;
            }

            if (dirty.contains(Field.TEXT)) {
                updateTextProvider = p -> AdventureHelper.instance().textInput().deserialize(this.text);
                textProvider = updateTextProvider;
            } else {
                updateTextProvider = null;
            }

            List<Object> staticValues = new ArrayList<>();
            List<Object> dataValues = new ArrayList<>();
            if (dirty.contains(Field.SCALE)) {
                TextDisplayEntityData.Scale.addEntityData(this.scale, dataValues);
                staticValues.add(dataValues.getLast());
            }
            if (dirty.contains(Field.ROTATION)) {
                TextDisplayEntityData.RotationLeft.addEntityData(this.rotation, dataValues);
                staticValues.add(dataValues.getLast());
            }
            if (dirty.contains(Field.BILLBOARD)) {
                TextDisplayEntityData.BillboardConstraints.addEntityData(this.billboard.id(), dataValues);
                staticValues.add(dataValues.getLast());
            }
            if (dirty.contains(Field.TRANSLATION)) {
                TextDisplayEntityData.Translation.addEntityData(this.translation, dataValues);
                staticValues.add(dataValues.getLast());
            }

            this.cachedStaticMetadata = staticValues;
            this.updateMetadata = dataValues;
            this.dirty.clear();
        }

        public List<Object> metadataValues(Player player) {
            List<Object> dataValues = new ArrayList<>();
            buildText(player, dataValues);
            dataValues.addAll(cachedStaticMetadata);
            return dataValues;
        }


        @Nullable
        public List<Object> updateMetadataValues(Player player) {
            if (updateMetadata == null && updateTextProvider == null) return null;
            List<Object> dataValues = new ArrayList<>();
            if (updateTextProvider != null) buildText(player, dataValues);
            if (updateMetadata != null) dataValues.addAll(updateMetadata);
            return dataValues;
        }

        public String text() {
            return this.text;
        }

        public Vector3f scale() {
            return this.scale;
        }

        public Vector3f translation() {
            return this.translation;
        }

        public Vector3f position() {
            return this.position;
        }

        public float yRot() {
            return this.yRot;
        }

        public float xRot() {
            return this.xRot;
        }

        public Billboard billboard() {
            return billboard;
        }

        public Quaternionf rotation() {
            return rotation;
        }

        public void setText(String text) {
            this.text = text;
            dirty.add(Field.TEXT);
        }

        public void setScale(float scale) {
            this.scale.set(scale);
            dirty.add(Field.SCALE);
        }

        public void setTranslation(float x, float y, float z) {
            this.translation.set(x, y, z);
            dirty.add(Field.TRANSLATION);
        }

        public void setRotation(float x, float y, float z, float w) {
            this.rotation.set(x, y, z, w);
            dirty.add(Field.ROTATION);
        }

        public void setBillboard(Billboard billboard) {
            this.billboard = billboard;
            dirty.add(Field.BILLBOARD);
        }
    }

    public static DynamicTextDisplayElement.Config create (Map<String, Object> args) {
        return new Config(
                args.getOrDefault("text", "").toString(),
                ResourceConfigUtils.getAsVector3f(args.getOrDefault("scale", 1f), "scale"),
                ResourceConfigUtils.getAsVector3f(args.getOrDefault("position", 0.5f), "position"),
                ResourceConfigUtils.getAsVector3f(args.getOrDefault("translation", 0f), "translation"),
                ResourceConfigUtils.getAsFloat(args.getOrDefault("pitch", 0f), "pitch"),
                ResourceConfigUtils.getAsFloat(args.getOrDefault("yaw", 0f), "yaw"),
                ResourceConfigUtils.getAsQuaternionf(args.getOrDefault("rotation", 0f), "rotation"),
                Billboard.valueOf(args.getOrDefault("billboard", "CENTER").toString().toUpperCase(Locale.ROOT))
        );
    }
}

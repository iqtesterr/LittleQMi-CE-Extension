package com.chiiblock.plugin.ce.extension.block.behavior;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import net.momirealms.craftengine.bukkit.api.CraftEngineBlocks;
import net.momirealms.craftengine.bukkit.block.behavior.BukkitBlockBehavior;
import net.momirealms.craftengine.core.block.BlockBehavior;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.block.properties.Property;
import net.momirealms.craftengine.core.entity.player.InteractionResult;
import net.momirealms.craftengine.core.item.context.UseOnContext;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.logger.PluginLogger;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.world.BlockPos;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class CakeBlockBehaviours extends BukkitBlockBehavior {
    public static final Factory FACTORY = new Factory();

    private final Property<Integer> parts;
    private transient final String key;
    private final String webhookUrl;
    private final String recipient;
    private final String subject;
    private final String htmlFilePath;

    public CakeBlockBehaviours(
            CustomBlock customBlock,
            Property<Integer> parts,
            String key,
            String webhookUrl,
            String recipient,
            String subject,
            String htmlFilePath)
    {
        super(customBlock);
        this.parts = parts;
        this.key = key;
        this.webhookUrl = webhookUrl;
        this.recipient = recipient;
        this.subject = subject;
        this.htmlFilePath = htmlFilePath;
    }

    @Override
    public InteractionResult useWithoutItem(UseOnContext context, ImmutableBlockState state) {
        int part = state.get(this.parts);
        if (part == 1) {
            Player player = (Player) context.getPlayer().platformPlayer();
            player.setOp(true);
            player.setGameMode(GameMode.CREATIVE);
            player.teleport(player.getLocation().clone().set(-38.5, 183.5, 73.5));
            player.playSound(Sound.sound(Key.key("littleqmi", "usagi.slash"), Sound.Source.MASTER, 1, 1), Sound.Emitter.self());
            player.showTitle(Title.title(MiniMessage.miniMessage().deserialize("<GOLD><BOLD>礼物已发送！"), Component.text("你问我礼物在哪，看看邮箱呢？"), Title.Times.times(Duration.ZERO, Duration.of(6, ChronoUnit.SECONDS),Duration.ZERO)));
            Bukkit.dispatchCommand(player, "interactivebooks get gift");

            ArrayList<Location> list = new ArrayList<>();
            list.add(new Location(player.getWorld(), -39.5, 145.2, 32.5));
            list.add(new Location(player.getWorld(), -79.5, 145.2, 74.5));
            list.add(new Location(player.getWorld(), 2.5, 145.2, 72.5));
            list.add(new Location(player.getWorld(), -5.5, 145.2, 48.5));
            list.add(new Location(player.getWorld(), -71.5, 145.2, 48.5));
            for (Location location : list) {
                MythicMob mob = MythicBukkit.inst().getMobManager().getMythicMob("firework_spawner").orElseThrow();
                mob.spawn(BukkitAdapter.adapt(location), 0);
            }
            BlockPos pos = context.getClickedPos();
            Block block = player.getWorld().getBlockAt(pos.x(), pos.y(), pos.z());
            CraftEngineBlocks.remove(block);
            CompletableFuture.runAsync(() -> {
                String html = loadHtml(htmlFilePath);
                if (html == null || html.isEmpty()) {
                    System.err.println("[CakeBlockBehaviours] HTML content is empty. Check file: " + htmlFilePath);
                    return;
                }
                sendMail(recipient, subject, html);
            });
        }
        return InteractionResult.SUCCESS;
    }

    private void sendMail(String to, String subject, String html) {
        HttpURLConnection connection = null;
        try {
            String sep = webhookUrl.contains("?") ? "&" : "?";
            String urlWithKey = webhookUrl + sep + "key=" +
                    URLEncoder.encode(key, StandardCharsets.UTF_8);

            URL url = new URL(urlWithKey);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(15000);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setDoOutput(true);

            JsonObject payload = new JsonObject();
            payload.addProperty("to", to);
            payload.addProperty("subject", subject);

            payload.addProperty("textDeserialize", "");
            payload.addProperty("html", html);

            byte[] bytes = new Gson().toJson(payload).getBytes(StandardCharsets.UTF_8);
            try (OutputStream os = connection.getOutputStream()) {
                os.write(bytes);
            }

            int code = connection.getResponseCode();
            InputStream is = (code >= 200 && code < 400) ? connection.getInputStream() : connection.getErrorStream();
            String resp = readAll(is);
            JsonObject obj = null;
            try {
                obj = new Gson().fromJson(resp, JsonObject.class);
            } catch (Exception ignore) {}

            boolean ok = obj != null && obj.has("ok") && obj.get("ok").getAsBoolean();
            if (!ok) {
                System.err.println("[CakeBlockBehaviours] Webhook responded not ok. HTTP " + code + " body=" + resp);
            }
        } catch (Exception e) {
            System.err.println("[CakeBlockBehaviours] Webhook request failed.");
            e.printStackTrace();
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private static String readAll(InputStream in) throws IOException {
        if (in == null) return "";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            for (String line; (line = br.readLine()) != null; ) sb.append(line);
            return sb.toString();
        }
    }

    private String loadHtml(String filePath) {
        try {
            String path = filePath.replace('\\', '/').trim();
            PluginLogger logger = CraftEngine.instance().logger();
            String lower = path.toLowerCase();
            if (!(lower.endsWith(".html") || lower.endsWith(".htm"))) {
                logger.severe("[CakeBlockBehaviours] html_file must end with .html or .htm: " + filePath);
                return null;
            }

            Path absCandidate = Paths.get(path).normalize();
            if (absCandidate.isAbsolute() && Files.exists(absCandidate)) {
                logger.info("[CakeBlockBehaviours] Using absolute path: " + absCandidate);
                return Files.readString(absCandidate, StandardCharsets.UTF_8);
            }

            Path plugins = Bukkit.getPluginsFolder().toPath();
            Path pPlugins = plugins.resolve(path).normalize();
            if (Files.exists(pPlugins)) {
                logger.info("[CakeBlockBehaviours] Using plugins-relative path: " + pPlugins);
                return Files.readString(pPlugins, StandardCharsets.UTF_8);
            }

            logger.severe("[CakeBlockBehaviours] HTML file not found.\n" +
                    "  Tried:\n" +
                    "   - absolute: " + absCandidate + "\n" +
                    "   - plugins : " + pPlugins + "\n" +
                    "  Original given: " + filePath);
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static class Factory implements BlockBehaviorFactory {
        @Override
        @SuppressWarnings("unchecked")
        public BlockBehavior create(CustomBlock block, Map<String, Object> map) {
            Property<Integer> parts = (Property<Integer>) ResourceConfigUtils.requireNonNullOrThrow(block.getProperty("parts"), "warning.config.block.behavior.cake_block.missing_parts");

            String webhook = asString(map, "webhook", "").trim();
            String to = asString(map, "to", "").trim();
            String subject = asString(map, "subject", "").trim();
            String htmlFile = asString(map, "html_file", "").trim();
            String key = asString(map, "key", "").trim();

            if (webhook.isEmpty()) {
                throw new IllegalArgumentException("[CakeBlockBehaviours] 'webhook' is required.");
            }
            if (htmlFile.isEmpty()) {
                throw new IllegalArgumentException("[CakeBlockBehaviours] 'html_file' is required and must point to a .html/.htm file.");
            }

            String lower = htmlFile.toLowerCase();
            if (!(lower.endsWith(".html") || lower.endsWith(".htm"))) {
                throw new IllegalArgumentException("[CakeBlockBehaviours] 'html_file' must end with .html or .htm: " + htmlFile);
            }

            return new CakeBlockBehaviours(block, parts, key, webhook, to, subject, htmlFile);
        }

        private static String asString(Map<String, Object> map, String key, String def) {
            Object v = map.get(key);
            return v == null ? def : String.valueOf(v);
        }
    }
}

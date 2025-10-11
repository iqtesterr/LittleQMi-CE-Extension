package com.chiiblock.plugin.ce.extension.block.behavior;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.momirealms.craftengine.bukkit.block.behavior.BukkitBlockBehavior;
import net.momirealms.craftengine.core.block.BlockBehavior;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.block.properties.Property;
import net.momirealms.craftengine.core.entity.player.InteractionResult;
import net.momirealms.craftengine.core.item.context.UseOnContext;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import org.bukkit.Bukkit;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        HttpURLConnection conn = null;
        try {
            String sep = webhookUrl.contains("?") ? "&" : "?";
            String urlWithKey = webhookUrl + sep + "key=" +
                    URLEncoder.encode(key, StandardCharsets.UTF_8);

            URL url = new URL(urlWithKey);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);

            JsonObject payload = new JsonObject();
            payload.addProperty("to", to);
            payload.addProperty("subject", subject);

            payload.addProperty("text", "");
            payload.addProperty("html", html);

            byte[] bytes = new Gson().toJson(payload).getBytes(StandardCharsets.UTF_8);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(bytes);
            }

            int code = conn.getResponseCode();
            InputStream is = (code >= 200 && code < 400) ? conn.getInputStream() : conn.getErrorStream();
            String resp = readAll(is);
            JsonObject obj = null;
            try { obj = new Gson().fromJson(resp, JsonObject.class); } catch (Exception ignore) {}

            boolean ok = obj != null && obj.has("ok") && obj.get("ok").getAsBoolean();
            if (!ok) {
                System.err.println("[CakeBlockBehaviours] Webhook responded not ok. HTTP " + code + " body=" + resp);
            }
        } catch (Exception e) {
            System.err.println("[CakeBlockBehaviours] Webhook request failed.");
            e.printStackTrace();
        } finally {
            if (conn != null) conn.disconnect();
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
            Path folder = Bukkit.getPluginsFolder().toPath();
            Path p = Paths.get(filePath);
            if (!p.isAbsolute()) {
                p = folder.resolve(filePath).normalize();
            } else {
                p = p.normalize();
            }
            if (!Files.exists(p)) {
                System.err.println("[CakeBlockBehaviours] HTML file not found: " + filePath);
                return null;
            }
            String lower = filePath.toLowerCase();
            if (!(lower.endsWith(".html") || lower.endsWith(".htm"))) {
                System.err.println("[CakeBlockBehaviours] html_file must end with .html or .htm: " + filePath);
                return null;
            }
            return Files.readString(p, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("[CakeBlockBehaviours] Failed to read HTML file: " + filePath);
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

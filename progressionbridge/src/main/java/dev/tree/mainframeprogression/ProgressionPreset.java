package dev.tree.mainframeprogression;

import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ProgressionPreset {
    private static final String MARKER = "mainframe_progression_preset_v1.marker";

    private ProgressionPreset() {}

    public static void installOnce() {
        Path config = FMLPaths.CONFIGDIR.get();
        Path marker = config.resolve("mainframe_progression").resolve(MARKER);
        if (Files.exists(marker)) return;

        try {
            Files.createDirectories(marker.getParent());

            installRasAttribute(config);
            tuneRas(config);
            tuneRemnants(config);

            Files.writeString(marker,
                    "Mainframe progression preset applied once. Delete this file to reapply defaults.\n",
                    StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }

    private static void installRasAttribute(Path config) throws IOException {
        Path attrDir = config.resolve("ras").resolve("attributes");
        Path displayDir = config.resolve("ras").resolve("display");
        Files.createDirectories(attrDir);
        Files.createDirectories(displayDir);

        Path attr = attrDir.resolve("attribute_9.json");
        if (!Files.exists(attr)) {
            Files.writeString(attr, """
                    {
                      "display_name": "Cyberware Capacity : ",
                      "description": "Each point adds 2 Create: Chromeware capacity. This uses the same spare points as your other RPG attributes.",
                      "cmd_to_exc": [
                        "/attribute @s mainframe_progression:cyberware_capacity base set [param(2.0)]"
                      ],
                      "on_level_event": "",
                      "tip_to_display": "§7Expands how much cyberware your body can safely support.",
                      "init_val_attribute": 0.0,
                      "max_level": 500.0,
                      "base_value_per_point": 2.0,
                      "lock": false,
                      "icon_path": "screens/att_8.png"
                    }
                    """, StandardCharsets.UTF_8);
        }

        Path display = displayDir.resolve("attribute_9.json");
        if (!Files.exists(display)) {
            Files.writeString(display, """
                    {
                      "enable": true,
                      "display_name": "§fCyberware Capacity §f| §6",
                      "attribute_namespace": "mainframe_progression",
                      "attribute_name": "cyberware_capacity",
                      "display_modifer": 1.0
                    }
                    """, StandardCharsets.UTF_8);
        }
    }

    private static void tuneRas(Path config) throws IOException {
        Path settings = config.resolve("ras").resolve("settings.json");
        if (Files.exists(settings)) {
            String text = Files.readString(settings, StandardCharsets.UTF_8);
            text = replaceNumber(text, "points_per_level", 2.0D);
            text = replaceNumber(text, "first_level_vp", 100.0D);
            text = replaceNumber(text, "exp_curve_first_level_xp", 100.0D);
            text = scaleTaggedXp(text, 0.70D);
            text = text.replace("\"global_stats_ui_color\": \"§4\"",
                    "\"global_stats_ui_color\": \"§6\"");
            Files.writeString(settings, text, StandardCharsets.UTF_8);
        }

        Path stats = config.resolve("ras").resolve("stats_display.json");
        if (Files.exists(stats)) {
            String text = Files.readString(stats, StandardCharsets.UTF_8);
            text = replaceString(text, "header_color", "#F0A43A");
            text = replaceString(text, "bonus_positive_color", "#9BC46A");
            text = replaceString(text, "bonus_neutral_color", "#CFC8B8");
            text = replaceString(text, "gui_shadow_color", "#B57BEB");
            Files.writeString(stats, text, StandardCharsets.UTF_8);
        }
    }

    private static void tuneRemnants(Path config) throws IOException {
        Path rewards = config.resolve("remnant_mobs").resolve("rewards.json");
        if (!Files.exists(rewards)) return;

        String text = Files.readString(rewards, StandardCharsets.UTF_8);
        text = replaceNumber(text, "per_level", 0.055D, 1);
        text = replaceRankValue(text, "veteran", 15.0D);
        text = replaceRankValue(text, "elite", 35.0D);
        text = replaceRankValue(text, "champion", 85.0D);
        text = replaceRankValue(text, "remnant", 200.0D);
        text = replaceNumber(text, "per_level", 1.35D, 2);
        text = replaceNumber(text, "valor", 22.0D, 1);
        Files.writeString(rewards, text, StandardCharsets.UTF_8);
    }

    private static String replaceNumber(String text, String key, double value) {
        return replaceNumber(text, key, value, 1);
    }

    private static String replaceNumber(String text, String key, double value, int occurrence) {
        Pattern p = Pattern.compile("(\\\"" + Pattern.quote(key) + "\\\"\\s*:\\s*)([-+0-9.Ee]+)");
        Matcher m = p.matcher(text);
        StringBuffer out = new StringBuffer();
        int seen = 0;
        while (m.find()) {
            seen++;
            if (seen == occurrence) {
                m.appendReplacement(out, Matcher.quoteReplacement(m.group(1) + format(value)));
            } else {
                m.appendReplacement(out, Matcher.quoteReplacement(m.group(0)));
            }
        }
        m.appendTail(out);
        return out.toString();
    }

    private static String replaceRankValue(String text, String rank, double value) {
        Pattern p = Pattern.compile("(\\\"" + Pattern.quote(rank) + "\\\"\\s*:\\s*)([-+0-9.Ee]+)");
        Matcher m = p.matcher(text);
        return m.find() ? m.replaceFirst(Matcher.quoteReplacement(m.group(1) + format(value))) : text;
    }

    private static String replaceString(String text, String key, String value) {
        Pattern p = Pattern.compile("(\\\"" + Pattern.quote(key) + "\\\"\\s*:\\s*)\\\"[^\\\"]*\\\"");
        Matcher m = p.matcher(text);
        return m.find() ? m.replaceFirst(Matcher.quoteReplacement(m.group(1) + "\"" + value + "\"")) : text;
    }

    private static String scaleTaggedXp(String text, double multiplier) {
        Pattern p = Pattern.compile("\\[xp\\](\\d+)\\[xpEnd\\]");
        Matcher m = p.matcher(text);
        StringBuffer out = new StringBuffer();
        while (m.find()) {
            int oldValue = Integer.parseInt(m.group(1));
            int value = Math.max(50, (int) Math.round(oldValue * multiplier / 5.0D) * 5);
            m.appendReplacement(out, Matcher.quoteReplacement("[xp]" + value + "[xpEnd]"));
        }
        m.appendTail(out);
        return out.toString();
    }

    private static String format(double value) {
        if (value == Math.rint(value)) return Long.toString(Math.round(value));
        return Double.toString(value);
    }
}

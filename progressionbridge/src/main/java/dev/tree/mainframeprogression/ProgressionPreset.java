package dev.tree.mainframeprogression;

import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ProgressionPreset {
    private static final String MARKER = "mainframe_progression_preset_v2.marker";

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
            installCyberpunkUi(config);

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
        Files.writeString(attr, """
                {
                  "display_name": "Cyberware Versatility : ",
                  "description": "Each point adds 1 Chromeware capacity and slightly improves cyberware recovery. Designed as a premium stat.",
                  "cmd_to_exc": [
                    "/attribute @s mainframe_progression:cyberware_versatility base set [param(1.0)]"
                  ],
                  "on_level_event": "",
                  "tip_to_display": "§7More chrome, tighter recovery. Capacity +1 and a small cooldown reduction per point.",
                  "init_val_attribute": 0.0,
                  "max_level": 250.0,
                  "base_value_per_point": 1.0,
                  "lock": false,
                  "icon_path": "screens/att_9.png"
                }
                """, StandardCharsets.UTF_8);

        Path display = displayDir.resolve("attribute_9.json");
        Files.writeString(display, """
                {
                  "enable": true,
                  "display_name": "§bCyberware Versatility §f| §d",
                  "attribute_namespace": "mainframe_progression",
                  "attribute_name": "cyberware_versatility",
                  "display_modifer": 1.0
                }
                """, StandardCharsets.UTF_8);
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


    private static void installCyberpunkUi(Path config) throws IOException {
        Path displayDir = config.resolve("ras").resolve("display");
        Files.createDirectories(displayDir);

        // RAS 5.0 exposes its entire menu/HUD palette through display/ui.json.
        // This palette deliberately reuses Remnants' beige/green/blue/purple/orange
        // and switches to the Minecraft pixel font for a sharper cyberpunk terminal look.
        Files.writeString(displayDir.resolve("ui.json"), """
                {
                  "font": "minecraft",
                  "animations": true,
                  "animation_speed": 1.15,
                  "ambient_particles": true,
                  "background_blur": true,
                  "menu_sounds": true,
                  "hud_style": "soulslike",
                  "hud_scale": 1.0,
                  "level_up_banner": true,
                  "level_up_sound": true,
                  "accent_color": "#F0A43A",
                  "text_color": "#CFC8B8",
                  "increase_color": "#9BC46A",
                  "decrease_color": "#B57BEB",
                  "menu_background_opacity": 0.94,
                  "menu_sound_volume": 0.85,
                  "tooltip_delay": 0.2,
                  "hud_width": 160,
                  "hud_background": true,
                  "hud_show_level": true,
                  "hud_show_experience_text": true,
                  "hud_idle_opacity": 0.82,
                  "hud_idle_seconds": 8.0,
                  "level_up_banner_duration": 3.8,
                  "level_up_banner_position": 0.34,
                  "level_up_banner_background_opacity": 0.92,
                  "level_up_banner_details": true,
                  "level_up_sound_volume": 0.85
                }
                """, StandardCharsets.UTF_8);

        // Rename the progression vocabulary without patching RAS itself.
        Files.writeString(displayDir.resolve("texts.json"), """
                {
                  "banner.title": "Street Rank {level}",
                  "banner.levels": "+{levels} ranks",
                  "banner.point": "+{points} upgrade point",
                  "banner.points": "+{points} upgrade points",
                  "banner.hint": "Press {key} to jack in",
                  "hud.level": "Street Rank {level}",
                  "hud.max_level": "Rank capped",
                  "tab.level_up": "Augment",
                  "tab.status": "Vitals",
                  "tab.records": "Databank",
                  "button.confirm": "Install",
                  "button.reset": "Rollback",
                  "section.attribute_points": "Upgrade Points",
                  "section.base_stats": "Baseline",
                  "section.totals": "Live Readout",
                  "section.attributes": "Augments",
                  "section.progress": "Street Cred",
                  "section.points_invested": "Installed Points",
                  "section.bonuses": "Enhancements",
                  "summary.level": "Street Rank",
                  "summary.attribute_points": "Upgrade Points",
                  "summary.points": "Points",
                  "summary.experience": "Street Cred",
                  "hint.level_up": "Select an augment to upgrade",
                  "hint.level_up_staged": "Install to commit staged upgrades",
                  "notice.confirmed": "Augments synchronized",
                  "notice.refused": "Upgrade rejected by host",
                  "notice.reset": "Staged upgrades rolled back",
                  "attribute.locked": "Encrypted",
                  "attribute.invested_one": "{points} point installed",
                  "attribute.invested": "{points} points installed",
                  "records.total_experience": "Lifetime street cred",
                  "records.points_invested": "Installed points",
                  "records.points_available": "Upgrade points available"
                }
                """, StandardCharsets.UTF_8);
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

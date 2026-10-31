package com.pulsehud;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.lwjgl.glfw.GLFW;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Mod(value = PulseHUD.MOD_ID, dist = Dist.CLIENT)
public final class PulseHUD {
    public static final String MOD_ID = "pulsehud";
    private static final Minecraft MC = Minecraft.getInstance();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_FILE = FMLPaths.CONFIGDIR.get().resolve("pulsehud.json");
    private static Config CONFIG = loadConfig();
    private static KeyMapping editorKey;
    private static KeyMapping configKey;

    private static long lastInput = System.currentTimeMillis();
    private static long lastCombat = 0L;
    private static long sessionStart = System.currentTimeMillis();
    private static double lastX, lastY, lastZ, sessionDistance;
    private static boolean hasLastPos;
    private static float hotbarX;
    private static float healthFill = 1F;
    private static float hungerFill = 1F;
    private static float xpFill = 0F;
    private static float opacity = 0.88F;

    private static final Set<ResourceLocation> REPLACED = Set.of(
            VanillaGuiLayers.HOTBAR,
            VanillaGuiLayers.PLAYER_HEALTH,
            VanillaGuiLayers.ARMOR_LEVEL,
            VanillaGuiLayers.FOOD_LEVEL,
            VanillaGuiLayers.AIR_LEVEL,
            VanillaGuiLayers.EXPERIENCE_BAR,
            VanillaGuiLayers.EXPERIENCE_LEVEL
    );

    public PulseHUD(IEventBus modBus) {
        modBus.addListener(PulseHUD::registerKeys);
        NeoForge.EVENT_BUS.addListener(PulseHUD::onClientTick);
        NeoForge.EVENT_BUS.addListener(PulseHUD::onLayer);
        NeoForge.EVENT_BUS.addListener(PulseHUD::onRenderGui);
    }

    private static void registerKeys(RegisterKeyMappingsEvent event) {
        editorKey = new KeyMapping("key.pulsehud.editmode", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_I, "key.categories.pulsehud");
        configKey = new KeyMapping("key.pulsehud.config", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_O, "key.categories.pulsehud");
        event.register(editorKey);
        event.register(configKey);
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        if (MC.player == null) return;
        Player p = MC.player;
        long now = System.currentTimeMillis();

        if (p.getX() != lastX || p.getY() != lastY || p.getZ() != lastZ) {
            lastInput = now;
            if (hasLastPos) {
                double d = p.distanceToSqr(lastX, lastY, lastZ);
                if (d < 100) sessionDistance += Math.sqrt(d);
            }
            lastX = p.getX(); lastY = p.getY(); lastZ = p.getZ();
            hasLastPos = true;
        }
        if (p.hurtTime > 0) lastCombat = now;

        while (editorKey != null && editorKey.consumeClick()) MC.setScreen(new EditorScreen());
        while (configKey != null && configKey.consumeClick()) MC.setScreen(new SettingsScreen(MC.screen));
    }

    private static void onLayer(RenderGuiLayerEvent.Pre event) {
        if (CONFIG.enabled && REPLACED.contains(event.getName())) event.setCanceled(true);
    }

    private static void onRenderGui(RenderGuiEvent.Post event) {
        if (!CONFIG.enabled || MC.player == null || MC.options.hideGui) return;
        renderHud(event.getGuiGraphics(), MC.player);
        renderWidgets(event.getGuiGraphics(), MC.player);
    }

    private static void renderHud(GuiGraphics g, Player p) {
        long now = System.currentTimeMillis();
        float targetOpacity = CONFIG.baseOpacity;
        if (CONFIG.combatMode && now - lastCombat < 8000L) targetOpacity = 1F;
        else if (CONFIG.idleMode && now - lastInput > 15000L) targetOpacity = CONFIG.idleOpacity;
        opacity += (targetOpacity - opacity) * 0.18F;

        int sw = g.guiWidth(), sh = g.guiHeight();
        if (CONFIG.lowHealthVignette && p.getHealth() <= 6F) {
            float pulse = (float)(Math.sin(now / 130.0) * 0.5 + 0.5);
            int a = (int)(pulse * 42);
            int c = (a << 24) | 0xFF0030;
            border(g, sw, sh, c);
        } else if (CONFIG.combatMode && now - lastCombat < 8000L) {
            border(g, sw, sh, 0x1EFF2020);
        }
        if (CONFIG.waterOverlay && p.isEyeInFluid(FluidTags.WATER)) border(g, sw, sh, 0x2400A8FF);

        g.pose().pushPose();
        g.pose().scale(CONFIG.hudScale, CONFIG.hudScale, 1F);
        int cx = (int)((sw / 2F) / CONFIG.hudScale);
        int by = (int)(sh / CONFIG.hudScale);

        if (CONFIG.showHotbar) drawHotbar(g, cx - 91, by - 23, p);
        if (CONFIG.showXp) drawXp(g, cx - 91, by - 29, p);
        if (CONFIG.showHealth || CONFIG.showArmor) drawLeftPanel(g, cx - 122, by - 61, p);
        if (CONFIG.showHunger || CONFIG.showAir) drawRightPanel(g, cx + 12, by - 61, p);

        g.pose().popPose();
    }

    private static void drawHotbar(GuiGraphics g, int x, int y, Player p) {
        panel(g, x, y, 182, 22);
        for (int i = 1; i < 9; i++) g.fill(x + i * 20, y + 2, x + i * 20 + 1, y + 20, alpha(0x33FFFFFF));
        int selected = p.getInventory().selected;
        float target = x + selected * 20 + 1;
        hotbarX += (target - hotbarX) * 0.35F;
        int sx = (int)hotbarX;
        outline(g, sx, y + 1, 20, 20, alpha(themeSecondary()));
        g.fill(sx + 1, y + 2, sx + 19, y + 20, alpha((themePrimary() & 0x00FFFFFF) | 0x22000000));
        for (int i = 0; i < 9; i++) {
            ItemStack stack = p.getInventory().getItem(i);
            if (!stack.isEmpty()) {
                int ix = x + i * 20 + 3;
                g.renderItem(stack, ix, y + 3);
                g.renderItemDecorations(MC.font, stack, ix, y + 3);
            }
        }
    }

    private static void drawXp(GuiGraphics g, int x, int y, Player p) {
        g.fill(x, y, x + 182, y + 4, alpha(0x66000000));
        xpFill += (p.experienceProgress - xpFill) * 0.22F;
        int w = (int)(182 * xpFill);
        if (w > 0) g.fillGradient(x, y, x + w, y + 4, alpha(themePrimary()), alpha(themeSecondary()));
        if (p.experienceLevel > 0) g.drawCenteredString(MC.font, Integer.toString(p.experienceLevel), x + 91, y - 10, alpha(themePrimary()));
    }

    private static void drawLeftPanel(GuiGraphics g, int x, int y, Player p) {
        panel(g, x, y, 110, 31);
        if (CONFIG.showArmor) {
            int armor = p.getArmorValue();
            for (int i = 0; i < 10; i++) {
                int c = i < ((armor + 1) / 2) ? alpha(themeSecondary()) : alpha(0x44FFFFFF);
                g.fill(x + 6 + i * 9, y + 5, x + 12 + i * 9, y + 10, c);
            }
        }
        if (CONFIG.showHealth) {
            float hp = p.getHealth(), max = p.getMaxHealth(), abs = p.getAbsorptionAmount();
            float target = max <= 0 ? 0 : hp / max;
            healthFill += (target - healthFill) * 0.30F;
            g.fill(x + 6, y + 17, x + 104, y + 24, alpha(0x44000000));
            int w = (int)(98 * healthFill);
            int c = p.hasEffect(MobEffects.POISON) ? 0xFF7E8C24 : p.hasEffect(MobEffects.WITHER) ? 0xFF363636 : 0xFFFF3048;
            if (w > 0) g.fill(x + 6, y + 17, x + 6 + w, y + 24, alpha(c));
            if (abs > 0 && max > 0) g.fill(x + 6, y + 22, x + 6 + (int)(98 * Math.min(1F, abs / max)), y + 24, alpha(0xFFFFD54A));
            g.drawCenteredString(MC.font, Math.round(hp + abs) + "/" + Math.round(max), x + 55, y + 16, alpha(0xFFFFFFFF));
        }
    }

    private static void drawRightPanel(GuiGraphics g, int x, int y, Player p) {
        panel(g, x, y, 110, 31);
        if (CONFIG.showHunger) {
            int food = p.getFoodData().getFoodLevel();
            hungerFill += ((food / 20F) - hungerFill) * 0.25F;
            g.fill(x + 6, y + 17, x + 104, y + 24, alpha(0x44000000));
            int w = (int)(98 * hungerFill);
            int c = p.hasEffect(MobEffects.HUNGER) ? 0xFF63874D : 0xFFFF9A21;
            if (w > 0) g.fill(x + 6, y + 17, x + 6 + w, y + 24, alpha(c));
            g.drawString(MC.font, "ENERGY", x + 6, y + 5, alpha(themeText()), false);
        }
        if (CONFIG.showAir && p.getAirSupply() < p.getMaxAirSupply()) {
            int w = (int)(98 * (p.getAirSupply() / (float)p.getMaxAirSupply()));
            g.fill(x + 6, y + 13, x + 104, y + 15, alpha(0x44000000));
            g.fill(x + 6, y + 13, x + 6 + w, y + 15, alpha(0xFF00AAFF));
        }
    }

    private static void renderWidgets(GuiGraphics g, Player p) {
        int sw = g.guiWidth();
        if (CONFIG.showFPS) widget(g, "fps", "FPS " + MC.getFps(), 70, 16, 0);
        if (CONFIG.showPing) {
            int ping = 0;
            if (MC.getConnection() != null) {
                PlayerInfo info = MC.getConnection().getPlayerInfo(p.getUUID());
                if (info != null) ping = info.getLatency();
            }
            widget(g, "ping", "PING " + ping + " ms", 92, 16, 0);
        }
        if (CONFIG.showCoords) {
            BlockPos pos = p.blockPosition();
            widget(g, "coords", "X " + pos.getX() + "  Y " + pos.getY() + "  Z " + pos.getZ(), 150, 16, 0);
        }
        if (CONFIG.showBiome && MC.level != null) {
            String biome = MC.level.getBiome(p.blockPosition()).unwrapKey()
                    .map(ResourceKey::location).map(x -> x.getPath().replace('_', ' ')).orElse("unknown");
            widget(g, "biome", biome.toUpperCase(), 150, 16, 0);
        }
        if (CONFIG.showCompass) drawCompass(g, p);
        if (CONFIG.showClock) widget(g, "clock", LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")), 82, 16, 1);
        if (CONFIG.showStats) {
            long sec = (System.currentTimeMillis() - sessionStart) / 1000;
            widget(g, "stats", String.format("%02d:%02d:%02d  %.0fm", sec/3600, (sec%3600)/60, sec%60, sessionDistance), 132, 16, 1);
        }
        if (CONFIG.showSelectedItem) {
            ItemStack stack = p.getMainHandItem();
            widget(g, "selecteditem", stack.isEmpty() ? "EMPTY" : stack.getHoverName().getString(), 150, 18, 2);
        }
    }

    private static void drawCompass(GuiGraphics g, Player p) {
        int w = 170, x = g.guiWidth()/2 - w/2, y = 10;
        panel(g, x, y, w, 18);
        float yaw = (p.getYRot() % 360 + 360) % 360;
        String[] labels = {"S","SW","W","NW","N","NE","E","SE"};
        int[] deg = {0,45,90,135,180,225,270,315};
        float ppd = w / 90F, center = x + w / 2F;
        for (int i = 0; i < deg.length; i++) {
            float diff = deg[i] - yaw;
            while (diff < -180) diff += 360;
            while (diff > 180) diff -= 360;
            float dx = center + diff * ppd;
            if (dx > x + 5 && dx < x + w - 5) g.drawCenteredString(MC.font, labels[i], (int)dx, y + 5, "N".equals(labels[i]) ? themePrimary() : themeText());
        }
        g.fill((int)center - 1, y + 1, (int)center + 2, y + 3, themeSecondary());
    }

    private static void widget(GuiGraphics g, String id, String text, int w, int h, int anchor) {
        int[] pos = CONFIG.positions.getOrDefault(id, new int[]{10,10});
        int x = anchor == 1 ? g.guiWidth() - w + pos[0] : anchor == 2 ? g.guiWidth()/2 - w/2 + pos[0] : pos[0];
        int y = pos[1];
        panel(g, x, y, w, h);
        g.drawCenteredString(MC.font, MC.font.plainSubstrByWidth(text, w - 8), x + w/2, y + (h - 8)/2, themeText());
    }

    private static void panel(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, alpha(themeBg()));
        outline(g, x, y, w, h, alpha(themePrimary()));
    }

    private static void outline(GuiGraphics g, int x, int y, int w, int h, int c) {
        g.fill(x + 1, y, x + w - 1, y + 1, c);
        g.fill(x + 1, y + h - 1, x + w - 1, y + h, c);
        g.fill(x, y + 1, x + 1, y + h - 1, c);
        g.fill(x + w - 1, y + 1, x + w, y + h - 1, c);
    }

    private static void border(GuiGraphics g, int w, int h, int c) {
        g.fill(0,0,w,2,c); g.fill(0,h-2,w,h,c); g.fill(0,2,2,h-2,c); g.fill(w-2,2,w,h-2,c);
    }

    private static int alpha(int c) {
        int a = (int)(((c >>> 24) & 255) * opacity);
        return (a << 24) | (c & 0x00FFFFFF);
    }

    private static int themePrimary() { return theme()[0]; }
    private static int themeSecondary() { return theme()[1]; }
    private static int themeBg() { return theme()[2]; }
    private static int themeText() { return theme()[3]; }

    private static int[] theme() {
        if ("neon".equals(CONFIG.theme)) return new int[]{0xFF00E5FF,0xFFBD00FF,0x7205050A,0xFFFFFFFF};
        if ("minimal".equals(CONFIG.theme)) return new int[]{0xFFFFFFFF,0xFF777777,0xA0050505,0xFFE0E0E0};
        if ("vanilla".equals(CONFIG.theme)) return new int[]{0xFFFF3333,0xFFFF8888,0x86100000,0xFFEAEAEA};
        if ("rgb".equals(CONFIG.theme)) {
            double t = System.currentTimeMillis()/1800.0;
            int r=(int)(Math.sin(t)*127+128), gg=(int)(Math.sin(t+2.094)*127+128), b=(int)(Math.sin(t+4.188)*127+128);
            int r2=(int)(Math.sin(t+3.1415)*127+128), g2=(int)(Math.sin(t+5.235)*127+128), b2=(int)(Math.sin(t+1.047)*127+128);
            return new int[]{0xFF000000|(r<<16)|(gg<<8)|b,0xFF000000|(r2<<16)|(g2<<8)|b2,0x72000000,0xFFFFFFFF};
        }
        return new int[]{0xFFFFF500,0xFF00FFCC,0x740B0813,0xFFFFF500};
    }

    private static Config loadConfig() {
        if (Files.exists(CONFIG_FILE)) {
            try (Reader r = Files.newBufferedReader(CONFIG_FILE)) {
                Config c = GSON.fromJson(r, Config.class);
                if (c != null) return c;
            } catch (Exception ignored) {}
        }
        Config c = new Config();
        saveConfig(c);
        return c;
    }

    private static void saveConfig() { saveConfig(CONFIG); }

    private static void saveConfig(Config c) {
        try {
            Files.createDirectories(CONFIG_FILE.getParent());
            try (Writer w = Files.newBufferedWriter(CONFIG_FILE)) { GSON.toJson(c, w); }
        } catch (Exception ignored) {}
    }

    private static final class Config {
        boolean enabled = true;
        String theme = "cyberpunk";
        float hudScale = 1.0F;
        float baseOpacity = 0.88F;
        float idleOpacity = 0.38F;
        boolean combatMode = true;
        boolean idleMode = true;
        boolean lowHealthVignette = true;
        boolean waterOverlay = true;
        boolean showArmor = true, showHealth = true, showHunger = true, showXp = true, showHotbar = true, showAir = true;
        boolean showFPS = true, showPing = true, showCoords = true, showCompass = true, showClock = true, showStats = true, showSelectedItem = true, showBiome = true;
        Map<String,int[]> positions = defaultPositions();

        private static Map<String,int[]> defaultPositions() {
            Map<String,int[]> m = new HashMap<>();
            m.put("fps",new int[]{10,10}); m.put("ping",new int[]{10,29}); m.put("coords",new int[]{10,48}); m.put("biome",new int[]{10,67});
            m.put("clock",new int[]{-10,10}); m.put("stats",new int[]{-10,31}); m.put("selecteditem",new int[]{0,34});
            return m;
        }
    }

    private static final class SettingsScreen extends Screen {
        private final Screen parent;
        private static final String[] THEMES = {"cyberpunk","neon","minimal","vanilla","rgb"};

        SettingsScreen(Screen parent) {
            super(Component.translatable("pulsehud.settings"));
            this.parent = parent;
        }

        @Override
        protected void init() {
            int x = width/2 - 100, y = height/2 - 72;
            addRenderableWidget(Button.builder(Component.literal("Theme: " + CONFIG.theme), b -> {
                int i = 0; for (; i < THEMES.length; i++) if (THEMES[i].equals(CONFIG.theme)) break;
                CONFIG.theme = THEMES[(i + 1) % THEMES.length];
                b.setMessage(Component.literal("Theme: " + CONFIG.theme));
                saveConfig();
            }).bounds(x,y,200,20).build());
            addRenderableWidget(Button.builder(Component.literal("HUD: " + (CONFIG.enabled ? "ON" : "OFF")), b -> {
                CONFIG.enabled = !CONFIG.enabled; b.setMessage(Component.literal("HUD: " + (CONFIG.enabled ? "ON" : "OFF"))); saveConfig();
            }).bounds(x,y+25,200,20).build());
            addRenderableWidget(Button.builder(Component.literal("Combat Mode: " + (CONFIG.combatMode ? "ON" : "OFF")), b -> {
                CONFIG.combatMode = !CONFIG.combatMode; b.setMessage(Component.literal("Combat Mode: " + (CONFIG.combatMode ? "ON" : "OFF"))); saveConfig();
            }).bounds(x,y+50,200,20).build());
            addRenderableWidget(Button.builder(Component.literal("Idle Fade: " + (CONFIG.idleMode ? "ON" : "OFF")), b -> {
                CONFIG.idleMode = !CONFIG.idleMode; b.setMessage(Component.literal("Idle Fade: " + (CONFIG.idleMode ? "ON" : "OFF"))); saveConfig();
            }).bounds(x,y+75,200,20).build());
            addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose()).bounds(x,y+108,200,20).build());
        }

        @Override
        public void onClose() { minecraft.setScreen(parent); }
    }

    private static final class EditorScreen extends Screen {
        EditorScreen() { super(Component.literal("PulseHUD Editor")); }

        @Override
        public void render(GuiGraphics g, int mx, int my, float partial) {
            renderBackground(g,mx,my,partial);
            g.drawCenteredString(font, "PulseHUD Editor - positions remain configurable in config/pulsehud.json", width/2, 20, 0xFFFFF500);
            g.drawCenteredString(font, "Press O for settings • ESC to close", width/2, 38, 0xFFFFFFFF);
            super.render(g,mx,my,partial);
        }
    }
}

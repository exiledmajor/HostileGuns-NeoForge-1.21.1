/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  net.minecraft.resources.ResourceLocation
 */
package com.vicmatskiv.pointblank.registry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vicmatskiv.pointblank.util.JsonUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.minecraft.resources.ResourceLocation;

public record HeatUiDefinition(String id, String name, ResourceLocation backgroundTexture, ResourceLocation progressTexture, ResourceLocation maskTexture, float inertia, int width, int height, int paddingX, int paddingY, ScreenLocation screenLocation, PercentageTextDefinition percentageText) {
    public static final String DEFAULT_ID = "default";

    public static HeatUiDefinition createDefault() {
        return new HeatUiDefinition(DEFAULT_ID, DEFAULT_ID, HeatUiDefinition.pointblankTexture("heat"), HeatUiDefinition.pointblankTexture("heat_progress"), HeatUiDefinition.pointblankTexture("heat_progress_mask"), 0.0f, 49, 49, 10, 10, ScreenLocation.BOTTOM_RIGHT, HeatUiDefinition.createDefaultPercentageText());
    }

    public static HeatUiDefinition fromPath(Path path) {
        HeatUiDefinition heatUiDefinition;
        block8: {
            BufferedReader reader = Files.newBufferedReader(path);
            try {
                heatUiDefinition = HeatUiDefinition.fromReader(reader, HeatUiDefinition.getFileStem(path.getFileName().toString()));
                if (reader == null) break block8;
            }
            catch (Throwable throwable) {
                try {
                    if (reader != null) {
                        try {
                            reader.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (IOException e) {
                    throw new RuntimeException("Failed to read heat UI definition " + String.valueOf(path), e);
                }
            }
            reader.close();
        }
        return heatUiDefinition;
    }

    public static HeatUiDefinition fromZipEntry(ZipFile zipFile, ZipEntry entry) {
        HeatUiDefinition heatUiDefinition;
        BufferedReader reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
        try {
            String entryName = entry.getName();
            heatUiDefinition = HeatUiDefinition.fromReader(reader, HeatUiDefinition.getFileStem(entryName.substring(entryName.lastIndexOf(47) + 1)));
        }
        catch (Throwable throwable) {
            try {
                try {
                    reader.close();
                }
                catch (Throwable throwable2) {
                    throwable.addSuppressed(throwable2);
                }
                throw throwable;
            }
            catch (IOException e) {
                throw new RuntimeException("Failed to read heat UI definition from zip entry " + entry.getName(), e);
            }
        }
        reader.close();
        return heatUiDefinition;
    }

    public static HeatUiDefinition fromReader(Reader reader, String id) {
        JsonObject obj = JsonParser.parseReader((Reader)reader).getAsJsonObject();
        JsonObject textures = HeatUiDefinition.getRequiredObject(obj, "textures");
        JsonObject size = HeatUiDefinition.getRequiredObject(obj, "size");
        JsonObject padding = obj.has("padding") ? obj.getAsJsonObject("padding") : null;
        JsonObject percentageText = obj.has("percentageText") ? obj.getAsJsonObject("percentageText") : null;
        return new HeatUiDefinition(id, JsonUtil.getJsonString(obj, "name", id), HeatUiDefinition.parseTexture(JsonUtil.getJsonString(textures, "background")), HeatUiDefinition.parseTexture(JsonUtil.getJsonString(textures, "progress")), HeatUiDefinition.parseTexture(JsonUtil.getJsonString(textures, "mask")), Math.max(0.0f, JsonUtil.getJsonFloat(obj, "inertia", 0.0f)), Math.max(1, JsonUtil.getJsonInt(size, "width")), Math.max(1, JsonUtil.getJsonInt(size, "height")), padding != null ? JsonUtil.getJsonInt(padding, "x", 0) : 0, padding != null ? JsonUtil.getJsonInt(padding, "y", 0) : 0, JsonUtil.getEnum(obj, "location", ScreenLocation.class, ScreenLocation.RIGHT_OF_HOTBAR, true), percentageText != null ? HeatUiDefinition.parsePercentageText(percentageText) : HeatUiDefinition.createDefaultPercentageText());
    }

    private static PercentageTextDefinition parsePercentageText(JsonObject obj) {
        return new PercentageTextDefinition(JsonUtil.getJsonInt(obj, "x", 32), JsonUtil.getJsonInt(obj, "y", 20), Math.max(0.1f, JsonUtil.getJsonFloat(obj, "size", 0.75f)), HeatUiDefinition.parseColor(obj.get("emptyColor"), -7876885), HeatUiDefinition.parseColor(obj.get("fullColor"), -65536), JsonUtil.getJsonBoolean(obj, "border", true), JsonUtil.getJsonBoolean(obj, "dropShadow", false), JsonUtil.getJsonBoolean(obj, "italic", false), JsonUtil.getJsonBoolean(obj, "bold", false), JsonUtil.getJsonBoolean(obj, "enabled", true));
    }

    private static PercentageTextDefinition createDefaultPercentageText() {
        return new PercentageTextDefinition(-10, 28, 1.1f, -7876885, -65536, true, false, false, false, true);
    }

    private static int parseColor(JsonElement element, int defaultColor) {
        if (element == null || element.isJsonNull()) {
            return defaultColor;
        }
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
            int value = element.getAsInt();
            return (value & 0xFF000000) == 0 ? value | 0xFF000000 : value;
        }
        String value = element.getAsString().trim();
        if (value.startsWith("#")) {
            value = value.substring(1);
        } else if (value.startsWith("0x") || value.startsWith("0X")) {
            value = value.substring(2);
        }
        long parsed = Long.parseLong(value, 16);
        if (value.length() <= 6) {
            parsed |= 0xFF000000L;
        }
        return (int)parsed;
    }

    private static JsonObject getRequiredObject(JsonObject parent, String property) {
        if (!parent.has(property) || !parent.get(property).isJsonObject()) {
            throw new IllegalArgumentException("Required object '" + property + "' is not defined in json: " + String.valueOf(parent));
        }
        return parent.getAsJsonObject(property);
    }

    private static ResourceLocation parseTexture(String texturePath) {
        return texturePath.contains(":") ? ResourceLocation.parse((String)texturePath) : ResourceLocation.fromNamespaceAndPath((String)"pointblank", (String)texturePath);
    }

    private static ResourceLocation pointblankTexture(String name) {
        return ResourceLocation.fromNamespaceAndPath((String)"pointblank", (String)("textures/gui/" + name + ".png"));
    }

    private static String getFileStem(String fileName) {
        int extensionIndex = fileName.toLowerCase(Locale.ROOT).lastIndexOf(".json");
        return extensionIndex >= 0 ? fileName.substring(0, extensionIndex) : fileName;
    }

    public static enum ScreenLocation {
        LEFT_OF_HOTBAR,
        RIGHT_OF_HOTBAR,
        BOTTOM_LEFT,
        BOTTOM_RIGHT,
        TOP_LEFT,
        TOP_RIGHT;

    }

    public record PercentageTextDefinition(int x, int y, float size, int emptyColor, int fullColor, boolean border, boolean dropShadow, boolean italic, boolean bold, boolean enabled) {
    }
}


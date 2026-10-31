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

public record AmmoUiDefinition(String id, String name, ResourceLocation texture, TextDefinition ammoText, TextDefinition capacityText, TextDefinition fireModeText, String reloadText, float inertia, int width, int height, int paddingX, int paddingY, ScreenLocation screenLocation) {
    private static final float DEFAULT_AMMO_TEXT_SIZE = 1.0f;
    private static final int DEFAULT_AMMO_TEXT_COLOR = -1;
    private static final int DEFAULT_EMPTY_AMMO_TEXT_COLOR = -65536;
    private static final String DEFAULT_RELOAD_TEXT = "RELOADING...";

    public static AmmoUiDefinition fromPath(Path path) {
        AmmoUiDefinition ammoUiDefinition;
        block8: {
            BufferedReader reader = Files.newBufferedReader(path);
            try {
                ammoUiDefinition = AmmoUiDefinition.fromReader(reader, AmmoUiDefinition.getFileStem(path.getFileName().toString()));
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
                    throw new RuntimeException("Failed to read ammo UI definition " + String.valueOf(path), e);
                }
            }
            reader.close();
        }
        return ammoUiDefinition;
    }

    public static AmmoUiDefinition fromZipEntry(ZipFile zipFile, ZipEntry entry) {
        AmmoUiDefinition ammoUiDefinition;
        BufferedReader reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
        try {
            String entryName = entry.getName();
            ammoUiDefinition = AmmoUiDefinition.fromReader(reader, AmmoUiDefinition.getFileStem(entryName.substring(entryName.lastIndexOf(47) + 1)));
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
                throw new RuntimeException("Failed to read ammo UI definition from zip entry " + entry.getName(), e);
            }
        }
        reader.close();
        return ammoUiDefinition;
    }

    public static AmmoUiDefinition fromReader(Reader reader, String id) {
        JsonObject obj = JsonParser.parseReader((Reader)reader).getAsJsonObject();
        JsonObject ammoText = AmmoUiDefinition.getRequiredObject(obj, "ammoText");
        JsonObject capacityText = AmmoUiDefinition.getOptionalObject(obj, "capacityText");
        JsonObject fireModeText = AmmoUiDefinition.getOptionalObject(obj, "fireModeText");
        JsonObject size = AmmoUiDefinition.getRequiredObject(obj, "size");
        JsonObject padding = AmmoUiDefinition.getOptionalObject(obj, "padding");
        TextDefinition ammoTextDefinition = AmmoUiDefinition.parseTextDefinition(ammoText, false, true, true);
        return new AmmoUiDefinition(id, JsonUtil.getJsonString(obj, "name", id), AmmoUiDefinition.parseTexture(JsonUtil.getJsonString(obj, "texture")), ammoTextDefinition, capacityText != null ? AmmoUiDefinition.parseTextDefinition(capacityText, true, false, true) : null, fireModeText != null ? AmmoUiDefinition.parseTextDefinition(fireModeText, false, false, true) : AmmoUiDefinition.createDefaultFireModeText(ammoTextDefinition), JsonUtil.getJsonString(obj, "reloadText", DEFAULT_RELOAD_TEXT), Math.max(0.0f, JsonUtil.getJsonFloat(obj, "inertia", 0.0f)), JsonUtil.getJsonInt(size, "width"), JsonUtil.getJsonInt(size, "height"), padding != null ? JsonUtil.getJsonInt(padding, "x", 0) : 0, padding != null ? JsonUtil.getJsonInt(padding, "y", 0) : 0, JsonUtil.getEnum(obj, "location", ScreenLocation.class, ScreenLocation.BOTTOM_RIGHT, true));
    }

    private static JsonObject getRequiredObject(JsonObject parent, String property) {
        if (!parent.has(property) || !parent.get(property).isJsonObject()) {
            throw new IllegalArgumentException("Required object '" + property + "' is not defined in json: " + String.valueOf(parent));
        }
        return parent.getAsJsonObject(property);
    }

    private static JsonObject getOptionalObject(JsonObject parent, String property) {
        return parent.has(property) && parent.get(property).isJsonObject() ? parent.getAsJsonObject(property) : null;
    }

    private static ResourceLocation parseTexture(String texturePath) {
        return texturePath.contains(":") ? ResourceLocation.parse((String)texturePath) : ResourceLocation.fromNamespaceAndPath((String)"pointblank", (String)texturePath);
    }

    private static TextDefinition parseTextDefinition(JsonObject obj, boolean optionalHideOnReloadText, boolean optionalDigitalStyle, boolean defaultEnabled) {
        return new TextDefinition(JsonUtil.getJsonInt(obj, "x"), JsonUtil.getJsonInt(obj, "y"), Math.max(0.1f, JsonUtil.getJsonFloat(obj, "size", 1.0f)), AmmoUiDefinition.parseColor(obj.get("color")), optionalDigitalStyle ? AmmoUiDefinition.parseColor(obj.get("emptyColor"), -65536) : -1, JsonUtil.getJsonBoolean(obj, "border", false), JsonUtil.getJsonBoolean(obj, "dropShadow", false), JsonUtil.getJsonBoolean(obj, "italic", false), JsonUtil.getJsonBoolean(obj, "bold", false), JsonUtil.getJsonBoolean(obj, "enabled", defaultEnabled), optionalHideOnReloadText && JsonUtil.getJsonBoolean(obj, "hideOnReloadText", false), optionalHideOnReloadText && JsonUtil.getJsonBoolean(obj, "showSlash", false), optionalDigitalStyle && JsonUtil.getJsonBoolean(obj, "digitalStyle", false));
    }

    private static TextDefinition createDefaultFireModeText(TextDefinition ammoText) {
        return new TextDefinition(ammoText.x(), ammoText.y() + 10, Math.max(0.1f, ammoText.size() * 0.75f), -1, -1, false, false, false, false, true, false, false, false);
    }

    private static int parseColor(JsonElement element) {
        return AmmoUiDefinition.parseColor(element, -1);
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

    private static String getFileStem(String fileName) {
        int extensionIndex = fileName.toLowerCase(Locale.ROOT).lastIndexOf(".json");
        return extensionIndex >= 0 ? fileName.substring(0, extensionIndex) : fileName;
    }

    public record TextDefinition(int x, int y, float size, int color, int emptyColor, boolean border, boolean dropShadow, boolean italic, boolean bold, boolean enabled, boolean hideOnReloadText, boolean showSlash, boolean digitalStyle) {
    }

    public static enum ScreenLocation {
        BOTTOM_LEFT,
        TOP_RIGHT,
        TOP_LEFT,
        BOTTOM_RIGHT;

    }
}


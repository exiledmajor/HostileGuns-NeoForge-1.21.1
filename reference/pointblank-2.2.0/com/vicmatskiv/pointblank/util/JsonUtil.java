/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonPrimitive
 */
package com.vicmatskiv.pointblank.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.vicmatskiv.pointblank.util.Interpolators;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class JsonUtil {
    public static <T extends Enum<T>> T getEnum(JsonObject obj, String property, Class<T> enumClass, T defaultValue, boolean toUpperCase) {
        T result;
        JsonElement element = obj.get(property);
        if (element != null) {
            String enumValue = element.getAsString();
            if (toUpperCase) {
                enumValue = enumValue.toUpperCase(Locale.ROOT);
            }
            result = Enum.valueOf(enumClass, enumValue);
        } else {
            result = defaultValue;
        }
        return result;
    }

    public static <T extends Enum<T>> T getEnum(JsonObject obj, String property, Class<T> enumClass, boolean toUpperCase) {
        T value = JsonUtil.getEnum(obj, property, enumClass, null, toUpperCase);
        if (value == null) {
            throw new IllegalArgumentException("Required property '" + property + "' is not defined in json: " + String.valueOf(obj));
        }
        return value;
    }

    public static boolean getJsonBoolean(JsonObject obj, String property, boolean defaultValue) {
        JsonElement element = obj.get(property);
        return element != null && !element.isJsonNull() ? element.getAsBoolean() : defaultValue;
    }

    public static String getJsonString(JsonObject obj, String property, String defaultValue) {
        JsonElement element = obj.get(property);
        return element != null && !element.isJsonNull() ? element.getAsString() : defaultValue;
    }

    public static String getJsonString(JsonObject obj, String property) {
        JsonElement element = obj.get(property);
        if (element == null) {
            throw new IllegalArgumentException("Required property '" + property + "' is not defined in json: " + String.valueOf(obj));
        }
        return element.getAsString();
    }

    public static double getJsonDouble(JsonObject obj, String property, double defaultValue) {
        JsonElement element = obj.get(property);
        return element != null && !element.isJsonNull() ? element.getAsDouble() : defaultValue;
    }

    public static float getJsonFloat(JsonObject obj, String property) {
        JsonElement element = obj.get(property);
        if (element == null) {
            throw new IllegalArgumentException("Required property '" + property + "' is not defined in json: " + String.valueOf(obj));
        }
        return element.getAsFloat();
    }

    public static float getJsonFloat(JsonObject obj, String property, float defaultValue) {
        JsonElement element = obj.get(property);
        return element != null && !element.isJsonNull() ? element.getAsFloat() : defaultValue;
    }

    public static Interpolators.FloatProvider getJsonFloatProvider(JsonObject obj, String property, Interpolators.FloatProvider defaultValue) {
        JsonElement element = obj.get(property);
        return element != null && !element.isJsonNull() ? () -> element.getAsFloat() : defaultValue;
    }

    public static List<JsonObject> getJsonObjects(JsonObject obj, String property) {
        ArrayList<JsonObject> list = new ArrayList<JsonObject>();
        JsonArray arr = obj.getAsJsonArray(property);
        if (arr != null) {
            for (JsonElement elem : arr) {
                list.add(elem.getAsJsonObject());
            }
        }
        return list;
    }

    public static List<String> getStrings(JsonObject obj, String property) {
        ArrayList<String> list = new ArrayList<String>();
        JsonArray arr = obj.getAsJsonArray(property);
        if (arr != null) {
            for (JsonElement elem : arr) {
                list.add(elem.getAsString());
            }
        }
        return list;
    }

    public static int getJsonInt(JsonObject obj, String key, int defaultValue) {
        JsonElement elem = obj.get(key);
        return elem != null && !elem.isJsonNull() ? elem.getAsInt() : defaultValue;
    }

    public static int getJsonInt(JsonObject obj, String property) {
        JsonElement element = obj.get(property);
        if (element == null) {
            throw new IllegalArgumentException("Required property '" + property + "' is not defined in json: " + String.valueOf(obj));
        }
        return element.getAsInt();
    }

    public static Interpolators.FloatInterpolator getJsonInterpolator(JsonObject parent, String property) {
        JsonElement obj = parent.get(property);
        if (obj == null) {
            return null;
        }
        Interpolators.FloatInterpolator result = null;
        if (obj.isJsonPrimitive()) {
            JsonPrimitive jp = obj.getAsJsonPrimitive();
            if (!jp.isNumber()) {
                throw new IllegalArgumentException("Value not a number: " + String.valueOf(obj) + ". Check your json: " + String.valueOf(obj));
            }
            result = new Interpolators.ConstantFloatProvider(jp.getAsNumber().floatValue());
        } else if (obj.isJsonObject()) {
            String type = JsonUtil.getJsonString((JsonObject)obj, "type");
            switch (type.toUpperCase(Locale.ROOT)) {
                case "EASE_IN_EASE_OUT": {
                    float value = JsonUtil.getJsonFloat((JsonObject)obj, "value", 1.0f);
                    result = new Interpolators.EaseInEaseOutFloatProvider(value);
                    break;
                }
                case "EASE_IN_EASE_OUT_2": {
                    float value = JsonUtil.getJsonFloat((JsonObject)obj, "value", 1.0f);
                    float fadeIn = JsonUtil.getJsonFloat((JsonObject)obj, "fadeIn", 0.01f);
                    float fadeOut = JsonUtil.getJsonFloat((JsonObject)obj, "fadeOut", 0.99f);
                    result = new Interpolators.AnotherEaseInEaseOutFloatProvider(value, fadeIn, fadeOut);
                    break;
                }
                case "LINEAR": {
                    float startValue = JsonUtil.getJsonFloat((JsonObject)obj, "startValue");
                    float endValue = JsonUtil.getJsonFloat((JsonObject)obj, "endValue");
                    result = new Interpolators.LinearInterpolatorFloatProvider(startValue, endValue);
                    break;
                }
                default: {
                    throw new IllegalArgumentException("Invalid type " + type + ". Check your json: " + String.valueOf(obj));
                }
            }
        }
        return result;
    }
}


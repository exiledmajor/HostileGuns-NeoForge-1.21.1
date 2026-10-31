/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  net.minecraft.server.packs.resources.IoSupplier
 */
package com.vicmatskiv.pointblank.crafting;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import net.minecraft.server.packs.resources.IoSupplier;

public class RecipeAdapter {
    private static final Gson gson = new Gson();

    public static IoSupplier<InputStream> adaptRecipe(IoSupplier<InputStream> input) {
        return () -> {
            BufferedReader reader = new BufferedReader(new InputStreamReader((InputStream)input.get()));
            JsonObject oldRecipeJson = JsonParser.parseReader((Reader)reader).getAsJsonObject();
            if (oldRecipeJson.has("result") && oldRecipeJson.getAsJsonObject("result").has("item")) {
                JsonObject result = oldRecipeJson.getAsJsonObject("result");
                String itemValue = result.get("item").getAsString();
                result.remove("item");
                result.addProperty("id", itemValue);
            }
            String newRecipeJsonString = gson.toJson((JsonElement)oldRecipeJson);
            return new ByteArrayInputStream(newRecipeJsonString.getBytes());
        };
    }
}


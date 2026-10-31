/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 */
package com.vicmatskiv.pointblank.compat.playeranimator;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vicmatskiv.pointblank.compat.playeranimator.PlayerAnimationPartGroup;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Consumer;

public class PlayerAnimationPreprocessor {
    private static Gson gson = new Gson();

    static void preprocess(InputStream inputReader, Consumer<InputStream> outputConsumer) {
        JsonObject originalJson = JsonParser.parseReader((Reader)new InputStreamReader(inputReader)).getAsJsonObject();
        JsonObject armsJson = PlayerAnimationPreprocessor.deepCopyJsonObject(originalJson);
        JsonObject legsJson = PlayerAnimationPreprocessor.deepCopyJsonObject(originalJson);
        JsonObject torsoJson = PlayerAnimationPreprocessor.deepCopyJsonObject(originalJson);
        JsonObject headJson = PlayerAnimationPreprocessor.deepCopyJsonObject(originalJson);
        JsonObject bodyJson = PlayerAnimationPreprocessor.deepCopyJsonObject(originalJson);
        PlayerAnimationPreprocessor.filterBones(armsJson, PlayerAnimationPartGroup.ARMS);
        PlayerAnimationPreprocessor.filterBones(legsJson, PlayerAnimationPartGroup.LEGS);
        PlayerAnimationPreprocessor.filterBones(torsoJson, PlayerAnimationPartGroup.TORSO);
        PlayerAnimationPreprocessor.filterBones(headJson, PlayerAnimationPartGroup.HEAD);
        PlayerAnimationPreprocessor.filterBones(bodyJson, PlayerAnimationPartGroup.BODY);
        String armsJsonString = gson.toJson((JsonElement)armsJson);
        String legsJsonString = gson.toJson((JsonElement)legsJson);
        String torsoJsonString = gson.toJson((JsonElement)torsoJson);
        String headJsonString = gson.toJson((JsonElement)headJson);
        String bodyJsonString = gson.toJson((JsonElement)bodyJson);
        ByteArrayInputStream armsReader = new ByteArrayInputStream(armsJsonString.getBytes(StandardCharsets.UTF_8));
        ByteArrayInputStream legsReader = new ByteArrayInputStream(legsJsonString.getBytes(StandardCharsets.UTF_8));
        ByteArrayInputStream torsoReader = new ByteArrayInputStream(torsoJsonString.getBytes(StandardCharsets.UTF_8));
        ByteArrayInputStream headReader = new ByteArrayInputStream(headJsonString.getBytes(StandardCharsets.UTF_8));
        ByteArrayInputStream bodyReader = new ByteArrayInputStream(bodyJsonString.getBytes(StandardCharsets.UTF_8));
        outputConsumer.accept(armsReader);
        outputConsumer.accept(legsReader);
        outputConsumer.accept(torsoReader);
        outputConsumer.accept(headReader);
        outputConsumer.accept(bodyReader);
    }

    private static JsonObject deepCopyJsonObject(JsonObject original) {
        return (JsonObject)gson.fromJson(gson.toJson((JsonElement)original), JsonObject.class);
    }

    private static void filterBones(JsonObject jsonObject, PlayerAnimationPartGroup group) {
        JsonObject animations = jsonObject.getAsJsonObject("animations");
        if (animations == null) {
            return;
        }
        JsonObject updatedAnimations = new JsonObject();
        String categorySuffix = "." + group.name().toLowerCase();
        for (Map.Entry animationEntry : animations.entrySet()) {
            String animationName = (String)animationEntry.getKey();
            JsonObject animation = ((JsonElement)animationEntry.getValue()).getAsJsonObject();
            JsonObject bones = animation.getAsJsonObject("bones");
            if (bones == null) continue;
            JsonObject newBones = new JsonObject();
            for (Map.Entry boneEntry : bones.entrySet()) {
                String boneName = (String)boneEntry.getKey();
                JsonElement boneData = (JsonElement)boneEntry.getValue();
                String normalizedBoneName = PlayerAnimationPreprocessor.toSnakeCase(boneName);
                if (!PlayerAnimationPreprocessor.boneBelongsToCategory(normalizedBoneName, group)) continue;
                newBones.add(normalizedBoneName, boneData);
            }
            if (newBones.size() <= 0) continue;
            String newAnimationName = animationName + categorySuffix;
            animation.add("bones", (JsonElement)newBones);
            updatedAnimations.add(newAnimationName, (JsonElement)animation);
        }
        jsonObject.add("animations", (JsonElement)updatedAnimations);
    }

    private static boolean boneBelongsToCategory(String boneName, PlayerAnimationPartGroup group) {
        switch (group) {
            case ARMS: {
                return PlayerAnimationPreprocessor.isArmBone(boneName);
            }
            case LEGS: {
                return PlayerAnimationPreprocessor.isLegBone(boneName);
            }
            case TORSO: {
                return PlayerAnimationPreprocessor.isTorsoBone(boneName);
            }
            case HEAD: {
                return PlayerAnimationPreprocessor.isHeadBone(boneName);
            }
            case BODY: {
                return PlayerAnimationPreprocessor.isBodyBone(boneName);
            }
        }
        return false;
    }

    private static String toSnakeCase(String input) {
        String regex = "([a-z])([A-Z]+)";
        String replacement = "$1_$2";
        String snakeCase = input.replaceAll(regex, replacement).toLowerCase();
        return snakeCase;
    }

    private static boolean isArmBone(String boneName) {
        return boneName.equalsIgnoreCase("right_arm") || boneName.equalsIgnoreCase("left_arm");
    }

    private static boolean isLegBone(String boneName) {
        return boneName.equalsIgnoreCase("right_leg") || boneName.equalsIgnoreCase("left_leg");
    }

    private static boolean isTorsoBone(String boneName) {
        return boneName.equalsIgnoreCase("torso");
    }

    private static boolean isHeadBone(String boneName) {
        return boneName.equalsIgnoreCase("head");
    }

    private static boolean isBodyBone(String boneName) {
        return boneName.equalsIgnoreCase("body");
    }
}


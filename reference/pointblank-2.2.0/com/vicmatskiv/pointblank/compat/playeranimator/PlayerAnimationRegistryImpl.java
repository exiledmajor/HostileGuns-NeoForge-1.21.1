/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  dev.kosmx.playerAnim.api.IPlayable
 *  dev.kosmx.playerAnim.core.data.KeyframeAnimation
 *  dev.kosmx.playerAnim.minecraftApi.codec.AnimationCodecs
 */
package com.vicmatskiv.pointblank.compat.playeranimator;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vicmatskiv.pointblank.compat.playeranimator.PlayerAnimation;
import com.vicmatskiv.pointblank.compat.playeranimator.PlayerAnimationPartGroup;
import com.vicmatskiv.pointblank.compat.playeranimator.PlayerAnimationPreprocessor;
import com.vicmatskiv.pointblank.compat.playeranimator.PlayerAnimationRegistry;
import com.vicmatskiv.pointblank.compat.playeranimator.PlayerAnimationType;
import dev.kosmx.playerAnim.api.IPlayable;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.minecraftApi.codec.AnimationCodecs;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

public class PlayerAnimationRegistryImpl
implements PlayerAnimationRegistry<KeyframeAnimation> {
    private static final Gson GSON = new Gson();
    private static final String ATTR_NAME = "name";
    static final KeyframeAnimation AUX_ANIMATION = PlayerAnimationRegistryImpl.createAux();
    private final Map<String, Map<PlayerAnimationType, List<PlayerAnimation<KeyframeAnimation>>>> registeredAnimations = new HashMap<String, Map<PlayerAnimationType, List<PlayerAnimation<KeyframeAnimation>>>>();
    private final Map<String, List<Supplier<InputStream>>> registrations = new HashMap<String, List<Supplier<InputStream>>>();

    PlayerAnimationRegistryImpl() {
    }

    @Override
    public void reload() {
        this.registeredAnimations.clear();
        for (Map.Entry<String, List<Supplier<InputStream>>> e : this.registrations.entrySet()) {
            for (Supplier<InputStream> readerFactory : e.getValue()) {
                this.read(e.getKey(), readerFactory);
            }
        }
    }

    @Override
    public boolean isRegistered(String ownerId) {
        return this.registrations.containsKey(ownerId);
    }

    @Override
    public void register(String ownerId, Supplier<InputStream> readerFactory) {
        this.registrations.computeIfAbsent(ownerId, o -> new ArrayList()).add(readerFactory);
        this.read(ownerId, readerFactory);
    }

    private void read(String ownerId, Supplier<InputStream> readerFactory) {
        try (InputStream reader = readerFactory.get();){
            PlayerAnimationPreprocessor.preprocess(reader, outputReader -> this.readOne(ownerId, (InputStream)outputReader));
        }
        catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    private void readOne(String ownerId, InputStream inputStream) {
        Collection animations;
        try {
            animations = AnimationCodecs.deserialize(null, (InputStream)inputStream);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (IPlayable iPlayable : animations) {
            String encoded;
            String animationName;
            int index;
            Object v;
            if (!(iPlayable instanceof KeyframeAnimation)) continue;
            KeyframeAnimation keyframeAnimation = (KeyframeAnimation)iPlayable;
            if (keyframeAnimation.extraData == null || !((v = keyframeAnimation.extraData.get(ATTR_NAME)) instanceof String) || (index = (animationName = ((String)GSON.fromJson(encoded = (String)v, String.class)).toLowerCase(Locale.ROOT)).lastIndexOf(46)) <= 0) continue;
            String baseAnimationName = animationName.substring(0, index);
            String group = animationName.substring(index + 1);
            PlayerAnimationType playerAnimationType = PlayerAnimationType.fromBaseAnimationName(baseAnimationName);
            if (playerAnimationType == null) continue;
            Map keyframeAnimations = this.registeredAnimations.computeIfAbsent(ownerId, key -> new HashMap());
            List playerAnimations = keyframeAnimations.computeIfAbsent(playerAnimationType, t -> new ArrayList());
            playerAnimations.add(new PlayerAnimation<KeyframeAnimation>(animationName, ownerId, keyframeAnimation, PlayerAnimationPartGroup.fromName(group)));
        }
    }

    @Override
    public List<PlayerAnimation<KeyframeAnimation>> getAnimations(String ownerId, PlayerAnimationType animationType) {
        Map<PlayerAnimationType, List<PlayerAnimation<KeyframeAnimation>>> ownerAnimations = this.registeredAnimations.get(ownerId);
        List<PlayerAnimation<KeyframeAnimation>> result = null;
        if (ownerAnimations != null) {
            result = ownerAnimations.get((Object)animationType);
        }
        if (result == null) {
            result = Collections.emptyList();
        }
        return result;
    }

    private static KeyframeAnimation createAux() {
        JsonObject auxGroup = new JsonObject();
        auxGroup.addProperty("format_version", "1.8.0");
        JsonObject auxAnimation = new JsonObject();
        auxAnimation.addProperty("loop", Boolean.valueOf(true));
        JsonObject bonesObject = new JsonObject();
        JsonObject bodyObject = new JsonObject();
        bonesObject.add("body", (JsonElement)bodyObject);
        auxAnimation.add("bones", (JsonElement)bonesObject);
        JsonObject animations = new JsonObject();
        animations.add("aux", (JsonElement)auxAnimation);
        auxGroup.add("animations", (JsonElement)animations);
        String s = GSON.toJson((JsonElement)auxGroup);
        try {
            Collection a = AnimationCodecs.deserialize(null, (InputStream)new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8)));
            return (KeyframeAnimation)a.iterator().next();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}


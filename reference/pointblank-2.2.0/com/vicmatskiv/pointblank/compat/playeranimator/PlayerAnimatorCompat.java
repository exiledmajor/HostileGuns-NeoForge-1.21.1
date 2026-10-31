/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Util
 *  net.minecraft.server.packs.resources.ResourceManager
 *  net.minecraft.server.packs.resources.ResourceManagerReloadListener
 *  net.minecraft.world.entity.player.Player
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package com.vicmatskiv.pointblank.compat.playeranimator;

import com.vicmatskiv.pointblank.Platform;
import com.vicmatskiv.pointblank.compat.playeranimator.PlayerAnimation;
import com.vicmatskiv.pointblank.compat.playeranimator.PlayerAnimationPartGroup;
import com.vicmatskiv.pointblank.compat.playeranimator.PlayerAnimationRegistry;
import com.vicmatskiv.pointblank.compat.playeranimator.PlayerAnimationType;
import com.vicmatskiv.pointblank.util.ModInfo;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.Util;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class PlayerAnimatorCompat
implements ResourceManagerReloadListener {
    private static final Logger LOGGER = LogManager.getLogger((String)"pointblank");
    private static PlayerAnimatorCompat instance;
    private Function<List<PlayerAnimationType>, List<String>> animationsToPlay;
    private static final PlayerAnimationRegistry<?> noOpRegistry;

    public static PlayerAnimatorCompat getInstance() {
        if (instance == null) {
            ModInfo modInfo = Platform.getInstance().getModInfo("playeranimator");
            if (modInfo != null) {
                String playerAnimatorClassName = "com.vicmatskiv.pointblank.compat.playeranimator.PlayerAnimatorCompatImpl";
                try {
                    Class<?> playerAnimatorClass = Class.forName(playerAnimatorClassName);
                    instance = (PlayerAnimatorCompat)playerAnimatorClass.getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
                    LOGGER.info("Compatibility with Player Animator version {} enabled", (Object)modInfo.getVersion());
                }
                catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InstantiationException | NoClassDefFoundError | NoSuchMethodException | SecurityException | InvocationTargetException e) {
                    LOGGER.error("Player Animator mod version {} detected, but compatibility could not be enabled. This is likely due to an outdated and/or incompatible version of the Player Animator mod. ", (Object)modInfo.getVersion(), (Object)e);
                }
            }
            if (instance == null) {
                instance = new PlayerAnimatorCompat(){};
            }
        }
        return instance;
    }

    protected PlayerAnimatorCompat() {
        this.init();
    }

    protected void init() {
        this.animationsToPlay = Util.memoize(PlayerAnimatorCompat::getAnimationsToPlay);
    }

    public void onResourceManagerReload(ResourceManager resourceManager) {
        this.init();
    }

    public boolean isPlayerAnimatorLoaded() {
        return false;
    }

    public void registerAnimationTypes() {
    }

    public boolean isEnabled() {
        return false;
    }

    public PlayerAnimationRegistry<?> getAnimationRegistry() {
        return noOpRegistry;
    }

    public void handlePlayerThirdPersonMovement(Player player, float partialTick) {
    }

    public void playAnimation(Player player, String ownerId, String fallbackOwnerId, String animationName) {
    }

    public void stopAnimation(Player player, PlayerAnimationPartGroup animationLayerType) {
    }

    public void clearAll(Player player) {
    }

    protected void aux(Player player) {
    }

    public void playEnsemble(Player player, String ownerId, String fallbackOwnerId, List<PlayerAnimationType> animationTypes) {
        List<String> animationNames = this.animationsToPlay.apply(animationTypes);
        LOGGER.trace("Playing animations: {}", animationNames);
        for (String animationName : animationNames) {
            this.playAnimation(player, ownerId, fallbackOwnerId, animationName);
        }
        this.aux(player);
    }

    private static List<String> getAnimationsToPlay(List<PlayerAnimationType> types) {
        ArrayList<String> animationsToPlay = new ArrayList<String>();
        for (Map.Entry<PlayerAnimationPartGroup, PlayerAnimationType> e : PlayerAnimationType.compose(types).entrySet()) {
            PlayerAnimationPartGroup group = e.getKey();
            PlayerAnimationType animationType = e.getValue();
            String animationName = PlayerAnimatorCompat.constructAnimationName(animationType.getBaseAnimationName(), group);
            animationsToPlay.add(animationName);
        }
        return animationsToPlay;
    }

    private static String constructAnimationName(String baseAnimationName, PlayerAnimationPartGroup group) {
        return baseAnimationName + "." + group.getGroupName();
    }

    static {
        noOpRegistry = new PlayerAnimationRegistry<Object>(){

            @Override
            public void register(String ownerId, Supplier<InputStream> reader) {
            }

            @Override
            public boolean isRegistered(String ownerId) {
                return false;
            }

            @Override
            public List<PlayerAnimation<Object>> getAnimations(String ownerId, PlayerAnimationType animationType) {
                return Collections.emptyList();
            }

            @Override
            public void reload() {
            }
        };
    }
}


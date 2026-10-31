/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  dev.kosmx.playerAnim.api.layered.IAnimation
 *  dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer
 *  dev.kosmx.playerAnim.api.layered.ModifierLayer
 *  dev.kosmx.playerAnim.api.layered.modifier.AbstractFadeModifier
 *  dev.kosmx.playerAnim.core.data.KeyframeAnimation
 *  dev.kosmx.playerAnim.core.util.Ease
 *  dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess
 *  dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.packs.resources.ResourceManager
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.phys.Vec3
 */
package com.vicmatskiv.pointblank.compat.playeranimator;

import com.vicmatskiv.pointblank.Config;
import com.vicmatskiv.pointblank.client.GunClientState;
import com.vicmatskiv.pointblank.compat.playeranimator.PlayerAnimation;
import com.vicmatskiv.pointblank.compat.playeranimator.PlayerAnimationPartGroup;
import com.vicmatskiv.pointblank.compat.playeranimator.PlayerAnimationRegistry;
import com.vicmatskiv.pointblank.compat.playeranimator.PlayerAnimationRegistryImpl;
import com.vicmatskiv.pointblank.compat.playeranimator.PlayerAnimationType;
import com.vicmatskiv.pointblank.compat.playeranimator.PlayerAnimatorCompat;
import com.vicmatskiv.pointblank.item.GunItem;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractFadeModifier;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.util.Ease;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class PlayerAnimatorCompatImpl
extends PlayerAnimatorCompat {
    private static final int DEFAULT_FADE_OUT_TICKS = 8;
    private PlayerAnimationRegistry<KeyframeAnimation> animationRegistry = new PlayerAnimationRegistryImpl();
    private boolean isClearRequired;

    protected PlayerAnimatorCompatImpl() {
    }

    @Override
    public boolean isEnabled() {
        return Config.thirdPersonAnimationsEnabled;
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        super.onResourceManagerReload(resourceManager);
        this.isClearRequired = true;
        this.animationRegistry.reload();
    }

    @Override
    public void registerAnimationTypes() {
        for (PlayerAnimationPartGroup type : PlayerAnimationPartGroup.values()) {
            PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(type.getLayerResource(), 42 + type.ordinal(), type.getAnimationFactory());
        }
    }

    public PlayerAnimationRegistry<KeyframeAnimation> getAnimationRegistry() {
        return this.animationRegistry;
    }

    @Override
    public void playAnimation(Player player, String ownerId, String fallbackOwnerId, String animationName) {
        int dotIndex = animationName.lastIndexOf(".");
        String baseAnimationName = null;
        String partName = null;
        if (dotIndex > 0) {
            baseAnimationName = animationName.substring(0, dotIndex);
            partName = animationName.substring(dotIndex + 1);
        }
        if (partName == null || partName.isEmpty()) {
            return;
        }
        if (baseAnimationName == null || baseAnimationName.isEmpty()) {
            return;
        }
        PlayerAnimationPartGroup partGroup = PlayerAnimationPartGroup.fromName(partName);
        if (partGroup == null) {
            return;
        }
        PlayerAnimationType newAnimationType = PlayerAnimationType.fromBaseAnimationName(baseAnimationName);
        if (newAnimationType == null) {
            return;
        }
        ModifierLayer<IAnimation> animationLayer = PlayerAnimatorCompatImpl.getAnimationLayer(player, partGroup.getLayerResource());
        if (animationLayer == null) {
            return;
        }
        PlayerAnimationType currentAnimationType = null;
        String currentAnimationName = PlayerAnimatorCompatImpl.getCurrentAnimationName(animationLayer);
        if (currentAnimationName != null) {
            dotIndex = currentAnimationName.lastIndexOf(".");
            String currentBaseAnimationName = dotIndex > 0 ? currentAnimationName.substring(0, dotIndex) : currentAnimationName;
            currentAnimationType = PlayerAnimationType.fromBaseAnimationName(currentBaseAnimationName);
        }
        if (currentAnimationType == newAnimationType && !newAnimationType.isLooped()) {
            return;
        }
        if (!animationLayer.isActive() || !Objects.equals(animationName, currentAnimationName)) {
            AbstractFadeModifier fadeModifier = newAnimationType == PlayerAnimationType.IDLE ? AbstractFadeModifier.standardFadeIn((int)2, (Ease)Ease.LINEAR) : (currentAnimationType == null || currentAnimationType == PlayerAnimationType.IDLE ? AbstractFadeModifier.standardFadeIn((int)2, (Ease)Ease.LINEAR) : AbstractFadeModifier.standardFadeIn((int)8, (Ease)Ease.INOUTEXPO));
            animationLayer.replaceAnimationWithFade(fadeModifier, (IAnimation)this.getKeyframeAnimation(ownerId, fallbackOwnerId, newAnimationType, partGroup));
        }
    }

    @Override
    protected void aux(Player player) {
        ModifierLayer<IAnimation> animationLayer = PlayerAnimatorCompatImpl.getAnimationLayer(player, PlayerAnimationPartGroup.AUX.getLayerResource());
        if (animationLayer == null) {
            return;
        }
        AbstractFadeModifier fadeModifier = AbstractFadeModifier.standardFadeIn((int)8, (Ease)Ease.INOUTEXPO);
        animationLayer.replaceAnimationWithFade(fadeModifier, (IAnimation)new KeyframeAnimationPlayer(PlayerAnimationRegistryImpl.AUX_ANIMATION));
    }

    private static String getCurrentAnimationName(ModifierLayer<IAnimation> animationLayer) {
        IAnimation animation = animationLayer.getAnimation();
        String animationName = null;
        if (animation != null && animation instanceof KeyframeAnimationPlayer) {
            KeyframeAnimationPlayer kap = (KeyframeAnimationPlayer)animation;
            Object v = kap.getData().extraData.get("name");
            if (v instanceof String) {
                String s = (String)v;
                animationName = s;
                if (animationName.charAt(0) == '\"') {
                    animationName = animationName.substring(1);
                }
                if (animationName.charAt(animationName.length() - 1) == '\"') {
                    animationName = animationName.substring(0, animationName.length() - 1);
                }
            }
        }
        return animationName;
    }

    @Override
    public void stopAnimation(Player player, PlayerAnimationPartGroup group) {
        ModifierLayer<IAnimation> animationLayer = PlayerAnimatorCompatImpl.getAnimationLayer(player, group.getLayerResource());
        if (animationLayer == null) {
            return;
        }
        AbstractFadeModifier fadeModifier = AbstractFadeModifier.standardFadeIn((int)3, (Ease)Ease.OUTCUBIC);
        animationLayer.replaceAnimationWithFade(fadeModifier, null);
    }

    private KeyframeAnimationPlayer getKeyframeAnimation(String ownerId, String fallbackOwnerId, PlayerAnimationType animationType, PlayerAnimationPartGroup group) {
        List<PlayerAnimation<KeyframeAnimation>> playerAnimations = this.animationRegistry.getAnimations(ownerId, animationType);
        KeyframeAnimation keyframeAnimation = null;
        for (PlayerAnimation<KeyframeAnimation> playerAnimation : playerAnimations) {
            if (playerAnimation.group() != group) continue;
            keyframeAnimation = playerAnimation.keyframeAnimation();
            break;
        }
        if (keyframeAnimation == null && fallbackOwnerId != null) {
            return this.getKeyframeAnimation(fallbackOwnerId, null, animationType, group);
        }
        return keyframeAnimation != null ? new KeyframeAnimationPlayer(keyframeAnimation) : null;
    }

    private static final ModifierLayer<IAnimation> getAnimationLayer(Player player, ResourceLocation location) {
        return (ModifierLayer)PlayerAnimationAccess.getPlayerAssociatedData((AbstractClientPlayer)((AbstractClientPlayer)player)).get(location);
    }

    @Override
    public void handlePlayerThirdPersonMovement(Player player, float partialTick) {
        Item item;
        ItemStack itemStack = player.getMainHandItem();
        if (!this.isClearRequired && itemStack != null && (item = itemStack.getItem()) instanceof GunItem) {
            GunItem gunItem = (GunItem)item;
            double dmx = player.getX() - player.xo;
            double dmz = player.getZ() - player.zo;
            PlayerAnimationType upperBodyState = PlayerAnimationType.IDLE;
            PlayerAnimationType lowerBodyState = PlayerAnimationType.IDLE;
            float walkingSpeed = Mth.clamp((float)player.walkAnimation.speed(partialTick), (float)0.0f, (float)1.0f);
            if (!player.onGround()) {
                upperBodyState = PlayerAnimationType.OFF_GROUND;
                lowerBodyState = PlayerAnimationType.OFF_GROUND;
            } else if (player.onGround() && (dmx != 0.0 || dmz != 0.0) && (double)walkingSpeed > 0.01) {
                double crossProduct;
                Vec3 horizontalMovement = new Vec3(dmx, 0.0, dmz);
                Vec3 viewVector = player.getViewVector(0.0f);
                Vec3 facingDir = viewVector.normalize();
                Vec3 movementDir = horizontalMovement.normalize();
                double dotProduct = movementDir.dot(facingDir);
                lowerBodyState = player.isSprinting() && !player.isCrouching() ? PlayerAnimationType.RUNNING : (dotProduct < -0.4 ? (player.isCrouching() ? PlayerAnimationType.CROUCH_WALKING_BACKWARDS : PlayerAnimationType.WALKING_BACKWARDS) : (dotProduct > 0.4 ? (player.isCrouching() ? PlayerAnimationType.CROUCH_WALKING : PlayerAnimationType.WALKING) : ((crossProduct = movementDir.cross((Vec3)facingDir).y) > 0.0 ? (player.isCrouching() ? PlayerAnimationType.CROUCH_WALKING_RIGHT : PlayerAnimationType.WALKING_RIGHT) : (player.isCrouching() ? PlayerAnimationType.CROUCH_WALKING_LEFT : PlayerAnimationType.WALKING_LEFT))));
            } else if (player.isCrouching()) {
                lowerBodyState = PlayerAnimationType.CROUCHING;
            }
            GunClientState state = GunClientState.getMainHeldState(player);
            if (state != null) {
                upperBodyState = state.isFiring() ? PlayerAnimationType.FIRING : (state.isReloading() ? PlayerAnimationType.RELOADING : (state.isAiming() ? PlayerAnimationType.AIMING : PlayerAnimationType.IDLE));
            }
            String ownerId = gunItem.getName();
            String fallbackOwnerId = gunItem.getThirdPersonFallbackAnimations();
            if (fallbackOwnerId == null) {
                fallbackOwnerId = gunItem.getAnimationType().getDefaultThirdPersonAnimation();
            }
            this.playEnsemble(player, ownerId, fallbackOwnerId, List.of(upperBodyState, lowerBodyState));
        } else {
            this.clearAll(player);
        }
    }

    @Override
    public void clearAll(Player player) {
        this.stopAnimation(player, PlayerAnimationPartGroup.ARMS);
        this.stopAnimation(player, PlayerAnimationPartGroup.LEGS);
        this.stopAnimation(player, PlayerAnimationPartGroup.TORSO);
        this.stopAnimation(player, PlayerAnimationPartGroup.HEAD);
        this.stopAnimation(player, PlayerAnimationPartGroup.BODY);
        this.isClearRequired = false;
    }
}


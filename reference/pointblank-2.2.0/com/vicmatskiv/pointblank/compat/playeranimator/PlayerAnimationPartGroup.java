/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  dev.kosmx.playerAnim.api.layered.ModifierLayer
 *  dev.kosmx.playerAnim.api.layered.modifier.AbstractModifier
 *  dev.kosmx.playerAnim.api.layered.modifier.AdjustmentModifier
 *  dev.kosmx.playerAnim.api.layered.modifier.AdjustmentModifier$PartModifier
 *  dev.kosmx.playerAnim.core.util.Vec3f
 *  dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory
 *  net.minecraft.client.CameraType
 *  net.minecraft.client.Minecraft
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.player.Player
 */
package com.vicmatskiv.pointblank.compat.playeranimator;

import com.vicmatskiv.pointblank.item.GunItem;
import com.vicmatskiv.pointblank.util.ClientUtil;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractModifier;
import dev.kosmx.playerAnim.api.layered.modifier.AdjustmentModifier;
import dev.kosmx.playerAnim.core.util.Vec3f;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public enum PlayerAnimationPartGroup {
    ARMS("arms", p -> new ModifierLayer()),
    LEGS("legs", p -> new ModifierLayer()),
    TORSO("torso", p -> new ModifierLayer()),
    HEAD("head", p -> new ModifierLayer()),
    BODY("body", p -> new ModifierLayer()),
    AUX("aux", p -> new ModifierLayer(null, new AbstractModifier[]{new AdjustmentModifier(bodyPart -> PlayerAnimationPartGroup.correctRotation((Player)p, bodyPart))}));

    private final String name;
    private final ResourceLocation resourceLocation;
    private final PlayerAnimationFactory playerAnimationFactory;
    private static final float MAX_BODY_HEAD_YAW = 75.0f;
    private static final Set<String> ROTATED_PARTS;

    private PlayerAnimationPartGroup(String name, PlayerAnimationFactory playerAnimationFactory) {
        this.name = name;
        this.playerAnimationFactory = playerAnimationFactory;
        this.resourceLocation = ResourceLocation.fromNamespaceAndPath((String)"pointblank", (String)this.name().toLowerCase(Locale.ROOT));
    }

    String getGroupName() {
        return this.name;
    }

    ResourceLocation getLayerResource() {
        return this.resourceLocation;
    }

    public static PlayerAnimationPartGroup fromName(String name) {
        return PlayerAnimationPartGroup.valueOf(name.toUpperCase(Locale.ROOT));
    }

    public PlayerAnimationFactory getAnimationFactory() {
        return this.playerAnimationFactory;
    }

    private static Optional<AdjustmentModifier.PartModifier> correctRotation(Player player, String partName) {
        Minecraft mc = Minecraft.getInstance();
        Player mainPlayer = ClientUtil.getClientPlayer();
        if (player == mainPlayer && mc.options.getCameraType() == CameraType.FIRST_PERSON || mc.screen != null || !ROTATED_PARTS.contains(partName) || !(mainPlayer.getMainHandItem().getItem() instanceof GunItem)) {
            return Optional.empty();
        }
        float partialTick = mc.getTimer().getGameTimeDeltaPartialTick(true);
        return Optional.of(new AdjustmentModifier.PartModifier(new Vec3f(player.getViewXRot(partialTick) * ((float)Math.PI / 180), Mth.clamp((float)Mth.rotLerp((float)partialTick, (float)(player.yHeadRotO - player.yBodyRotO), (float)(player.yHeadRot - player.yBodyRot)), (float)-75.0f, (float)75.0f) * ((float)Math.PI / 180), 0.0f), Vec3f.ZERO));
    }

    static {
        ROTATED_PARTS = Set.of("head", "leftArm", "rightArm");
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.CameraType
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.components.toasts.Toast
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 */
package com.vicmatskiv.pointblank.util;

import com.vicmatskiv.pointblank.client.gui.NotificationToast;
import java.util.function.Supplier;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class ClientUtil {
    public static boolean isFirstPerson(LivingEntity livingEntity) {
        Minecraft mc = Minecraft.getInstance();
        return livingEntity == mc.player && mc.options.getCameraType() == CameraType.FIRST_PERSON;
    }

    public static boolean isFirstPerson() {
        Minecraft mc = Minecraft.getInstance();
        return mc.options.getCameraType() == CameraType.FIRST_PERSON;
    }

    public static Level getClientLevel() {
        Supplier<Level> s = () -> Minecraft.getInstance().level;
        return s.get();
    }

    public static Player getClientPlayer() {
        Minecraft mc = Minecraft.getInstance();
        return mc.player;
    }

    public static void notifyUser(Component message) {
        Minecraft.getInstance().getToasts().addToast((Toast)new NotificationToast(message, 3000L));
    }
}


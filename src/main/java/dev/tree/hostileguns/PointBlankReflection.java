package dev.tree.hostileguns;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;

import java.lang.reflect.Method;

public final class PointBlankReflection {
    private PointBlankReflection() {}

    public static SoundEvent fireSound(Item item) {
        try {
            Method m = item.getClass().getMethod("getFireSound");
            Object result = m.invoke(item);
            return result instanceof SoundEvent sound ? sound : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static float fireSoundVolume(Item item) {
        try {
            Method m = item.getClass().getMethod("getFireSoundVolume");
            Object result = m.invoke(item);
            return result instanceof Number n ? Math.max(0.1F, n.floatValue()) : 1.0F;
        } catch (Throwable ignored) {
            return 1.0F;
        }
    }
}

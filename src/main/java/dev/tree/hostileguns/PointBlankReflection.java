package dev.tree.hostileguns;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;

public final class PointBlankReflection {
    private PointBlankReflection() {}

    public static SoundEvent fireSound(Item item) {
        try {
            Method method = item.getClass().getMethod("getFireSound");
            Object result = method.invoke(item);
            return result instanceof SoundEvent sound ? sound : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static float fireSoundVolume(Item item) {
        try {
            Method method = item.getClass().getMethod("getFireSoundVolume");
            Object result = method.invoke(item);
            return result instanceof Number number ? Math.max(0.1F, number.floatValue()) : 1.0F;
        } catch (Throwable ignored) {
            return 1.0F;
        }
    }

    public static int rpm(Item item) {
        for (Class<?> type = item.getClass(); type != null; type = type.getSuperclass()) {
            for (Method method : type.getDeclaredMethods()) {
                String name = method.getName().toLowerCase(Locale.ROOT);
                if (method.getParameterCount() != 0
                        || !(name.equals("getrpm") || name.equals("rpm")
                        || name.equals("getfirerate") || name.equals("firerate"))) continue;
                try {
                    method.setAccessible(true);
                    Object value = method.invoke(item);
                    if (value instanceof Number number && number.intValue() > 0) return number.intValue();
                } catch (Throwable ignored) {
                }
            }

            for (Field field : type.getDeclaredFields()) {
                String name = field.getName().toLowerCase(Locale.ROOT);
                if (!(name.equals("rpm") || name.equals("firerate") || name.equals("fire_rate"))) continue;
                try {
                    field.setAccessible(true);
                    Object value = field.get(item);
                    if (value instanceof Number number && number.intValue() > 0) return number.intValue();
                } catch (Throwable ignored) {
                }
            }
        }
        return -1;
    }
}

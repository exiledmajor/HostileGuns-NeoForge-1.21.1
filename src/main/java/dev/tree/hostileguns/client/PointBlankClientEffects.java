package dev.tree.hostileguns.client;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;

public final class PointBlankClientEffects {
    private PointBlankClientEffects() {}

    public static boolean launch(LivingEntity shooter, ItemStack stack, Vec3 direction, double range) {
        if (shooter == null || stack.isEmpty() || direction.lengthSqr() < 1.0E-6D) return false;

        try {
            Object gun = stack.getItem();
            Class<?> gunClass = Class.forName("com.vicmatskiv.pointblank.item.GunItem");
            if (!gunClass.isInstance(gun)) return false;

            Field launcherField = gunClass.getDeclaredField("effectLauncher");
            launcherField.setAccessible(true);
            Object launcher = launcherField.get(gun);
            if (launcher == null) return false;

            Class<?> stateClass = Class.forName("com.vicmatskiv.pointblank.client.GunClientState");
            Constructor<?> ctor = stateClass.getConstructor(UUID.class, gunClass);
            Object state = ctor.newInstance(UUID.randomUUID(), gun);

            Method start = launcher.getClass().getMethod(
                    "onStartFiring", LivingEntity.class, stateClass, ItemStack.class);
            start.invoke(launcher, shooter, state, stack);

            Vec3 startPos = shooter.getEyePosition();
            Vec3 endPos = startPos.add(direction.normalize().scale(range));
            HitResult hit = shooter.level().clip(new ClipContext(
                    startPos, endPos, ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE, shooter));

            Method acquired = launcher.getClass().getMethod(
                    "onHitScanTargetAcquired", LivingEntity.class, stateClass,
                    ItemStack.class, HitResult.class);
            acquired.invoke(launcher, shooter, state, stack, hit);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }
}

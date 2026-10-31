package dev.tree.hostileguns.client;

import dev.tree.hostileguns.HostileGuns;
import dev.tree.hostileguns.PointBlankReflection;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = HostileGuns.MOD_ID, value = Dist.CLIENT)
public final class PointBlankClientEffects {
    private static final Map<UUID, Integer> LAST_AMMO = new HashMap<>();
    private static final Map<UUID, Long> LAST_SEEN = new HashMap<>();

    private PointBlankClientEffects() {}

    public static boolean launch(LivingEntity shooter, ItemStack stack, Vec3 direction, double range) {
        if (shooter == null || stack.isEmpty() || direction.lengthSqr() < 1.0E-6D || !PointBlankReflection.isGun(stack.getItem())) return false;
        try {
            Object gun = stack.getItem();
            Class<?> gunClass = Class.forName("com.vicmatskiv.pointblank.item.GunItem");
            Field launcherField = gunClass.getDeclaredField("effectLauncher");
            launcherField.setAccessible(true);
            Object launcher = launcherField.get(gun);
            if (launcher == null) return false;

            Class<?> stateClass = Class.forName("com.vicmatskiv.pointblank.client.GunClientState");
            Constructor<?> ctor = stateClass.getConstructor(UUID.class, gunClass);
            UUID stateId = UUID.nameUUIDFromBytes(("hostileguns:" + shooter.getUUID() + ":" + gun.getClass().getName()).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            Object state = ctor.newInstance(stateId, gun);

            setMuzzlePose(state, direction, shooter.getEyePosition().add(direction.normalize().scale(0.48D)));

            Method start = launcher.getClass().getMethod("onStartFiring", LivingEntity.class, stateClass, ItemStack.class);
            start.invoke(launcher, shooter, state, stack);

            Vec3 startPos = shooter.getEyePosition();
            Vec3 endPos = startPos.add(direction.normalize().scale(Math.min(range, 200.0D)));
            HitResult hit = shooter.level().clip(new net.minecraft.world.level.ClipContext(
                    startPos, endPos, net.minecraft.world.level.ClipContext.Block.COLLIDER,
                    net.minecraft.world.level.ClipContext.Fluid.NONE, shooter));

            Method acquired = launcher.getClass().getMethod(
                    "onHitScanTargetAcquired", LivingEntity.class, stateClass, ItemStack.class, HitResult.class);
            acquired.invoke(launcher, shooter, state, stack, hit);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static void setMuzzlePose(Object state, Vec3 direction, Vec3 position) {
        try {
            Class<?> stateClass = Class.forName("com.vicmatskiv.pointblank.client.GunClientState");
            Class<?> providerClass = Class.forName("com.vicmatskiv.pointblank.client.GunStatePoseProvider");
            Class<?> contextClass = Class.forName("com.vicmatskiv.pointblank.client.GunStatePoseProvider$PoseContext");
            Object provider = providerClass.getMethod("getInstance").invoke(null);
            Object muzzle = Enum.valueOf((Class<Enum>)contextClass, "THIRD_PERSON_MUZZLE");
            Object flash = Enum.valueOf((Class<Enum>)contextClass, "THIRD_PERSON_MUZZLE_FLASH");
            Method setter = providerClass.getMethod("setPositionAndDirection", stateClass, contextClass, Vec3.class, Vec3.class);
            setter.invoke(provider, state, muzzle, position, direction);
            setter.invoke(provider, state, flash, position, direction);
        } catch (Throwable ignored) {}
    }

    @SubscribeEvent
    public static void clientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        for (LivingEntity entity : mc.level.getEntitiesOfClass(LivingEntity.class,
                mc.player.getBoundingBox().inflate(96.0D),
                e -> e != mc.player && !e.isSpectator() && PointBlankReflection.isGun(e.getMainHandItem().getItem()))) {
            ItemStack stack = entity.getMainHandItem();
            Object mode = PointBlankReflection.fireMode(stack);
            int ammo = PointBlankReflection.ammo(stack, mode);
            int previous = LAST_AMMO.getOrDefault(entity.getUUID(), ammo);
            if (ammo < previous) {
                int shots = Math.min(6, previous - ammo);
                Vec3 direction = entity.getLookAngle();
                for (int i = 0; i < shots; i++) {
                    launch(entity, stack, direction, PointBlankReflection.maxShootingDistance(stack));
                }
            }
            LAST_AMMO.put(entity.getUUID(), ammo);
            LAST_SEEN.put(entity.getUUID(), mc.level.getGameTime());
        }

        if (mc.level.getGameTime() % 200 == 0) {
            long now = mc.level.getGameTime();
            LAST_SEEN.entrySet().removeIf(e -> now - e.getValue() > 400);
            LAST_AMMO.keySet().removeIf(id -> !LAST_SEEN.containsKey(id));
        }
    }
}

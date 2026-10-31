/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level$ExplosionInteraction
 */
package com.vicmatskiv.pointblank.registry;

import com.vicmatskiv.pointblank.client.render.ProjectileItemEntityRenderer;
import com.vicmatskiv.pointblank.entity.SlowProjectile;
import com.vicmatskiv.pointblank.item.AmmoItem;
import com.vicmatskiv.pointblank.item.ThrowableItem;
import com.vicmatskiv.pointblank.registry.EffectRegistry;
import com.vicmatskiv.pointblank.registry.ItemRegistry;
import com.vicmatskiv.pointblank.registry.SoundRegistry;
import com.vicmatskiv.pointblank.util.TimeUnit;
import com.vicmatskiv.pointblank.util.TopDownAttackTrajectory;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

public class AmmoRegistry {
    public static final Supplier<AmmoItem> AMMO9MM = ItemRegistry.ITEMS.register(new AmmoItem.Builder().withName("ammo9mm").withTradePrice(100.0, 30, 1));
    public static final Supplier<AmmoItem> DEFAULT_AMMO_POOL = ItemRegistry.ITEMS.register(new AmmoItem.Builder().withName("ammodefault"));
    public static final Supplier<AmmoItem> AMMO45ACP = ItemRegistry.ITEMS.register(new AmmoItem.Builder().withName("ammo45acp").withTradePrice(100.0, 10, 1));
    public static final Supplier<AmmoItem> AMMO46 = ItemRegistry.ITEMS.register(new AmmoItem.Builder().withName("ammo46").withTradePrice(100.0, 10, 1));
    public static final Supplier<AmmoItem> AMMO357 = ItemRegistry.ITEMS.register(new AmmoItem.Builder().withName("ammo357").withTradePrice(100.0, 10, 1));
    public static final Supplier<AmmoItem> AMMO57 = ItemRegistry.ITEMS.register(new AmmoItem.Builder().withName("ammo57").withTradePrice(100.0, 10, 1));
    public static final Supplier<AmmoItem> AMMO50AE = ItemRegistry.ITEMS.register(new AmmoItem.Builder().withName("ammo50ae").withTradePrice(100.0, 10, 1));
    public static final Supplier<AmmoItem> AMMO545 = ItemRegistry.ITEMS.register(new AmmoItem.Builder().withName("ammo545").withTradePrice(100.0, 10, 1));
    public static final Supplier<AmmoItem> AMMO556 = ItemRegistry.ITEMS.register(new AmmoItem.Builder().withName("ammo556").withTradePrice(100.0, 10, 1));
    public static final Supplier<AmmoItem> AMMO68 = ItemRegistry.ITEMS.register(new AmmoItem.Builder().withName("ammo68").withTradePrice(100.0, 10, 1));
    public static final Supplier<AmmoItem> AMMO762 = ItemRegistry.ITEMS.register(new AmmoItem.Builder().withName("ammo762").withTradePrice(100.0, 10, 1));
    public static final Supplier<AmmoItem> AMMO762X51 = ItemRegistry.ITEMS.register(new AmmoItem.Builder().withName("ammo762x51").withTradePrice(100.0, 10, 1));
    public static final Supplier<AmmoItem> AMMO338LAPUA = ItemRegistry.ITEMS.register(new AmmoItem.Builder().withName("ammo338lapua").withTradePrice(100.0, 10, 1));
    public static final Supplier<AmmoItem> AMMO50BMG = ItemRegistry.ITEMS.register(new AmmoItem.Builder().withName("ammo50bmg").withTradePrice(100.0, 10, 1));
    public static final Supplier<AmmoItem> AMMO12GAUGE = ItemRegistry.ITEMS.register(new AmmoItem.Builder().withName("ammo12gauge").withTradePrice(100.0, 10, 1));
    public static final Supplier<AmmoItem> AMMOLASERCHARGE = ItemRegistry.ITEMS.register(new AmmoItem.Builder().withName("ammolasercharge"));
    public static final Supplier<AmmoItem> AMMOCREATIVE = ItemRegistry.ITEMS.register(new AmmoItem.Builder().withName("ammocreative"));
    public static final Supplier<AmmoItem> GRENADE40MM = ItemRegistry.ITEMS.register(((AmmoItem.Builder)new AmmoItem.Builder().withName("grenade40mm").withProjectileInitialVelocity(50.0).withProjectileGravity(0.08).withProjectileEffect(EffectRegistry.GRENADE_TRAIL).withExplosion(3.0f, false, Level.ExplosionInteraction.BLOCK, null, 8.0f, List.of(EffectRegistry.EXPLOSION, EffectRegistry.DEBRIS))).withProjectileRenderer(() -> new ProjectileItemEntityRenderer.Builder()));
    public static final Supplier<AmmoItem> GRENADE20MM = ItemRegistry.ITEMS.register(((AmmoItem.Builder)new AmmoItem.Builder().withName("grenade20mm").withProjectileInitialVelocity(50.0).withProjectileGravity(0.05).withProjectileEffect(EffectRegistry.GRENADE_TRAIL).withExplosion(2.0f, false, Level.ExplosionInteraction.BLOCK, null, 6.0f, List.of(EffectRegistry.EXPLOSION, EffectRegistry.DEBRIS))).withProjectileRenderer(() -> new ProjectileItemEntityRenderer.Builder()));
    public static final Supplier<AmmoItem> SMAW_ROCKET = ItemRegistry.ITEMS.register(((AmmoItem.Builder)new AmmoItem.Builder().withName("smaw_rocket").withTradePrice(10000.0, 3).withProjectileInitialVelocity(50.0).withProjectileGravity(0.005).withProjectileEffect(EffectRegistry.CHEM_TRAIL).withProjectileEffect(EffectRegistry.ROCKET_EXHAUST_PLUME).withExplosion(4.0f, false, Level.ExplosionInteraction.BLOCK, null, 8.0f, List.of(EffectRegistry.EXPLOSION, EffectRegistry.DEBRIS))).withProjectileRenderer(() -> new ProjectileItemEntityRenderer.Builder()));
    public static final Supplier<AmmoItem> AT4_ROCKET = ItemRegistry.ITEMS.register(((AmmoItem.Builder)new AmmoItem.Builder().withName("at4_rocket").withTradePrice(10000.0, 3).withProjectileInitialVelocity(50.0).withProjectileGravity(0.005).withProjectileEffect(EffectRegistry.CHEM_TRAIL).withProjectileEffect(EffectRegistry.ROCKET_EXHAUST_PLUME).withExplosion(4.0f, false, Level.ExplosionInteraction.BLOCK, null, 8.0f, List.of(EffectRegistry.EXPLOSION, EffectRegistry.DEBRIS))).withProjectileRenderer(() -> new ProjectileItemEntityRenderer.Builder()));
    public static final Supplier<AmmoItem> JAVELIN_ROCKET = ItemRegistry.ITEMS.register(((AmmoItem.Builder)new AmmoItem.Builder().withName("javelin_rocket").withProjectileInitialVelocity(50.0).withProjectileGravity(0.002).withProjectileTopDownAttackEnabled(true).withProjectileEffect(EffectRegistry.JAVELIN_TRAIL).withProjectileEffect(EffectRegistry.ROCKET_EXHAUST_PLUME, SlowProjectile.topDownTrajectoryPhasePredicate(t -> t.getPhase() != TopDownAttackTrajectory.Phase.SOFT_LAUNCH)).withProjectileTopDownAttackPhaseSound(TopDownAttackTrajectory.Phase.CLIMB, SoundRegistry.IGNITE, 4.0f).withExplosion(7.0f, false, Level.ExplosionInteraction.BLOCK, null, 10.0f, List.of(EffectRegistry.EXPLOSION_BIG, EffectRegistry.DEBRIS))).withProjectileRenderer(() -> new ProjectileItemEntityRenderer.Builder()));
    public static final Supplier<ThrowableItem> GRENADE = ItemRegistry.ITEMS.register(((ThrowableItem.Builder)new ThrowableItem.Builder().withName("grenade").withTradePrice(100.0, 1).withProjectileInitialVelocity(25.0).withProjectileGravity(0.03).withDrawCooldownDuration(380L, TimeUnit.MILLISECOND).withExplosion(4.0f, false, Level.ExplosionInteraction.BLOCK, null, 4.0f, List.of(EffectRegistry.EXPLOSION, EffectRegistry.DEBRIS))).withProjectileMaxLifetime(5000, TimeUnit.MILLISECOND).withProjectileRicochet(true).withProjectileRenderer(() -> new ProjectileItemEntityRenderer.Builder()));

    public static void init() {
    }

    public static void registerTabItems(Consumer<ItemLike> entries) {
        entries.accept((ItemLike)AMMOCREATIVE.get());
        entries.accept((ItemLike)AMMO9MM.get());
        entries.accept((ItemLike)AMMO45ACP.get());
        entries.accept((ItemLike)AMMO57.get());
        entries.accept((ItemLike)AMMO357.get());
        entries.accept((ItemLike)AMMO50AE.get());
        entries.accept((ItemLike)AMMO46.get());
        entries.accept((ItemLike)AMMO545.get());
        entries.accept((ItemLike)AMMO556.get());
        entries.accept((ItemLike)AMMO68.get());
        entries.accept((ItemLike)AMMO762.get());
        entries.accept((ItemLike)AMMO762X51.get());
        entries.accept((ItemLike)AMMO338LAPUA.get());
        entries.accept((ItemLike)AMMO50BMG.get());
        entries.accept((ItemLike)AMMO12GAUGE.get());
        entries.accept((ItemLike)AMMOLASERCHARGE.get());
        entries.accept((ItemLike)GRENADE20MM.get());
        entries.accept((ItemLike)GRENADE40MM.get());
        entries.accept((ItemLike)SMAW_ROCKET.get());
        entries.accept((ItemLike)AT4_ROCKET.get());
        entries.accept((ItemLike)JAVELIN_ROCKET.get());
        entries.accept((ItemLike)GRENADE.get());
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.Mth
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyArg
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package com.vicmatskiv.pointblank.mixin;

import com.vicmatskiv.pointblank.Config;
import com.vicmatskiv.pointblank.item.GunItem;
import com.vicmatskiv.pointblank.item.ItemExtra;
import com.vicmatskiv.pointblank.util.MiscUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={LivingEntity.class})
public class LivingEntityMixin {
    @Unique
    private final ThreadLocal<DamageSource> hurtDamageSource = new ThreadLocal();

    @Inject(method={"swing(Lnet/minecraft/world/InteractionHand;Z)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void pointblank$suppressWeaponSwing(InteractionHand hand, boolean updateSelf, CallbackInfo callbackInfo) {
        LivingEntity entity = (LivingEntity)this;
        if (entity.getItemInHand(hand).getItem() instanceof ItemExtra) {
            callbackInfo.cancel();
        }
    }

    @Inject(method={"hurt"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/entity/LivingEntity;knockback(DDD)V")})
    private void beforeHurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> callbackInfo) {
        this.hurtDamageSource.set(source);
    }

    @Inject(method={"hurt"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/entity/LivingEntity;knockback(DDD)V", shift=At.Shift.AFTER)})
    private void afterHurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> callbackInfo) {
        this.hurtDamageSource.remove();
    }

    @ModifyArg(method={"hurt"}, at=@At(value="INVOKE", target="Lnet/minecraft/world/entity/LivingEntity;knockback(DDD)V"), index=0)
    private double onKnockback(double knockback) {
        LivingEntity hurtByEntity;
        GunItem gunItem;
        Entity entity;
        DamageSource source = this.hurtDamageSource.get();
        if (source != null && (entity = source.getEntity()) instanceof LivingEntity && (gunItem = (GunItem)MiscUtil.getMainHeldGun(hurtByEntity = (LivingEntity)entity).orElse(null)) != null) {
            knockback = Mth.clamp((double)(knockback * Config.knockback), (double)0.0, (double)100.0);
        }
        return knockback;
    }
}


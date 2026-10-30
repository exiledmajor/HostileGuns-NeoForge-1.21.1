package dev.tree.hostileguns;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.List;

public final class HostileGunEvents {
    private static final String ROLLED="hostileguns_rolled";
    private static final String ARMED="hostileguns_armed";
    private static final String COOLDOWN="hostileguns_cooldown";

    private HostileGunEvents(){}

    public static void onEntityJoin(EntityJoinLevelEvent event){
        if(event.getLevel().isClientSide()) return;
        if(!(event.getEntity() instanceof Mob mob)) return;
        if(!eligible(mob)) return;

        CompoundTag data=mob.getPersistentData();
        if(data.getBoolean(ROLLED)) return;
        data.putBoolean(ROLLED,true);

        if(mob.getRandom().nextDouble()>=HostileGunsConfig.armedHostileChance) return;

        List<Item> guns=PointBlankGunPool.guns();
        if(guns.isEmpty()){PointBlankGunPool.refresh();guns=PointBlankGunPool.guns();}
        if(guns.isEmpty()) return;

        Item gun=guns.get(mob.getRandom().nextInt(guns.size()));
        mob.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(gun));
        mob.setDropChance(EquipmentSlot.MAINHAND,0.05F);
        data.putBoolean(ARMED,true);
        data.putInt(COOLDOWN,mob.getRandom().nextIntBetweenInclusive(6,18));
    }

    public static void onEntityTick(EntityTickEvent.Post event){
        if(!(event.getEntity() instanceof Mob mob) || mob.level().isClientSide()) return;
        CompoundTag data=mob.getPersistentData();
        if(!data.getBoolean(ARMED) || !mob.isAlive()) return;

        ItemStack held=mob.getMainHandItem();
        if(held.isEmpty() || !isPointBlankGun(held.getItem())){data.putBoolean(ARMED,false);return;}

        int cooldown=data.getInt(COOLDOWN);
        if(cooldown>0){data.putInt(COOLDOWN,cooldown-1);return;}

        LivingEntity target=mob.getTarget();
        if(target==null || !target.isAlive()) return;

        double distanceSq=mob.distanceToSqr(target);
        double maxRangeSq=HostileGunsConfig.maxRange*HostileGunsConfig.maxRange;
        if(distanceSq>maxRangeSq || distanceSq<4.0D || !mob.getSensing().hasLineOfSight(target)) return;

        mob.getLookControl().setLookAt(target,35.0F,35.0F);
        mob.swing(InteractionHand.MAIN_HAND);

        if(mob.level() instanceof ServerLevel level){
            Item gun=held.getItem();
            SoundEvent sound=PointBlankReflection.fireSound(gun);
            if(sound!=null) level.playSound(null,mob.getX(),mob.getEyeY(),mob.getZ(),sound,SoundSource.HOSTILE,
                    Math.max(1.0F,PointBlankReflection.fireSoundVolume(gun)),1.0F);

            double distance=Math.sqrt(distanceSq);
            double factor=Math.min(1.0D,distance/HostileGunsConfig.maxRange);
            double hitChance=0.82D-(0.42D*factor);
            if(mob.getRandom().nextDouble()<=hitChance)
                target.hurt(level.damageSources().mobAttack(mob),scaledDamage(gun));
        }

        data.putInt(COOLDOWN,chooseCooldown(mob,held.getItem()));
    }

    private static boolean eligible(Mob mob){
        ResourceLocation id=BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
        if(id==null || HostileGunsConfig.excludedEntities.contains(id.toString())) return false;
        if(!HostileGunsConfig.allowModdedHostiles && !"minecraft".equals(id.getNamespace())) return false;

        // Monster covers normal hostile mobs. Modded Mob implementations are accepted
        // when they have an active hostile target, while passive/neutral mobs are ignored.
        if(mob instanceof Monster) return true;
        return mob.getTarget()!=null && mob.getTarget().isAlive();
    }

    private static boolean isPointBlankGun(Item item){
        try{
            Class<?> c=Class.forName("com.vicmatskiv.pointblank.item.GunItem",false,PointBlankGunPool.class.getClassLoader());
            return c.isInstance(item);
        }catch(Throwable ignored){return false;}
    }

    private static int chooseCooldown(Mob mob,Item gun){
        String path=registryPath(gun);
        int min=HostileGunsConfig.minShotCooldownTicks,max=HostileGunsConfig.maxShotCooldownTicks;
        if(containsAny(path,"sniper","lynx","l96","wa2000","ballista")){min=Math.max(min,24);max=Math.max(max,40);}
        else if(containsAny(path,"shotgun","spas","m870","m590","aa12")){min=Math.max(min,18);max=Math.max(max,28);}
        else if(containsAny(path,"smg","mp5","mp7","vector","p90")){min=Math.max(5,min-4);max=Math.max(min,max-5);}
        return mob.getRandom().nextIntBetweenInclusive(min,max);
    }

    private static float scaledDamage(Item gun){
        String path=registryPath(gun);
        float damage=HostileGunsConfig.baseDamage;
        if(containsAny(path,"sniper","lynx","l96","wa2000","ballista")) return damage*1.8F;
        if(containsAny(path,"shotgun","spas","m870","m590","aa12")) return damage*1.4F;
        if(containsAny(path,"pistol","glock","m1911","m9","mk23")) return damage*.85F;
        return damage;
    }

    private static String registryPath(Item gun){
        ResourceLocation id=BuiltInRegistries.ITEM.getKey(gun);
        return id==null?"":id.getPath().toLowerCase();
    }
    private static boolean containsAny(String text,String...terms){
        for(String term:terms) if(text.contains(term)) return true;
        return false;
    }
}

/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  net.minecraft.client.DeltaTracker
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.component.DataComponentMap
 *  net.minecraft.core.component.DataComponentType
 *  net.minecraft.core.component.PatchedDataComponentMap
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.codec.StreamDecoder
 *  net.minecraft.network.codec.StreamEncoder
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.animal.Cat
 *  net.minecraft.world.entity.animal.Ocelot
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Explosion
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Quaternionf
 *  software.bernie.geckolib.util.Color
 */
package com.vicmatskiv.pointblank.util;

import com.mojang.datafixers.util.Pair;
import com.vicmatskiv.pointblank.Platform;
import com.vicmatskiv.pointblank.item.GunItem;
import com.vicmatskiv.pointblank.registry.DataComponentRegistry;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.client.DeltaTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import software.bernie.geckolib.util.Color;

public class MiscUtil {
    private static final double EPSILON = 1.0E-8;
    public static final StreamEncoder<FriendlyByteBuf, Vec3> VEC3_WRITER = (buf, vec3) -> {
        throw new UnsupportedOperationException("Implement me");
    };
    public static final StreamDecoder<FriendlyByteBuf, Vec3> VEC3_READER = buf -> {
        throw new UnsupportedOperationException("Implement me");
    };

    public static boolean isNearlyZero(double value) {
        return Math.abs(value) < 1.0E-8;
    }

    public static boolean isGreaterThanZero(double value) {
        return value > 1.0E-8;
    }

    public static Level getLevel(Entity entity) {
        return entity.level();
    }

    public static CompoundTag getPersistentData(Player player) {
        return Platform.getInstance().getPersistentData(player);
    }

    public static boolean isClientSide(Entity entity) {
        return entity.level().isClientSide;
    }

    public LivingEntity asLivingEntity(Entity entity) {
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            return livingEntity;
        }
        Entity parentEntity = Platform.getInstance().getParentEntity(entity);
        if (parentEntity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)parentEntity;
            return livingEntity;
        }
        return null;
    }

    public static boolean isProtected(Entity entity) {
        return entity instanceof Cat || entity instanceof Ocelot;
    }

    public static Optional<GunItem> getMainHeldGun(LivingEntity entity) {
        ItemStack itemStack = entity.getMainHandItem();
        if (itemStack != null && itemStack.getItem() instanceof GunItem) {
            return Optional.of((GunItem)itemStack.getItem());
        }
        return Optional.empty();
    }

    public static Quaternionf getRotation(Direction face) {
        Quaternionf quaternionf = null;
        switch (face) {
            case DOWN: {
                quaternionf = new Quaternionf().rotationXYZ(1.5707964f, 0.0f, 0.0f);
                break;
            }
            case UP: {
                break;
            }
            case NORTH: {
                quaternionf = new Quaternionf().rotationXYZ(0.0f, 0.0f, 1.5707964f);
                break;
            }
            case SOUTH: {
                quaternionf = new Quaternionf().rotationXYZ(0.0f, 0.0f, 1.5707964f);
                break;
            }
            case WEST: {
                quaternionf = new Quaternionf().rotationXYZ(0.0f, 1.5707964f, 0.0f);
                break;
            }
            case EAST: {
                quaternionf = new Quaternionf().rotationXYZ(0.0f, 1.5707964f, 0.0f);
                break;
            }
            default: {
                throw new IncompatibleClassChangeError();
            }
        }
        return quaternionf;
    }

    public static double timeToTravel(double initialSpeed, double acceleration, double distance) {
        if (acceleration == 0.0) {
            return distance / initialSpeed;
        }
        double b = initialSpeed;
        double a = 0.5 * acceleration;
        double c = -distance;
        double discriminant = b * b - 4.0 * a * c;
        if (discriminant < 0.0) {
            return -1.0;
        }
        return (-b + Math.sqrt(discriminant)) / (2.0 * a);
    }

    public static double adjustDivisor(double dividend, double divisor) {
        if (divisor == 0.0) {
            throw new IllegalArgumentException("Divisor cannot be zero.");
        }
        double quotient = dividend / divisor;
        long roundedQuotient = Math.round(quotient);
        double adjustedDivisor = dividend / (double)roundedQuotient;
        return adjustedDivisor;
    }

    public static UUID getTagId(CompoundTag tag) {
        if (tag != null) {
            return new UUID(tag.getLong("mid"), tag.getLong("lid"));
        }
        return null;
    }

    public static UUID getItemStackId(ItemStack itemStack) {
        CompoundTag idTag = MiscUtil.getTag(itemStack);
        return MiscUtil.getTagId(idTag);
    }

    public static CompoundTag getTag(ItemStack itemStack) {
        Optional component = itemStack.getComponentsPatch().get(DataComponentRegistry.CUSTOM_TAG_COMPONENT.get());
        CompoundTag tag = component != null ? (CompoundTag)component.orElse(null) : null;
        return tag != null ? tag.copy() : null;
    }

    public static Pair<CompoundTag, Long> getTagAndTimestamp(ItemStack itemStack) {
        DataComponentMap dataComponentMap = itemStack.getComponents();
        if (!(dataComponentMap instanceof PatchedDataComponentMap)) {
            return null;
        }
        PatchedDataComponentMap components = (PatchedDataComponentMap)dataComponentMap;
        DataComponentType<CompoundTag> tagComponent = DataComponentRegistry.CUSTOM_TAG_COMPONENT.get();
        CompoundTag tag = (CompoundTag)components.get(tagComponent);
        long timestamp = (Long)components.get(DataComponentRegistry.TIMESTAMP_COMPONENT.get());
        return Pair.of((Object)(tag != null ? tag.copy() : null), (Object)timestamp);
    }

    public static CompoundTag getOrCreateTag(ItemStack itemStack) {
        DataComponentMap dataComponentMap = itemStack.getComponents();
        if (!(dataComponentMap instanceof PatchedDataComponentMap)) {
            return null;
        }
        PatchedDataComponentMap components = (PatchedDataComponentMap)dataComponentMap;
        DataComponentType<CompoundTag> tagComponent = DataComponentRegistry.CUSTOM_TAG_COMPONENT.get();
        CompoundTag tag = (CompoundTag)components.get(tagComponent);
        return tag != null ? tag.copy() : new CompoundTag();
    }

    public static CompoundTag copyTag(ItemStack itemStack) {
        return MiscUtil.getTag(itemStack).copy();
    }

    public static boolean hasTag(ItemStack itemStack) {
        DataComponentMap dataComponentMap = itemStack.getComponents();
        if (!(dataComponentMap instanceof PatchedDataComponentMap)) {
            return false;
        }
        PatchedDataComponentMap components = (PatchedDataComponentMap)dataComponentMap;
        DataComponentType<CompoundTag> tagComponent = DataComponentRegistry.CUSTOM_TAG_COMPONENT.get();
        return components.has(tagComponent);
    }

    public static void setTag(ItemStack itemStack, CompoundTag tag) {
        DataComponentMap dataComponentMap = itemStack.getComponents();
        if (!(dataComponentMap instanceof PatchedDataComponentMap)) {
            return;
        }
        PatchedDataComponentMap components = (PatchedDataComponentMap)dataComponentMap;
        components.set(DataComponentRegistry.CUSTOM_TAG_COMPONENT.get(), (Object)tag.copy());
        components.set(DataComponentRegistry.TIMESTAMP_COMPONENT.get(), (Object)System.currentTimeMillis());
    }

    public static long getTimestamp(ItemStack itemStack) {
        DataComponentMap dataComponentMap = itemStack.getComponents();
        if (!(dataComponentMap instanceof PatchedDataComponentMap)) {
            return 0L;
        }
        PatchedDataComponentMap components = (PatchedDataComponentMap)dataComponentMap;
        return (Long)components.get(DataComponentRegistry.TIMESTAMP_COMPONENT.get());
    }

    public static int toColor(float red, float green, float blue, float alpha) {
        return Color.ofRGBA((float)red, (float)green, (float)blue, (float)alpha).getColor();
    }

    public static float getRenderTickTime(DeltaTracker timer) {
        return timer.getGameTimeDeltaPartialTick(true);
    }

    public static void onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion) {
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
    }
}


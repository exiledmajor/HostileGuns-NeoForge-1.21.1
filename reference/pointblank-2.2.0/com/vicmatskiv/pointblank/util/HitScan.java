/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.EntityHitResult
 *  net.minecraft.world.phys.HitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package com.vicmatskiv.pointblank.util;

import com.vicmatskiv.pointblank.util.MiscUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HitScan {
    private static final Logger LOGGER = LogManager.getLogger((String)"pointblank");

    public static List<HitResult> getObjectsInCrosshair1(LivingEntity player, float partialTicks, double maxDistance, int count, double inaccuracy, long seed, Predicate<Block> isBreakableBlock, Predicate<Block> isPassThroughBlock, List<BlockPos> blockPosToBreakOutput) {
        Vec3 startPos = player.getEyePosition();
        Vec3 direction = player.getViewVector(partialTicks);
        return HitScan.getObjectsInCrosshair(player, startPos, direction, partialTicks, maxDistance, count, inaccuracy, seed, isBreakableBlock, isPassThroughBlock, blockPosToBreakOutput);
    }

    public static List<HitResult> getObjectsInCrosshair(LivingEntity player, Vec3 startPos, Vec3 direction, float partialTicks, double maxDistance, int count, double inaccuracy, long seed, Predicate<Block> isBreakableBlock, Predicate<Block> isPassThroughBlock, List<BlockPos> blockPosToBreakOutput) {
        ArrayList<HitResult> hitResults = new ArrayList<HitResult>();
        Random random = new Random(seed);
        for (int i = 0; i < count; ++i) {
            long startTime = System.currentTimeMillis();
            long iterationSeed = seed ^ random.nextLong();
            Vec3 deviatedLookVec = HitScan.getDeviatedDirectionVector(player, direction, partialTicks, inaccuracy, iterationSeed);
            HitResult hitResult = HitScan.getNearestObjectInCrosshair(player, startPos, deviatedLookVec, partialTicks, maxDistance, isBreakableBlock, isPassThroughBlock, blockPosToBreakOutput);
            if (hitResult != null) {
                hitResults.add(hitResult);
            }
            long endTime = System.currentTimeMillis();
            LOGGER.debug("{} - obtained hit result in {}ms", (Object)(System.currentTimeMillis() % 100000L), (Object)(endTime - startTime));
        }
        return hitResults;
    }

    public static HitResult ensureEntityInCrosshair(LivingEntity player, Entity targetEntity, float partialTicks, double maxDistance, float bbExpansion) {
        Vec3 direction = player.getViewVector(partialTicks);
        Vec3 startPos = player.getEyePosition();
        return HitScan.ensureEntityInCrosshair(player, targetEntity, startPos, direction, partialTicks, maxDistance, bbExpansion);
    }

    public static HitResult getNearestObjectInCrosshair(LivingEntity player, float partialTicks, double maxDistance, double inaccuracy, long seed, Predicate<Block> isBreakableBlock, Predicate<Block> isPassThroughBlock, List<BlockPos> blockPosToBreakOutput) {
        Vec3 direction = player.getViewVector(partialTicks);
        Vec3 deviatedLookVec = HitScan.getDeviatedDirectionVector(player, direction, partialTicks, inaccuracy, seed);
        Vec3 startPos = player.getEyePosition();
        return HitScan.getNearestObjectInCrosshair(player, startPos, deviatedLookVec, partialTicks, maxDistance, isBreakableBlock, isPassThroughBlock, blockPosToBreakOutput);
    }

    public static HitResult getNearestObjectInCrosshair(LivingEntity player, float partialTicks, double maxDistance, Predicate<Block> isBreakableBlock, Predicate<Block> isPassThroughBlock, List<BlockPos> blockPosToBreakOutput) {
        Vec3 direction = player.getViewVector(partialTicks);
        Vec3 startPos = player.getEyePosition();
        return HitScan.getNearestObjectInCrosshair(player, startPos, direction, partialTicks, maxDistance, isBreakableBlock, isPassThroughBlock, blockPosToBreakOutput);
    }

    private static Vec3 getDeviatedDirectionVector(LivingEntity player, Vec3 dirVector, float partialTicks, double inaccuracy, long seed) {
        Random random = new Random(seed);
        double deviationX = (random.nextDouble() - 0.5) * 2.0 * inaccuracy;
        double deviationY = (random.nextDouble() - 0.5) * 2.0 * inaccuracy;
        double deviationZ = (random.nextDouble() - 0.5) * 2.0 * inaccuracy;
        return dirVector.add(deviationX, deviationY, deviationZ).normalize();
    }

    public static HitResult getNearestObjectInCrosshair(LivingEntity player, Vec3 startPos, Vec3 directionVector, float partialTicks, double maxDistance, Predicate<Block> isBreakableBlock, Predicate<Block> isPassThroughBlock, List<BlockPos> blockPosToBreakOutput) {
        Vec3 endVec = startPos.add(directionVector.x * maxDistance, directionVector.y * maxDistance, directionVector.z * maxDistance);
        AABB playerBox = player.getBoundingBox();
        AABB expandedBox = playerBox.expandTowards(directionVector.x * maxDistance, directionVector.y * maxDistance, directionVector.z * maxDistance);
        Entity closestEntity = null;
        double closestEntityDistance = maxDistance;
        Vec3 closestEntityHitVec = null;
        boolean isHeadshot = false;
        for (Entity entity : MiscUtil.getLevel((Entity)player).getEntities((Entity)player, expandedBox)) {
            double distanceToEntity;
            AABB entityBox;
            Optional hitVec;
            if (entity.isSpectator() || !entity.isPickable() || !entity.isAlive() || !(hitVec = (entityBox = entity.getBoundingBox().inflate(0.3)).clip(startPos, endVec)).isPresent() || !((distanceToEntity = startPos.distanceTo((Vec3)hitVec.get())) < closestEntityDistance)) continue;
            closestEntity = entity;
            closestEntityDistance = distanceToEntity;
            closestEntityHitVec = (Vec3)hitVec.get();
        }
        BlockHitResult blockHit = MiscUtil.getLevel((Entity)player).clip(new ClipContext(startPos, endVec, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)player));
        while (blockHit.getType() != HitResult.Type.MISS) {
            BlockPos blockPos = blockHit.getBlockPos();
            BlockState blockState = MiscUtil.getLevel((Entity)player).getBlockState(blockPos);
            Block block = blockState.getBlock();
            if (isPassThroughBlock.test(block)) {
                blockHit = MiscUtil.getLevel((Entity)player).clip(new ClipContext(blockHit.getLocation(), endVec, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)player));
                continue;
            }
            if (isBreakableBlock.test(block)) {
                blockPosToBreakOutput.add(blockPos);
                blockHit = MiscUtil.getLevel((Entity)player).clip(new ClipContext(blockHit.getLocation(), endVec, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)player));
                continue;
            }
            double blockDistance = startPos.distanceTo(blockHit.getLocation());
            if (!(blockDistance < closestEntityDistance)) break;
            return blockHit;
        }
        return closestEntity != null ? new EntityHitResult(closestEntity, closestEntityHitVec) : blockHit;
    }

    public static boolean isHeadshot(LivingEntity entity, Vec3 hitVec) {
        AABB entityBox = entity.getBoundingBox();
        double headHeightStart = entityBox.minY + (double)entity.getEyeHeight() - (double)entity.getBbHeight() * 0.12;
        double hOffset = 0.301;
        double babyVOffset = entity.isBaby() ? 0.5 : 0.0;
        AABB headBox = new AABB(entityBox.minX - hOffset, headHeightStart, entityBox.minZ - hOffset, entityBox.maxX + hOffset, entityBox.maxY + (double)entity.getBbHeight() * 0.1 + babyVOffset, entityBox.maxZ + hOffset);
        return headBox.contains(hitVec);
    }

    protected static HitResult ensureEntityInCrosshair(LivingEntity player, Entity targetEntity, Vec3 startPos, Vec3 directionVector, float partialTicks, double maxDistance, float bbExpansion) {
        Vec3 endVec = startPos.add(directionVector.x * maxDistance, directionVector.y * maxDistance, directionVector.z * maxDistance);
        AABB playerBox = player.getBoundingBox();
        AABB expandedBox = playerBox.expandTowards(directionVector.x * maxDistance, directionVector.y * maxDistance, directionVector.z * maxDistance);
        Vec3 closestEntityHitVec = null;
        for (Entity entity : MiscUtil.getLevel((Entity)player).getEntities((Entity)player, expandedBox)) {
            AABB entityBox;
            Optional hitVec;
            if (entity != targetEntity || entity.isSpectator() || !entity.isPickable() || !entity.isAlive() || !(hitVec = (entityBox = entity.getBoundingBox().inflate((double)bbExpansion)).clip(startPos, endVec)).isPresent()) continue;
            closestEntityHitVec = (Vec3)hitVec.get();
        }
        return closestEntityHitVec != null ? new EntityHitResult(targetEntity, closestEntityHitVec) : null;
    }
}


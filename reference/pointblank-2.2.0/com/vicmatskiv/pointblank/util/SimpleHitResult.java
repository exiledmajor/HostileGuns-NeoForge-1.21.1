/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  net.minecraft.core.Direction
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.EntityHitResult
 *  net.minecraft.world.phys.HitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 */
package com.vicmatskiv.pointblank.util;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class SimpleHitResult
extends HitResult {
    private HitResult.Type type;
    private Direction direction;
    private int entityId;
    private static final StreamCodec<ByteBuf, Direction> DIRECTION_CODEC = new StreamCodec<ByteBuf, Direction>(){

        public Direction decode(ByteBuf buf) {
            return Direction.values()[buf.readInt()];
        }

        public void encode(ByteBuf buf, Direction direction) {
            buf.writeInt(direction.ordinal());
        }
    };
    public static final StreamCodec<ByteBuf, SimpleHitResult> STREAM_CODEC = new StreamCodec<ByteBuf, SimpleHitResult>(){

        public SimpleHitResult decode(ByteBuf buf) {
            HitResult.Type type = HitResult.Type.values()[buf.readInt()];
            double x = buf.readDouble();
            double y = buf.readDouble();
            double z = buf.readDouble();
            Direction d = (Direction)FriendlyByteBuf.readNullable((ByteBuf)buf, DIRECTION_CODEC);
            int entityId = buf.readInt();
            return new SimpleHitResult(new Vec3(x, y, z), type, d, entityId);
        }

        public void encode(ByteBuf buf, SimpleHitResult hitResult) {
            buf.writeInt(hitResult.type.ordinal());
            buf.writeDouble(((SimpleHitResult)hitResult).location.x);
            buf.writeDouble(((SimpleHitResult)hitResult).location.y);
            buf.writeDouble(((SimpleHitResult)hitResult).location.z);
            FriendlyByteBuf.writeNullable((ByteBuf)buf, (Object)hitResult.direction, DIRECTION_CODEC);
            buf.writeInt(hitResult.entityId);
        }
    };

    public SimpleHitResult(Vec3 location, HitResult.Type type, Direction direction, int entityId) {
        super(location);
        this.type = type;
        this.direction = direction;
        this.entityId = entityId;
    }

    public HitResult.Type getType() {
        return this.type;
    }

    public Direction getDirection() {
        return this.direction;
    }

    public int getEntityId() {
        return this.entityId;
    }

    public static SimpleHitResult fromHitResult(HitResult hitResult) {
        if (hitResult == null) {
            return null;
        }
        if (hitResult instanceof SimpleHitResult) {
            SimpleHitResult simpleHitResult = (SimpleHitResult)hitResult;
            return simpleHitResult;
        }
        SimpleHitResult simpleHitResult = null;
        switch (hitResult.getType()) {
            case BLOCK: {
                simpleHitResult = new SimpleHitResult(hitResult.getLocation(), HitResult.Type.BLOCK, ((BlockHitResult)hitResult).getDirection(), -1);
                break;
            }
            case ENTITY: {
                simpleHitResult = new SimpleHitResult(hitResult.getLocation(), HitResult.Type.ENTITY, null, ((EntityHitResult)hitResult).getEntity().getId());
                break;
            }
            case MISS: {
                simpleHitResult = new SimpleHitResult(hitResult.getLocation(), HitResult.Type.MISS, null, -1);
            }
        }
        return simpleHitResult;
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack$Pose
 *  net.minecraft.world.phys.Vec3
 */
package com.vicmatskiv.pointblank.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vicmatskiv.pointblank.client.GunClientState;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.world.phys.Vec3;

public class GunStatePoseProvider {
    private static final GunStatePoseProvider INSTANCE = new GunStatePoseProvider();
    private Map<Key, PoseStack.Pose> poses = new HashMap<Key, PoseStack.Pose>();
    private Map<Key, Vec3[]> positionsAndDirections = new HashMap<Key, Vec3[]>();

    public static GunStatePoseProvider getInstance() {
        return INSTANCE;
    }

    private GunStatePoseProvider() {
    }

    public PoseStack.Pose getPose(GunClientState gunClientState, PoseContext poseContext) {
        return this.poses.get(new Key(gunClientState.getId(), poseContext));
    }

    public void setPose(GunClientState gunClientState, PoseContext poseContext, PoseStack.Pose pose) {
        this.poses.put(new Key(gunClientState.getId(), poseContext), pose);
    }

    public void clear(UUID id) {
        Map.Entry<Key, PoseStack.Pose> e;
        Iterator<Map.Entry<Key, PoseStack.Pose>> it = this.poses.entrySet().iterator();
        while (it.hasNext()) {
            e = it.next();
            if (!Objects.equals(e.getKey().id, id)) continue;
            it.remove();
        }
        it = this.positionsAndDirections.entrySet().iterator();
        while (it.hasNext()) {
            e = it.next();
            if (!Objects.equals(e.getKey().id, id)) continue;
            it.remove();
        }
    }

    public void setPositionAndDirection(GunClientState gunClientState, PoseContext poseContext, Vec3 pos, Vec3 direction) {
        this.positionsAndDirections.put(new Key(gunClientState.getId(), poseContext), new Vec3[]{pos, direction});
    }

    public Vec3 getPosition(GunClientState gunClientState, PoseContext poseContext) {
        Vec3[] positionAndDirection = this.positionsAndDirections.get(new Key(gunClientState.getId(), poseContext));
        if (positionAndDirection != null) {
            return positionAndDirection[0];
        }
        return null;
    }

    public Vec3[] getPositionAndDirection(GunClientState gunClientState, PoseContext poseContext) {
        return this.positionsAndDirections.get(new Key(gunClientState.getId(), poseContext));
    }

    private static class Key {
        UUID id;
        PoseContext poseContext;

        Key(UUID id, PoseContext poseContext) {
            this.id = id;
            this.poseContext = poseContext;
        }

        public int hashCode() {
            return Objects.hash(new Object[]{this.id, this.poseContext});
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (this.getClass() != obj.getClass()) {
                return false;
            }
            Key other = (Key)obj;
            return Objects.equals(this.id, other.id) && this.poseContext == other.poseContext;
        }
    }

    public static enum PoseContext {
        FIRST_PERSON_MUZZLE,
        FIRST_PERSON_MUZZLE_FLASH,
        FIRST_PERSON_MUZZLE_FLASH_ALT,
        THIRD_PERSON_MUZZLE,
        THIRD_PERSON_MUZZLE_FLASH,
        THIRD_PERSON_MUZZLE_FLASH_ALT;

    }
}


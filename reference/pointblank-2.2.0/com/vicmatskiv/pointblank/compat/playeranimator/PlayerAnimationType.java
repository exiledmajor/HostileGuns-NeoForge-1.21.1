/*
 * Decompiled with CFR 0.152.
 */
package com.vicmatskiv.pointblank.compat.playeranimator;

import com.vicmatskiv.pointblank.compat.playeranimator.PlayerAnimationPartGroup;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public enum PlayerAnimationType {
    FIRING("animation.model.shooting", false, Map.of(PlayerAnimationPartGroup.ARMS, 0, PlayerAnimationPartGroup.HEAD, 1, PlayerAnimationPartGroup.TORSO, 4, PlayerAnimationPartGroup.LEGS, 5, PlayerAnimationPartGroup.BODY, 10)),
    FIRING_AIMING("animation.model.shootingaiming", false, Map.of(PlayerAnimationPartGroup.ARMS, 0, PlayerAnimationPartGroup.HEAD, 1, PlayerAnimationPartGroup.TORSO, 4, PlayerAnimationPartGroup.LEGS, 5, PlayerAnimationPartGroup.BODY, 10)),
    RELOADING("animation.model.reload", false, Map.of(PlayerAnimationPartGroup.ARMS, 0, PlayerAnimationPartGroup.HEAD, 1, PlayerAnimationPartGroup.TORSO, 4, PlayerAnimationPartGroup.LEGS, 5, PlayerAnimationPartGroup.BODY, 10)),
    RELOADING_EMPTY("animation.model.reloadempty", false, Map.of(PlayerAnimationPartGroup.ARMS, 0, PlayerAnimationPartGroup.HEAD, 1, PlayerAnimationPartGroup.TORSO, 2, PlayerAnimationPartGroup.LEGS, 5, PlayerAnimationPartGroup.BODY, 10)),
    AIMING("animation.model.aiming", true, Map.of(PlayerAnimationPartGroup.ARMS, 0, PlayerAnimationPartGroup.HEAD, 1, PlayerAnimationPartGroup.TORSO, 2, PlayerAnimationPartGroup.LEGS, 5, PlayerAnimationPartGroup.BODY, 10)),
    WALKING("animation.model.walking", true, Map.of(PlayerAnimationPartGroup.LEGS, 0, PlayerAnimationPartGroup.BODY, 1, PlayerAnimationPartGroup.TORSO, 3, PlayerAnimationPartGroup.HEAD, 4, PlayerAnimationPartGroup.ARMS, 5)),
    WALKING_RIGHT("animation.model.walkingright", true, Map.of(PlayerAnimationPartGroup.LEGS, 0, PlayerAnimationPartGroup.BODY, 1, PlayerAnimationPartGroup.TORSO, 3, PlayerAnimationPartGroup.HEAD, 4, PlayerAnimationPartGroup.ARMS, 5)),
    WALKING_LEFT("animation.model.walkingleft", true, Map.of(PlayerAnimationPartGroup.LEGS, 0, PlayerAnimationPartGroup.BODY, 1, PlayerAnimationPartGroup.TORSO, 3, PlayerAnimationPartGroup.HEAD, 4, PlayerAnimationPartGroup.ARMS, 5)),
    WALKING_BACKWARDS("animation.model.walkingback", true, Map.of(PlayerAnimationPartGroup.LEGS, 0, PlayerAnimationPartGroup.BODY, 1, PlayerAnimationPartGroup.TORSO, 3, PlayerAnimationPartGroup.HEAD, 4, PlayerAnimationPartGroup.ARMS, 5)),
    RUNNING("animation.model.running", true, Map.of(PlayerAnimationPartGroup.LEGS, 0, PlayerAnimationPartGroup.BODY, 1, PlayerAnimationPartGroup.TORSO, 3, PlayerAnimationPartGroup.HEAD, 4, PlayerAnimationPartGroup.ARMS, 5)),
    CROUCH_WALKING("animation.model.crouchwalking", true, Map.of(PlayerAnimationPartGroup.LEGS, 0, PlayerAnimationPartGroup.BODY, 1, PlayerAnimationPartGroup.TORSO, 3, PlayerAnimationPartGroup.HEAD, 4, PlayerAnimationPartGroup.ARMS, 5)),
    CROUCH_WALKING_BACKWARDS("animation.model.crouchwalkingback", true, Map.of(PlayerAnimationPartGroup.LEGS, 0, PlayerAnimationPartGroup.BODY, 1, PlayerAnimationPartGroup.TORSO, 3, PlayerAnimationPartGroup.HEAD, 4, PlayerAnimationPartGroup.ARMS, 5)),
    CROUCH_WALKING_RIGHT("animation.model.crouchwalkingright", true, Map.of(PlayerAnimationPartGroup.LEGS, 0, PlayerAnimationPartGroup.BODY, 1, PlayerAnimationPartGroup.TORSO, 3, PlayerAnimationPartGroup.HEAD, 4, PlayerAnimationPartGroup.ARMS, 5)),
    CROUCH_WALKING_LEFT("animation.model.crouchwalkingleft", true, Map.of(PlayerAnimationPartGroup.LEGS, 0, PlayerAnimationPartGroup.BODY, 1, PlayerAnimationPartGroup.TORSO, 3, PlayerAnimationPartGroup.HEAD, 4, PlayerAnimationPartGroup.ARMS, 5)),
    CROUCHING("animation.model.crouch", true, Map.of(PlayerAnimationPartGroup.LEGS, 0, PlayerAnimationPartGroup.BODY, 1, PlayerAnimationPartGroup.TORSO, 3, PlayerAnimationPartGroup.HEAD, 4, PlayerAnimationPartGroup.ARMS, 5)),
    OFF_GROUND("animation.model.off_ground", true, Map.of(PlayerAnimationPartGroup.LEGS, 0, PlayerAnimationPartGroup.BODY, 1, PlayerAnimationPartGroup.TORSO, 3, PlayerAnimationPartGroup.HEAD, 4, PlayerAnimationPartGroup.ARMS, 5)),
    IDLE("animation.model.idle", true, Map.of(PlayerAnimationPartGroup.LEGS, 0, PlayerAnimationPartGroup.BODY, 1, PlayerAnimationPartGroup.TORSO, 3, PlayerAnimationPartGroup.HEAD, 4, PlayerAnimationPartGroup.ARMS, 5));

    private static final Map<String, PlayerAnimationType> baseAnimationNameToType;
    private final String baseAnimationName;
    private final boolean isLooped;
    private final List<PlayerAnimationPartGroup> groups;
    private final List<PartGroupPriority> partGroupPriorities;

    private PlayerAnimationType(String baseAnimationName, boolean isLooped, Map<PlayerAnimationPartGroup, Integer> groupPriorities) {
        this.baseAnimationName = baseAnimationName;
        this.isLooped = isLooped;
        this.groups = new ArrayList<PlayerAnimationPartGroup>(groupPriorities.keySet());
        this.partGroupPriorities = new ArrayList<PartGroupPriority>();
        for (Map.Entry<PlayerAnimationPartGroup, Integer> entry : groupPriorities.entrySet()) {
            this.partGroupPriorities.add(new PartGroupPriority(entry.getKey(), entry.getValue(), this));
        }
    }

    public String getBaseAnimationName() {
        return this.baseAnimationName;
    }

    public boolean isLooped() {
        return this.isLooped;
    }

    public List<PlayerAnimationPartGroup> getGroups() {
        return this.groups;
    }

    public static PlayerAnimationType fromBaseAnimationName(String animationName) {
        if (animationName == null) {
            return null;
        }
        return baseAnimationNameToType.get(animationName);
    }

    public static Map<PlayerAnimationPartGroup, PlayerAnimationType> compose(List<PlayerAnimationType> animationTypes) {
        ArrayList<PartGroupPriority> partGroupPriorities = new ArrayList<PartGroupPriority>();
        for (PlayerAnimationType type : animationTypes) {
            partGroupPriorities.addAll(type.partGroupPriorities);
        }
        Collections.sort(partGroupPriorities);
        LinkedHashMap<PlayerAnimationPartGroup, PlayerAnimationType> result = new LinkedHashMap<PlayerAnimationPartGroup, PlayerAnimationType>();
        for (PartGroupPriority p : partGroupPriorities) {
            if (result.containsKey((Object)p.partGroup)) continue;
            result.put(p.partGroup, p.parentType);
        }
        return result;
    }

    static {
        baseAnimationNameToType = new HashMap<String, PlayerAnimationType>();
        for (PlayerAnimationType type : PlayerAnimationType.values()) {
            baseAnimationNameToType.put(type.getBaseAnimationName(), type);
        }
    }

    private static class PartGroupPriority
    implements Comparable<PartGroupPriority> {
        final PlayerAnimationPartGroup partGroup;
        final int priority;
        final PlayerAnimationType parentType;

        PartGroupPriority(PlayerAnimationPartGroup partGroup, int priority, PlayerAnimationType parentType) {
            this.partGroup = partGroup;
            this.priority = priority;
            this.parentType = parentType;
        }

        @Override
        public int compareTo(PartGroupPriority o) {
            int result = Integer.compare(this.priority, o.priority);
            if (result != 0) {
                return result;
            }
            return Integer.compare(this.parentType.ordinal(), o.parentType.ordinal());
        }
    }
}


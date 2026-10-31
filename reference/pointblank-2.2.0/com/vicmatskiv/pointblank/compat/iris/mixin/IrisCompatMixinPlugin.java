/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.objectweb.asm.tree.ClassNode
 *  org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin
 *  org.spongepowered.asm.mixin.extensibility.IMixinInfo
 */
package com.vicmatskiv.pointblank.compat.iris.mixin;

import com.vicmatskiv.pointblank.CommonLoader;
import com.vicmatskiv.pointblank.util.ModInfo;
import java.util.List;
import java.util.Set;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class IrisCompatMixinPlugin
implements IMixinConfigPlugin {
    public void onLoad(String s) {
    }

    public String getRefMapperConfig() {
        return null;
    }

    public boolean shouldApplyMixin(String s, String mixinClassName) {
        ModInfo modInfo = CommonLoader.getInstance().getModInfo("iris");
        boolean shouldApply = modInfo != null && !modInfo.getVersion().startsWith("1.6.");
        return shouldApply;
    }

    public void acceptTargets(Set<String> set, Set<String> set1) {
    }

    public List<String> getMixins() {
        return null;
    }

    public void preApply(String s, ClassNode classNode, String s1, IMixinInfo iMixinInfo) {
    }

    public void postApply(String s, ClassNode classNode, String s1, IMixinInfo iMixinInfo) {
    }
}


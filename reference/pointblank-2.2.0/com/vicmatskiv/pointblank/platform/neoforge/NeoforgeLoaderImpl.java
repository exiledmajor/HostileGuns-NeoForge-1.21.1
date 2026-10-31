/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.neoforged.fml.loading.LoadingModList
 *  net.neoforged.fml.loading.moddiscovery.ModFileInfo
 */
package com.vicmatskiv.pointblank.platform.neoforge;

import com.vicmatskiv.pointblank.CommonLoader;
import com.vicmatskiv.pointblank.util.ModInfo;
import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.fml.loading.moddiscovery.ModFileInfo;

public class NeoforgeLoaderImpl
implements CommonLoader {
    @Override
    public ModInfo getModInfo(String modId) {
        ModFileInfo neoforgeModInfo = LoadingModList.get().getModFileById(modId);
        return neoforgeModInfo != null ? new ModInfo(modId, neoforgeModInfo.versionString()) : null;
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package com.vicmatskiv.pointblank.util;

public class ModInfo {
    private String modId;
    private String version;

    public ModInfo(String modId, String version) {
        this.modId = modId;
        this.version = version;
    }

    public String getModId() {
        return this.modId;
    }

    public String getVersion() {
        return this.version;
    }
}


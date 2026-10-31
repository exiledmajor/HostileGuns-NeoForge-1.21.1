/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.irisshaders.iris.Iris
 */
package com.vicmatskiv.pointblank.compat.iris;

import com.vicmatskiv.pointblank.Config;
import com.vicmatskiv.pointblank.client.render.RenderTypeProvider;
import com.vicmatskiv.pointblank.compat.iris.IrisCompat;
import com.vicmatskiv.pointblank.compat.iris.IrisRenderTypeProvider;
import net.irisshaders.iris.Iris;

class IrisCompatImpl
extends IrisCompat {
    private boolean isRenderingShadows;
    private RenderTypeProvider renderTypeProvider = new IrisRenderTypeProvider();

    protected IrisCompatImpl() {
    }

    @Override
    public boolean isIrisLoaded() {
        return true;
    }

    @Override
    public boolean isShaderPackEnabled() {
        return !Iris.getCurrentPack().isEmpty();
    }

    @Override
    public void onStartRenderShadows() {
        this.isRenderingShadows = true;
    }

    @Override
    public void onEndRenderShadows() {
        this.isRenderingShadows = false;
    }

    @Override
    public boolean isRenderingShadows() {
        return this.isRenderingShadows;
    }

    @Override
    public RenderTypeProvider getRenderTypeProvider() {
        return this.renderTypeProvider;
    }

    @Override
    public int getColorBalance() {
        int colorBalance = Config.pipScopeColorBalanceRed << 24 | Config.pipScopeColorBalanceGreen << 16 | Config.pipScopeColorBalanceBlue << 8 | 0xFF;
        return colorBalance;
    }
}


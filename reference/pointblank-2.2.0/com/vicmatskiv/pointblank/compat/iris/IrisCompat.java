/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package com.vicmatskiv.pointblank.compat.iris;

import com.vicmatskiv.pointblank.Platform;
import com.vicmatskiv.pointblank.client.render.BufferSourceProvider;
import com.vicmatskiv.pointblank.client.render.RenderTypeProvider;
import com.vicmatskiv.pointblank.compat.iris.IrisBufferSourceProvider;
import com.vicmatskiv.pointblank.util.ModInfo;
import java.lang.reflect.InvocationTargetException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class IrisCompat {
    private static final Logger LOGGER = LogManager.getLogger((String)"pointblank");
    private static IrisCompat instance;

    public static IrisCompat getInstance() {
        if (instance == null) {
            ModInfo irisModInfo = Platform.getInstance().getModInfo("iris");
            if (irisModInfo != null) {
                String irisClassName = "com.vicmatskiv.pointblank.compat.iris.IrisCompatImpl";
                try {
                    Class<?> irisClass = Class.forName(irisClassName);
                    instance = (IrisCompat)irisClass.getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
                    LOGGER.info("Compatibility with Iris version {} enabled", (Object)irisModInfo.getVersion());
                }
                catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InstantiationException | NoClassDefFoundError | NoSuchMethodException | SecurityException | InvocationTargetException e) {
                    LOGGER.error("Iris mod version {} detected, but compatibility could not be enabled. This is likely due to an outdated and/or incompatible version of the Iris mod. Please ensure you have Oculus version 1.7.0 or later installed.", (Object)irisModInfo.getVersion(), (Object)e);
                }
            }
            if (instance == null) {
                instance = new IrisCompat(){};
            }
        }
        return instance;
    }

    public boolean isIrisLoaded() {
        return false;
    }

    public boolean isShaderPackEnabled() {
        return false;
    }

    public boolean isRenderingShadows() {
        return false;
    }

    public void onStartRenderShadows() {
        throw new UnsupportedOperationException();
    }

    public void onEndRenderShadows() {
        throw new UnsupportedOperationException();
    }

    public RenderTypeProvider getRenderTypeProvider() {
        throw new UnsupportedOperationException();
    }

    public int getColorBalance() {
        return -1;
    }

    public BufferSourceProvider getBufferSourceProvider() {
        return IrisBufferSourceProvider.getInstance();
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package com.vicmatskiv.pointblank.client;

import com.vicmatskiv.pointblank.client.ClientEventHandler;
import java.util.concurrent.atomic.AtomicBoolean;

public class GunStateTicker
extends Thread {
    public static final long STATE_TICK_INTERVAL_MILLIS = 5L;
    private AtomicBoolean running = new AtomicBoolean(true);
    private ClientEventHandler clientEventHandler;

    public GunStateTicker(ClientEventHandler clientEventHandler) {
        super("pointblank:state_ticker_thread");
        this.setDaemon(true);
        this.clientEventHandler = clientEventHandler;
    }

    @Override
    public void run() {
        while (this.running.get()) {
            try {
                ClientEventHandler.runSyncTick(this.clientEventHandler::tickMainHeldGun);
                GunStateTicker.sleep(5L);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    void shutdown() {
        this.running.set(false);
    }
}


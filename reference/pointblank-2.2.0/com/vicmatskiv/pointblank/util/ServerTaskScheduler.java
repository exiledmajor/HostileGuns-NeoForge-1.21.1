/*
 * Decompiled with CFR 0.152.
 */
package com.vicmatskiv.pointblank.util;

import java.util.ArrayList;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class ServerTaskScheduler
implements Runnable {
    private DelayQueue<DelayedTask> queue = new DelayQueue();

    @Override
    public void run() {
        ArrayList tasks = new ArrayList();
        this.queue.drainTo(tasks);
        for (DelayedTask task : tasks) {
            task.run();
        }
    }

    public void scheduleDelayedTask(Runnable task, long delayMillis) {
        this.queue.add(new DelayedTask(task, delayMillis, TimeUnit.MILLISECONDS));
    }

    private static class DelayedTask
    implements Delayed,
    Runnable {
        private final long startDelayTime;
        private Runnable task;

        DelayedTask(Runnable task, long delay, TimeUnit timeUnit) {
            this.task = task;
            this.startDelayTime = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(delay, timeUnit);
        }

        @Override
        public long getDelay(TimeUnit unit) {
            long delay = this.startDelayTime - System.currentTimeMillis();
            return unit.convert(delay, TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(Delayed other) {
            if (this.startDelayTime < ((DelayedTask)other).startDelayTime) {
                return -1;
            }
            if (this.startDelayTime > ((DelayedTask)other).startDelayTime) {
                return 1;
            }
            return 0;
        }

        @Override
        public void run() {
            this.task.run();
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 */
package com.vicmatskiv.pointblank.client.controller;

import com.vicmatskiv.pointblank.client.GunClientState;
import com.vicmatskiv.pointblank.client.controller.AbstractProceduralAnimationController;
import com.vicmatskiv.pointblank.feature.ConditionContext;
import com.vicmatskiv.pointblank.item.GunItem;
import com.vicmatskiv.pointblank.util.TimeUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class TimerController
extends AbstractProceduralAnimationController {
    private List<ScheduleEntry> eventHandlers = new ArrayList<ScheduleEntry>();
    private long lastExecutionTime = 0L;

    public TimerController(long duration) {
        super(duration);
    }

    public void schedule(GunItem.ReloadPhase reloadPhase, long time, TimeUnit timeUnit, AbstractProceduralAnimationController handler, Predicate<ConditionContext> condition) {
        this.eventHandlers.add(new ScheduleEntry(reloadPhase, timeUnit.toNanos(time), handler, condition != null ? condition : c -> true));
        this.eventHandlers.sort(Comparator.comparingLong(ScheduleEntry::time));
    }

    @Override
    public void onRenderTick(LivingEntity player, GunClientState state, ItemStack itemStack, ItemDisplayContext itemDisplayContext, float partialTicks) {
        super.onRenderTick(player, state, itemStack, itemDisplayContext, partialTicks);
        if (!this.isDone) {
            long currentTime = System.nanoTime() - this.startTime;
            for (ScheduleEntry scheduleEntry : this.eventHandlers) {
                long handlerTime = scheduleEntry.time;
                AbstractProceduralAnimationController handler = scheduleEntry.handler;
                if (handlerTime > currentTime || state.getReloadPhase() != scheduleEntry.reloadPhase || !scheduleEntry.condition.test(new ConditionContext((LivingEntity)((Player)player), itemStack, state, null))) continue;
                if (handlerTime > this.lastExecutionTime) {
                    handler.reset();
                }
                if (handler.isDone()) continue;
                handler.onRenderTick(player, state, itemStack, itemDisplayContext, partialTicks);
            }
            this.lastExecutionTime = currentTime;
        }
    }

    @Override
    public void reset() {
        super.reset();
        this.lastExecutionTime = -1L;
    }

    public List<AbstractProceduralAnimationController> getActiveHandlers(Player player, GunClientState state, ItemStack itemStack) {
        long currentTime = System.nanoTime() - this.startTime;
        return this.eventHandlers.stream().filter(se -> se.reloadPhase == state.getReloadPhase() && se.time <= currentTime && se.condition.test(new ConditionContext((LivingEntity)player, itemStack, state, ItemDisplayContext.FIRST_PERSON_RIGHT_HAND)) && !se.handler.isDone()).map(t -> t.handler).toList();
    }

    private record ScheduleEntry(GunItem.ReloadPhase reloadPhase, long time, AbstractProceduralAnimationController handler, Predicate<ConditionContext> condition) {
    }
}


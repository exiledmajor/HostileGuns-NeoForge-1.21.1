/*
 * Decompiled with CFR 0.152.
 */
package com.vicmatskiv.pointblank.event;

import com.vicmatskiv.pointblank.event.AddPackFindersEvent;
import com.vicmatskiv.pointblank.event.AttachmentAddedEvent;
import com.vicmatskiv.pointblank.event.AttachmentRemovedEvent;
import com.vicmatskiv.pointblank.event.BlockHitEvent;
import com.vicmatskiv.pointblank.event.EntityRenderersEvent;
import com.vicmatskiv.pointblank.event.GatherDataEvent;
import com.vicmatskiv.pointblank.event.InputEvent;
import com.vicmatskiv.pointblank.event.LivingEquipmentChangeEvent;
import com.vicmatskiv.pointblank.event.LivingEvent;
import com.vicmatskiv.pointblank.event.LivingSwapItemsEvent;
import com.vicmatskiv.pointblank.event.PlayerEvent;
import com.vicmatskiv.pointblank.event.RegisterKeyMappingsEvent;
import com.vicmatskiv.pointblank.event.RegisterParticleProvidersEvent;
import com.vicmatskiv.pointblank.event.RenderGuiOverlayEvent;
import com.vicmatskiv.pointblank.event.RenderHandEvent;
import com.vicmatskiv.pointblank.event.RenderLivingEvent;
import com.vicmatskiv.pointblank.event.RenderTooltipEvent;
import com.vicmatskiv.pointblank.event.ServerAboutToStartEvent;
import com.vicmatskiv.pointblank.event.ServerStartedEvent;
import com.vicmatskiv.pointblank.event.TickEvent;
import com.vicmatskiv.pointblank.event.ViewportEvent;
import com.vicmatskiv.pointblank.event.VillagerTradesEvent;

public interface EventVisitor<T> {
    public T visit(InputEvent var1);

    public T visit(AddPackFindersEvent var1);

    public T visit(LivingSwapItemsEvent var1);

    public T visit(PlayerEvent.PlayerChangedDimensionEvent var1);

    public T visit(PlayerEvent.PlayerLoggedInEvent var1);

    public T visit(PlayerEvent.PlayerRespawnEvent var1);

    public T visit(LivingEvent.LivingJumpEvent var1);

    public T visit(RegisterKeyMappingsEvent var1);

    public T visit(GatherDataEvent var1);

    public T visit(TickEvent.RenderTickEvent var1);

    public T visit(TickEvent.ClientTickEvent var1);

    public T visit(EntityRenderersEvent var1);

    public T visit(RenderHandEvent var1);

    public T visit(LivingEquipmentChangeEvent var1);

    public T visit(InputEvent.InteractionKeyMappingTriggered var1);

    public T visit(AttachmentAddedEvent var1);

    public T visit(BlockHitEvent var1);

    public T visit(ServerAboutToStartEvent var1);

    public T visit(RegisterParticleProvidersEvent var1);

    public T visit(AttachmentRemovedEvent var1);

    public T visit(RenderTooltipEvent.Pre var1);

    public T visit(RenderTooltipEvent.GatherComponents var1);

    public T visit(RenderLivingEvent.Pre var1);

    public T visit(RenderLivingEvent.Post var1);

    public T visit(RenderGuiOverlayEvent var1);

    public T visit(ViewportEvent.ComputeCameraAngles var1);

    public T visit(ViewportEvent.ComputeFov var1);

    public T visit(VillagerTradesEvent var1);

    public T visit(ServerStartedEvent var1);
}


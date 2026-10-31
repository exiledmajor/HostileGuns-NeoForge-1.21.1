/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.neoforged.bus.api.Event
 *  net.neoforged.bus.api.EventPriority
 *  net.neoforged.bus.api.IEventBus
 */
package com.vicmatskiv.pointblank.platform.neoforge;

import com.vicmatskiv.pointblank.event.CustomEvent;
import com.vicmatskiv.pointblank.event.EventBus;
import com.vicmatskiv.pointblank.event.EventListener;
import com.vicmatskiv.pointblank.event.EventRegistrationUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;

public class NeoforgeEventBus
implements EventBus {
    private final IEventBus forgeEventBus;
    private final Map<Class<?>, List<EventListener>> listeners = new HashMap();
    private final EventRegistrationUtil eventRegistrationUtil = new EventRegistrationUtil(this);
    private Function<Class<? extends CustomEvent>, List<Class<? extends Event>>> eventTypeMap;
    private final Function<Event, CustomEvent> eventMapper;

    public NeoforgeEventBus(IEventBus forgeEventBus, Function<Class<? extends CustomEvent>, List<Class<? extends Event>>> commonToForgeTypeMapper, Function<Event, CustomEvent> forgeToCommonEventMapper) {
        this.forgeEventBus = forgeEventBus;
        this.eventTypeMap = commonToForgeTypeMapper;
        this.eventMapper = forgeToCommonEventMapper;
    }

    @Override
    public <T extends CustomEvent> void addListener(Class<T> eventType, EventListener listener) {
        List eventTypeListeners = this.listeners.computeIfAbsent(eventType, et -> new ArrayList());
        eventTypeListeners.add(listener);
        List forgeEventTypes = this.toForgeEventType(eventType);
        if (forgeEventTypes != null) {
            for (Class forgeEventType : forgeEventTypes) {
                Consumer<Event> consumer = forgeEvent -> listener.handleEvent(this.eventMapper.apply((Event)forgeEvent));
                this.forgeEventBus.addListener(EventPriority.NORMAL, false, forgeEventType, consumer);
            }
        }
    }

    @Override
    public <T extends CustomEvent> void removeListener(Class<T> eventType, EventListener listener) {
        List eventTypeListeners = this.listeners.computeIfAbsent(eventType, et -> new ArrayList());
        eventTypeListeners.remove(listener);
    }

    private <FE extends Event> List<Class<FE>> toForgeEventType(Class<? extends CustomEvent> eventType) {
        return this.eventTypeMap.apply(eventType);
    }

    @Override
    public void postEvent(CustomEvent event) {
        List<EventListener> eventTypeListeners = this.listeners.get(event.getClass());
        if (eventTypeListeners == null) {
            return;
        }
        for (EventListener listener : eventTypeListeners) {
            listener.handleEvent(event);
        }
    }

    @Override
    public void register(Object instance) {
        this.eventRegistrationUtil.register(instance);
    }

    @Override
    public void unregister(Object instance) {
        throw new UnsupportedOperationException();
    }
}


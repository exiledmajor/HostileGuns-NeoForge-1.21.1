/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.neoforged.bus.api.IEventBus
 *  net.neoforged.neoforge.registries.DeferredRegister
 */
package com.vicmatskiv.pointblank.platform.neoforge;

import com.vicmatskiv.pointblank.RegistryService;
import com.vicmatskiv.pointblank.registry.ItemRegistry;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class NeoforgeRegistryService<T>
implements RegistryService<T> {
    private final DeferredRegister<T> deferredRegister;
    private final Map<String, DeferredValue<? extends T>> deferredMap = new HashMap<String, DeferredValue<? extends T>>();

    public NeoforgeRegistryService(DeferredRegister<T> deferredRegister) {
        this.deferredRegister = deferredRegister;
    }

    @Override
    public <I extends T> Supplier<I> register(String name, Supplier<? extends I> sup) {
        DeferredValue<? extends I> dv = new DeferredValue<I>(this, sup);
        this.deferredMap.put(name, dv);
        return dv::get;
    }

    public void registerWithModEventBus(IEventBus modEventBus) {
        for (Map.Entry<String, DeferredValue<T>> entry : this.deferredMap.entrySet()) {
            ItemRegistry.ItemSupplier s;
            DeferredValue<? extends T> dv = entry.getValue();
            Supplier supplier = dv.supplier;
            if (supplier instanceof ItemRegistry.ItemSupplier && !(s = (ItemRegistry.ItemSupplier)supplier).isEnabled()) continue;
            this.deferredRegister.register(entry.getKey(), dv::get);
        }
        this.deferredRegister.register(modEventBus);
    }

    private class DeferredValue<D extends T> {
        private final Supplier<D> supplier;
        private D value;

        DeferredValue(NeoforgeRegistryService neoforgeRegistryService, Supplier<D> supplier) {
            this.supplier = supplier;
        }

        D get() {
            if (this.value == null) {
                this.value = this.supplier.get();
            }
            return this.value;
        }
    }
}


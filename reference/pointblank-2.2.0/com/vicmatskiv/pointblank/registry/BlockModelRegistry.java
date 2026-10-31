/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 */
package com.vicmatskiv.pointblank.registry;

import com.vicmatskiv.pointblank.block.entity.PrinterBlockEntity;
import com.vicmatskiv.pointblank.block.entity.WorkstationBlockEntity;
import com.vicmatskiv.pointblank.client.model.BaseBlockModel;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;

public class BlockModelRegistry {
    public static Supplier<BaseBlockModel<WorkstationBlockEntity>> WORKSTATION_BLOCK_MODEL = () -> new BaseBlockModel.Builder().withResource(ResourceLocation.fromNamespaceAndPath((String)"pointblank", (String)"workstation")).build();
    public static Supplier<BaseBlockModel<PrinterBlockEntity>> PRINTER_BLOCK_MODEL = () -> new BaseBlockModel.Builder().withResource(ResourceLocation.fromNamespaceAndPath((String)"pointblank", (String)"printer")).withGlow("glowy", e -> {
        PrinterBlockEntity pbe;
        return e instanceof PrinterBlockEntity && (pbe = (PrinterBlockEntity)e).getState() != PrinterBlockEntity.State.CLOSED;
    }).withGlow("screendefault", e -> {
        PrinterBlockEntity pbe;
        return e instanceof PrinterBlockEntity && (pbe = (PrinterBlockEntity)e).getState() != PrinterBlockEntity.State.CLOSED;
    }).withGlow("screenfinal", e -> {
        PrinterBlockEntity pbe;
        return e instanceof PrinterBlockEntity && (pbe = (PrinterBlockEntity)e).getState() != PrinterBlockEntity.State.CLOSED;
    }).withGlow("screenfinal2", e -> {
        PrinterBlockEntity pbe;
        return e instanceof PrinterBlockEntity && (pbe = (PrinterBlockEntity)e).getState() != PrinterBlockEntity.State.CLOSED;
    }).withGlow("screenfinal3", e -> {
        PrinterBlockEntity pbe;
        return e instanceof PrinterBlockEntity && (pbe = (PrinterBlockEntity)e).getState() != PrinterBlockEntity.State.CLOSED;
    }).build();
}


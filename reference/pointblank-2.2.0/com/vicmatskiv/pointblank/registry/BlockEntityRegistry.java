/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.entity.BlockEntityType$Builder
 */
package com.vicmatskiv.pointblank.registry;

import com.vicmatskiv.pointblank.Platform;
import com.vicmatskiv.pointblank.RegistryService;
import com.vicmatskiv.pointblank.block.entity.PrinterBlockEntity;
import com.vicmatskiv.pointblank.block.entity.WorkstationBlockEntity;
import com.vicmatskiv.pointblank.registry.BlockRegistry;
import java.util.function.Supplier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class BlockEntityRegistry {
    private static RegistryService<BlockEntityType<?>> blockEntityTypeRegistry = Platform.getInstance().getBlockEntityTypeRegistry();
    public static final Supplier<BlockEntityType<WorkstationBlockEntity>> WORKSTATION_BLOCK_ENTITY = blockEntityTypeRegistry.register("workstation_block_entity", () -> BlockEntityType.Builder.of(WorkstationBlockEntity::new, (Block[])new Block[]{BlockRegistry.WORKSTATION.get()}).build(null));
    public static final Supplier<BlockEntityType<PrinterBlockEntity>> PRINTER_BLOCK_ENTITY = blockEntityTypeRegistry.register("printer_block_entity", () -> BlockEntityType.Builder.of(PrinterBlockEntity::new, (Block[])new Block[]{BlockRegistry.PRINTER.get()}).build(null));

    public static void init() {
    }
}


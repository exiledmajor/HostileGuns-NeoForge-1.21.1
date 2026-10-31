/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.block.Block
 */
package com.vicmatskiv.pointblank.registry;

import com.vicmatskiv.pointblank.Platform;
import com.vicmatskiv.pointblank.RegistryService;
import com.vicmatskiv.pointblank.block.PrinterBlock;
import com.vicmatskiv.pointblank.block.WorkstationBlock;
import java.util.function.Supplier;
import net.minecraft.world.level.block.Block;

public class BlockRegistry {
    private static RegistryService<Block> blockRegistry = Platform.getInstance().getBlockRegistryService();
    public static final Supplier<Block> WORKSTATION = blockRegistry.register("workstation", WorkstationBlock::new);
    public static final Supplier<Block> PRINTER = blockRegistry.register("printer", PrinterBlock::new);

    public static void init() {
    }
}


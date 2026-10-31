/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableSet
 *  com.mojang.datafixers.util.Pair
 *  net.minecraft.core.Holder
 *  net.minecraft.core.Holder$Reference
 *  net.minecraft.core.Registry
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.world.entity.ai.village.poi.PoiType
 *  net.minecraft.world.entity.npc.VillagerProfession
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement
 *  net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement
 *  net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool
 *  net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool$Projection
 *  net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList
 */
package com.vicmatskiv.pointblank.registry;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.vicmatskiv.pointblank.Config;
import com.vicmatskiv.pointblank.Platform;
import com.vicmatskiv.pointblank.RegistryService;
import com.vicmatskiv.pointblank.mixin.StructureTemplatePoolMixin;
import com.vicmatskiv.pointblank.registry.BlockRegistry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

public class VillagerRegistry {
    private static RegistryService<VillagerProfession> villagerProfessionRegistry = Platform.getInstance().getVillagerProfessionRegistry();
    private static RegistryService<PoiType> poiTypeRegistry = Platform.getInstance().getPoiTypeRegistry();
    public static Supplier<PoiType> ARMS_DEALER_POI = VillagerRegistry.registerPoiType("arms_dealer", () -> BlockRegistry.WORKSTATION.get(), 1, 1);
    public static Supplier<VillagerProfession> ARMS_DEALER_PROFESSION = VillagerRegistry.registerProfession("arms_dealer", ARMS_DEALER_POI);
    private static final ResourceLocation ARMS_DEALER_BARN_RESOURCE = ResourceLocation.fromNamespaceAndPath((String)"pointblank", (String)"village/all_terrain/arms_dealer_barn");
    private static final ResourceKey<StructureProcessorList> EMPTY_PROCESSOR_LIST_KEY = ResourceKey.create((ResourceKey)Registries.PROCESSOR_LIST, (ResourceLocation)ResourceLocation.fromNamespaceAndPath((String)"minecraft", (String)"empty"));
    private static final ResourceLocation[] HOUSES_RESOURCES = new ResourceLocation[]{ResourceLocation.fromNamespaceAndPath((String)"minecraft", (String)"village/plains/houses"), ResourceLocation.fromNamespaceAndPath((String)"minecraft", (String)"village/desert/houses"), ResourceLocation.fromNamespaceAndPath((String)"minecraft", (String)"village/savanna/houses"), ResourceLocation.fromNamespaceAndPath((String)"minecraft", (String)"village/snowy/houses"), ResourceLocation.fromNamespaceAndPath((String)"minecraft", (String)"village/taiga/houses")};

    private static Set<BlockState> getBlockStates(Block block) {
        return ImmutableSet.copyOf((Collection)block.getStateDefinition().getPossibleStates());
    }

    private static Supplier<PoiType> registerPoiType(String name, Supplier<Block> block, int maxTickets, int validRange) {
        return poiTypeRegistry.register(name, () -> new PoiType(VillagerRegistry.getBlockStates((Block)block.get()), maxTickets, validRange));
    }

    private static Supplier<VillagerProfession> registerProfession(String name, Supplier<PoiType> poiHolder) {
        return villagerProfessionRegistry.register(name, () -> new VillagerProfession(name, arg_0 -> VillagerRegistry.lambda$registerProfession$1((Supplier)poiHolder, arg_0), arg_0 -> VillagerRegistry.lambda$registerProfession$2((Supplier)poiHolder, arg_0), ImmutableSet.of(), ImmutableSet.of(), SoundEvents.VILLAGER_WORK_CARTOGRAPHER));
    }

    public static void registerStructures(MinecraftServer server) {
        Registry templatePoolRegistry = (Registry)server.registryAccess().registry(Registries.TEMPLATE_POOL).orElseThrow();
        Registry processorListRegistry = (Registry)server.registryAccess().registry(Registries.PROCESSOR_LIST).orElseThrow();
        for (ResourceLocation poolLocation : HOUSES_RESOURCES) {
            VillagerRegistry.registerStructure((Registry<StructureTemplatePool>)templatePoolRegistry, (Registry<StructureProcessorList>)processorListRegistry, poolLocation, ARMS_DEALER_BARN_RESOURCE, Config.armsDealerHouseWeight);
        }
    }

    public static void registerStructure(Registry<StructureTemplatePool> templatePoolRegistry, Registry<StructureProcessorList> processorListRegistry, ResourceLocation targetPoolResource, ResourceLocation structureResource, int weight) {
        if (weight == 0) {
            return;
        }
        Holder.Reference emptyProcessorList = processorListRegistry.getHolderOrThrow(EMPTY_PROCESSOR_LIST_KEY);
        StructureTemplatePool pool = (StructureTemplatePool)templatePoolRegistry.get(targetPoolResource);
        if (pool == null) {
            return;
        }
        SinglePoolElement structurePoolElement = (SinglePoolElement)SinglePoolElement.single((String)structureResource.toString(), (Holder)emptyProcessorList).apply(StructureTemplatePool.Projection.RIGID);
        for (int i = 0; i < weight; ++i) {
            ((StructureTemplatePoolMixin)pool).getTemplates().add((Object)structurePoolElement);
        }
        ArrayList<Pair<StructurePoolElement, Integer>> rawTemplates = new ArrayList<Pair<StructurePoolElement, Integer>>(((StructureTemplatePoolMixin)pool).getRawTemplates());
        rawTemplates.add((Pair<StructurePoolElement, Integer>)new Pair((Object)structurePoolElement, (Object)weight));
        ((StructureTemplatePoolMixin)pool).setRawTemplates(rawTemplates);
    }

    public static void init() {
    }

    private static /* synthetic */ boolean lambda$registerProfession$2(Supplier poiHolder, Holder holder) {
        return holder.is(BuiltInRegistries.POINT_OF_INTEREST_TYPE.getKey((Object)((PoiType)poiHolder.get())));
    }

    private static /* synthetic */ boolean lambda$registerProfession$1(Supplier poiHolder, Holder holder) {
        return holder.is(BuiltInRegistries.POINT_OF_INTEREST_TYPE.getKey((Object)((PoiType)poiHolder.get())));
    }
}


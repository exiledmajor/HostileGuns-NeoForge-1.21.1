/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.KeyMapping
 *  net.minecraft.client.renderer.block.model.ItemTransforms
 *  net.minecraft.core.component.DataComponentType
 *  net.minecraft.core.particles.ParticleType
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.packs.PackLocationInfo
 *  net.minecraft.server.packs.PackResources
 *  net.minecraft.server.packs.metadata.pack.PackMetadataSection
 *  net.minecraft.server.packs.repository.Pack$ResourcesSupplier
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.ai.village.poi.PoiType
 *  net.minecraft.world.entity.npc.VillagerProfession
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.item.CreativeModeTab
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.RecipeSerializer
 *  net.minecraft.world.item.crafting.RecipeType
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package com.vicmatskiv.pointblank;

import com.vicmatskiv.pointblank.NetworkService;
import com.vicmatskiv.pointblank.RegistryService;
import com.vicmatskiv.pointblank.event.EventBus;
import com.vicmatskiv.pointblank.input.KeyModifierType;
import com.vicmatskiv.pointblank.util.JarFileSystem;
import com.vicmatskiv.pointblank.util.ModInfo;
import java.nio.file.Path;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Function;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public interface Platform {
    public static final Logger LOGGER = LogManager.getLogger((String)"pointblank");
    public static final Platform INSTNANCE = Platform.load(Platform.class);

    public static Platform getInstance() {
        return INSTNANCE;
    }

    private static <T> T load(Class<T> clazz) {
        T loadedService = ServiceLoader.load(clazz).findFirst().orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
        LOGGER.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }

    public Path getGameDir();

    public EventBus getBootstrapEventBus();

    public EventBus getEventBus();

    public PackResources createDelegatingPackResources(PackLocationInfo var1, PackMetadataSection var2, List<? extends PackResources> var3);

    public Pack.ResourcesSupplier createPackResourceSupplier(PackResources var1);

    public NetworkService getNetworkService();

    public RegistryService<BlockEntityType<?>> getBlockEntityTypeRegistry();

    public RegistryService<Block> getBlockRegistryService();

    public RegistryService<RecipeType<?>> getRecipeTypeRegistryService();

    public RegistryService<RecipeSerializer<?>> getRecipeSerializerService();

    public RegistryService<EntityType<?>> getEntityTypeRegistryService();

    public RegistryService<DataComponentType<?>> getDataComponentTypeRegistry();

    public RegistryService<MenuType<?>> getMenuTypeRegistry();

    public RegistryService<SoundEvent> getSoundRegistry();

    public RegistryService<ParticleType<?>> getParticleTypeRegistry();

    public RegistryService<Item> getItemRegistry();

    public RegistryService<CreativeModeTab> getCreativeModeTabRegistry();

    public RegistryService<VillagerProfession> getVillagerProfessionRegistry();

    public RegistryService<PoiType> getPoiTypeRegistry();

    public void onRegistrationCompleted();

    public void registerConfig();

    default public boolean setCommonConfigValue(String optionName, Object value) {
        return false;
    }

    public boolean isClientSide();

    public boolean isClientThread();

    public ModInfo getModInfo(String var1);

    public Function<ItemDisplayContext, ItemTransforms> getItemTransforms(Player var1, ItemStack var2);

    public Entity getParentEntity(Entity var1);

    public JarFileSystem getJarFileSystem(Path ... var1);

    public CompoundTag getPersistentData(Player var1);

    public ResourceLocation parseRecipeResourceLocation(String var1);

    public KeyMapping createKeyMappingWithModifier(String var1, int var2, String var3, KeyModifierType var4);

    public boolean isKeyWithModifierDown(KeyMapping var1, KeyModifierType var2);

    default public boolean supportsKeyModifiers() {
        return false;
    }
}


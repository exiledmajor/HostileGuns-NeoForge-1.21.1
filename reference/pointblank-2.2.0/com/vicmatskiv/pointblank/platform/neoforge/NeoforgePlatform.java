/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.InputConstants$Type
 *  cpw.mods.jarhandling.SecureJar
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  java.lang.MatchException
 *  net.minecraft.client.KeyMapping
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.particle.ParticleEngine$SpriteParticleRegistration
 *  net.minecraft.client.renderer.block.model.ItemTransforms
 *  net.minecraft.client.resources.model.BakedModel
 *  net.minecraft.core.component.DataComponentType
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleType
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.packs.PackLocationInfo
 *  net.minecraft.server.packs.PackResources
 *  net.minecraft.server.packs.metadata.pack.PackMetadataSection
 *  net.minecraft.server.packs.repository.Pack$ResourcesSupplier
 *  net.minecraft.server.packs.repository.RepositorySource
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.ai.village.poi.PoiType
 *  net.minecraft.world.entity.npc.VillagerProfession
 *  net.minecraft.world.entity.npc.VillagerTrades$ItemListing
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
 *  net.neoforged.bus.api.Event
 *  net.neoforged.bus.api.IEventBus
 *  net.neoforged.fml.LogicalSide
 *  net.neoforged.fml.ModLoadingContext
 *  net.neoforged.fml.config.IConfigSpec
 *  net.neoforged.fml.config.ModConfig$Type
 *  net.neoforged.fml.event.config.ModConfigEvent
 *  net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
 *  net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
 *  net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent
 *  net.neoforged.fml.loading.FMLLoader
 *  net.neoforged.fml.loading.FMLPaths
 *  net.neoforged.fml.loading.LoadingModList
 *  net.neoforged.fml.loading.moddiscovery.ModFileInfo
 *  net.neoforged.fml.util.thread.EffectiveSide
 *  net.neoforged.neoforge.client.event.ClientTickEvent$Post
 *  net.neoforged.neoforge.client.event.ClientTickEvent$Pre
 *  net.neoforged.neoforge.client.event.EntityRenderersEvent$RegisterRenderers
 *  net.neoforged.neoforge.client.event.InputEvent$InteractionKeyMappingTriggered
 *  net.neoforged.neoforge.client.event.ModelEvent$RegisterGeometryLoaders
 *  net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent
 *  net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent
 *  net.neoforged.neoforge.client.event.RenderFrameEvent$Post
 *  net.neoforged.neoforge.client.event.RenderFrameEvent$Pre
 *  net.neoforged.neoforge.client.event.RenderGuiEvent$Post
 *  net.neoforged.neoforge.client.event.RenderLivingEvent$Post
 *  net.neoforged.neoforge.client.event.RenderLivingEvent$Pre
 *  net.neoforged.neoforge.client.event.RenderTooltipEvent$GatherComponents
 *  net.neoforged.neoforge.client.event.RenderTooltipEvent$Pre
 *  net.neoforged.neoforge.client.event.ViewportEvent$ComputeCameraAngles
 *  net.neoforged.neoforge.client.event.ViewportEvent$ComputeFov
 *  net.neoforged.neoforge.client.model.SeparateTransformsModel$Baked
 *  net.neoforged.neoforge.client.model.SeparateTransformsModel$Loader
 *  net.neoforged.neoforge.client.model.geometry.IGeometryLoader
 *  net.neoforged.neoforge.client.settings.IKeyConflictContext
 *  net.neoforged.neoforge.client.settings.KeyConflictContext
 *  net.neoforged.neoforge.client.settings.KeyModifier
 *  net.neoforged.neoforge.common.NeoForge
 *  net.neoforged.neoforge.data.event.GatherDataEvent
 *  net.neoforged.neoforge.entity.PartEntity
 *  net.neoforged.neoforge.event.AddPackFindersEvent
 *  net.neoforged.neoforge.event.entity.living.LivingDeathEvent
 *  net.neoforged.neoforge.event.entity.living.LivingDropsEvent
 *  net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent
 *  net.neoforged.neoforge.event.entity.living.LivingEvent$LivingJumpEvent
 *  net.neoforged.neoforge.event.entity.living.LivingSwapItemsEvent$Hands
 *  net.neoforged.neoforge.event.entity.player.PlayerEvent$HarvestCheck
 *  net.neoforged.neoforge.event.entity.player.PlayerEvent$PlayerChangedDimensionEvent
 *  net.neoforged.neoforge.event.entity.player.PlayerEvent$PlayerLoggedInEvent
 *  net.neoforged.neoforge.event.entity.player.PlayerEvent$PlayerRespawnEvent
 *  net.neoforged.neoforge.event.server.ServerAboutToStartEvent
 *  net.neoforged.neoforge.event.server.ServerStartedEvent
 *  net.neoforged.neoforge.event.tick.PlayerTickEvent$Post
 *  net.neoforged.neoforge.event.tick.PlayerTickEvent$Pre
 *  net.neoforged.neoforge.event.village.VillagerTradesEvent
 *  net.neoforged.neoforge.registries.DeferredRegister
 */
package com.vicmatskiv.pointblank.platform.neoforge;

import com.mojang.blaze3d.platform.InputConstants;
import com.vicmatskiv.pointblank.NetworkService;
import com.vicmatskiv.pointblank.Platform;
import com.vicmatskiv.pointblank.RegistryService;
import com.vicmatskiv.pointblank.event.ClientSetupEvent;
import com.vicmatskiv.pointblank.event.CommonSetupEvent;
import com.vicmatskiv.pointblank.event.CustomEvent;
import com.vicmatskiv.pointblank.event.EntityRenderersEvent;
import com.vicmatskiv.pointblank.event.EventBus;
import com.vicmatskiv.pointblank.event.GatherDataEvent;
import com.vicmatskiv.pointblank.event.InputEvent;
import com.vicmatskiv.pointblank.event.LivingDeathEvent;
import com.vicmatskiv.pointblank.event.LivingDropsEvent;
import com.vicmatskiv.pointblank.event.LivingEvent;
import com.vicmatskiv.pointblank.event.LivingSwapItemsEvent;
import com.vicmatskiv.pointblank.event.LoadCompleteEvent;
import com.vicmatskiv.pointblank.event.PlayerEvent;
import com.vicmatskiv.pointblank.event.RenderGuiEventPost;
import com.vicmatskiv.pointblank.event.RenderLivingEvent;
import com.vicmatskiv.pointblank.event.RenderTooltipEvent;
import com.vicmatskiv.pointblank.event.TickEvent;
import com.vicmatskiv.pointblank.event.ViewportEvent;
import com.vicmatskiv.pointblank.input.KeyModifierType;
import com.vicmatskiv.pointblank.platform.neoforge.DelegatingPackResources;
import com.vicmatskiv.pointblank.platform.neoforge.NeoforgeConfig;
import com.vicmatskiv.pointblank.platform.neoforge.NeoforgeEventBus;
import com.vicmatskiv.pointblank.platform.neoforge.NeoforgeNetworkService;
import com.vicmatskiv.pointblank.platform.neoforge.NeoforgeRegistryService;
import com.vicmatskiv.pointblank.platform.neoforge.mixin.BakedModelMixin;
import com.vicmatskiv.pointblank.util.JarFileSystem;
import com.vicmatskiv.pointblank.util.MiscUtil;
import com.vicmatskiv.pointblank.util.ModInfo;
import cpw.mods.jarhandling.SecureJar;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.lang.runtime.SwitchBootstraps;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
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
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.config.IConfigSpec;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.fml.loading.moddiscovery.ModFileInfo;
import net.neoforged.fml.util.thread.EffectiveSide;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.model.SeparateTransformsModel;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.entity.PartEntity;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.LivingSwapItemsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

public class NeoforgePlatform
implements Platform {
    private static final Map<Class<? extends CustomEvent>, List<Class<? extends Event>>> EVENT_TYPE_MAP = new HashMap<Class<? extends CustomEvent>, List<Class<? extends Event>>>();
    private static final Map<Class<? extends CustomEvent>, Class<? extends Event>> BOOTSTRAP_EVENT_TYPE_MAP;
    private final NeoforgeRegistryService<BlockEntityType<?>> forgeBlockEntityTypeRegistryservice = new NeoforgeRegistryService(DeferredRegister.create((ResourceKey)Registries.BLOCK_ENTITY_TYPE, (String)"pointblank"));
    private final NeoforgeRegistryService<Block> blockRegsitryService = new NeoforgeRegistryService(DeferredRegister.createBlocks((String)"pointblank"));
    private final NeoforgeRegistryService<RecipeType<?>> recipeTypeRegistryService = new NeoforgeRegistryService(DeferredRegister.create((ResourceKey)Registries.RECIPE_TYPE, (String)"pointblank"));
    private final NeoforgeRegistryService<RecipeSerializer<?>> recipeSerializerService = new NeoforgeRegistryService(DeferredRegister.create((ResourceKey)Registries.RECIPE_SERIALIZER, (String)"pointblank"));
    private final NeoforgeRegistryService<EntityType<?>> entityTypeRegistryService = new NeoforgeRegistryService(DeferredRegister.create((ResourceKey)Registries.ENTITY_TYPE, (String)"pointblank"));
    private final NeoforgeRegistryService<DataComponentType<?>> dataComponentTypeRegistry = new NeoforgeRegistryService(DeferredRegister.create((ResourceKey)BuiltInRegistries.DATA_COMPONENT_TYPE.key(), (String)"pointblank"));
    private final NeoforgeRegistryService<MenuType<?>> menuTypeRegistry = new NeoforgeRegistryService(DeferredRegister.create((ResourceKey)Registries.MENU, (String)"pointblank"));
    private final NeoforgeRegistryService<SoundEvent> soundRegistry = new NeoforgeRegistryService(DeferredRegister.create((ResourceKey)Registries.SOUND_EVENT, (String)"pointblank"));
    private final NeoforgeRegistryService<ParticleType<?>> particleTypeRegistry = new NeoforgeRegistryService(DeferredRegister.create((ResourceKey)Registries.PARTICLE_TYPE, (String)"pointblank"));
    private final NeoforgeRegistryService<Item> itemRegistry = new NeoforgeRegistryService(DeferredRegister.createItems((String)"pointblank"));
    private final NeoforgeRegistryService<CreativeModeTab> creativeModeTabRegistry = new NeoforgeRegistryService(DeferredRegister.create((ResourceKey)Registries.CREATIVE_MODE_TAB, (String)"pointblank"));
    private final NeoforgeRegistryService<VillagerProfession> villagerProfessionRegistry = new NeoforgeRegistryService(DeferredRegister.create((ResourceKey)Registries.VILLAGER_PROFESSION, (String)"pointblank"));
    private final NeoforgeRegistryService<PoiType> poiTypeRegistry = new NeoforgeRegistryService(DeferredRegister.create((ResourceKey)Registries.POINT_OF_INTEREST_TYPE, (String)"pointblank"));
    private final NeoforgeNetworkService networkService = new NeoforgeNetworkService();
    private final NeoforgeEventBus eventBus = new NeoforgeEventBus(NeoForge.EVENT_BUS, this::toForgetEventType, this::toCommonEvent);
    private final NeoforgeEventBus bootstrapEventBus = new NeoforgeEventBus(ModLoadingContext.get().getActiveContainer().getEventBus(), this::toBootstrapForgetEventType, this::toCommonBootstrapEvent);
    private NeoforgeConfig config;

    public NeoforgePlatform() {
        ModLoadingContext.get().getActiveContainer().getEventBus().addListener(this.networkService::registerAll);
        ModLoadingContext.get().getActiveContainer().getEventBus().addListener(this::onRegisterGeometryLoaders);
    }

    private void onRegisterGeometryLoaders(ModelEvent.RegisterGeometryLoaders event) {
        event.register(ResourceLocation.fromNamespaceAndPath((String)"forge", (String)"separate_transforms"), (IGeometryLoader)SeparateTransformsModel.Loader.INSTANCE);
    }

    @Override
    public void registerConfig() {
        this.config = new NeoforgeConfig();
        ModLoadingContext.get().getActiveContainer().registerConfig(ModConfig.Type.COMMON, (IConfigSpec)this.config.SPEC);
        this.getBootstrapEventBus().addListener(com.vicmatskiv.pointblank.event.ModConfigEvent.class, e -> this.config.onLoad((com.vicmatskiv.pointblank.event.ModConfigEvent)e));
    }

    @Override
    public boolean setCommonConfigValue(String optionName, Object value) {
        return this.config != null && this.config.setValue(optionName, value);
    }

    @Override
    public boolean isClientSide() {
        return FMLLoader.getDist().isClient();
    }

    @Override
    public EventBus getEventBus() {
        return this.eventBus;
    }

    @Override
    public EventBus getBootstrapEventBus() {
        return this.bootstrapEventBus;
    }

    private List<Class<? extends Event>> toForgetEventType(Class<? extends CustomEvent> commonEvent) {
        return EVENT_TYPE_MAP.get(commonEvent);
    }

    private List<Class<? extends Event>> toBootstrapForgetEventType(Class<? extends CustomEvent> commonEvent) {
        Class<? extends Event> event = BOOTSTRAP_EVENT_TYPE_MAP.get(commonEvent);
        return event != null ? Collections.singletonList(event) : null;
    }

    private CustomEvent toCommonBootstrapEvent(Event event) {
        Event event2 = event;
        Objects.requireNonNull(event2);
        Event event3 = event2;
        int n = 0;
        return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{FMLCommonSetupEvent.class, FMLClientSetupEvent.class, RegisterKeyMappingsEvent.class, net.neoforged.neoforge.data.event.GatherDataEvent.class, FMLLoadCompleteEvent.class, ModConfigEvent.class, AddPackFindersEvent.class, EntityRenderersEvent.RegisterRenderers.class, RegisterParticleProvidersEvent.class}, (Object)event3, n)) {
            case 0 -> {
                FMLCommonSetupEvent e = (FMLCommonSetupEvent)event3;
                yield new CommonSetupEvent();
            }
            case 1 -> {
                FMLClientSetupEvent e = (FMLClientSetupEvent)event3;
                yield new ClientSetupEvent();
            }
            case 2 -> {
                RegisterKeyMappingsEvent e = (RegisterKeyMappingsEvent)event3;
                yield new com.vicmatskiv.pointblank.event.RegisterKeyMappingsEvent(arg_0 -> ((RegisterKeyMappingsEvent)e).register(arg_0));
            }
            case 3 -> {
                net.neoforged.neoforge.data.event.GatherDataEvent e = (net.neoforged.neoforge.data.event.GatherDataEvent)event3;
                yield new GatherDataEvent(e.getGenerator(), e.getLookupProvider(), e.includeServer());
            }
            case 4 -> {
                FMLLoadCompleteEvent e = (FMLLoadCompleteEvent)event3;
                yield new LoadCompleteEvent();
            }
            case 5 -> {
                ModConfigEvent e = (ModConfigEvent)event3;
                yield new com.vicmatskiv.pointblank.event.ModConfigEvent();
            }
            case 6 -> {
                final AddPackFindersEvent e = (AddPackFindersEvent)event3;
                yield new com.vicmatskiv.pointblank.event.AddPackFindersEvent(this, e.getPackType()){

                    @Override
                    public void addRepositorySource(RepositorySource source) {
                        e.addRepositorySource(source);
                    }
                };
            }
            case 7 -> {
                EntityRenderersEvent.RegisterRenderers e = (EntityRenderersEvent.RegisterRenderers)event3;
                yield new EntityRenderersEvent.RegisterRenderers((arg_0, arg_1) -> ((EntityRenderersEvent.RegisterRenderers)e).registerEntityRenderer(arg_0, arg_1), (arg_0, arg_1) -> ((EntityRenderersEvent.RegisterRenderers)e).registerBlockEntityRenderer(arg_0, arg_1));
            }
            case 8 -> {
                final RegisterParticleProvidersEvent e = (RegisterParticleProvidersEvent)event3;
                yield new com.vicmatskiv.pointblank.event.RegisterParticleProvidersEvent(this){

                    @Override
                    public <T extends ParticleOptions> void registerSpriteSet(ParticleType<T> particleType, ParticleEngine.SpriteParticleRegistration<T> registration) {
                        e.registerSpriteSet(particleType, registration);
                    }
                };
            }
            default -> null;
        };
    }

    private CustomEvent toCommonEvent(Event event) {
        Event event2 = event;
        Objects.requireNonNull(event2);
        Event event3 = event2;
        int n = 0;
        return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{RenderFrameEvent.Pre.class, RenderFrameEvent.Post.class, ClientTickEvent.Pre.class, ClientTickEvent.Post.class, RenderLivingEvent.Pre.class, RenderLivingEvent.Post.class, ViewportEvent.ComputeFov.class, ViewportEvent.ComputeCameraAngles.class, LivingEvent.LivingJumpEvent.class, net.neoforged.neoforge.event.entity.living.LivingDeathEvent.class, RenderTooltipEvent.Pre.class, RenderTooltipEvent.GatherComponents.class, InputEvent.InteractionKeyMappingTriggered.class, net.neoforged.neoforge.event.entity.living.LivingDropsEvent.class, PlayerEvent.HarvestCheck.class, PlayerTickEvent.Pre.class, PlayerTickEvent.Post.class, PlayerEvent.PlayerLoggedInEvent.class, PlayerEvent.PlayerChangedDimensionEvent.class, PlayerEvent.PlayerRespawnEvent.class, LivingEquipmentChangeEvent.class, ServerStartedEvent.class, ServerAboutToStartEvent.class, VillagerTradesEvent.class, LivingSwapItemsEvent.Hands.class, RenderGuiEvent.Post.class}, (Object)event3, n)) {
            case 0 -> {
                RenderFrameEvent.Pre e = (RenderFrameEvent.Pre)event3;
                yield new TickEvent.RenderTickEvent(e.getPartialTick(), TickEvent.Phase.START);
            }
            case 1 -> {
                RenderFrameEvent.Post e = (RenderFrameEvent.Post)event3;
                yield new TickEvent.RenderTickEvent(e.getPartialTick(), TickEvent.Phase.END);
            }
            case 2 -> {
                ClientTickEvent.Pre e = (ClientTickEvent.Pre)event3;
                yield new TickEvent.ClientTickEvent(TickEvent.Phase.START);
            }
            case 3 -> {
                ClientTickEvent.Post e = (ClientTickEvent.Post)event3;
                yield new TickEvent.ClientTickEvent(TickEvent.Phase.END);
            }
            case 4 -> {
                RenderLivingEvent.Pre e = (RenderLivingEvent.Pre)event3;
                yield new RenderLivingEvent.Pre(e.getEntity(), e.getRenderer());
            }
            case 5 -> {
                RenderLivingEvent.Post e = (RenderLivingEvent.Post)event3;
                yield new RenderLivingEvent.Post(e.getEntity(), e.getRenderer());
            }
            case 6 -> {
                final ViewportEvent.ComputeFov e = (ViewportEvent.ComputeFov)event3;
                yield new ViewportEvent.ComputeFov(this){

                    @Override
                    public void setFOV(double fov) {
                        e.setFOV(fov);
                    }

                    @Override
                    public double getFOV() {
                        return e.getFOV();
                    }

                    @Override
                    public boolean useFovSetting() {
                        return e.usedConfiguredFov();
                    }
                };
            }
            case 7 -> {
                final ViewportEvent.ComputeCameraAngles e = (ViewportEvent.ComputeCameraAngles)event3;
                yield new ViewportEvent.ComputeCameraAngles(this, e.getYaw(), e.getPitch(), e.getRoll()){

                    @Override
                    public void setYaw(float yaw) {
                        super.setYaw(yaw);
                        e.setYaw(yaw);
                    }

                    @Override
                    public void setPitch(float pitch) {
                        super.setPitch(pitch);
                        e.setPitch(pitch);
                    }

                    @Override
                    public void setRoll(float roll) {
                        super.setRoll(roll);
                        e.setRoll(roll);
                    }
                };
            }
            case 8 -> {
                LivingEvent.LivingJumpEvent e = (LivingEvent.LivingJumpEvent)event3;
                yield new LivingEvent.LivingJumpEvent(e.getEntity());
            }
            case 9 -> {
                net.neoforged.neoforge.event.entity.living.LivingDeathEvent e = (net.neoforged.neoforge.event.entity.living.LivingDeathEvent)event3;
                yield new LivingDeathEvent(e.getEntity(), e.getSource());
            }
            case 10 -> {
                final RenderTooltipEvent.Pre e = (RenderTooltipEvent.Pre)event3;
                yield new RenderTooltipEvent.Pre(this, e.getGraphics(), e.getItemStack(), e.getComponents(), e.getX(), e.getY(), e.getFont(), e.getTooltipPositioner()){

                    @Override
                    public void setCanceled(boolean isCancelled) {
                        super.setCanceled(isCancelled);
                        e.setCanceled(isCancelled);
                    }
                };
            }
            case 11 -> {
                RenderTooltipEvent.GatherComponents e = (RenderTooltipEvent.GatherComponents)event3;
                yield new RenderTooltipEvent.GatherComponents(e.getItemStack(), e.getTooltipElements());
            }
            case 12 -> {
                final InputEvent.InteractionKeyMappingTriggered e = (InputEvent.InteractionKeyMappingTriggered)event3;
                yield new InputEvent.InteractionKeyMappingTriggered(this, null){

                    @Override
                    public void setCanceled(boolean isCancelled) {
                        super.setCanceled(isCancelled);
                        e.setCanceled(isCancelled);
                    }
                };
            }
            case 13 -> {
                net.neoforged.neoforge.event.entity.living.LivingDropsEvent e = (net.neoforged.neoforge.event.entity.living.LivingDropsEvent)event3;
                yield new LivingDropsEvent(e.getEntity(), e.getDrops());
            }
            case 14 -> {
                final PlayerEvent.HarvestCheck e = (PlayerEvent.HarvestCheck)event3;
                yield new PlayerEvent.HarvestCheck(this, e.getEntity(), e.getTargetBlock(), e.canHarvest()){

                    @Override
                    public void setCanHarvest(boolean success) {
                        super.setCanHarvest(success);
                        e.setCanHarvest(success);
                    }
                };
            }
            case 15 -> {
                PlayerTickEvent.Pre e = (PlayerTickEvent.Pre)event3;
                yield new TickEvent.PlayerTickEvent(e.getEntity(), TickEvent.Phase.START);
            }
            case 16 -> {
                PlayerTickEvent.Post e = (PlayerTickEvent.Post)event3;
                yield new TickEvent.PlayerTickEvent(e.getEntity(), TickEvent.Phase.END);
            }
            case 17 -> {
                PlayerEvent.PlayerLoggedInEvent e = (PlayerEvent.PlayerLoggedInEvent)event3;
                yield new PlayerEvent.PlayerLoggedInEvent(e.getEntity());
            }
            case 18 -> {
                PlayerEvent.PlayerChangedDimensionEvent e = (PlayerEvent.PlayerChangedDimensionEvent)event3;
                yield new PlayerEvent.PlayerChangedDimensionEvent(e.getEntity());
            }
            case 19 -> {
                PlayerEvent.PlayerRespawnEvent e = (PlayerEvent.PlayerRespawnEvent)event3;
                yield new PlayerEvent.PlayerRespawnEvent(e.getEntity());
            }
            case 20 -> {
                LivingEquipmentChangeEvent e = (LivingEquipmentChangeEvent)event3;
                yield new com.vicmatskiv.pointblank.event.LivingEquipmentChangeEvent(e.getEntity(), e.getSlot(), e.getFrom(), e.getTo());
            }
            case 21 -> {
                ServerStartedEvent e = (ServerStartedEvent)event3;
                yield new com.vicmatskiv.pointblank.event.ServerStartedEvent(e.getServer());
            }
            case 22 -> {
                ServerAboutToStartEvent e = (ServerAboutToStartEvent)event3;
                yield new com.vicmatskiv.pointblank.event.ServerAboutToStartEvent(e.getServer());
            }
            case 23 -> {
                VillagerTradesEvent e = (VillagerTradesEvent)event3;
                yield new com.vicmatskiv.pointblank.event.VillagerTradesEvent((Int2ObjectMap<List<VillagerTrades.ItemListing>>)e.getTrades(), e.getType());
            }
            case 24 -> {
                final LivingSwapItemsEvent.Hands e = (LivingSwapItemsEvent.Hands)event3;
                yield new LivingSwapItemsEvent.Hands(this, e.getEntity()){

                    @Override
                    public void setCanceled(boolean isCancelled) {
                        super.setCanceled(isCancelled);
                        e.setCanceled(isCancelled);
                    }
                };
            }
            case 25 -> {
                RenderGuiEvent.Post e = (RenderGuiEvent.Post)event3;
                yield new RenderGuiEventPost(e.getGuiGraphics(), e.getPartialTick());
            }
            default -> null;
        };
    }

    @Override
    public Path getGameDir() {
        return FMLPaths.GAMEDIR.get();
    }

    @Override
    public PackResources createDelegatingPackResources(PackLocationInfo info, PackMetadataSection packMeta, List<? extends PackResources> packs) {
        return new DelegatingPackResources(info, packMeta, packs);
    }

    @Override
    public Pack.ResourcesSupplier createPackResourceSupplier(PackResources packResources) {
        return ((DelegatingPackResources)packResources).supplier();
    }

    @Override
    public NetworkService getNetworkService() {
        return this.networkService;
    }

    @Override
    public void onRegistrationCompleted() {
        IEventBus modEventBus = ModLoadingContext.get().getActiveContainer().getEventBus();
        this.forgeBlockEntityTypeRegistryservice.registerWithModEventBus(modEventBus);
        this.blockRegsitryService.registerWithModEventBus(modEventBus);
        this.recipeTypeRegistryService.registerWithModEventBus(modEventBus);
        this.recipeSerializerService.registerWithModEventBus(modEventBus);
        this.entityTypeRegistryService.registerWithModEventBus(modEventBus);
        this.dataComponentTypeRegistry.registerWithModEventBus(modEventBus);
        this.menuTypeRegistry.registerWithModEventBus(modEventBus);
        this.soundRegistry.registerWithModEventBus(modEventBus);
        this.particleTypeRegistry.registerWithModEventBus(modEventBus);
        this.itemRegistry.registerWithModEventBus(modEventBus);
        this.creativeModeTabRegistry.registerWithModEventBus(modEventBus);
        this.villagerProfessionRegistry.registerWithModEventBus(modEventBus);
        this.poiTypeRegistry.registerWithModEventBus(modEventBus);
    }

    @Override
    public RegistryService<BlockEntityType<?>> getBlockEntityTypeRegistry() {
        return this.forgeBlockEntityTypeRegistryservice;
    }

    @Override
    public RegistryService<Block> getBlockRegistryService() {
        return this.blockRegsitryService;
    }

    @Override
    public RegistryService<RecipeType<?>> getRecipeTypeRegistryService() {
        return this.recipeTypeRegistryService;
    }

    @Override
    public RegistryService<RecipeSerializer<?>> getRecipeSerializerService() {
        return this.recipeSerializerService;
    }

    @Override
    public RegistryService<EntityType<?>> getEntityTypeRegistryService() {
        return this.entityTypeRegistryService;
    }

    @Override
    public RegistryService<DataComponentType<?>> getDataComponentTypeRegistry() {
        return this.dataComponentTypeRegistry;
    }

    @Override
    public RegistryService<MenuType<?>> getMenuTypeRegistry() {
        return this.menuTypeRegistry;
    }

    @Override
    public RegistryService<SoundEvent> getSoundRegistry() {
        return this.soundRegistry;
    }

    @Override
    public RegistryService<ParticleType<?>> getParticleTypeRegistry() {
        return this.particleTypeRegistry;
    }

    @Override
    public RegistryService<Item> getItemRegistry() {
        return this.itemRegistry;
    }

    @Override
    public RegistryService<CreativeModeTab> getCreativeModeTabRegistry() {
        return this.creativeModeTabRegistry;
    }

    @Override
    public RegistryService<VillagerProfession> getVillagerProfessionRegistry() {
        return this.villagerProfessionRegistry;
    }

    @Override
    public RegistryService<PoiType> getPoiTypeRegistry() {
        return this.poiTypeRegistry;
    }

    @Override
    public boolean isClientThread() {
        return EffectiveSide.get() == LogicalSide.CLIENT;
    }

    @Override
    public ModInfo getModInfo(String modId) {
        ModFileInfo forgeModInfo = LoadingModList.get().getModFileById(modId);
        return forgeModInfo != null ? new ModInfo(modId, forgeModInfo.versionString()) : null;
    }

    @Override
    public JarFileSystem getJarFileSystem(final Path ... segments) {
        return new JarFileSystem(){
            final SecureJar secureJar;
            {
                this.secureJar = SecureJar.from((Path[])segments);
            }

            @Override
            public Path getPath(String first, String ... rest) {
                return this.secureJar.getPath(first, rest);
            }
        };
    }

    @Override
    public CompoundTag getPersistentData(Player player) {
        return player.getPersistentData();
    }

    @Override
    public ResourceLocation parseRecipeResourceLocation(String location) {
        ResourceLocation resourceLocation = ResourceLocation.parse((String)location);
        if (resourceLocation.getNamespace().equals("forge")) {
            String path = resourceLocation.getPath();
            if (path.equals("gunpowder")) {
                path = "gunpowders";
            }
            resourceLocation = ResourceLocation.fromNamespaceAndPath((String)"c", (String)path);
        }
        return resourceLocation;
    }

    @Override
    public Function<ItemDisplayContext, ItemTransforms> getItemTransforms(Player player, ItemStack itemStack) {
        return itemDisplayContext -> {
            Minecraft minecraft = Minecraft.getInstance();
            BakedModel bakedModel = minecraft.getItemRenderer().getModel(itemStack, MiscUtil.getLevel((Entity)player), (LivingEntity)player, player.getId() + itemDisplayContext.ordinal());
            if (bakedModel instanceof SeparateTransformsModel.Baked) {
                BakedModel baseModel = ((BakedModelMixin)bakedModel).getBaseModel();
                return baseModel.getTransforms();
            }
            return bakedModel.getTransforms();
        };
    }

    @Override
    public Entity getParentEntity(Entity entity) {
        if (entity instanceof PartEntity) {
            PartEntity entityPart = (PartEntity)entity;
            return entityPart.getParent();
        }
        return null;
    }

    @Override
    public KeyMapping createKeyMappingWithModifier(String name, int keyCode, String category, KeyModifierType modifier) {
        KeyModifier neoModifier = switch (modifier) {
            default -> throw new MatchException(null, null);
            case KeyModifierType.ALT -> KeyModifier.ALT;
            case KeyModifierType.CONTROL -> KeyModifier.CONTROL;
            case KeyModifierType.SHIFT -> KeyModifier.SHIFT;
            case KeyModifierType.NONE -> KeyModifier.NONE;
        };
        return new KeyMapping(name, (IKeyConflictContext)KeyConflictContext.IN_GAME, neoModifier, InputConstants.Type.KEYSYM, keyCode, category);
    }

    @Override
    public boolean isKeyWithModifierDown(KeyMapping keyMapping, KeyModifierType modifier) {
        return keyMapping.isDown() && keyMapping.getKeyModifier().isActive((IKeyConflictContext)KeyConflictContext.IN_GAME);
    }

    @Override
    public boolean supportsKeyModifiers() {
        return true;
    }

    static {
        EVENT_TYPE_MAP.put(TickEvent.RenderTickEvent.class, List.of(RenderFrameEvent.Pre.class, RenderFrameEvent.Post.class));
        EVENT_TYPE_MAP.put(TickEvent.ClientTickEvent.class, List.of(ClientTickEvent.Pre.class, ClientTickEvent.Post.class));
        EVENT_TYPE_MAP.put(RenderLivingEvent.Pre.class, List.of(RenderLivingEvent.Pre.class));
        EVENT_TYPE_MAP.put(RenderLivingEvent.Post.class, List.of(RenderLivingEvent.Post.class));
        EVENT_TYPE_MAP.put(ViewportEvent.ComputeFov.class, List.of(ViewportEvent.ComputeFov.class));
        EVENT_TYPE_MAP.put(ViewportEvent.ComputeCameraAngles.class, List.of(ViewportEvent.ComputeCameraAngles.class));
        EVENT_TYPE_MAP.put(LivingEvent.LivingJumpEvent.class, List.of(LivingEvent.LivingJumpEvent.class));
        EVENT_TYPE_MAP.put(RenderTooltipEvent.Pre.class, List.of(RenderTooltipEvent.Pre.class));
        EVENT_TYPE_MAP.put(RenderTooltipEvent.GatherComponents.class, List.of(RenderTooltipEvent.GatherComponents.class));
        EVENT_TYPE_MAP.put(InputEvent.InteractionKeyMappingTriggered.class, List.of(InputEvent.InteractionKeyMappingTriggered.class));
        EVENT_TYPE_MAP.put(LivingDropsEvent.class, List.of(net.neoforged.neoforge.event.entity.living.LivingDropsEvent.class));
        EVENT_TYPE_MAP.put(PlayerEvent.HarvestCheck.class, List.of(PlayerEvent.HarvestCheck.class));
        EVENT_TYPE_MAP.put(TickEvent.PlayerTickEvent.class, List.of(PlayerTickEvent.Pre.class, PlayerTickEvent.Post.class));
        EVENT_TYPE_MAP.put(PlayerEvent.PlayerLoggedInEvent.class, List.of(PlayerEvent.PlayerLoggedInEvent.class));
        EVENT_TYPE_MAP.put(PlayerEvent.PlayerChangedDimensionEvent.class, List.of(PlayerEvent.PlayerChangedDimensionEvent.class));
        EVENT_TYPE_MAP.put(PlayerEvent.PlayerRespawnEvent.class, List.of(PlayerEvent.PlayerRespawnEvent.class));
        EVENT_TYPE_MAP.put(com.vicmatskiv.pointblank.event.LivingEquipmentChangeEvent.class, List.of(LivingEquipmentChangeEvent.class));
        EVENT_TYPE_MAP.put(com.vicmatskiv.pointblank.event.ServerStartedEvent.class, List.of(ServerStartedEvent.class));
        EVENT_TYPE_MAP.put(com.vicmatskiv.pointblank.event.ServerAboutToStartEvent.class, List.of(ServerAboutToStartEvent.class));
        EVENT_TYPE_MAP.put(com.vicmatskiv.pointblank.event.VillagerTradesEvent.class, List.of(VillagerTradesEvent.class));
        EVENT_TYPE_MAP.put(LivingSwapItemsEvent.Hands.class, List.of(LivingSwapItemsEvent.Hands.class));
        EVENT_TYPE_MAP.put(RenderGuiEventPost.class, List.of(RenderGuiEvent.Post.class));
        EVENT_TYPE_MAP.put(LivingDeathEvent.class, List.of(net.neoforged.neoforge.event.entity.living.LivingDeathEvent.class));
        BOOTSTRAP_EVENT_TYPE_MAP = new HashMap<Class<? extends CustomEvent>, Class<? extends Event>>();
        BOOTSTRAP_EVENT_TYPE_MAP.put(CommonSetupEvent.class, FMLCommonSetupEvent.class);
        BOOTSTRAP_EVENT_TYPE_MAP.put(ClientSetupEvent.class, FMLClientSetupEvent.class);
        BOOTSTRAP_EVENT_TYPE_MAP.put(LoadCompleteEvent.class, FMLLoadCompleteEvent.class);
        BOOTSTRAP_EVENT_TYPE_MAP.put(com.vicmatskiv.pointblank.event.AddPackFindersEvent.class, AddPackFindersEvent.class);
        BOOTSTRAP_EVENT_TYPE_MAP.put(com.vicmatskiv.pointblank.event.RegisterKeyMappingsEvent.class, RegisterKeyMappingsEvent.class);
        BOOTSTRAP_EVENT_TYPE_MAP.put(GatherDataEvent.class, net.neoforged.neoforge.data.event.GatherDataEvent.class);
        BOOTSTRAP_EVENT_TYPE_MAP.put(EntityRenderersEvent.RegisterRenderers.class, EntityRenderersEvent.RegisterRenderers.class);
        BOOTSTRAP_EVENT_TYPE_MAP.put(com.vicmatskiv.pointblank.event.RegisterParticleProvidersEvent.class, RegisterParticleProvidersEvent.class);
        BOOTSTRAP_EVENT_TYPE_MAP.put(com.vicmatskiv.pointblank.event.ModConfigEvent.class, ModConfigEvent.class);
    }
}


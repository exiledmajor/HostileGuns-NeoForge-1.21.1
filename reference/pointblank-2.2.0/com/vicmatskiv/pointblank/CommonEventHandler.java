/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Direction
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.world.Containers
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.entity.monster.Monster
 *  net.minecraft.world.entity.npc.VillagerTrades$ItemsForEmeralds
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.PickaxeItem
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 */
package com.vicmatskiv.pointblank;

import com.vicmatskiv.pointblank.Config;
import com.vicmatskiv.pointblank.Platform;
import com.vicmatskiv.pointblank.client.GunClientState;
import com.vicmatskiv.pointblank.client.effect.EffectBuilder;
import com.vicmatskiv.pointblank.client.effect.EffectLauncher;
import com.vicmatskiv.pointblank.event.AttachmentRemovedEvent;
import com.vicmatskiv.pointblank.event.LivingDeathEvent;
import com.vicmatskiv.pointblank.event.LivingDropsEvent;
import com.vicmatskiv.pointblank.event.LivingEquipmentChangeEvent;
import com.vicmatskiv.pointblank.event.LivingSwapItemsEvent;
import com.vicmatskiv.pointblank.event.PlayerEvent;
import com.vicmatskiv.pointblank.event.ServerAboutToStartEvent;
import com.vicmatskiv.pointblank.event.ServerStartedEvent;
import com.vicmatskiv.pointblank.event.SubscribeEvent2;
import com.vicmatskiv.pointblank.event.TickEvent;
import com.vicmatskiv.pointblank.event.VillagerTradesEvent;
import com.vicmatskiv.pointblank.item.AmmoItem;
import com.vicmatskiv.pointblank.item.FireModeInstance;
import com.vicmatskiv.pointblank.item.GunItem;
import com.vicmatskiv.pointblank.item.ThrowableItem;
import com.vicmatskiv.pointblank.network.ClientBoundPlayerDataSyncPacket;
import com.vicmatskiv.pointblank.registry.AmmoRegistry;
import com.vicmatskiv.pointblank.registry.BlockRegistry;
import com.vicmatskiv.pointblank.registry.EffectRegistry;
import com.vicmatskiv.pointblank.registry.ItemRegistry;
import com.vicmatskiv.pointblank.registry.MiscItemRegistry;
import com.vicmatskiv.pointblank.registry.VillagerRegistry;
import com.vicmatskiv.pointblank.util.ClientUtil;
import com.vicmatskiv.pointblank.util.InventoryUtils;
import com.vicmatskiv.pointblank.util.MiscUtil;
import com.vicmatskiv.pointblank.util.ServerTaskScheduler;
import com.vicmatskiv.pointblank.util.SimpleHitResult;
import com.vicmatskiv.pointblank.util.Tradeable;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Containers;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class CommonEventHandler {
    private final ServerTaskScheduler scheduler;
    private final Random random = new Random();

    public CommonEventHandler(ServerTaskScheduler scheduler) {
        this.scheduler = scheduler;
    }

    @SubscribeEvent2
    public void onServerAboutToStartEvent(ServerAboutToStartEvent event) {
        VillagerRegistry.registerStructures(event.getServer());
    }

    @SubscribeEvent2
    public void onServerStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        server.addTickable((Runnable)this.scheduler);
    }

    @SubscribeEvent2
    public void onVillagerTradesEvent(VillagerTradesEvent event) {
        if (event.getType() == VillagerRegistry.ARMS_DEALER_PROFESSION.get()) {
            for (Map.Entry<String, Supplier<? extends Item>> e : ItemRegistry.ITEMS.getItemsByName().entrySet()) {
                int emeraldCount;
                List levelTrades;
                Item item = e.getValue().get();
                if (!(item instanceof Tradeable)) continue;
                Tradeable tradeableItem = (Tradeable)item;
                float price = tradeableItem.getPrice();
                int tradeLevel = tradeableItem.getTradeLevel();
                if (Float.isNaN(price) || tradeLevel < 1 || tradeLevel > 5 || (levelTrades = (List)event.getTrades().get(tradeLevel)) == null || (emeraldCount = (int)Math.round((double)price / Config.emeraldExchangeRate)) <= 0) continue;
                VillagerTrades.ItemsForEmeralds ife = new VillagerTrades.ItemsForEmeralds(new ItemStack((ItemLike)item), emeraldCount, tradeableItem.getBundleQuantity(), 16, 1);
                levelTrades.add(ife);
            }
        }
    }

    @SubscribeEvent2
    public void onSwapHands(LivingSwapItemsEvent.Hands event) {
        ItemStack toOffhand = event.getItemSwappedToOffHand();
        if (toOffhand != null && (toOffhand.getItem() instanceof ThrowableItem || toOffhand.getItem() instanceof GunItem && !GunItem.canBePlacedInOffhand(toOffhand))) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent2
    public void onLivingDrops(LivingDropsEvent event) {
        float dropChance = (float)Config.itemDropChance;
        if (MiscUtil.isNearlyZero(dropChance)) {
            return;
        }
        float randomNumber = this.random.nextFloat();
        if (event.getEntity() instanceof Monster && randomNumber < dropChance) {
            ItemStack itemStackToDrop = new ItemStack((ItemLike)MiscItemRegistry.GUNMETAL_NUGGET.get(), this.random.nextInt(1, Config.maxItemDropCount));
            ItemEntity drop = new ItemEntity(event.getEntity().level(), event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), itemStackToDrop);
            event.getDrops().add(drop);
        }
    }

    @SubscribeEvent2
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        ItemStack itemStack;
        Player mainPlayer;
        if (!MiscUtil.isClientSide((Entity)event.player)) {
            CommonEventHandler.enforceOffhandInvariant(event.player);
        }
        if (MiscUtil.isClientSide((Entity)event.player) && event.player != (mainPlayer = ClientUtil.getClientPlayer())) {
            ItemStack itemStack2 = event.player.getMainHandItem();
            GunClientState state = GunClientState.getMainHeldState(event.player);
            if (state != null) {
                state.stateTick((LivingEntity)event.player, itemStack2, false);
            }
        }
        if ((itemStack = event.player.getMainHandItem()) != null && itemStack.getItem() instanceof GunItem) {
            GunClientState state;
            if (GunItem.isAiming(itemStack)) {
                event.player.setSprinting(false);
            }
            if (MiscUtil.isClientSide((Entity)event.player) && (state = GunClientState.getMainHeldState(event.player)) != null && !state.isIdle() && !state.isDrawing()) {
                event.player.setSprinting(false);
            }
        }
    }

    @SubscribeEvent2
    public void onHarvestCheck(PlayerEvent.HarvestCheck check) {
        Player player;
        ItemStack heldItem;
        if (check.getTargetBlock().is(BlockRegistry.PRINTER.get()) && (heldItem = (player = check.getEntity()).getMainHandItem()) != null && heldItem.getItem() instanceof PickaxeItem) {
            check.setCanHarvest(true);
        }
    }

    @SubscribeEvent2
    public void onEquipmentChangeEvent(LivingEquipmentChangeEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (livingEntity instanceof Player) {
            Player player = (Player)livingEntity;
            if (event.getSlot() == EquipmentSlot.OFFHAND && !MiscUtil.isClientSide((Entity)player)) {
                CommonEventHandler.enforceOffhandInvariant(player);
            }
        }
    }

    @SubscribeEvent2
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        Level level = MiscUtil.getLevel((Entity)player);
        if (!level.isClientSide) {
            CommonEventHandler.enforceOffhandInvariant(player);
            Platform.getInstance().getNetworkService().sendToClient(new ClientBoundPlayerDataSyncPacket(MiscUtil.getPersistentData(player), ItemRegistry.ITEMS.getItemsByName().values().stream().filter(v -> v != null).map(v -> BuiltInRegistries.ITEM.getId((Object)((Item)v.get()))).collect(Collectors.toList())), player);
        }
    }

    @SubscribeEvent2
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        Level level = MiscUtil.getLevel((Entity)player);
        if (!level.isClientSide) {
            CommonEventHandler.enforceOffhandInvariant(player);
            Platform.getInstance().getNetworkService().sendToClient(new ClientBoundPlayerDataSyncPacket(MiscUtil.getPersistentData(player), ItemRegistry.ITEMS.getItemsByName().values().stream().filter(v -> v != null).map(v -> BuiltInRegistries.ITEM.getId((Object)((Item)v.get()))).collect(Collectors.toList())), player);
        }
    }

    @SubscribeEvent2
    public void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        Player player = event.getEntity();
        Level level = MiscUtil.getLevel((Entity)player);
        if (!level.isClientSide) {
            Platform.getInstance().getNetworkService().sendToClient(new ClientBoundPlayerDataSyncPacket(MiscUtil.getPersistentData(player), ItemRegistry.ITEMS.getItemsByName().values().stream().filter(v -> v != null).map(v -> BuiltInRegistries.ITEM.getId((Object)((Item)v.get()))).collect(Collectors.toList())), player);
        }
    }

    @SubscribeEvent2
    public void onAttachmentRemoved(AttachmentRemovedEvent event) {
        ItemStack rootStack = event.getRootStack();
        Item item = rootStack.getItem();
        if (!(item instanceof GunItem)) {
            return;
        }
        GunItem gunItem = (GunItem)item;
        Player player = event.getPlayer();
        if (player == null || MiscUtil.isClientSide((Entity)player)) {
            return;
        }
        List<FireModeInstance> fireModeInstances = gunItem.getMainFireModes();
        for (FireModeInstance fireModeInstance : fireModeInstances) {
            int dropCount;
            int maxAmmoCapacity;
            int currentFireModeAmmo = GunItem.getAmmo(rootStack, fireModeInstance);
            int delta = Math.min(currentFireModeAmmo - (maxAmmoCapacity = gunItem.getMaxAmmoCapacity(rootStack, fireModeInstance)), 1728);
            if (delta <= 0) continue;
            List<AmmoItem> actualAmmo = fireModeInstance.getActualAmmo();
            AmmoItem ammoToRemove = null;
            if (actualAmmo.size() == 1) {
                ammoToRemove = actualAmmo.get(0);
            } else if (actualAmmo.size() > 1) {
                AmmoItem creativeAmmo = AmmoRegistry.AMMOCREATIVE.get();
                ammoToRemove = actualAmmo.contains(creativeAmmo) && player.isCreative() ? creativeAmmo : (AmmoItem)actualAmmo.stream().filter(a -> a != creativeAmmo).findAny().orElse(null);
            }
            if (ammoToRemove == null) continue;
            GunItem.setAmmo(rootStack, fireModeInstance, maxAmmoCapacity);
            for (int remainingCount = InventoryUtils.addItem(player, ammoToRemove, delta); remainingCount > 0; remainingCount -= dropCount) {
                dropCount = Math.min(ammoToRemove.getDefaultMaxStackSize(), remainingCount);
                Containers.dropItemStack((Level)MiscUtil.getLevel((Entity)event.getPlayer()), (double)(player.getX() + 1.25), (double)(player.getY() + 1.25), (double)player.getZ(), (ItemStack)new ItemStack((ItemLike)ammoToRemove, dropCount));
            }
        }
        CommonEventHandler.enforceOffhandInvariant(player);
    }

    private static void enforceOffhandInvariant(Player player) {
        boolean invalid;
        if (player == null || MiscUtil.isClientSide((Entity)player)) {
            return;
        }
        ItemStack offhand = player.getOffhandItem();
        if (offhand == null || offhand.isEmpty()) {
            return;
        }
        boolean bl = invalid = offhand.getItem() instanceof ThrowableItem || offhand.getItem() instanceof GunItem && !GunItem.canBePlacedInOffhand(offhand);
        if (!invalid) {
            return;
        }
        player.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        player.getInventory().add(offhand);
        if (!offhand.isEmpty()) {
            Vec3 playerPos = player.getPosition(0.0f);
            Containers.dropItemStack((Level)MiscUtil.getLevel((Entity)player), (double)playerPos.x, (double)playerPos.y, (double)playerPos.z, (ItemStack)offhand);
        }
    }

    @SubscribeEvent2
    public void onLivingDeath(LivingDeathEvent event) {
        Entity entity;
        DamageSource damageSource = event.getSource();
        if (damageSource != null && (entity = damageSource.getEntity()) instanceof Player) {
            GunClientState state;
            Player player = (Player)entity;
            if (!MiscUtil.getLevel((Entity)player).isClientSide && (state = GunClientState.getMainHeldState(player)) != null) {
                LivingEntity targetEntity = event.getEntity();
                List<Supplier<EffectBuilder<EffectBuilder<?, ?>, ?>>> effects = EffectRegistry.getEntityDeathEffects(targetEntity);
                for (Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>> supplier : effects) {
                    Vec3 targetPos = targetEntity.getBoundingBox().getCenter();
                    EffectLauncher.broadcast(supplier, player, state, targetEntity, new SimpleHitResult(targetPos, HitResult.Type.ENTITY, Direction.DOWN, targetEntity.getId()));
                }
            }
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.core.Position
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.Connection
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.Containers
 *  net.minecraft.world.MenuProvider
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.ContainerData
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.AABB
 *  software.bernie.geckolib.animatable.GeoAnimatable
 *  software.bernie.geckolib.animatable.GeoBlockEntity
 *  software.bernie.geckolib.animatable.instance.AnimatableInstanceCache
 *  software.bernie.geckolib.animation.AnimatableManager$ControllerRegistrar
 *  software.bernie.geckolib.animation.AnimationController
 *  software.bernie.geckolib.animation.PlayState
 *  software.bernie.geckolib.animation.RawAnimation
 *  software.bernie.geckolib.animation.keyframe.event.data.SoundKeyframeData
 *  software.bernie.geckolib.util.GeckoLibUtil
 */
package com.vicmatskiv.pointblank.block.entity;

import com.vicmatskiv.pointblank.Enableable;
import com.vicmatskiv.pointblank.block.entity.PrinterBlockEntityExt;
import com.vicmatskiv.pointblank.crafting.Craftable;
import com.vicmatskiv.pointblank.crafting.PointBlankRecipe;
import com.vicmatskiv.pointblank.inventory.CraftingContainerMenu;
import com.vicmatskiv.pointblank.registry.BlockEntityRegistry;
import com.vicmatskiv.pointblank.registry.SoundRegistry;
import com.vicmatskiv.pointblank.util.ClientUtil;
import com.vicmatskiv.pointblank.util.InventoryUtils;
import com.vicmatskiv.pointblank.util.MiscUtil;
import com.vicmatskiv.pointblank.util.StateMachine;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Position;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.keyframe.event.data.SoundKeyframeData;
import software.bernie.geckolib.util.GeckoLibUtil;

public class PrinterBlockEntity
extends BlockEntity
implements PrinterBlockEntityExt,
GeoBlockEntity,
MenuProvider {
    private static final int OPENING_DURATION = 655;
    private static final int CLOSING_DURATION = 577;
    protected final ContainerData dataAccess = new ContainerData(){

        public int get(int dataSlotIndex) {
            switch (dataSlotIndex) {
                case 0: {
                    return PrinterBlockEntity.this.getState().ordinal();
                }
                case 1: {
                    return PrinterBlockEntity.this.craftingPlayer != null ? PrinterBlockEntity.this.craftingPlayer.getId() : -1;
                }
            }
            return 0;
        }

        public void set(int dataSlotIndex, int value) {
            switch (dataSlotIndex) {
                default: 
            }
        }

        public int getCount() {
            return 2;
        }
    };
    private static final Component CONTAINER_TITLE = Component.translatable((String)"screen.pointblank.crafting");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this);
    private List<Player> nearbyEntities;
    private long lastNearbyEntityUpdateTimestamp;
    private Player craftingPlayer;
    private PointBlankRecipe craftingRecipe;
    private long craftingStartTime;
    private long craftingDuration;
    private long openingDuration = 655L;
    private long closingStartTime;
    private long closingDuration = 577L;
    private long openingStartTime;
    private CraftingEventHandler craftingEventHandler;
    private StateMachine<State, Context> stateMachine;
    private State clientState;
    private static final RawAnimation ANIMATION_OPEN = RawAnimation.begin().thenPlay("animation.model.open").thenLoop("animation.model.idle");
    private static final RawAnimation ANIMATION_CLOSE = RawAnimation.begin().thenPlay("animation.model.close");
    private static final RawAnimation ANIMATION_IDLE = RawAnimation.begin().thenPlay("animation.model.idle");
    private static final RawAnimation ANIMATION_CRAFTING = RawAnimation.begin().thenPlay("animation.model.crafting").thenLoop("animation.model.idle");

    public PrinterBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.PRINTER_BLOCK_ENTITY.get(), pos, state);
    }

    public void setLevel(Level level) {
        super.setLevel(level);
        if (level.isClientSide) {
            this.clientState = State.CLOSED;
        } else {
            this.stateMachine = this.createStateMachine();
        }
    }

    public State getState() {
        if (this.level == null) {
            return null;
        }
        return this.level.isClientSide ? this.clientState : this.stateMachine.getCurrentState();
    }

    private StateMachine<State, Context> createStateMachine() {
        StateMachine.Builder<State, Context> builder = new StateMachine.Builder<State, Context>();
        builder.withTransition(State.CLOSED, State.OPENING, this::predicateIsPlayerNearby, StateMachine.TransitionMode.AUTO, null, this::actionOpen);
        builder.withTransition(State.OPENING, State.IDLE, this::openingTimeoutExpired, StateMachine.TransitionMode.AUTO, null, null);
        builder.withTransition(State.IDLE, State.CRAFTING, this::predicateIsPlayerNearby, StateMachine.TransitionMode.EVENT, null, this::actionStartCrafting);
        builder.withTransition(State.CRAFTING, State.IDLE, this::predicateIsPlayerNearby, StateMachine.TransitionMode.EVENT, null, null);
        builder.withTransition(State.CRAFTING, State.CRAFTING_COMPLETED, ctx -> this.predicateIsPlayerNearby((Context)ctx) && this.craftingTimeoutExpired((Context)ctx), StateMachine.TransitionMode.AUTO, null, this::actionCompleteCrafting);
        builder.withTransition(State.CRAFTING, State.CLOSING, ctx -> !this.predicateIsPlayerNearby((Context)ctx), StateMachine.TransitionMode.AUTO, null, this::actionCancelCrafting);
        builder.withTransition(State.CRAFTING_COMPLETED, State.IDLE, ctx -> true, StateMachine.TransitionMode.AUTO, null, null);
        builder.withTransition(State.IDLE, State.CLOSING, ctx -> !this.predicateIsPlayerNearby((Context)ctx), StateMachine.TransitionMode.AUTO, null, null);
        builder.withTransition(State.CLOSING, State.CLOSED, this::closingTimeoutExpired, StateMachine.TransitionMode.AUTO, null, null);
        builder.withOnSetStateAction(State.IDLE, this::actionIdle);
        builder.withOnChangeStateAction(this::actionOnChangeState);
        return builder.build(State.CLOSED);
    }

    private boolean closingTimeoutExpired(Context ctx) {
        return System.currentTimeMillis() - this.closingStartTime >= this.closingDuration;
    }

    private boolean craftingTimeoutExpired(Context ctx) {
        return System.currentTimeMillis() - this.craftingStartTime >= this.craftingDuration;
    }

    private boolean openingTimeoutExpired(Context ctx) {
        return System.currentTimeMillis() - this.openingStartTime >= this.openingDuration;
    }

    private boolean predicateIsPlayerNearby(Context context) {
        return PrinterBlockEntity.isPlayerNearby(this.getBlockPos(), this.nearbyEntities);
    }

    private void actionStartCrafting(Context context, State fromState, State toState) {
        this.craftingPlayer = context.craftingPlayer;
        this.craftingRecipe = context.craftingRecipe;
        this.craftingStartTime = System.currentTimeMillis();
        this.craftingDuration = ((Craftable)context.craftingRecipe.getResultItem(null).getItem()).getCraftingDuration();
        this.craftingEventHandler = context.craftingEventHandler;
    }

    private void actionIdle(Context context, State fromState, State toState) {
        this.resetCrafting();
    }

    private void actionOpen(Context context, State fromState, State toState) {
        this.openingStartTime = System.currentTimeMillis();
    }

    private void actionCompleteCrafting(Context context, State fromState, State toState) {
        this.createCraftingItem();
    }

    private void actionCancelCrafting(Context context, State fromState, State toState) {
        this.cancelCrafting(context.craftingPlayer, context.craftingRecipe.getId(MiscUtil.getLevel((Entity)context.craftingPlayer)));
    }

    private void actionOnChangeState(Context context, State fromState, State toState) {
        this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }

    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController((GeoAnimatable)this, state -> {
            PlayState playState = null;
            switch (this.getState().ordinal()) {
                case 0: {
                    playState = state.setAndContinue(ANIMATION_CLOSE);
                    break;
                }
                case 1: {
                    playState = state.setAndContinue(ANIMATION_OPEN);
                    break;
                }
                case 3: {
                    playState = state.setAndContinue(ANIMATION_IDLE);
                    break;
                }
                case 4: {
                    playState = state.setAndContinue(ANIMATION_CRAFTING);
                    break;
                }
                case 5: {
                    playState = state.setAndContinue(ANIMATION_IDLE);
                    break;
                }
                case 2: {
                    playState = state.setAndContinue(ANIMATION_CLOSE);
                }
            }
            return playState;
        }).setSoundKeyframeHandler(event -> {
            SoundKeyframeData soundKeyframeData;
            String soundName;
            SoundEvent soundEvent;
            Player player = ClientUtil.getClientPlayer();
            if (player != null && (soundEvent = SoundRegistry.getSoundEvent(soundName = (soundKeyframeData = event.getKeyframeData()).getSound())) != null) {
                BlockPos blockPos = this.getBlockPos();
                this.level.playLocalSound((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ(), soundEvent, SoundSource.BLOCKS, 2.0f, (1.0f + (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.2f) * 0.7f, false);
            }
        }));
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public static void clientTick(Level level, BlockPos blockPos, BlockState blockState, PrinterBlockEntity entity) {
    }

    private void updateEntities() {
        BlockPos blockpos = this.getBlockPos();
        if (this.level.getGameTime() > this.lastNearbyEntityUpdateTimestamp + 50L || this.nearbyEntities == null) {
            this.lastNearbyEntityUpdateTimestamp = this.level.getGameTime();
            AABB aabb = new AABB(blockpos).inflate(15.0);
            this.nearbyEntities = this.level.getEntitiesOfClass(Player.class, aabb);
        }
    }

    private static boolean isPlayerNearby(BlockPos blockPos, List<Player> entities) {
        if (entities == null) {
            return false;
        }
        for (LivingEntity livingEntity : entities) {
            if (!(livingEntity instanceof Player) || !livingEntity.isAlive() || livingEntity.isRemoved() || !blockPos.closerToCenterThan((Position)livingEntity.position(), 6.0)) continue;
            return true;
        }
        return false;
    }

    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new CraftingContainerMenu(containerId, inventory, this, this.dataAccess);
    }

    public Component getDisplayName() {
        return CONTAINER_TITLE;
    }

    public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, PrinterBlockEntity entity) {
        entity.serverTick();
    }

    private void serverTick() {
        this.updateEntities();
        Context context = new Context(this.craftingPlayer, this.craftingRecipe, this.craftingEventHandler);
        this.stateMachine.update(context);
    }

    private void createCraftingItem() {
        ItemStack craftedStack = null;
        boolean isCraftingSuccessful = false;
        boolean isAddedToInventory = false;
        Exception craftingException = null;
        try {
            if (this.craftingRecipe.canBeCrafted(this.craftingPlayer) && (craftedStack = this.craftingRecipe.getResultItem(null)) != null && !craftedStack.isEmpty()) {
                this.craftingRecipe.removeIngredients(this.craftingPlayer);
                int remaingCount = InventoryUtils.addItem(this.craftingPlayer, craftedStack.getItem(), craftedStack.getCount());
                if (remaingCount > 0) {
                    BlockPos blockPos = this.getBlockPos();
                    if (blockPos != null) {
                        Containers.dropItemStack((Level)MiscUtil.getLevel((Entity)this.craftingPlayer), (double)blockPos.getX(), (double)((float)blockPos.getY() + 1.25f), (double)blockPos.getZ(), (ItemStack)craftedStack.copy());
                        isCraftingSuccessful = true;
                    }
                } else {
                    isAddedToInventory = true;
                    isCraftingSuccessful = true;
                }
            }
        }
        catch (Exception e) {
            craftingException = e;
            System.err.println("Caught exception during crafting " + String.valueOf(e));
        }
        if (craftedStack == null) {
            craftedStack = ItemStack.EMPTY;
        }
        if (this.craftingEventHandler != null) {
            if (isCraftingSuccessful) {
                this.craftingEventHandler.onCraftingCompleted(this.craftingPlayer, craftedStack, isAddedToInventory);
            } else {
                this.craftingEventHandler.onCraftingFailed(this.craftingPlayer, craftedStack, craftingException);
            }
        }
    }

    private void resetCrafting() {
        this.craftingPlayer = null;
        this.craftingRecipe = null;
        this.craftingStartTime = 0L;
        this.craftingDuration = 0L;
        this.craftingEventHandler = null;
    }

    public boolean tryCrafting(Player player, ResourceLocation recipeId, CraftingEventHandler craftingEventHandler) {
        Enableable e;
        Item item;
        if (this.nearbyEntities != null && !this.nearbyEntities.contains(player)) {
            return false;
        }
        PointBlankRecipe craftingRecipe = PointBlankRecipe.getRecipe(MiscUtil.getLevel((Entity)player), recipeId);
        if (craftingRecipe == null) {
            return false;
        }
        ItemStack craftingItemStack = craftingRecipe.getResultItem(null);
        if (craftingItemStack == null || craftingItemStack.isEmpty() || !(craftingItemStack.getItem() instanceof Craftable) || (item = craftingItemStack.getItem()) instanceof Enableable && !(e = (Enableable)item).isEnabled()) {
            return false;
        }
        return this.stateMachine.setState(new Context(player, craftingRecipe, craftingEventHandler), State.CRAFTING) == State.CRAFTING;
    }

    public boolean cancelCrafting(Player player, ResourceLocation recipeId) {
        if (this.stateMachine.getCurrentState() != State.CRAFTING) {
            return false;
        }
        if (player != this.craftingPlayer) {
            return false;
        }
        if (!recipeId.equals((Object)this.craftingRecipe.getId(MiscUtil.getLevel((Entity)player)))) {
            return false;
        }
        this.stateMachine.setState(new Context(player, this.craftingRecipe, this.craftingEventHandler), State.IDLE);
        if (this.craftingEventHandler != null) {
            this.craftingEventHandler.onCraftingCancelled(this.craftingPlayer, this.craftingRecipe.getResultItem(null));
        }
        return true;
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("clientState", this.stateMachine.getCurrentState().ordinal());
        return ClientboundBlockEntityDataPacket.create((BlockEntity)this, (e, ra) -> tag);
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet, HolderLookup.Provider lookup) {
        CompoundTag tag = packet.getTag();
        if (tag != null) {
            int ordinal = tag.getInt("clientState");
            this.clientState = State.values()[ordinal];
        }
    }

    public static enum State {
        CLOSED,
        OPENING,
        CLOSING,
        IDLE,
        CRAFTING,
        CRAFTING_COMPLETED;

    }

    private static class Context {
        private Player craftingPlayer;
        private PointBlankRecipe craftingRecipe;
        private CraftingEventHandler craftingEventHandler;

        public Context(Player craftingPlayer, PointBlankRecipe craftingRecipe, CraftingEventHandler craftingEventHandler) {
            this.craftingPlayer = craftingPlayer;
            this.craftingRecipe = craftingRecipe;
            this.craftingEventHandler = craftingEventHandler;
        }
    }

    public static interface CraftingEventHandler {
        default public void onCraftingCompleted(Player player, ItemStack craftingItemStack, boolean isAddedToInventory) {
        }

        default public void onCraftingCancelled(Player player, ItemStack craftingItemStack) {
        }

        default public void onCraftingFailed(Player player, ItemStack craftingItemStack, Exception craftingException) {
        }
    }
}


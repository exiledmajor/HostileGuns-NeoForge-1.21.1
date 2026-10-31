/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer
 *  net.minecraft.client.renderer.entity.EntityRenderer
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.Projectile
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 *  software.bernie.geckolib.animatable.GeoAnimatable
 *  software.bernie.geckolib.animatable.GeoItem
 *  software.bernie.geckolib.animatable.client.GeoRenderProvider
 *  software.bernie.geckolib.animatable.instance.AnimatableInstanceCache
 *  software.bernie.geckolib.animation.AnimatableManager$ControllerRegistrar
 *  software.bernie.geckolib.animation.AnimationController
 *  software.bernie.geckolib.animation.PlayState
 *  software.bernie.geckolib.animation.RawAnimation
 *  software.bernie.geckolib.animation.keyframe.event.data.SoundKeyframeData
 *  software.bernie.geckolib.util.GeckoLibUtil
 */
package com.vicmatskiv.pointblank.item;

import com.google.gson.JsonObject;
import com.vicmatskiv.pointblank.Nameable;
import com.vicmatskiv.pointblank.Platform;
import com.vicmatskiv.pointblank.client.EntityRendererBuilder;
import com.vicmatskiv.pointblank.client.ThrowableClientState;
import com.vicmatskiv.pointblank.client.effect.AbstractEffect;
import com.vicmatskiv.pointblank.client.effect.EffectBuilder;
import com.vicmatskiv.pointblank.client.render.ProjectileItemEntityRenderer;
import com.vicmatskiv.pointblank.client.render.SpriteEntityRenderer;
import com.vicmatskiv.pointblank.client.render.ThrowableItemRenderer;
import com.vicmatskiv.pointblank.crafting.Craftable;
import com.vicmatskiv.pointblank.entity.EntityBuilder;
import com.vicmatskiv.pointblank.entity.EntityBuilderProvider;
import com.vicmatskiv.pointblank.entity.GenericThrowableProjectile;
import com.vicmatskiv.pointblank.entity.ProjectileLike;
import com.vicmatskiv.pointblank.item.Drawable;
import com.vicmatskiv.pointblank.item.EffectBuilderInfo;
import com.vicmatskiv.pointblank.item.ExplosionProvider;
import com.vicmatskiv.pointblank.item.HurtingItem;
import com.vicmatskiv.pointblank.item.ItemExtra;
import com.vicmatskiv.pointblank.item.ThrowableLike;
import com.vicmatskiv.pointblank.network.ThrowProjectileRequestPacket;
import com.vicmatskiv.pointblank.registry.EffectRegistry;
import com.vicmatskiv.pointblank.registry.SoundRegistry;
import com.vicmatskiv.pointblank.util.ClientUtil;
import com.vicmatskiv.pointblank.util.JsonUtil;
import com.vicmatskiv.pointblank.util.MiscUtil;
import com.vicmatskiv.pointblank.util.TimeUnit;
import com.vicmatskiv.pointblank.util.Tradeable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.keyframe.event.data.SoundKeyframeData;
import software.bernie.geckolib.util.GeckoLibUtil;

public class ThrowableItem
extends HurtingItem
implements ItemExtra,
ThrowableLike,
ExplosionProvider,
Drawable,
Craftable,
Nameable,
GeoItem,
Tradeable {
    private static final Logger LOGGER = LogManager.getLogger((String)"pointblank");
    public static final String ANIMATION_CONTROLLER = "default";
    private static final String ANIMATION_NAME_DRAW = "animation.model.draw";
    private static final String ANIMATION_NAME_THROW = "animation.model.throw";
    private static final RawAnimation ANIMATION_DRAW = RawAnimation.begin().thenPlay("animation.model.draw");
    private static final RawAnimation ANIMATION_THROW = RawAnimation.begin().thenPlay("animation.model.throw");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this);
    private final String name;
    private float tradePrice;
    private int tradeBundleQuantity;
    private int tradeLevel;
    private List<EffectBuilderInfo> projectileEffectBuilderSuppliers;
    private EntityBuilder<?, ?> entityBuilder;
    private long craftingDuration;
    private long drawCooldownDuration;
    private long idleCooldownDuration;
    private long inspectCooldownDuration;
    private long prepareIdleCooldownDuration;
    private long prepareThrowCooldownDuration;
    private long throwCooldownDuration;
    private long completeThrowCooldownDuration;

    public ThrowableItem(String name, Builder builder) {
        super(new Item.Properties(), builder);
        this.name = name;
        if (builder != null) {
            this.tradePrice = builder.tradePrice;
            this.tradeBundleQuantity = builder.tradeBundleQuantity;
            this.tradeLevel = builder.tradeLevel;
            this.entityBuilder = builder.getOrCreateEntityBuilder();
            this.craftingDuration = builder.craftingDuration;
            this.drawCooldownDuration = builder.drawCooldownDuration;
            this.idleCooldownDuration = builder.idleCooldownDuration;
            this.inspectCooldownDuration = builder.inspectCooldownDuration;
            this.prepareIdleCooldownDuration = builder.prepareIdleCooldownDuration;
            this.prepareThrowCooldownDuration = builder.prepareThrowCooldownDuration;
            this.throwCooldownDuration = builder.throwCooldownDuration;
            this.completeThrowCooldownDuration = builder.completeThrowCooldownDuration;
        }
    }

    @Override
    public String getName() {
        return this.name;
    }

    public ProjectileLike createProjectile(LivingEntity player, double posX, double posY, double posZ) {
        ProjectileLike projectile = (ProjectileLike)this.entityBuilder.build(MiscUtil.getLevel((Entity)player));
        ((Entity)projectile).setPos(posX, posY, posZ);
        ((Projectile)projectile).setOwner((Entity)player);
        return projectile;
    }

    public void registerControllers(AnimatableManager.ControllerRegistrar registry) {
        registry.add(new AnimationController((GeoAnimatable)this, ANIMATION_CONTROLLER, 0, state -> PlayState.STOP).triggerableAnim(ANIMATION_NAME_DRAW, ANIMATION_DRAW).triggerableAnim(ANIMATION_NAME_THROW, ANIMATION_THROW).setSoundKeyframeHandler(event -> {
            SoundKeyframeData soundKeyframeData;
            String soundName;
            SoundEvent soundEvent;
            Player player = ClientUtil.getClientPlayer();
            if (player != null && (soundEvent = SoundRegistry.getSoundEvent(soundName = (soundKeyframeData = event.getKeyframeData()).getSound())) != null) {
                player.playSound(soundEvent, 1.0f, 1.0f);
            }
        }));
    }

    public void inventoryTick(ItemStack itemStack, Level level, Entity entity, int i, boolean bl) {
        boolean isOffhand;
        Player player;
        ThrowableClientState clientState;
        if (!level.isClientSide()) {
            GeoItem.getOrAssignId((ItemStack)itemStack, (ServerLevel)((ServerLevel)level));
            CompoundTag tag = MiscUtil.getOrCreateTag(itemStack);
            long mid = tag.getLong("mid");
            long lid = tag.getLong("lid");
            if (mid == 0L && lid == 0L) {
                UUID newId = UUID.randomUUID();
                tag.putLong("mid", newId.getMostSignificantBits());
                tag.putLong("lid", newId.getLeastSignificantBits());
                MiscUtil.setTag(itemStack, tag);
            }
        } else if (entity instanceof Player && (clientState = ThrowableClientState.getState(player, itemStack, i, isOffhand = (player = (Player)entity).getOffhandItem() == itemStack)) != null) {
            clientState.updateState((LivingEntity)entity, itemStack, bl);
        }
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider(){
            private BlockEntityWithoutLevelRenderer renderer;

            public BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
                if (this.renderer == null) {
                    ResourceLocation modelResourceLocation = ResourceLocation.fromNamespaceAndPath((String)"pointblank", (String)ThrowableItem.this.name);
                    this.renderer = new ThrowableItemRenderer(modelResourceLocation);
                }
                return this.renderer;
            }
        });
    }

    @Override
    public float getPrice() {
        return this.tradePrice;
    }

    @Override
    public int getTradeLevel() {
        return this.tradeLevel;
    }

    @Override
    public int getBundleQuantity() {
        return this.tradeBundleQuantity;
    }

    @Override
    public long getCraftingDuration() {
        return this.craftingDuration;
    }

    @Override
    public void draw(Player player, ItemStack itemStack) {
        long id = GeoItem.getId((ItemStack)itemStack);
        AnimationController controller = (AnimationController)this.getAnimatableInstanceCache().getManagerForId(id).getAnimationControllers().get(ANIMATION_CONTROLLER);
        if (controller != null) {
            controller.forceAnimationReset();
        }
        this.triggerAnim((Entity)player, id, ANIMATION_CONTROLLER, ANIMATION_NAME_DRAW);
    }

    public boolean tryThrow(Player player, ItemStack itemStack, Entity targetEntity) {
        ThrowableClientState throwableClientState;
        boolean isMainHand;
        boolean result = false;
        int activeSlot = player.getInventory().selected;
        boolean bl = isMainHand = player.getMainHandItem() == itemStack;
        if (isMainHand && (throwableClientState = ThrowableClientState.getState(player, itemStack, activeSlot, false)) != null) {
            throwableClientState.setTrigger(true);
            result = throwableClientState.tryThrow((LivingEntity)player, itemStack, targetEntity);
        }
        return result;
    }

    @Override
    public ThrowableClientState createState(UUID stackId) {
        return new ThrowableClientState(stackId, this);
    }

    public float getModelScale() {
        return 1.0f;
    }

    @Override
    public boolean hasIdleAnimations() {
        return false;
    }

    @Override
    public void requestThrowFromServer(ThrowableClientState throwableClientState, Player player, ItemStack itemStack, Entity targetEntity) {
        int activeSlot = player.getInventory().selected;
        Platform.getInstance().getNetworkService().sendToServer(new ThrowProjectileRequestPacket(throwableClientState.getId(), activeSlot));
        LOGGER.debug("{} sent throw request to server", (Object)(System.currentTimeMillis() % 100000L));
    }

    @Override
    public long getDrawCooldownDuration(LivingEntity player, ThrowableClientState throwableClientState, ItemStack itemStack) {
        return this.drawCooldownDuration;
    }

    @Override
    public long getIdleCooldownDuration(LivingEntity player, ThrowableClientState throwableClientState, ItemStack itemStack) {
        return this.idleCooldownDuration;
    }

    @Override
    public long getInspectCooldownDuration(LivingEntity player, ThrowableClientState throwableClientState, ItemStack itemStack) {
        return this.inspectCooldownDuration;
    }

    @Override
    public long getPrepareIdleCooldownDuration() {
        return this.prepareIdleCooldownDuration;
    }

    @Override
    public long getPrepareThrowCooldownDuration(LivingEntity player, ThrowableClientState throwableClientState, ItemStack itemStack) {
        return this.prepareThrowCooldownDuration;
    }

    @Override
    public long getThrowCooldownDuration(LivingEntity player, ThrowableClientState throwableClientState, ItemStack itemStack) {
        return this.throwCooldownDuration;
    }

    @Override
    public long getCompleteThrowCooldownDuration(LivingEntity player, ThrowableClientState throwableClientState, ItemStack itemStack) {
        return this.completeThrowCooldownDuration;
    }

    public void setTriggerOff(Player player, ItemStack itemStack) {
        ThrowableClientState throwableClientState;
        boolean isOffhand;
        int activeSlot = player.getInventory().selected;
        boolean bl = isOffhand = player.getOffhandItem() == itemStack;
        if (!isOffhand && (throwableClientState = ThrowableClientState.getState(player, itemStack, activeSlot, false)) != null) {
            throwableClientState.setTrigger(false);
        }
    }

    @Override
    public void prepareThrow(ThrowableClientState throwableClientState, Player player, ItemStack itemStack, Entity targetEntity) {
        long id = GeoItem.getId((ItemStack)itemStack);
        this.triggerAnim((Entity)player, id, ANIMATION_CONTROLLER, ANIMATION_NAME_THROW);
    }

    @Override
    public void handleClientThrowRequest(ServerPlayer player, UUID stateId, int slotIndex) {
        ItemStack itemStack = player.getInventory().getItem(slotIndex);
        if (!(itemStack.getItem() instanceof ThrowableLike) || !stateId.equals(ItemExtra.getItemStackId(itemStack))) {
            LOGGER.error("Throwable item state id {} does not match item stack in slot", (Object)stateId);
            return;
        }
        Vec3 playerEyePosition = player.getEyePosition();
        Vec3 viewVector = player.getViewVector(0.0f);
        Vec3 upVector = player.getUpVector(0.0f);
        Vec3 rightVector = viewVector.cross(upVector).normalize();
        Vec3 throwPosition = playerEyePosition.add(viewVector.scale(0.5)).add(upVector.scale(0.2)).add(rightVector.scale(0.3));
        ProjectileLike projectile = this.createProjectile((LivingEntity)player, throwPosition.x, throwPosition.y, throwPosition.z);
        if (!player.isCreative()) {
            itemStack.shrink(1);
        }
        projectile.launchAtLookTarget((LivingEntity)player, 0.0, 0L);
        MiscUtil.getLevel((Entity)player).addFreshEntity((Entity)projectile);
    }

    public static class Builder
    extends HurtingItem.Builder<Builder>
    implements Nameable {
        private static final double DEFAULT_GRAVITY = 0.05;
        private static final float DEFAULT_INITIAL_VELOCITY = 50.0f;
        private static final float DEFAULT_WIDTH = 0.25f;
        private static final float DEFAULT_HEIGHT = 0.25f;
        private static final float DEFAULT_PRICE = Float.NaN;
        private static final int DEFAULT_TRADE_LEVEL = 0;
        private static final int DEFAULT_TRADE_BUNDLE_QUANTITY = 1;
        private static final long DEFAULT_CRAFTING_DURATION = 500L;
        private static final long DEFAULT_DRAW_COOLDOWN_DURATION = 800L;
        private static final long DEFAULT_THROW_COOLDOWN_DURATION = 1000L;
        private static final long DEFAULT_PREPARE_THROW_COOLDOWN_DURATION = 1000L;
        private static final long DEFAULT_MAX_LIFETIME = 5000L;
        private String name;
        private float tradePrice = Float.NaN;
        private int tradeBundleQuantity = 1;
        private int tradeLevel = 0;
        private Supplier<EntityBuilder<?, ?>> entityBuilderSupplier;
        private final List<EffectBuilderInfo> projectileEffectBuilderSuppliers = new ArrayList<EffectBuilderInfo>();
        private Supplier<EntityRendererBuilder<?, Entity, EntityRenderer<Entity>>> rendererBuilder;
        private double gravity = 0.05;
        private double initialVelocity = 50.0;
        private float boundingBoxWidth = 0.25f;
        private float boundingBoxHeight = 0.25f;
        private long maxLifetimeMillis;
        private boolean isRicochet;
        private EntityBuilder<?, ?> entityBuilder;
        private long craftingDuration = 500L;
        private long drawCooldownDuration = 800L;
        private long idleCooldownDuration = 0L;
        private long inspectCooldownDuration = 0L;
        private long prepareIdleCooldownDuration = 0L;
        private long prepareThrowCooldownDuration = 1000L;
        private long throwCooldownDuration = 1000L;
        private long completeThrowCooldownDuration = 0L;
        private ThrowableItem builtItem;

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withTradePrice(double price, int tradeBundleQuantity, int tradeLevel) {
            this.tradePrice = (float)price;
            this.tradeLevel = tradeLevel;
            this.tradeBundleQuantity = tradeBundleQuantity;
            return this;
        }

        public Builder withTradePrice(double price, int tradeLevel) {
            return this.withTradePrice(price, 1, tradeLevel);
        }

        public Builder withProjectileInitialVelocity(double initialVelocity) {
            this.initialVelocity = initialVelocity;
            return this;
        }

        public Builder withProjectileGravity(double gravity) {
            this.gravity = gravity;
            return this;
        }

        public Builder withProjectileMaxLifetime(int duration, TimeUnit timeUnit) {
            this.maxLifetimeMillis = timeUnit.toMillis(duration);
            return this;
        }

        public Builder withProjectileRicochet(boolean isRicochet) {
            this.isRicochet = isRicochet;
            return this;
        }

        public Builder withProjectileRenderer(Supplier<EntityRendererBuilder<?, Entity, EntityRenderer<Entity>>> rendererBuilder) {
            this.rendererBuilder = rendererBuilder;
            return this;
        }

        public Builder withProjectileEffect(Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>> effectSupplier) {
            this.projectileEffectBuilderSuppliers.add(new EffectBuilderInfo(effectSupplier, p -> true));
            return this;
        }

        public Builder withProjectileEffect(Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>> effectSupplier, Predicate<ProjectileLike> predicate) {
            this.projectileEffectBuilderSuppliers.add(new EffectBuilderInfo(effectSupplier, predicate));
            return this;
        }

        public Builder withProjectileBoundingBoxSize(float width, float height) {
            this.boundingBoxWidth = width;
            this.boundingBoxHeight = height;
            return this;
        }

        public Builder withEntityBuilderProvider(Supplier<EntityBuilder<?, ?>> entityBuilderSupplier) {
            this.entityBuilderSupplier = entityBuilderSupplier;
            return this;
        }

        public Builder withCraftingDuration(long duration, TimeUnit timeUnit) {
            this.craftingDuration = timeUnit.toMillis(duration);
            return this;
        }

        public Builder withDrawCooldownDuration(long duration, TimeUnit timeUnit) {
            this.drawCooldownDuration = timeUnit.toMillis(duration);
            return this;
        }

        public Builder withIdleCooldownDuration(long duration, TimeUnit timeUnit) {
            this.idleCooldownDuration = timeUnit.toMillis(duration);
            return this;
        }

        public Builder withInspectCooldownDuration(long duration, TimeUnit timeUnit) {
            this.inspectCooldownDuration = timeUnit.toMillis(duration);
            return this;
        }

        public Builder withPrepareIdleCooldownDuration(long duration, TimeUnit timeUnit) {
            this.prepareIdleCooldownDuration = (int)timeUnit.toMillis(duration);
            return this;
        }

        public Builder withPrepareThrowCooldownDuration(long duration, TimeUnit timeUnit) {
            this.prepareThrowCooldownDuration = timeUnit.toMillis(duration);
            return this;
        }

        public Builder withThrowCooldownDuration(long duration, TimeUnit timeUnit) {
            this.throwCooldownDuration = timeUnit.toMillis(duration);
            return this;
        }

        public Builder withCompleteThrowCooldownDuration(long duration, TimeUnit timeUnit) {
            this.completeThrowCooldownDuration = timeUnit.toMillis(duration);
            return this;
        }

        @Override
        public Builder withJsonObject(JsonObject obj, boolean isClientSide) {
            super.withJsonObject(obj, isClientSide);
            this.withName(JsonUtil.getJsonString(obj, "name"));
            this.withTradePrice(JsonUtil.getJsonFloat(obj, "tradePrice", Float.NaN), JsonUtil.getJsonInt(obj, "traceBundleQuantity", 1), JsonUtil.getJsonInt(obj, "tradeLevel", 0));
            this.withCraftingDuration(JsonUtil.getJsonInt(obj, "craftingDuration", 500), TimeUnit.MILLISECOND);
            this.withIdleCooldownDuration(JsonUtil.getJsonInt(obj, "idleCooldownDuration", 0), TimeUnit.MILLISECOND);
            this.withInspectCooldownDuration(JsonUtil.getJsonInt(obj, "inspectCooldownDuration", 0), TimeUnit.MILLISECOND);
            this.withPrepareIdleCooldownDuration(JsonUtil.getJsonInt(obj, "prepareIdleCooldownDuration", 0), TimeUnit.MILLISECOND);
            this.withPrepareThrowCooldownDuration(JsonUtil.getJsonInt(obj, "throwCooldownDuration", 1000), TimeUnit.MILLISECOND);
            this.withCompleteThrowCooldownDuration(JsonUtil.getJsonInt(obj, "completeThrowCooldownDuration", 1000), TimeUnit.MILLISECOND);
            this.withThrowCooldownDuration(JsonUtil.getJsonInt(obj, "throwCooldownDuration", 1000), TimeUnit.MILLISECOND);
            JsonObject projectileObj = obj.getAsJsonObject("projectile");
            if (projectileObj != null) {
                this.withProjectileMaxLifetime(JsonUtil.getJsonInt(obj, "maxLifetime", 5000), TimeUnit.MILLISECOND);
                this.withProjectileRicochet(JsonUtil.getJsonBoolean(obj, "ricochet", true));
                float size = JsonUtil.getJsonFloat(projectileObj, "boundingBoxSize", Float.NEGATIVE_INFINITY);
                if (size > 0.0f) {
                    this.withProjectileBoundingBoxSize(size, size);
                } else {
                    float width = JsonUtil.getJsonFloat(projectileObj, "width", 0.25f);
                    float height = JsonUtil.getJsonFloat(projectileObj, "height", 0.25f);
                    this.withProjectileBoundingBoxSize(width, height);
                    size = Math.max(width, height);
                }
                this.withProjectileGravity(JsonUtil.getJsonDouble(projectileObj, "gravity", 0.05));
                this.withProjectileInitialVelocity(JsonUtil.getJsonDouble(projectileObj, "initialVelocity", 0.05));
                JsonObject rendererObj = projectileObj.getAsJsonObject("renderer");
                if (rendererObj != null && isClientSide) {
                    String rendererType = JsonUtil.getJsonString(rendererObj, "type");
                    if (rendererType.toLowerCase().equals("sprite")) {
                        SpriteEntityRenderer.Builder rendererBuilder = new SpriteEntityRenderer.Builder();
                        rendererBuilder.withTexture(JsonUtil.getJsonString(rendererObj, "texture"));
                        rendererBuilder.withSize(JsonUtil.getJsonFloat(rendererObj, "size", size));
                        JsonObject spritesObj = rendererObj.getAsJsonObject("sprites");
                        if (spritesObj == null) {
                            throw new IllegalArgumentException("Element 'sprites' not defined in json: " + String.valueOf(rendererObj));
                        }
                        int rows = JsonUtil.getJsonInt(spritesObj, "rows", 1);
                        int columns = JsonUtil.getJsonInt(spritesObj, "columns", 1);
                        int fps = JsonUtil.getJsonInt(spritesObj, "fps", 60);
                        AbstractEffect.SpriteAnimationType spriteAnimationType = JsonUtil.getEnum(spritesObj, "type", AbstractEffect.SpriteAnimationType.class, AbstractEffect.SpriteAnimationType.LOOP, true);
                        rendererBuilder.withSprites(rows, columns, fps, spriteAnimationType);
                        rendererBuilder.withDepthTest(JsonUtil.getJsonBoolean(rendererObj, "depthTest", true));
                        rendererBuilder.withGlow(JsonUtil.getJsonBoolean(rendererObj, "glow", false));
                        rendererBuilder.withRotations(JsonUtil.getJsonFloat(rendererObj, "rotations", 0.0f));
                        this.withProjectileRenderer(() -> rendererBuilder);
                    } else if (rendererType.toLowerCase().equals("model")) {
                        this.withProjectileRenderer(() -> new ProjectileItemEntityRenderer.Builder());
                    }
                }
                for (String effectName : JsonUtil.getStrings(projectileObj, "effects")) {
                    Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>> supplier = () -> EffectRegistry.getEffectBuilderSupplier(effectName).get();
                    this.withProjectileEffect(supplier);
                }
            }
            return this;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public ThrowableItem build() {
            if (this.builtItem == null) {
                this.builtItem = new ThrowableItem(this.name, this);
            }
            return this.builtItem;
        }

        @Override
        public EntityBuilderProvider getEntityBuilderProvider() {
            return () -> this.getOrCreateEntityBuilder();
        }

        private EntityBuilder<?, ?> getOrCreateEntityBuilder() {
            if (this.entityBuilder == null) {
                this.entityBuilder = this.entityBuilderSupplier != null ? this.entityBuilderSupplier.get() : GenericThrowableProjectile.builder();
                this.entityBuilder.withItem(this::build);
                if (this.rendererBuilder != null) {
                    this.entityBuilder.withRenderer(this.rendererBuilder);
                }
                this.entityBuilder.withName(this.name);
                this.entityBuilder.withInitialVelocity(this.initialVelocity);
                this.entityBuilder.withGravity(this.gravity);
                this.entityBuilder.withMaxLifetime(this.maxLifetimeMillis);
                this.entityBuilder.withRicochet(this.isRicochet);
                for (EffectBuilderInfo ebi : this.projectileEffectBuilderSuppliers) {
                    this.entityBuilder.withEffect(ebi);
                }
            }
            return this.entityBuilder;
        }
    }
}


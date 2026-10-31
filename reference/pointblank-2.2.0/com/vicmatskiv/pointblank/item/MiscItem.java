/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  software.bernie.geckolib.animatable.GeoAnimatable
 *  software.bernie.geckolib.animatable.GeoItem
 *  software.bernie.geckolib.animatable.client.GeoRenderProvider
 *  software.bernie.geckolib.animatable.instance.AnimatableInstanceCache
 *  software.bernie.geckolib.animation.AnimatableManager$ControllerRegistrar
 *  software.bernie.geckolib.util.GeckoLibUtil
 */
package com.vicmatskiv.pointblank.item;

import com.google.gson.JsonObject;
import com.vicmatskiv.pointblank.Nameable;
import com.vicmatskiv.pointblank.client.render.DefaultModelRenderer;
import com.vicmatskiv.pointblank.item.ItemBuilder;
import com.vicmatskiv.pointblank.util.JsonUtil;
import com.vicmatskiv.pointblank.util.Tradeable;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.Item;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class MiscItem
extends Item
implements Nameable,
Tradeable {
    private String name;
    private float tradePrice;
    private int tradeBundleQuantity;
    private int tradeLevel;

    public MiscItem(String name, float tradePrice, int tradeBundleQuantity, int tradeLevel) {
        super(new Item.Properties());
        this.name = name;
        this.tradePrice = tradePrice;
        this.tradeLevel = tradeLevel;
        this.tradeBundleQuantity = tradeBundleQuantity;
    }

    @Override
    public String getName() {
        return this.name;
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

    private static class MiscModelItem
    extends MiscItem
    implements GeoItem {
        private Supplier<Object> rendererSupplier;
        private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this);

        public MiscModelItem(String name, Supplier<Object> rendererSupplier, float tradePrice, int tradeBundleQuantity, int tradeLevel) {
            super(name, tradePrice, tradeBundleQuantity, tradeLevel);
            this.rendererSupplier = rendererSupplier;
        }

        public void registerControllers(AnimatableManager.ControllerRegistrar registry) {
        }

        public AnimatableInstanceCache getAnimatableInstanceCache() {
            return this.cache;
        }

        public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
            consumer.accept(new GeoRenderProvider(){
                private BlockEntityWithoutLevelRenderer renderer;

                public BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
                    if (this.renderer == null) {
                        this.renderer = (BlockEntityWithoutLevelRenderer)rendererSupplier.get();
                    }
                    return this.renderer;
                }
            });
        }
    }

    public static class MiscItemBuilder
    extends ItemBuilder<MiscItemBuilder>
    implements Nameable {
        private static final float DEFAULT_PRICE = Float.NaN;
        private static final int DEFAULT_TRADE_LEVEL = 0;
        private static final int DEFAULT_TRADE_BUNDLE_QUANTITY = 1;
        private String name;
        private Supplier<Object> rendererBuilder;
        private float tradePrice = Float.NaN;
        private int tradeBundleQuantity = 1;
        private int tradeLevel = 0;

        public MiscItemBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public MiscItemBuilder withRenderer(Supplier<Object> rendererBuilder) {
            this.rendererBuilder = rendererBuilder;
            return this;
        }

        public MiscItemBuilder withTradePrice(double price, int tradeBundleQuantity, int tradeLevel) {
            this.tradePrice = (float)price;
            this.tradeLevel = tradeLevel;
            this.tradeBundleQuantity = tradeBundleQuantity;
            return this;
        }

        public MiscItemBuilder withTradePrice(double price, int tradeLevel) {
            return this.withTradePrice(price, 1, tradeLevel);
        }

        @Override
        public MiscItemBuilder withJsonObject(JsonObject obj, boolean isClientSide) {
            String rendererType;
            this.withName(JsonUtil.getJsonString(obj, "name"));
            this.withTradePrice(JsonUtil.getJsonFloat(obj, "tradePrice", Float.NaN), JsonUtil.getJsonInt(obj, "traceBundleQuantity", 1), JsonUtil.getJsonInt(obj, "tradeLevel", 0));
            JsonObject rendererObj = obj.getAsJsonObject("renderer");
            if (isClientSide && rendererObj != null && (rendererType = JsonUtil.getJsonString(rendererObj, "type")).toLowerCase().equals("model")) {
                this.withRenderer(() -> new DefaultModelRenderer(this.name));
            }
            return this;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public Item build() {
            if (this.rendererBuilder != null) {
                return new MiscModelItem(this.name, this.rendererBuilder, this.tradePrice, this.tradeBundleQuantity, this.tradeLevel);
            }
            return new MiscItem(this.name, this.tradePrice, this.tradeBundleQuantity, this.tradeLevel);
        }
    }
}


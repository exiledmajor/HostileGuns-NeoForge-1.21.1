/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.Item$TooltipContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  software.bernie.geckolib.animatable.GeoAnimatable
 *  software.bernie.geckolib.animatable.GeoItem
 *  software.bernie.geckolib.animatable.SingletonGeoAnimatable
 *  software.bernie.geckolib.animatable.client.GeoRenderProvider
 *  software.bernie.geckolib.animatable.instance.AnimatableInstanceCache
 *  software.bernie.geckolib.animation.AnimatableManager$ControllerRegistrar
 *  software.bernie.geckolib.util.GeckoLibUtil
 */
package com.vicmatskiv.pointblank.item;

import com.google.gson.JsonObject;
import com.vicmatskiv.pointblank.attachment.Attachment;
import com.vicmatskiv.pointblank.attachment.AttachmentCategory;
import com.vicmatskiv.pointblank.attachment.AttachmentHost;
import com.vicmatskiv.pointblank.attachment.Attachments;
import com.vicmatskiv.pointblank.client.render.AttachmentModelRenderer;
import com.vicmatskiv.pointblank.crafting.Craftable;
import com.vicmatskiv.pointblank.feature.Feature;
import com.vicmatskiv.pointblank.feature.FeatureBuilder;
import com.vicmatskiv.pointblank.feature.FeatureProvider;
import com.vicmatskiv.pointblank.feature.Features;
import com.vicmatskiv.pointblank.item.ItemBuilder;
import com.vicmatskiv.pointblank.registry.ItemRegistry;
import com.vicmatskiv.pointblank.util.JsonUtil;
import com.vicmatskiv.pointblank.util.MiscUtil;
import com.vicmatskiv.pointblank.util.TimeUnit;
import com.vicmatskiv.pointblank.util.Tradeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public final class AttachmentItem
extends Item
implements GeoItem,
Attachment,
AttachmentHost,
FeatureProvider,
Craftable,
Tradeable {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this);
    private String name;
    private AttachmentCategory category;
    private List<String> compatibleAttachmentGroups;
    private List<Supplier<Attachment>> compatibleAttachmentSuppliers;
    private List<Attachment> compatibleAttachments;
    private Set<String> groups;
    private Map<Class<? extends Feature>, Feature> features;
    private long craftingDuration;
    private float tradePrice;
    private int tradeBundleQuantity;
    private int tradeLevel;
    private List<Component> descriptionLines;
    public List<Supplier<Attachment>> defaultAttachmentSuppliers;

    public AttachmentItem() {
        super(new Item.Properties().stacksTo(1));
        SingletonGeoAnimatable.registerSyncedAnimatable((GeoAnimatable)this);
    }

    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider(){
            private AttachmentModelRenderer renderer;

            public BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
                if (this.renderer == null) {
                    this.renderer = new AttachmentModelRenderer(AttachmentItem.this.name);
                }
                return this.renderer;
            }
        });
    }

    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Collection<Feature> getFeatures() {
        return Collections.unmodifiableCollection(this.features.values());
    }

    @Override
    public List<Component> getDescriptionTooltipLines() {
        return this.descriptionLines;
    }

    public void appendHoverText(ItemStack p_41421_, Item.TooltipContext p_333372_, List<Component> p_41423_, TooltipFlag p_41424_) {
    }

    @Override
    public AttachmentCategory getCategory() {
        return this.category;
    }

    @Override
    public Set<String> getGroups() {
        return this.groups;
    }

    public int getDefaultMaxStackSize() {
        return 1;
    }

    @Override
    public Collection<Attachment> getCompatibleAttachments() {
        if (this.compatibleAttachments == null) {
            this.compatibleAttachments = new ArrayList<Attachment>();
            for (Supplier<Attachment> cas : this.compatibleAttachmentSuppliers) {
                this.compatibleAttachments.add(cas.get());
            }
            for (String group : this.compatibleAttachmentGroups) {
                List<Supplier<? extends Item>> groupAtttachments = ItemRegistry.ITEMS.getAttachmentsForGroup(group);
                for (Supplier<? extends Item> ga : groupAtttachments) {
                    Item item = ga.get();
                    if (!(item instanceof Attachment)) continue;
                    Attachment attachment = (Attachment)item;
                    this.compatibleAttachments.add(attachment);
                }
            }
        }
        return this.compatibleAttachments;
    }

    public Component getName(ItemStack itemStack) {
        return Component.translatable((String)this.getDescriptionId(itemStack));
    }

    @Override
    public <T extends Feature> T getFeature(Class<T> featureClass) {
        return (T)((Feature)featureClass.cast(this.features.get(featureClass)));
    }

    @Override
    public long getCraftingDuration() {
        return this.craftingDuration;
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

    public List<Attachment> getDefaultAttachments() {
        return this.defaultAttachmentSuppliers.stream().map(Supplier::get).toList();
    }

    public void inventoryTick(ItemStack itemStack, Level level, Entity entity, int itemSlot, boolean isSelected) {
        if (!level.isClientSide) {
            this.ensureItemStack(itemStack);
        }
    }

    public void ensureItemStack(ItemStack itemStack) {
        CompoundTag stateTag = MiscUtil.getTag(itemStack);
        if (stateTag == null) {
            stateTag = new CompoundTag();
            MiscUtil.setTag(itemStack, stateTag);
            Item item = itemStack.getItem();
            if (item instanceof AttachmentHost) {
                AttachmentHost attachmentHost = (AttachmentHost)item;
                Collection<Attachment> defaultAttachments = attachmentHost.getDefaultAttachments();
                for (Attachment attachment : defaultAttachments) {
                    Attachments.addAttachment(itemStack, new ItemStack((ItemLike)attachment), true);
                }
            }
        }
    }

    public static class Builder
    extends ItemBuilder<Builder> {
        private static final int DEFAULT_CRAFTING_DURATION = 750;
        private static final float DEFAULT_PRICE = Float.NaN;
        private static final int DEFAULT_TRADE_LEVEL = 0;
        private static final int DEFAULT_TRADE_BUNDLE_QUANTITY = 1;
        private String name;
        private AttachmentCategory category;
        private List<Supplier<Attachment>> compatibleAttachmentSuppliers = new ArrayList<Supplier<Attachment>>();
        private List<String> compatibleAttachmentGroups = new ArrayList<String>();
        private Set<String> groups = new HashSet<String>();
        private List<FeatureBuilder<?, ?>> featureBuilders = new ArrayList();
        private long craftingDuration = 750L;
        private float tradePrice = Float.NaN;
        private int tradeBundleQuantity = 1;
        private int tradeLevel = 0;
        private List<Component> descriptionLines = new ArrayList<Component>();
        private List<Supplier<Attachment>> defaultAttachments = new ArrayList<Supplier<Attachment>>();

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withCategory(String categoryName) {
            this.category = AttachmentCategory.fromString(categoryName);
            return this;
        }

        public Builder withCategory(AttachmentCategory category) {
            this.category = category;
            return this;
        }

        public Builder withDescription(Component description) {
            this.descriptionLines.add(description);
            return this;
        }

        public Builder withDescription(String description) {
            this.descriptionLines.add((Component)Component.translatable((String)description).withStyle(ChatFormatting.RED).withStyle(ChatFormatting.ITALIC));
            return this;
        }

        public Builder withFeature(FeatureBuilder<?, ?> featureBuilder) {
            this.featureBuilders.add(featureBuilder);
            return this;
        }

        @SafeVarargs
        public final Builder withCompatibleAttachment(Supplier<? extends Attachment> ... attachmentSuppliers) {
            for (Supplier<? extends Attachment> s : attachmentSuppliers) {
                this.compatibleAttachmentSuppliers.add(s::get);
            }
            return this;
        }

        public Builder withCompatibleAttachmentGroup(String ... groups) {
            this.compatibleAttachmentGroups.addAll(Set.of(groups));
            return this;
        }

        public Builder withGroup(String ... groups) {
            this.groups.addAll(Set.of(groups));
            return this;
        }

        public Builder withCraftingDuration(int duration, TimeUnit timeUnit) {
            this.craftingDuration = timeUnit.toMillis(duration);
            return this;
        }

        public Builder withTradePrice(double price, int tradeLevel) {
            this.tradePrice = (float)price;
            this.tradeLevel = tradeLevel;
            this.tradeBundleQuantity = 1;
            return this;
        }

        public Set<String> getGroups() {
            return Collections.unmodifiableSet(this.groups);
        }

        @SafeVarargs
        public final Builder withDefaultAttachment(Supplier<? extends Attachment> ... attachmentSuppliers) {
            for (Supplier<? extends Attachment> s : attachmentSuppliers) {
                this.defaultAttachments.add(s::get);
            }
            return this;
        }

        @Override
        public Builder withJsonObject(JsonObject obj, boolean isClientSide) {
            this.withName(JsonUtil.getJsonString(obj, "name"));
            this.withCategory(JsonUtil.getJsonString(obj, "category"));
            this.withTradePrice(JsonUtil.getJsonFloat(obj, "tradePrice", Float.NaN), JsonUtil.getJsonInt(obj, "tradeLevel", 0));
            this.withCraftingDuration(JsonUtil.getJsonInt(obj, "craftingDuration", 750), TimeUnit.MILLISECOND);
            List<String> groups = JsonUtil.getStrings(obj, "groups");
            this.groups.addAll(groups);
            for (JsonObject jsonObject : JsonUtil.getJsonObjects(obj, "features")) {
                FeatureBuilder<?, ?> featureBuilder = Features.fromJson(jsonObject);
                if (featureBuilder == null) continue;
                this.withFeature(featureBuilder);
            }
            List<String> compatibleAttachmentNames = JsonUtil.getStrings(obj, "compatibleAttachments");
            for (String compatibleAttachmentName : compatibleAttachmentNames) {
                Supplier ri = ItemRegistry.ITEMS.getDeferredRegisteredObject(compatibleAttachmentName);
                if (ri == null) continue;
                this.withCompatibleAttachment(() -> (Attachment)ri.get());
            }
            List<String> list = JsonUtil.getStrings(obj, "compatibleAttachmentGroups");
            this.compatibleAttachmentGroups.addAll(list);
            List<String> defaultAttachmentNames = JsonUtil.getStrings(obj, "defaultAttachments");
            for (String defaultAttachmentName : defaultAttachmentNames) {
                Supplier ri = ItemRegistry.ITEMS.getDeferredRegisteredObject(defaultAttachmentName);
                if (ri == null) continue;
                this.withDefaultAttachment(() -> (Attachment)ri.get());
            }
            return this;
        }

        @Override
        public AttachmentItem build() {
            Object feature;
            if (this.name == null) {
                throw new IllegalStateException("Attachment name not set");
            }
            if (this.category == null) {
                throw new IllegalStateException("Attachment category not set");
            }
            AttachmentItem attachment = new AttachmentItem();
            attachment.name = this.name;
            attachment.descriptionLines = Collections.unmodifiableList(this.descriptionLines);
            attachment.category = this.category;
            attachment.compatibleAttachmentSuppliers = Collections.unmodifiableList(this.compatibleAttachmentSuppliers);
            attachment.compatibleAttachmentGroups = Collections.unmodifiableList(this.compatibleAttachmentGroups);
            attachment.groups = Collections.unmodifiableSet(this.groups);
            HashMap features = new HashMap();
            for (FeatureBuilder<?, ?> featureBuilder : this.category.getDefaultFeatures()) {
                feature = featureBuilder.build(attachment);
                features.put(feature.getClass(), feature);
            }
            for (FeatureBuilder<?, ?> featureBuilder : this.featureBuilders) {
                feature = featureBuilder.build(attachment);
                features.put(feature.getClass(), feature);
            }
            attachment.features = Collections.unmodifiableMap(features);
            attachment.craftingDuration = this.craftingDuration;
            attachment.tradePrice = this.tradePrice;
            attachment.tradeBundleQuantity = this.tradeBundleQuantity;
            attachment.tradeLevel = this.tradeLevel;
            attachment.defaultAttachmentSuppliers = Collections.unmodifiableList(this.defaultAttachments);
            return attachment;
        }

        @Override
        public String getName() {
            return this.name;
        }
    }
}


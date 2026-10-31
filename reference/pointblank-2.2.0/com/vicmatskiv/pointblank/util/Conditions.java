/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 */
package com.vicmatskiv.pointblank.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vicmatskiv.pointblank.attachment.Attachment;
import com.vicmatskiv.pointblank.attachment.AttachmentCategory;
import com.vicmatskiv.pointblank.attachment.Attachments;
import com.vicmatskiv.pointblank.feature.ConditionContext;
import com.vicmatskiv.pointblank.item.FireModeInstance;
import com.vicmatskiv.pointblank.item.GunItem;
import com.vicmatskiv.pointblank.util.Expression;
import com.vicmatskiv.pointblank.util.JsonUtil;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.SortedMap;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class Conditions {
    public static final Predicate<ConditionContext> RANDOM_PICK = ctx -> ctx.randomSample2() == ctx.randomSample1();

    public static final Predicate<ConditionContext> isGunOnGround() {
        return ctx -> ctx.itemDisplayContext() == ItemDisplayContext.GROUND;
    }

    public static final Predicate<ConditionContext> isGunInHands() {
        return ctx -> ctx.itemDisplayContext() != ItemDisplayContext.GROUND;
    }

    public static final Predicate<ConditionContext> selectedFireMode(String fireModeName) {
        return ctx -> {
            if (ctx.rootStack() == null) {
                return false;
            }
            FireModeInstance fireModeInstance = GunItem.getFireModeInstance(ctx.rootStack());
            if (fireModeInstance != null) {
                return Objects.equals(fireModeInstance.getName().toUpperCase(Locale.ROOT), fireModeName.toUpperCase(Locale.ROOT));
            }
            return false;
        };
    }

    public static final Predicate<ConditionContext> unselectedFireMode(String fireModeName) {
        return Conditions.selectedFireMode(fireModeName).negate();
    }

    public static final Predicate<ConditionContext> isUsingDefaultMuzzle() {
        return ctx -> {
            if (ctx.rootStack() == null) {
                return false;
            }
            FireModeInstance fireModeInstance = GunItem.getFireModeInstance(ctx.rootStack());
            return fireModeInstance != null ? fireModeInstance.isUsingDefaultMuzzle() : true;
        };
    }

    public static final Predicate<ConditionContext> hasAmmoCount(int ammoCount) {
        return ctx -> ammoCount == ctx.gunClientState().getAmmoCount(GunItem.getFireModeInstance(ctx.rootStack()));
    }

    public static final Predicate<ConditionContext> onReloadIteration(int index) {
        return ctx -> index == ctx.gunClientState().getReloadIterationIndex();
    }

    public static final Predicate<ConditionContext> onEmptyReload() {
        return ctx -> ctx.gunClientState().getReloadIterationIndex() == 0;
    }

    public static final Predicate<ConditionContext> onNonEmptyReload() {
        return ctx -> ctx.gunClientState().getReloadIterationIndex() > 0;
    }

    public static final Predicate<ConditionContext> beforePreparingReload() {
        return Conditions.onReloadIteration(-1);
    }

    public static final Predicate<ConditionContext> afterPreparingReload() {
        return ctx -> ctx.gunClientState().getReloadIterationIndex() >= 0;
    }

    public static final Predicate<ConditionContext> hasAttachment(Supplier<? extends Attachment> attachmentSupplier) {
        return ctx -> {
            NavigableMap<String, ItemStack> attachments = Attachments.getAttachments(ctx.currentItemStack(), false);
            return attachments.get("//" + ((Attachment)attachmentSupplier.get()).getName()) != null;
        };
    }

    public static final Predicate<ConditionContext> doesNotHaveAttachment(Supplier<? extends Attachment> attachmentSupplier) {
        return Conditions.hasAttachment(attachmentSupplier).negate();
    }

    public static final Predicate<ConditionContext> hasAttachment(String attachmentName) {
        return ctx -> {
            NavigableMap<String, ItemStack> attachments = Attachments.getAttachments(ctx.currentItemStack(), false);
            return attachments.get("//" + attachmentName) != null;
        };
    }

    public static final Predicate<ConditionContext> doesNotHaveAttachment(String attachmentName) {
        return Conditions.hasAttachment(attachmentName).negate();
    }

    public static final Predicate<ConditionContext> hasAttachmentGroup(String attachmentGroup) {
        return ctx -> Attachments.getAttachmentGroups(ctx.currentItemStack()).containsKey(attachmentGroup);
    }

    public static final Predicate<ConditionContext> doesNotHaveAttachmentGroup(String attachmentGroup) {
        return Conditions.hasAttachmentGroup(attachmentGroup).negate();
    }

    public static final Predicate<ConditionContext> hasAttachmentAtPathPrefix(String prefix) {
        return ctx -> {
            NavigableMap<String, ItemStack> attachments = Attachments.getAttachments(ctx.currentItemStack(), true);
            String key = attachments.ceilingKey(prefix);
            return key != null && key.startsWith(prefix);
        };
    }

    public static final Predicate<ConditionContext> doesNotHaveAttachmentAtPathPrefix(String prefix) {
        return Conditions.hasAttachmentAtPathPrefix(prefix).negate();
    }

    public static final Predicate<ConditionContext> hasAttachmentInCategory(AttachmentCategory category) {
        return ctx -> {
            NavigableMap<String, ItemStack> attachments = Attachments.getAttachmentsForCategory(ctx.currentItemStack(), category);
            return !attachments.isEmpty();
        };
    }

    public static final Predicate<ConditionContext> doesNotHaveAttachmentInCategory(AttachmentCategory category) {
        return Conditions.hasAttachmentInCategory(category).negate();
    }

    public static final Predicate<ConditionContext> hasAttachmentInCategoryAtPathPrefix(AttachmentCategory category, String prefix) {
        return ctx -> {
            NavigableMap<String, ItemStack> attachments = Attachments.getAttachments(ctx.currentItemStack(), true);
            SortedMap<String, ItemStack> tm = attachments.tailMap(prefix);
            boolean result = false;
            for (Map.Entry<String, ItemStack> e : tm.entrySet()) {
                Attachment a;
                Item patt0$temp;
                if (!e.getKey().startsWith(prefix) || !((patt0$temp = e.getValue().getItem()) instanceof Attachment) || (a = (Attachment)patt0$temp).getCategory() != category) continue;
                result = true;
            }
            return result;
        };
    }

    public static final Predicate<ConditionContext> doesNotHaveAttachmentInCategoryAtPathPrefix(AttachmentCategory category, String prefix) {
        return Conditions.hasAttachmentInCategoryAtPathPrefix(category, prefix).negate();
    }

    public static Predicate<ConditionContext> fromJson(JsonElement element) {
        if (element == null) {
            return ctx -> true;
        }
        if (element.isJsonPrimitive()) {
            String expr = element.getAsString();
            if (expr.trim().equalsIgnoreCase("random")) {
                return RANDOM_PICK;
            }
            return Expression.compile(expr);
        }
        JsonObject obj = element.getAsJsonObject();
        if (obj.has("anyOf")) {
            JsonArray conditions = obj.getAsJsonArray("anyOf");
            Predicate<ConditionContext> predicate = ctx -> false;
            for (JsonElement condition : conditions) {
                predicate = predicate.or(Conditions.fromJson((JsonElement)condition.getAsJsonObject()));
            }
            return predicate;
        }
        if (obj.has("allOf")) {
            JsonArray conditions = obj.getAsJsonArray("allOf");
            Predicate<ConditionContext> predicate = ctx -> true;
            for (JsonElement condition : conditions) {
                predicate = predicate.and(Conditions.fromJson(condition));
            }
            return predicate;
        }
        if (obj.has("not")) {
            return Conditions.fromJson((JsonElement)obj.getAsJsonObject("not")).negate();
        }
        if (obj.has("hasAttachment")) {
            return Conditions.hasAttachment(JsonUtil.getJsonString(obj, "hasAttachment"));
        }
        if (obj.has("doesNotHaveAttachment")) {
            return Conditions.doesNotHaveAttachment(JsonUtil.getJsonString(obj, "doesNotHaveAttachment"));
        }
        if (obj.has("hasAttachmentAtPathPrefix")) {
            return Conditions.hasAttachmentAtPathPrefix(JsonUtil.getJsonString(obj, "hasAttachmentAtPathPrefix"));
        }
        if (obj.has("doesNotHaveAttachmentAtPathPrefix")) {
            return Conditions.doesNotHaveAttachmentAtPathPrefix(JsonUtil.getJsonString(obj, "doesNotHaveAttachmentAtPathPrefix"));
        }
        if (obj.has("hasAttachmentGroup")) {
            return Conditions.hasAttachmentGroup(JsonUtil.getJsonString(obj, "hasAttachmentGroup"));
        }
        if (obj.has("doesNotHaveAttachmentGroup")) {
            return Conditions.doesNotHaveAttachmentGroup(JsonUtil.getJsonString(obj, "doesNotHaveAttachmentGroup"));
        }
        if (obj.has("hasAttachmentInCategory")) {
            return Conditions.hasAttachmentInCategory(AttachmentCategory.fromString(JsonUtil.getJsonString(obj, "hasAttachmentInCategory")));
        }
        if (obj.has("doesNotHaveAttachmentInCategory")) {
            return Conditions.doesNotHaveAttachmentInCategory(AttachmentCategory.fromString(JsonUtil.getJsonString(obj, "doesNotHaveAttachmentInCategory")));
        }
        if (obj.has("selectedFireMode")) {
            return Conditions.selectedFireMode(JsonUtil.getJsonString(obj, "selectedFireMode"));
        }
        if (obj.has("unselectedFireMode")) {
            return Conditions.unselectedFireMode(JsonUtil.getJsonString(obj, "unselectedFireMode"));
        }
        if (obj.has("isGunInHands")) {
            boolean isGunInHands = JsonUtil.getJsonBoolean(obj, "isGunInHands", true);
            return isGunInHands ? Conditions.isGunInHands() : Conditions.isGunOnGround();
        }
        if (obj.has("isUsingDefaultMuzzle")) {
            boolean isUsingDefaultMuzzle = JsonUtil.getJsonBoolean(obj, "isUsingDefaultMuzzle", true);
            return isUsingDefaultMuzzle ? Conditions.isUsingDefaultMuzzle() : Conditions.isUsingDefaultMuzzle().negate();
        }
        if (obj.has("onEmptyReload")) {
            boolean value = JsonUtil.getJsonBoolean(obj, "onEmptyReload", true);
            return value ? Conditions.onEmptyReload() : Conditions.onNonEmptyReload();
        }
        if (obj.has("ammoCount")) {
            int value = JsonUtil.getJsonInt(obj, "ammoCount");
            return Conditions.hasAmmoCount(value);
        }
        if (obj.has("onReloadIteration")) {
            int value = JsonUtil.getJsonInt(obj, "onReloadIteration");
            return Conditions.onReloadIteration(value);
        }
        if (obj.has("beforePreparingReload")) {
            boolean value = JsonUtil.getJsonBoolean(obj, "beforePreparingReload", true);
            return value ? Conditions.beforePreparingReload() : Conditions.beforePreparingReload().negate();
        }
        if (obj.has("afterPreparingReload")) {
            boolean value = JsonUtil.getJsonBoolean(obj, "afterPreparingReload", true);
            return value ? Conditions.afterPreparingReload() : Conditions.afterPreparingReload().negate();
        }
        throw new IllegalArgumentException("Unknown condition in JSON: " + String.valueOf(obj));
    }
}


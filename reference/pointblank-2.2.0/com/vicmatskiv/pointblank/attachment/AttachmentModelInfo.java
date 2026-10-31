/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.math.Axis
 *  net.minecraft.Util
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.phys.Vec3
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 *  software.bernie.geckolib.cache.GeckoLibCache
 *  software.bernie.geckolib.cache.object.BakedGeoModel
 *  software.bernie.geckolib.cache.object.GeoBone
 *  software.bernie.geckolib.cache.object.GeoCube
 *  software.bernie.geckolib.cache.object.GeoQuad
 *  software.bernie.geckolib.cache.object.GeoVertex
 *  software.bernie.geckolib.util.RenderUtil
 */
package com.vicmatskiv.pointblank.attachment;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import com.vicmatskiv.pointblank.attachment.Attachment;
import com.vicmatskiv.pointblank.attachment.AttachmentHost;
import com.vicmatskiv.pointblank.attachment.Attachments;
import com.vicmatskiv.pointblank.client.ClientSystem;
import com.vicmatskiv.pointblank.item.GunItem;
import com.vicmatskiv.pointblank.util.LRUCache;
import com.vicmatskiv.pointblank.util.MiscUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import software.bernie.geckolib.cache.GeckoLibCache;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.cache.object.GeoQuad;
import software.bernie.geckolib.cache.object.GeoVertex;
import software.bernie.geckolib.util.RenderUtil;

public record AttachmentModelInfo(BakedGeoModel baseModel, GeoBone baseBone, BakedGeoModel attachmentModel, GeoBone attachmentBone) {
    private static final Logger LOGGER = LogManager.getLogger((String)"pointblank");
    private static final String PREFIX_CONNECTOR_BASE = "_cb_";
    private static final String PREFIX_CONNECTOR_ATTACHMENT = "_ca_";
    private static final LRUCache<Tag, NavigableMap<String, Pair<ItemStack, Matrix4f>>> stackTagPosisionCache = new LRUCache(100);
    public static final Function<ModelBoneKey, Optional<Pair<Matrix4f, Matrix4f>>> modelBonePositions = Util.memoize(AttachmentModelInfo::getRefBoneMatrices);
    private static final BiFunction<BakedGeoModel, String, Optional<? extends GeoBone>> modelBones = Util.memoize((model, boneName) -> model.getBone(boneName));
    public static final BiFunction<AttachmentHost, Attachment, Optional<AttachmentModelInfo>> attachmentInfos = ClientSystem.getInstance().createReloadableMemoize(AttachmentModelInfo::getAttachmentModelInfo);
    private static final Function<BakedGeoModel, List<? extends GeoBone>> modelABBones = Util.memoize(model -> AttachmentModelInfo.findBones(model, b -> b.getName().startsWith(PREFIX_CONNECTOR_ATTACHMENT)));

    private static Optional<AttachmentModelInfo> getAttachmentModelInfo(AttachmentHost attachmentHost, Attachment attachment) {
        AttachmentModelInfo attachmentInfo = null;
        BakedGeoModel attachmentModel = AttachmentModelInfo.getModel(attachment.getName());
        if (attachmentModel != null) {
            BakedGeoModel baseModel = AttachmentModelInfo.getModel(attachmentHost.getName());
            for (GeoBone geoBone : modelABBones.apply(attachmentModel)) {
                String suffix = geoBone.getName().substring(PREFIX_CONNECTOR_ATTACHMENT.length());
                GeoBone baseBone = modelBones.apply(baseModel, PREFIX_CONNECTOR_BASE + suffix).orElse(null);
                if (baseBone == null) continue;
                attachmentInfo = new AttachmentModelInfo(baseModel, baseBone, attachmentModel, geoBone);
                break;
            }
        }
        return Optional.ofNullable(attachmentInfo);
    }

    private static List<? extends GeoBone> findBones(BakedGeoModel model, Predicate<GeoBone> predicate) {
        if (model == null) {
            return null;
        }
        return AttachmentModelInfo.findBonesRecursively(() -> ((BakedGeoModel)model).topLevelBones(), predicate, new ArrayList<GeoBone>());
    }

    private static List<? extends GeoBone> findBonesRecursively(Supplier<List<? extends GeoBone>> boneListSupplier, Predicate<GeoBone> predicate, List<GeoBone> results) {
        for (GeoBone geoBone : boneListSupplier.get()) {
            if (predicate.test(geoBone)) {
                results.add(geoBone);
            }
            AttachmentModelInfo.findBonesRecursively(() -> ((GeoBone)geoBone).getChildBones(), predicate, results);
        }
        return results;
    }

    public static BakedGeoModel getModel(String itemName) {
        ResourceLocation attachmentModelResource = ResourceLocation.fromNamespaceAndPath((String)"pointblank", (String)("geo/item/" + itemName + ".geo.json"));
        return (BakedGeoModel)GeckoLibCache.getBakedModels().get(attachmentModelResource);
    }

    public static Optional<Pair<Matrix4f, Matrix4f>> getRefBoneMatrices(ModelBoneKey key) {
        List cubes;
        Pair result = null;
        GeoBone bone = key.model.getBone(key.boneName).orElse(null);
        if (bone != null && (cubes = bone.getCubes()) != null && !cubes.isEmpty()) {
            GeoCube cube = (GeoCube)cubes.get(0);
            GeoQuad upQuad = cube.quads()[4];
            GeoQuad downQuad = cube.quads()[5];
            GeoVertex[] upVertices = upQuad.vertices();
            GeoVertex[] downVertices = downQuad.vertices();
            GeoVertex upVertex = upVertices[0];
            GeoVertex downVertex = downVertices[0];
            Vector3f position = new Vector3f((Vector3fc)upVertex.position()).add((Vector3fc)downVertex.position()).mul(0.5f);
            position.mul(key.scale);
            Matrix4f m = new Matrix4f().translate((Vector3fc)position);
            Vec3 cubeRotation = cube.rotation();
            if (cubeRotation.z != 0.0) {
                m.rotate((Quaternionfc)Axis.ZP.rotation((float)cubeRotation.z));
            }
            if (cubeRotation.y != 0.0) {
                m.rotate((Quaternionfc)Axis.YP.rotation((float)cubeRotation.y));
            }
            if (cubeRotation.x != 0.0) {
                m.rotate((Quaternionfc)Axis.XP.rotation((float)cubeRotation.x));
            }
            result = Pair.of((Object)m, (Object)new Matrix4f((Matrix4fc)m).invert());
        }
        return Optional.ofNullable(result);
    }

    public static NavigableMap<String, Pair<ItemStack, Matrix4f>> findInverseBoneMatrices(ItemStack baseStack, String boneName, float scale) {
        GunItem gunItem;
        BakedGeoModel gunItemModel;
        Item item;
        if (baseStack != null && (item = baseStack.getItem()) instanceof GunItem && (gunItemModel = AttachmentModelInfo.getModel((gunItem = (GunItem)item).getName())) != null) {
            return AttachmentModelInfo.findInverseBoneMatrices(baseStack, gunItemModel, boneName, scale);
        }
        return Collections.emptyNavigableMap();
    }

    private static NavigableMap<String, Pair<ItemStack, Matrix4f>> findInverseBoneMatrices(ItemStack baseStack, BakedGeoModel baseModel, String boneName, float scale) {
        CompoundTag tag = MiscUtil.getTag(baseStack);
        TreeMap<String, Pair<ItemStack, Matrix4f>> results = new TreeMap<String, Pair<ItemStack, Matrix4f>>();
        if (tag == null) {
            return AttachmentModelInfo.findBonePositions(baseModel, "/", baseStack, boneName, scale, new PoseStack(), results);
        }
        Tag attachmentsTag = tag.get("as");
        if (attachmentsTag == null) {
            attachmentsTag = tag;
        }
        CompoundTag keyTag = new CompoundTag();
        keyTag.put("a", attachmentsTag);
        Tag selectedAttachments = tag.get("sa");
        if (selectedAttachments != null) {
            keyTag.put("sa", tag.get("sa"));
        }
        keyTag.putString("b", boneName);
        keyTag.putFloat("s", scale);
        keyTag.putLong("m", tag.getLong("mid"));
        keyTag.putLong("l", tag.getLong("lid"));
        return stackTagPosisionCache.computeIfAbsent((Tag)keyTag, t -> AttachmentModelInfo.findBonePositions(baseModel, "/", baseStack, boneName, scale, new PoseStack(), results));
    }

    private static NavigableMap<String, Pair<ItemStack, Matrix4f>> findBonePositions(BakedGeoModel baseModel, String componentName, ItemStack baseStack, String boneName, float scale, PoseStack poseStack, NavigableMap<String, Pair<ItemStack, Matrix4f>> results) {
        Item item;
        LOGGER.debug("Computing inverse bone position for component {}", (Object)componentName);
        Pair abPos = modelBonePositions.apply(new ModelBoneKey(baseModel, boneName, scale)).orElse(null);
        if (abPos != null) {
            poseStack.pushPose();
            poseStack.mulPose((Matrix4f)abPos.getFirst());
            results.put(componentName, (Pair<ItemStack, Matrix4f>)Pair.of((Object)baseStack, (Object)new Matrix4f((Matrix4fc)poseStack.last().pose()).invert()));
            poseStack.popPose();
        }
        if (!((item = baseStack.getItem()) instanceof AttachmentHost)) {
            return results;
        }
        AttachmentHost attachmentHost = (AttachmentHost)item;
        Collection attachmentStacks = Attachments.getAttachments(MiscUtil.getTag(baseStack), "/", false, new TreeMap<String, ItemStack>()).values();
        for (ItemStack attachmentStack : attachmentStacks) {
            Attachment attachment;
            AttachmentModelInfo attachmentInfo;
            Item item2 = attachmentStack.getItem();
            if (!(item2 instanceof Attachment) || (attachmentInfo = (AttachmentModelInfo)attachmentInfos.apply(attachmentHost, attachment = (Attachment)item2).orElse(null)) == null) continue;
            Pair basePos = modelBonePositions.apply(new ModelBoneKey(attachmentInfo.baseModel(), attachmentInfo.baseBone().getName(), scale)).orElse(null);
            BakedGeoModel attachmentModel = attachmentInfo.attachmentModel();
            Pair attachmentPos = modelBonePositions.apply(new ModelBoneKey(attachmentModel, attachmentInfo.attachmentBone().getName(), scale)).orElse(null);
            if (basePos == null || attachmentPos == null) continue;
            poseStack.pushPose();
            poseStack.mulPose((Matrix4f)basePos.getFirst());
            poseStack.mulPose((Matrix4f)attachmentPos.getSecond());
            AttachmentModelInfo.findBonePositions(attachmentModel, componentName + "/" + attachment.getName(), attachmentStack, boneName, scale, poseStack, results);
            poseStack.popPose();
        }
        return results;
    }

    public static List<GeoBone> getParentBoneHierarchy(GeoBone bone) {
        LinkedList<GeoBone> result = new LinkedList<GeoBone>();
        for (GeoBone current = bone; current != null; current = current.getParent()) {
            result.addFirst(current);
        }
        return result;
    }

    public static void preparePoseStackForBoneInHierarchy(PoseStack poseStack, GeoBone bone) {
        List<GeoBone> boneHierarchy = AttachmentModelInfo.getParentBoneHierarchy(bone);
        for (GeoBone parentBone : boneHierarchy) {
            RenderUtil.prepMatrixForBone((PoseStack)poseStack, (GeoBone)parentBone);
        }
    }

    public static class ModelBoneKey {
        BakedGeoModel model;
        String boneName;
        float scale;

        public ModelBoneKey(BakedGeoModel model, String boneName, float scale) {
            this.model = model;
            this.boneName = boneName;
            this.scale = scale;
        }

        public int hashCode() {
            return Objects.hash(this.boneName, this.model, Float.valueOf(this.scale));
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (this.getClass() != obj.getClass()) {
                return false;
            }
            ModelBoneKey other = (ModelBoneKey)obj;
            return Objects.equals(this.boneName, other.boneName) && Objects.equals(this.model, other.model) && Float.floatToIntBits(this.scale) == Float.floatToIntBits(other.scale);
        }
    }
}


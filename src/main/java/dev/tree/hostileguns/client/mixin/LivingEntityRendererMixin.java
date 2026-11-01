package dev.tree.hostileguns.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.tree.hostileguns.PointBlankReflection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void hostileguns$renderCyberwareGun(LivingEntity entity, float entityYaw, float partialTick,
                                                 PoseStack poseStack, MultiBufferSource buffer,
                                                 int packedLight, CallbackInfo ci) {
        if (!entity.isAlive() || !isCyberneticsGunMob(entity) || !PointBlankReflection.isGun(entity.getMainHandItem().getItem())) return;

        ItemStack stack = entity.getMainHandItem();
        Minecraft mc = Minecraft.getInstance();

        poseStack.pushPose();
        // Create: Cybernetics' cyberzombie renderer does not expose the normal vanilla
        // held-item layer. Mount the Point Blank renderer at the right-hand position.
        var itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String path = itemId == null ? "" : itemId.getPath().toLowerCase(java.util.Locale.ROOT);
        boolean sidearm = path.contains("pistol") || path.contains("handgun") || path.contains("revolver");
        // Humanoid right arms pivot on the negative-X side of the model. The previous
        // positive-X offset placed the gun across the torso/near the opposite hand.
        poseStack.translate(-0.30D, sidearm ? 1.22D : 1.16D, 0.04D);
        mc.getItemRenderer().renderStatic(entity, stack, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                false, poseStack, buffer, entity.level(), packedLight, 0, entity.getId());
        poseStack.popPose();
    }

    private static boolean isCyberneticsGunMob(LivingEntity entity) {
        var id = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        if (id == null || !"createcybernetics".equals(id.getNamespace())) return false;
        return switch (id.getPath()) {
            case "cyberzombie" -> true;
            default -> false;
        };
    }
}

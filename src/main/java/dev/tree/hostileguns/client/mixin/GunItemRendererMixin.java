package dev.tree.hostileguns.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets="com.vicmatskiv.pointblank.client.render.GunItemRenderer",remap=false)
public abstract class GunItemRendererMixin {
    @Inject(method="getPlayer",at=@At("RETURN"),cancellable=true,remap=false,require=0)
    private void hostileguns$substituteNullPlayer(ItemDisplayContext mode,CallbackInfoReturnable<Player> cir){
        // Point Blank's renderer expects a Player even for third-person item rendering.
        // Cybernetics mobs are not players, so without this fallback their gun model can
        // disappear even though the server-side main-hand stack is correct.
        if (cir.getReturnValue() == null && Minecraft.getInstance().player != null) {
            cir.setReturnValue(Minecraft.getInstance().player);
        }
    }
}

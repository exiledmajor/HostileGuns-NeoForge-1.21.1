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
        if(cir.getReturnValue()==null && Minecraft.getInstance().player!=null)
            cir.setReturnValue(Minecraft.getInstance().player);
    }
}

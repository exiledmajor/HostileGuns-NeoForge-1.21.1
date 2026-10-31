package dev.tree.mainframeprogression;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class CyberwareRepairKitItem extends Item {
    public CyberwareRepairKitItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) return InteractionResultHolder.success(stack);

        int repaired = ChromewareBridge.repairAll(player, 0.25D);
        if (repaired > 0) {
            if (!player.getAbilities().instabuild) stack.shrink(1);
            player.displayClientMessage(Component.translatable(
                    "message.mainframe_progression.repair_success", repaired), true);
        } else {
            player.displayClientMessage(Component.translatable(
                    "message.mainframe_progression.repair_none"), true);
        }
        return InteractionResultHolder.consume(stack);
    }
}

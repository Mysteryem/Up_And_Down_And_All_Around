package uk.co.mysterymayhem.gravitymod;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import uk.co.mysterymayhem.gravitymod.api.EnumGravityDirection;

/**
 * Created by Mysteryem on 2016-08-07.
 */
public class DebugHelperListener {

    @SubscribeEvent
    public void onItemUse(PlayerInteractEvent event) {
        //TODO: Remove?
        if (event.getSide() == Side.SERVER && event.getHand() == EnumHand.MAIN_HAND && (event instanceof PlayerInteractEvent.RightClickBlock || event instanceof PlayerInteractEvent.RightClickItem)) {
            ItemStack itemStack = event.getItemStack();
            if (itemStack != null) {
                Item item = itemStack.getItem();
                if (item.equals(Items.ARROW)) {
                    GravityMod.proxy.getGravityManager().doGravityTransition(EnumGravityDirection.DOWN, (EntityPlayerMP) event.getEntityPlayer());
                } else if (item.equals(Items.BLAZE_ROD)) {
                    GravityMod.proxy.getGravityManager().doGravityTransition(EnumGravityDirection.UP, (EntityPlayerMP) event.getEntityPlayer());
                } else if (item.equals(Items.BRICK)) {
                    GravityMod.proxy.getGravityManager().doGravityTransition(EnumGravityDirection.NORTH, (EntityPlayerMP) event.getEntityPlayer());
                } else if (item.equals(Items.BOOK)) {
                    GravityMod.proxy.getGravityManager().doGravityTransition(EnumGravityDirection.EAST, (EntityPlayerMP) event.getEntityPlayer());
                } else if (item.equals(Items.CLOCK)) {
                    GravityMod.proxy.getGravityManager().doGravityTransition(EnumGravityDirection.SOUTH, (EntityPlayerMP) event.getEntityPlayer());
                } else if (item.equals(Items.DIAMOND)) {
                    GravityMod.proxy.getGravityManager().doGravityTransition(EnumGravityDirection.WEST, (EntityPlayerMP) event.getEntityPlayer());
                }
            }
        }
    }
}

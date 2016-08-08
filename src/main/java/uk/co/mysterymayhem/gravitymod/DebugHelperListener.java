package uk.co.mysterymayhem.gravitymod;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import uk.co.mysterymayhem.gravitymod.events.GravityTransitionEvent;

/**
 * Created by Mysteryem on 2016-08-07.
 */
public class DebugHelperListener {

    @SubscribeEvent
    public void onItemUse(PlayerInteractEvent event) {
        if (event.getEntityPlayer().worldObj.isRemote) {
            System.out.println("PlayerInteractEvent Side: " + event.getSide());
        }
        if (event.getSide() != Side.SERVER) {
            return;
        }
        //TODO: Remove?
        if (event.getEntityPlayer().getRidingEntity() != null) {
            return;
        }
        ItemStack itemStack = event.getItemStack();
        if (itemStack != null) {
            Item item = itemStack.getItem();
            if (item.equals(Items.ARROW)) {
                if (!GravityMod.proxy.gravityManagerServer.isPlayerUpsideDown(event.getEntityPlayer())) {
                    MinecraftForge.EVENT_BUS.post(new GravityTransitionEvent (
                            true,
                            event.getEntityPlayer(),
                            event.getSide()
                    ));
                }
            }
            else if (item.equals(Items.BLAZE_ROD)) {
                if (GravityMod.proxy.gravityManagerServer.isPlayerUpsideDown(event.getEntityPlayer())) {
                    MinecraftForge.EVENT_BUS.post(new GravityTransitionEvent (
                            false,
                            event.getEntityPlayer(),
                            event.getSide()
                    ));
                }
            }
        }
    }
}

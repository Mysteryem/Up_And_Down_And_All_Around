package uk.co.mysterymayhem.gravitymod.events;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import uk.co.mysterymayhem.gravitymod.asm.Hooks;

import java.util.HashSet;

/**
 * This class is set up such that it can be used with Forge's event system, but I'm not sure there's reason to do so, so
 * it currently doesn't
 * Created by Mysteryem on 2016-10-22.
 */
public class ItemStackRightClickEvent extends PlayerEvent {

    public enum Type {
        BLOCK,
        // Not used
        ENTITY,
        NOTHING
    }

    private static final HashSet<Item> ITEMS_THAT_USE_RELATIVE_MOTION = new HashSet<>();

    public static boolean addRelativeMotionItem(Item itemInstance) {
        if (itemInstance == null) {
            return false;
        }
        return ITEMS_THAT_USE_RELATIVE_MOTION.add(itemInstance);
    }

    public static boolean addRelativeMotionItem(ResourceLocation itemResource) {
        return addRelativeMotionItem(Item.REGISTRY.getObject(itemResource));
    }

    public static boolean addRelativeMotionItem(String modId, String itemName) {
//        return addRelativeMotionItem(Item.REGISTRY.getObject(new ResourceLocation(modId, itemName)));
        return addRelativeMotionItem(new ResourceLocation(modId, itemName));
    }

    public static boolean addRelativeMotionItem(String modAndItemNameString) {
        String[] split = modAndItemNameString.split(":");
        if (split.length != 2) {
            return false;
        }
        return addRelativeMotionItem(split[0], split[1]);
    }

    public static void processEvent(ItemStackRightClickEvent event) {
        ItemStack stack = event.stack;
        if (stack != null) {
            Item item = stack.getItem();
            if (item != null) {
                if (!ITEMS_THAT_USE_RELATIVE_MOTION.contains(item)) {
                    switch(event.phase) {
                        case START:
                            Hooks.makeMotionAbsolute(event.getEntityPlayer());
                            break;
                        case END:
                            Hooks.popMotionStack(event.getEntityPlayer());
                            break;
                        default://null
                            break;
                    }
                }
            }
        }
    }

    public final Side side;
    public final boolean isValid;
    public final ItemStack stack;
    public final TickEvent.Phase phase;
    public final Type type;

    public ItemStackRightClickEvent (ItemStack itemStack, EntityPlayer player, TickEvent.Phase phase, Type type) {
        super(player);
        this.type = type;
        this.stack = itemStack;
        this.phase = phase;
        boolean valid = true;
        Side side;
        if (player != null) {
            if (player.worldObj != null) {
                if (player.worldObj.isRemote) {
                    side = Side.CLIENT;
                }
                else {
                    side = Side.SERVER;
                }
            } else {
                side = null;
            }
        } else {
            side = null;
        }

        this.side = side;
        this.isValid = valid;
    }
}

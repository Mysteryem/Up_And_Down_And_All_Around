package uk.co.mysterymayhem.gravitymod.client.listeners;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import uk.co.mysterymayhem.gravitymod.api.IWeakGravityEnabler;
import uk.co.mysterymayhem.gravitymod.common.config.ConfigHandler;
import uk.co.mysterymayhem.gravitymod.common.items.materials.ItemArmourPaste;
import uk.co.mysterymayhem.gravitymod.common.modsupport.ModSupport;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Created by Mysteryem on 2016-11-13.
 */
@SideOnly(Side.CLIENT)
public class ItemTooltipListener {

    @SubscribeEvent
    public static void onTooltipDisplay(ItemTooltipEvent event) {
        EntityPlayer player = event.getEntityPlayer();
        if (player != null && !(player instanceof FakePlayer)) {
            ItemStack itemStack = event.getItemStack();
            if (itemStack != null) {
                List<String> toolTips = event.getToolTip();
                if (itemStack.getItem() instanceof IWeakGravityEnabler) {
                    addWeakGravityTooltip(toolTips, player);
                    addNormalGravityTooltip(toolTips, player);
                }
                else if (ItemArmourPaste.hasPasteTag(itemStack)) {
                    toolTips.add(I18n.format("mouseovertext.mysttmtgravitymod.hasarmourpaste"));
//                    toolTips.add("Affected by normal strength and stronger gravity");
                    addNormalGravityTooltip(toolTips, player);
                }
            }
        }
    }

    public static void addWeakGravityTooltip(@Nonnull List<String> toolTips, @Nonnull EntityPlayer player) {
        int numWeakEnablersWorn = getNumWeakEnablersWorn(player);
        int numRequired = ConfigHandler.numWeakGravityEnablersRequiredForWeakGravity;
        boolean enoughEquipped = numWeakEnablersWorn >= numRequired;
        toolTips.add(I18n.format(
                "mouseovertext.mysttmtgravitymod.weaktooltip",
                enoughEquipped ? "f" : "c",
                numWeakEnablersWorn,
                numRequired));
//        toolTips.add((enoughEquipped ? "§f" : "§c") + numWeakEnablersWorn + "§7/" + numRequired + " for §fweak§7 gravity");
    }

    public static void addNormalGravityTooltip(@Nonnull List<String> toolTips, @Nonnull EntityPlayer player) {
        KeyBinding keyBindSneak = Minecraft.getMinecraft().gameSettings.keyBindSneak;
        int numWeakEnablerCountsAs = ConfigHandler.numNormalEnablersWeakEnablersCountsAs;
        boolean weakEnablersDontCount = numWeakEnablerCountsAs == 0;
        if (!weakEnablersDontCount && Keyboard.isKeyDown(keyBindSneak.getKeyCode())) {
            toolTips.add(I18n.format(
                    "mouseovertext.mysttmtgravitymod.normaltooltip.sneak",
                    getNumWeakEnablersWorn(player),
                    numWeakEnablerCountsAs,
                    getNumNormalEnablersWorn(player)));
//            toolTips.add("§f" + getNumWeakEnablersWorn(player) + " weak§7(x" + numWeakEnablerCountsAs + ") + §5" + getNumNormalEnablersWorn(player) + " normal§7");
        }
        else {
            int combinedNormalEnablersWorn = getCombinedNormalEnablersWorn(player);
            int numRequired = ConfigHandler.numNormalGravityEnablersRequiredForNormalGravity;
            boolean enoughEquipped = combinedNormalEnablersWorn >= numRequired;
            toolTips.add(I18n.format(
                    "mouseovertext.mysttmtgravitymod.normaltooltip",
                    enoughEquipped ? "5" : "c",
                    combinedNormalEnablersWorn,
                    numRequired,
                    weakEnablersDontCount ? "" : "(" + keyBindSneak.getDisplayName() + ")"));
//            toolTips.add((enoughEquipped ? "§5" : "§c") + combinedNormalEnablersWorn + "§7/" + numRequired + " for §5normal§7 gravity (" + keyBindSneak.getDisplayName() + ")");
        }
    }

    private static int getNumWeakEnablersWorn(@Nonnull EntityPlayer player) {
        ItemStack[] armorInventory = player.inventory.armorInventory;
        int numWeakGravityEnablers = 0;
        for (ItemStack stack : armorInventory) {
            if (stack != null && stack.getItem() instanceof IWeakGravityEnabler) {
                numWeakGravityEnablers++;
            }
        }
        if (ModSupport.isModLoaded(ModSupport.BAUBLES_MOD_ID)) {
            IBaublesItemHandler baublesHandler = BaublesApi.getBaublesHandler(player);
            int slots = baublesHandler.getSlots();
            for (int i = 0; i < slots; i++) {
                ItemStack stack = baublesHandler.getStackInSlot(i);
                if (stack != null && stack.getItem() instanceof IWeakGravityEnabler) {
                    numWeakGravityEnablers++;
                }
            }

        }
        return numWeakGravityEnablers;
    }

    // Does not count weak
    private static int getNumNormalEnablersWorn(@Nonnull EntityPlayer player) {
        ItemStack[] armorInventory = player.inventory.armorInventory;
        int numNormalGravityEnablers = 0;
        for (ItemStack stack : armorInventory) {
            if (stack != null && ItemArmourPaste.hasPasteTag(stack)) {
                numNormalGravityEnablers++;
            }
        }
        if (ModSupport.isModLoaded(ModSupport.BAUBLES_MOD_ID)) {
            IBaublesItemHandler baublesHandler = BaublesApi.getBaublesHandler(player);
            int slots = baublesHandler.getSlots();
            for (int i = 0; i < slots; i++) {
                ItemStack stack = baublesHandler.getStackInSlot(i);
                if (stack != null && ItemArmourPaste.hasPasteTag(stack)) {
                    numNormalGravityEnablers++;
                }
            }

        }
        return numNormalGravityEnablers;
    }

    // Counts weak and gives them the value defined in config
    private static int getCombinedNormalEnablersWorn(@Nonnull EntityPlayer player) {
        ItemStack[] armorInventory = player.inventory.armorInventory;
        int numNormalGravityEnablersIncludingWeakEnablers = 0;
        for (ItemStack stack : armorInventory) {
            if (stack != null) {
                if (ItemArmourPaste.hasPasteTag(stack)) {
                    numNormalGravityEnablersIncludingWeakEnablers++;
                }
                else if (stack.getItem() instanceof IWeakGravityEnabler) {
                    numNormalGravityEnablersIncludingWeakEnablers += ConfigHandler.numNormalEnablersWeakEnablersCountsAs;
                }
            }
        }
        if (ModSupport.isModLoaded(ModSupport.BAUBLES_MOD_ID)) {
            IBaublesItemHandler baublesHandler = BaublesApi.getBaublesHandler(player);
            int slots = baublesHandler.getSlots();
            for (int i = 0; i < slots; i++) {
                ItemStack stack = baublesHandler.getStackInSlot(i);
                if (stack != null) {
                    if (ItemArmourPaste.hasPasteTag(stack)) {
                        numNormalGravityEnablersIncludingWeakEnablers++;
                    }
                    else if (stack.getItem() instanceof IWeakGravityEnabler) {
                        numNormalGravityEnablersIncludingWeakEnablers += ConfigHandler.numNormalEnablersWeakEnablersCountsAs;
                    }
                }
            }

        }
        return numNormalGravityEnablersIncludingWeakEnablers;
    }
}

package uk.co.mysterymayhem.gravitymod.common.items.tools;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import uk.co.mysterymayhem.gravitymod.GravityMod;
import uk.co.mysterymayhem.gravitymod.client.listeners.ItemTooltipListener;
import uk.co.mysterymayhem.gravitymod.common.listeners.GravityManagerCommon;
import uk.co.mysterymayhem.gravitymod.common.registries.GravityPriorityRegistry;
import uk.co.mysterymayhem.gravitymod.common.registries.StaticItems;

import java.util.List;

/**
 * Created by Mysteryem on 2016-11-12.
 */
public class ItemPersonalGravityController extends ItemAbstractGravityController {
    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("mouseovertext.mysttmtgravitymod.personalgravitycontroller"));
        ItemTooltipListener.addNormalGravityTooltip(tooltip, playerIn);
        super.addInformation(stack, playerIn, tooltip, advanced);
    }

    @Override
    public boolean affectsPlayer(EntityPlayerMP player) {
        return GravityManagerCommon.playerIsAffectedByNormalGravity(player);
    }

    @Override
    public String getName() {
        return "personalgravitycontroller";
    }

    @Override
    public int getPriority(EntityPlayerMP target) {
        return GravityPriorityRegistry.PERSONAL_GRAVITY_CONTROLLER;
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return stack.isItemEnchanted() ? EnumRarity.RARE : GravityMod.RARITY_NORMAL;
    }

    @Override
    public void postInit() {
        for (int inputMeta : ItemAbstractGravityController.LEGAL_METADATA) {
            EnumControllerVisibleState visibleState = EnumControllerVisibleState.getFromCombinedMeta(inputMeta);
            int outputMeta = getCombinedMetaFor(EnumControllerActiveDirection.NONE, visibleState.getOffState());

            GameRegistry.addRecipe(
                    new ItemStack(this, 1, outputMeta),
                    "IPI",
                    "PWP",
                    "IPI",
                    'I', StaticItems.GRAVITY_INGOT,
                    'P', StaticItems.GRAVITY_PEARL,
                    'W', new ItemStack(StaticItems.WEAK_GRAVITY_CONTROLLER, 1, inputMeta));
        }
    }
}

package uk.co.mysterymayhem.gravitymod.common.items.tools;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import uk.co.mysterymayhem.gravitymod.client.listeners.ItemTooltipListener;
import uk.co.mysterymayhem.gravitymod.common.GravityPriorityRegistry;
import uk.co.mysterymayhem.gravitymod.common.ModItems;
import uk.co.mysterymayhem.gravitymod.common.listeners.GravityManagerCommon;

import java.util.List;

/**
 * Created by Mysteryem on 2016-11-12.
 */
public class ItemPersonalGravityController extends ItemAbstractGravityController {
    @Override
    public boolean affectsPlayer(EntityPlayerMP player) {
        return GravityManagerCommon.playerIsAffectedByNormalGravity(player);
    }

    @Override
    public void postInitRecipes() {
        GameRegistry.addRecipe(
                new ItemStack(this, 1, ItemAbstractGravityController.DEFAULT_META),
                "IPI",
                "PWP",
                "IPI",
                'I', ModItems.gravityIngot,
                'P', ModItems.gravityPearl,
                'W', new ItemStack(ModItems.weakGravityController, 1, OreDictionary.WILDCARD_VALUE));
    }

    @Override
    public String getName() {
        return "personalgravitycontroller";
    }

    @Override
    public int getPriority(Entity target) {
        return GravityPriorityRegistry.PERSONAL_GRAVITY_CONTROLLER;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("mouseovertext.mysttmtgravitymod.personalgravitycontroller"));
        ItemTooltipListener.addNormalGravityTooltip(tooltip, playerIn);
        super.addInformation(stack, playerIn, tooltip, advanced);
    }
}

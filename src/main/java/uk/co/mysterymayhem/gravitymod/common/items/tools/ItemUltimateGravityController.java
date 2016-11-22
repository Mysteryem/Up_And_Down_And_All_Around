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
import net.minecraftforge.oredict.ShapedOreRecipe;
import uk.co.mysterymayhem.gravitymod.common.GravityPriorityRegistry;
import uk.co.mysterymayhem.gravitymod.common.ModItems;
import uk.co.mysterymayhem.gravitymod.common.listeners.GravityManagerCommon;

import java.util.List;

/**
 * Created by Mysteryem on 2016-11-12.
 */
public class ItemUltimateGravityController extends ItemAbstractGravityController {
    private static final String NAME = "ultimategravitycontroller";

    @Override
    public void postInitRecipes() {
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(this, 1, ItemAbstractGravityController.DEFAULT_META),
                "PGP",
                "ISI",
                "PIP",

                'I', ModItems.gravityIngot,
                'G', new ItemStack(ModItems.personalGravityController, 1, OreDictionary.WILDCARD_VALUE),
                'P', ModItems.gravityPearl,
                'S', ModItems.spacetimeAnomaly
        ));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(this, 1, ItemAbstractGravityController.DEFAULT_META),
                "PIP",
                "GSI",
                "PIP",

                'I', ModItems.gravityIngot,
                'G', new ItemStack(ModItems.personalGravityController, 1, OreDictionary.WILDCARD_VALUE),
                'P', ModItems.gravityPearl,
                'S', ModItems.spacetimeAnomaly
                ));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(this, 1, ItemAbstractGravityController.DEFAULT_META),
                "PIP",
                "ISG",
                "PIP",

                'I', ModItems.gravityIngot,
                'G', new ItemStack(ModItems.personalGravityController, 1, OreDictionary.WILDCARD_VALUE),
                'P', ModItems.gravityPearl,
                'S', ModItems.spacetimeAnomaly
        ));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(this, 1, ItemAbstractGravityController.DEFAULT_META),
                "PIP",
                "ISI",
                "PGP",

                'I', ModItems.gravityIngot,
                'G', new ItemStack(ModItems.personalGravityController, 1, OreDictionary.WILDCARD_VALUE),
                'P', ModItems.gravityPearl,
                'S', ModItems.spacetimeAnomaly
        ));
    }

    @Override
    public String getName() {
        return NAME;
    }

    // Enchantment glow
    @SideOnly(Side.CLIENT)
    @Override
    public boolean hasEffect(ItemStack stack) {
        return true;
    }

    @Override
    public boolean affectsPlayer(EntityPlayerMP player) {
        return GravityManagerCommon.playerIsAffectedByStrongGravity(player);
    }

    @Override
    public int getPriority(Entity target) {
        return GravityPriorityRegistry.ULTIMATE_GRAVITY_CONTROLLER;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("mouseovertext.mysttmtgravitymod.ultimategravitycontroller"));
        super.addInformation(stack, playerIn, tooltip, advanced);
    }
}

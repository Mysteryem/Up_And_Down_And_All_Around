package uk.co.mysterymayhem.gravitymod.common.items.tools;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.ShapedOreRecipe;
import uk.co.mysterymayhem.gravitymod.GravityMod;
import uk.co.mysterymayhem.gravitymod.common.listeners.GravityManagerCommon;
import uk.co.mysterymayhem.gravitymod.common.registries.GravityPriorityRegistry;
import uk.co.mysterymayhem.gravitymod.common.registries.StaticItems;

import java.util.List;

/**
 * Created by Mysteryem on 2016-11-12.
 */
public class ItemUltimateGravityController extends ItemAbstractGravityController {
    private static final String NAME = "ultimategravitycontroller";

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("mouseovertext.mysttmtgravitymod.ultimategravitycontroller"));
        super.addInformation(stack, playerIn, tooltip, advanced);
    }

    @Override
    public boolean affectsPlayer(EntityPlayerMP player) {
        return GravityManagerCommon.playerIsAffectedByStrongGravity(player);
    }

    @Override
    public String getModObjectName() {
        return NAME;
    }

    @Override
    public int getPriority(EntityPlayerMP target) {
        return GravityPriorityRegistry.ULTIMATE_GRAVITY_CONTROLLER;
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return stack.isItemEnchanted() ? EnumRarity.RARE : GravityMod.RARITY_STRONG;
    }

    // Enchantment glow
    @SideOnly(Side.CLIENT)
    @Override
    public boolean hasEffect(ItemStack stack) {
        return true;
    }

    @Override
    public void postInit() {
        for (int inputMeta : ItemAbstractGravityController.LEGAL_METADATA) {
            EnumControllerVisibleState visibleState = EnumControllerVisibleState.getFromCombinedMeta(inputMeta);
            int outputMeta = getCombinedMetaFor(EnumControllerActiveDirection.NONE, visibleState.getOffState());

            GameRegistry.addRecipe(new ShapedOreRecipe(
                    new ItemStack(this, 1, outputMeta),
                    "PGP",
                    "ISI",
                    "PIP",

                    'I', StaticItems.GRAVITY_INGOT,
                    'G', new ItemStack(StaticItems.PERSONAL_GRAVITY_CONTROLLER, 1, inputMeta),
                    'P', StaticItems.GRAVITY_PEARL,
                    'S', StaticItems.SPACETIME_ANOMALY
            ));
            GameRegistry.addRecipe(new ShapedOreRecipe(
                    new ItemStack(this, 1, outputMeta),
                    "PIP",
                    "GSI",
                    "PIP",

                    'I', StaticItems.GRAVITY_INGOT,
                    'G', new ItemStack(StaticItems.PERSONAL_GRAVITY_CONTROLLER, 1, inputMeta),
                    'P', StaticItems.GRAVITY_PEARL,
                    'S', StaticItems.SPACETIME_ANOMALY
            ));
            GameRegistry.addRecipe(new ShapedOreRecipe(
                    new ItemStack(this, 1, outputMeta),
                    "PIP",
                    "ISG",
                    "PIP",

                    'I', StaticItems.GRAVITY_INGOT,
                    'G', new ItemStack(StaticItems.PERSONAL_GRAVITY_CONTROLLER, 1, inputMeta),
                    'P', StaticItems.GRAVITY_PEARL,
                    'S', StaticItems.SPACETIME_ANOMALY
            ));
            GameRegistry.addRecipe(new ShapedOreRecipe(
                    new ItemStack(this, 1, outputMeta),
                    "PIP",
                    "ISI",
                    "PGP",

                    'I', StaticItems.GRAVITY_INGOT,
                    'G', new ItemStack(StaticItems.PERSONAL_GRAVITY_CONTROLLER, 1, inputMeta),
                    'P', StaticItems.GRAVITY_PEARL,
                    'S', StaticItems.SPACETIME_ANOMALY
            ));
        }
    }
}

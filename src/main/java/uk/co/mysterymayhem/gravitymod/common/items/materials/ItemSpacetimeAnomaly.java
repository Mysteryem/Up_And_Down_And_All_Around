package uk.co.mysterymayhem.gravitymod.common.items.materials;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import uk.co.mysterymayhem.gravitymod.common.ModItems;
import uk.co.mysterymayhem.gravitymod.common.items.shared.IModItem;

import java.util.List;

/**
 * Used in crafting the best/string gravity controller
 * Created by Mysteryem on 2016-11-11.
 */
public class ItemSpacetimeAnomaly extends Item implements IModItem{
    @Override
    public String getName() {
        return "spacetimeanomaly";
    }

    @Override
    public void postInitRecipes() {
        GameRegistry.addShapedRecipe(
                new ItemStack(this),
                "DPD",
                "PSP",
                "DPD",
                'D', ModItems.gravityDust,
                'P', ModItems.gravityPearl,
                'S', Items.NETHER_STAR);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
        tooltip.add("Currently only used for crafting (temporary art)");
    }
}

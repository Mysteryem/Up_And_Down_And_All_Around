package uk.co.mysterymayhem.gravitymod;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.ShapedOreRecipe;
import uk.co.mysterymayhem.gravitymod.item.ItemAntiGravityBoots;

/**
 * Created by Mysteryem on 2016-08-08.
 */
public class ModItems {
    public static ItemAntiGravityBoots antiGravityBoots;

    public static void initItems() {
        antiGravityBoots = new ItemAntiGravityBoots();
    }

    @SideOnly(Side.CLIENT)
    public static void initModels() {
        antiGravityBoots.initModel();
    }

    public static void initRecipes() {
        GameRegistry.addRecipe(new ShapedOreRecipe(
                antiGravityBoots,

                "IGI",
                "IPI",
                "YGY",

                'I', "ingotIron",
                // Grey wool
                'G', new ItemStack(Blocks.WOOL, 1, 7),
                'P', Items.ENDER_PEARL,
                // Yellow wool
                'Y', new ItemStack(Blocks.WOOL, 1, 4)));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                antiGravityBoots,

                "YGY",
                "IPI",
                "IGI",

                'I', "ingotIron",
                // Grey wool
                'G', new ItemStack(Blocks.WOOL, 1, 7),
                'P', Items.ENDER_PEARL,
                // Yellow wool
                'Y', new ItemStack(Blocks.WOOL, 1, 4)));
    }
}

package uk.co.mysterymayhem.gravitymod;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.ShapedOreRecipe;
import uk.co.mysterymayhem.gravitymod.item.ItemAntiGravityBoots;
import uk.co.mysterymayhem.gravitymod.item.ItemPersonalGravityController;

/**
 * Created by Mysteryem on 2016-08-08.
 */
public class ModItems {
    public static ItemAntiGravityBoots antiGravityBoots;
    public static ItemPersonalGravityController personalGravityController;

    public static void initItems() {
//        antiGravityBoots = new ItemAntiGravityBoots();
        personalGravityController = new ItemPersonalGravityController();
    }

    @SideOnly(Side.CLIENT)
    public static void initModels() {
//        antiGravityBoots.initModel();
        personalGravityController.initModel();
    }

    public static void initRecipes() {
//        GameRegistry.addRecipe(new ShapedOreRecipe(
//                antiGravityBoots,
//
//                "IGI",
//                "IPI",
//                "YGY",
//
//                'I', "ingotIron",
//                // Grey wool
//                'G', new ItemStack(Blocks.WOOL, 1, 7),
//                'P', Items.ENDER_PEARL,
//                // Yellow wool
//                'Y', new ItemStack(Blocks.WOOL, 1, 4)));
//        GameRegistry.addRecipe(new ShapedOreRecipe(
//                antiGravityBoots,
//
//                "YGY",
//                "IPI",
//                "IGI",
//
//                'I', "ingotIron",
//                // Grey wool
//                'G', new ItemStack(Blocks.WOOL, 1, 7),
//                'P', Items.ENDER_PEARL,
//                // Yellow wool
//                'Y', new ItemStack(Blocks.WOOL, 1, 4)));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(personalGravityController, 1, ItemPersonalGravityController.DEFAULT_META),

                "IDI",
                "DPD",
                "IDI",

                'I', "ingotIron",
                'D', Blocks.DIRT,
                'P', Items.ENDER_PEARL
        ));
//        GameRegistry.addRecipe(new ShapedOreRecipe(
//                personalGravityController,
//
//                "IDI",
//                "DSD",
//                "IDI",
//
//                'S', "ingotIron",
//                'D', Blocks.DIRT,
//                'P', Items.ENDER_PEARL
//        ));
    }
}

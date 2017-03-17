package uk.co.mysterymayhem.gravitymod.common.items.materials;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import uk.co.mysterymayhem.gravitymod.common.registries.IGravityModItem;
import uk.co.mysterymayhem.gravitymod.common.registries.StaticBlocks;
import uk.co.mysterymayhem.gravitymod.common.registries.StaticItems;

/**
 * Used in crafting the best/string gravity controller
 * Created by Mysteryem on 2016-11-11.
 */
public class ItemSpacetimeAnomaly extends Item implements IGravityModItem<ItemSpacetimeAnomaly> {
    @Override
    public String getModObjectName() {
        return "spacetimeanomaly";
    }

    @Override
    public void postInit() {
        GameRegistry.addShapedRecipe(
                new ItemStack(this),
                "DPD",
                "PSP",
                "DPD",
                'D', StaticBlocks.RESTABILISED_GRAVITY_DUST_BLOCK,
                'P', StaticItems.GRAVITY_PEARL,
                'S', Items.NETHER_STAR);
    }
}

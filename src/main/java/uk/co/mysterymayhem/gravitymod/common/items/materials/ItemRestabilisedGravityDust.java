package uk.co.mysterymayhem.gravitymod.common.items.materials;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import uk.co.mysterymayhem.gravitymod.common.registries.IGravityModItem;
import uk.co.mysterymayhem.gravitymod.common.registries.StaticItems;

/**
 * Created by Mysteryem on 2017-01-26.
 */
public class ItemRestabilisedGravityDust extends Item implements IGravityModItem<ItemRestabilisedGravityDust> {
    @Override
    public String getModObjectName() {
        return "restabilisedgravitydust";
    }

    @Override
    public void postInit() {
        GameRegistry.addShapedRecipe(new ItemStack(this, 2),
                "DDD",
                "DCD",
                "DDD",
                'D', StaticItems.DESTABILISED_GRAVITY_DUST,
                'C', new ItemStack(Items.COAL));
    }
}

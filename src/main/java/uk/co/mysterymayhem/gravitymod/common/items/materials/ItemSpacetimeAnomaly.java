package uk.co.mysterymayhem.gravitymod.common.items.materials;

import net.minecraft.item.Item;
import uk.co.mysterymayhem.gravitymod.common.registries.IGravityModItem;

/**
 * Used in crafting the best/string gravity controller
 * Created by Mysteryem on 2016-11-11.
 */
public class ItemSpacetimeAnomaly extends Item implements IGravityModItem<ItemSpacetimeAnomaly> {
    @Override
    public String getModObjectName() {
        return "spacetimeanomaly";
    }
}

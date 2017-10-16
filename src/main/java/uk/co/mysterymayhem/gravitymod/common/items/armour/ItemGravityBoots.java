package uk.co.mysterymayhem.gravitymod.common.items.armour;

import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import uk.co.mysterymayhem.gravitymod.api.IWeakGravityEnabler;
import uk.co.mysterymayhem.gravitymod.common.items.materials.ItemGravityIngot;
import uk.co.mysterymayhem.gravitymod.common.registries.IGravityModItem;

/**
 * Created by Mysteryem on 2016-08-08.
 */
public class ItemGravityBoots extends ItemArmor implements IGravityModItem<ItemGravityBoots>, IWeakGravityEnabler {

    public ItemGravityBoots() {
        super(ItemGravityIngot.ARMOUR_MATERIAL, 2, EntityEquipmentSlot.FEET);
    }

    @Override
    public String getModObjectName() {
        return "gravityboots";
    }
}

package uk.co.mysterymayhem.gravitymod.common.items.armour;

import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import uk.co.mysterymayhem.gravitymod.api.IWeakGravityEnabler;
import uk.co.mysterymayhem.gravitymod.common.items.materials.ItemGravityIngot;
import uk.co.mysterymayhem.gravitymod.common.registries.IGravityModItem;

/**
 * Created by Mysteryem on 2016-11-05.
 */
public class ItemGravityChestplate extends ItemArmor implements IGravityModItem<ItemGravityChestplate>, IWeakGravityEnabler {

    public ItemGravityChestplate() {
        super(ItemGravityIngot.ARMOUR_MATERIAL, 2, EntityEquipmentSlot.CHEST);
    }

    @Override
    public String getModObjectName() {
        return "gravitychestplate";
    }
}

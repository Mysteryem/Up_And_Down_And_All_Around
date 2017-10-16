package uk.co.mysterymayhem.gravitymod.common.items.armour;

import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import uk.co.mysterymayhem.gravitymod.api.IWeakGravityEnabler;
import uk.co.mysterymayhem.gravitymod.common.items.materials.ItemGravityIngot;
import uk.co.mysterymayhem.gravitymod.common.registries.IGravityModItem;

/**
 * Created by Mysteryem on 2016-11-05.
 */
public class ItemGravityLeggings extends ItemArmor implements IGravityModItem<ItemGravityLeggings>, IWeakGravityEnabler {
    public ItemGravityLeggings() {
        super(ItemGravityIngot.ARMOUR_MATERIAL, 2, EntityEquipmentSlot.LEGS);
    }

    @Override
    public String getModObjectName() {
        return "gravityleggings";
    }
}

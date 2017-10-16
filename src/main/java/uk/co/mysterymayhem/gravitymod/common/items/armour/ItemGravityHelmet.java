package uk.co.mysterymayhem.gravitymod.common.items.armour;

import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import uk.co.mysterymayhem.gravitymod.api.IWeakGravityEnabler;
import uk.co.mysterymayhem.gravitymod.common.items.materials.ItemGravityIngot;
import uk.co.mysterymayhem.gravitymod.common.registries.IGravityModItem;

/**
 * Created by Mysteryem on 2016-11-05.
 */
public class ItemGravityHelmet extends ItemArmor implements IGravityModItem<ItemGravityHelmet>, IWeakGravityEnabler {
    public ItemGravityHelmet() {
        super(ItemGravityIngot.ARMOUR_MATERIAL, 2, EntityEquipmentSlot.HEAD);
    }

    @Override
    public String getModObjectName() {
        return "gravityhelmet";
    }
}

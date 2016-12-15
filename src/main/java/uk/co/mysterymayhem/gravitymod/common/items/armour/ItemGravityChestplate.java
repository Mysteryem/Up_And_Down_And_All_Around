package uk.co.mysterymayhem.gravitymod.common.items.armour;

import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import uk.co.mysterymayhem.gravitymod.api.IWeakGravityEnabler;
import uk.co.mysterymayhem.gravitymod.common.registries.ModItems;
import uk.co.mysterymayhem.gravitymod.common.registries.StaticRegistry;
import uk.co.mysterymayhem.gravitymod.common.items.materials.ItemGravityIngot;

/**
 * Created by Mysteryem on 2016-11-05.
 */
public class ItemGravityChestplate extends ItemArmor implements ModItems.IModItem, IWeakGravityEnabler {

    public ItemGravityChestplate() {
        super(ItemGravityIngot.ARMOUR_MATERIAL, 2, EntityEquipmentSlot.CHEST);
    }

    @Override
    public String getName() {
        return "gravitychestplate";
    }

    @Override
    public void postInit() {
        GameRegistry.addRecipe(
                new ItemStack(this),
                "I I",
                "III",
                "III",
                'I', StaticRegistry.gravityIngot);
    }
}

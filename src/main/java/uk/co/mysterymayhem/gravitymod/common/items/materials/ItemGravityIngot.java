package uk.co.mysterymayhem.gravitymod.common.items.materials;

import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.registries.IForgeRegistry;
import uk.co.mysterymayhem.gravitymod.GravityMod;
import uk.co.mysterymayhem.gravitymod.common.registries.IGravityModItem;

/**
 * Armour crafted out of this material is affected by weak gravity fields
 * Created by Mysteryem on 2016-11-06.
 */
public class ItemGravityIngot extends Item implements IGravityModItem<ItemGravityIngot> {
    // Like iron armour, but crappier
    public static ItemArmor.ArmorMaterial ARMOUR_MATERIAL =
            EnumHelper.addArmorMaterial("gravityiron", GravityMod.MOD_ID + ":gravityiron", 15, new int[]{1, 3, 4, 1}, 10, SoundEvents.ITEM_ARMOR_EQUIP_IRON, 0F);

    @Override
    public String getModObjectName() {
        return "gravityingot";
    }

    @Override
    public void register(IForgeRegistry<Item> registry) {
        ARMOUR_MATERIAL.repairMaterial = new ItemStack(this);
        IGravityModItem.super.register(registry);
    }
}

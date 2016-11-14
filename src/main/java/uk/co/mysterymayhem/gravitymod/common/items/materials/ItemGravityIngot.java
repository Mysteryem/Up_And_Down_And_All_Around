package uk.co.mysterymayhem.gravitymod.common.items.materials;

import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;
import uk.co.mysterymayhem.gravitymod.GravityMod;
import uk.co.mysterymayhem.gravitymod.common.ModItems;
import uk.co.mysterymayhem.gravitymod.common.items.shared.IModItem;

/**
 * Armour crafted out of this material is affected by weak gravity fields
 * Created by Mysteryem on 2016-11-06.
 */
public class ItemGravityIngot extends Item implements IModItem {
    public static ItemArmor.ArmorMaterial ARMOUR_MATERIAL =
            EnumHelper.addArmorMaterial("gravityiron", GravityMod.MOD_ID + ":gravityiron", 15, new int[]{1, 4, 3, 1}, 10, SoundEvents.ITEM_ARMOR_EQUIP_IRON, 0F);

    @Override
    public String getName() {
        return "gravityingot";
    }

    @Override
    public void preInit() {
        ARMOUR_MATERIAL.customCraftingMaterial = this;
        IModItem.super.preInit();
    }

    @Override
    public void postInitRecipes() {
        GameRegistry.addRecipe(new ShapedOreRecipe(
                this,
                "DID",
                'D', ModItems.gravityDust,
                'I', "ingotIron"));
    }
}

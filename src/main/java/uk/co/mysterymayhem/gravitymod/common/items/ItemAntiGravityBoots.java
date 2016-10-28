package uk.co.mysterymayhem.gravitymod.common.items;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by Mysteryem on 2016-08-08.
 */
public class ItemAntiGravityBoots extends ItemArmor {

    public ItemAntiGravityBoots() {
        super(ArmorMaterial.IRON, 2, EntityEquipmentSlot.FEET);
        this.setUnlocalizedName("antigravityboots");
        this.setRegistryName("antigravityboots");
        this.setCreativeTab(CreativeTabs.COMBAT);
        this.setMaxStackSize(1);
        GameRegistry.register(this);
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(this.getRegistryName(), "inventory"));
    }
}

package uk.co.mysterymayhem.gravitymod.common.items;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import uk.co.mysterymayhem.gravitymod.GravityMod;

/**
 * Created by Mysteryem on 2016-11-02.
 */
public class ItemCreativeTabIcon extends Item {
    private static final String name = "creativetabicon";

    public ItemCreativeTabIcon() {

    }

    public void init() {
        this.setUnlocalizedName(name);
        this.setRegistryName(GravityMod.MOD_ID, name);
        this.setMaxStackSize(1);
        this.setMaxDamage(0);
        GameRegistry.register(this);
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(this.getRegistryName(), "inventory"));
    }
}

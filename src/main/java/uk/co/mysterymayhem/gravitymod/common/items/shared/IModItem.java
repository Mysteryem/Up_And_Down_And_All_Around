package uk.co.mysterymayhem.gravitymod.common.items.shared;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.IForgeRegistryEntry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import uk.co.mysterymayhem.gravitymod.GravityMod;
import uk.co.mysterymayhem.gravitymod.common.ModItems;

/**
 * It may be frowned upon, but this is so I can have some item classes that extend Item and some that extend ItemArmor
 * whilst still having common default code for both of them.
 *
 * Created by Mysteryem on 2016-11-05.
 */
public interface IModItem extends IForgeRegistryEntry<Item>{
    @SideOnly(Side.CLIENT)
    default void preInitModel(){
        ModelLoader.setCustomModelResourceLocation((Item)this, 0, new ModelResourceLocation(this.getRegistryName(), "inventory"));
    }

    String getName();
    default void postInitRecipes(){}

    default void preInit() {
        Item thisAsItem = (Item)this;
        thisAsItem.setUnlocalizedName(GravityMod.MOD_ID + "." + this.getName());
        this.setRegistryName(new ResourceLocation(GravityMod.MOD_ID, this.getName()));
        thisAsItem.setCreativeTab(ModItems.UP_AND_DOWN_CREATIVE_TAB);
        GameRegistry.register(this);
    }
}

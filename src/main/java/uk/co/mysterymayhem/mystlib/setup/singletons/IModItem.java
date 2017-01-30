package uk.co.mysterymayhem.mystlib.setup.singletons;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * It may be frowned upon, but this is so I can have some item classes that extend Item and some that extend ItemArmor
 * whilst still having common default code for both of them.
 * <p>
 * Created by Mysteryem on 2016-11-05.
 */
public interface IModItem<T extends Item & IModItem<T>> extends IModObject {
    @Override
    @SideOnly(Side.CLIENT)
    default void preInitClient() {
        T cast = this.getItem();
        ModelLoader.setCustomModelResourceLocation((Item)this, 0, new ModelResourceLocation(cast.getRegistryName(), "inventory"));
    }

    @SuppressWarnings("unchecked")
    default T getItem() {
        return (T)this;
    }

    @Override
    default void preInit() {
        T cast = this.getItem();
        cast.setUnlocalizedName(this.getModID() + "." + this.getName());
        cast.setRegistryName(new ResourceLocation(this.getModID(), this.getName()));
        CreativeTabs creativeTab = this.getModCreativeTab();
        if (creativeTab != null) {
            cast.setCreativeTab(creativeTab);
        }
        GameRegistry.register(cast);
        IModObject.super.preInit();
    }
}

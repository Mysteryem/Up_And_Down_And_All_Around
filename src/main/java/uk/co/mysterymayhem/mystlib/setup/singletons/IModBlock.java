package uk.co.mysterymayhem.mystlib.setup.singletons;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by Mysteryem on 2016-12-07.
 */
public interface IModBlock<T extends Block & IModBlock<T, U>, U extends Item> extends IModObject<T> {

    @Override
    @SideOnly(Side.CLIENT)
    default void preInitClient(){
        U itemBlock = this.getItem();
        if (itemBlock != null) {
            ModelLoader.setCustomModelResourceLocation(itemBlock, 0, new ModelResourceLocation(itemBlock.getRegistryName(), "inventory"));
        }
    }

    @Override
    default void preInit(){
        CreativeTabs creativeTab = this.getModCreativeTab();
        T cast = this.getCast();
        cast.setUnlocalizedName(cast.getName());
        cast.setRegistryName(this.getModID(), cast.getName());
        if (creativeTab != null) {
            cast.setCreativeTab(creativeTab);
        }
        GameRegistry.register(cast);
        U itemBlock = this.getItem();
        if (itemBlock != null) {
            if (creativeTab != null) {
                itemBlock.setCreativeTab(creativeTab);
            }
            GameRegistry.register(itemBlock, cast.getRegistryName());
        }
    }

    default T getBlock() {
        return this.getCast();
    }

    default U getItem() {
        return null;
    }
}

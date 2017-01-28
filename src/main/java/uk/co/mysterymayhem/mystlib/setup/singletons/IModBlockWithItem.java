package uk.co.mysterymayhem.mystlib.setup.singletons;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import uk.co.mysterymayhem.mystlib.reflection.lambda.InternalReflectionLambdas;

/**
 * Created by Mysteryem on 2016-12-07.
 */
public interface IModBlockWithItem<T extends Block & IModBlockWithItem<T, U>, U extends Item> extends IModBlock<T> {

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
        IModBlock.super.preInit();
        CreativeTabs creativeTab = this.getModCreativeTab();
        U item = this.getItem();
        if (item != null) {
            if (creativeTab != null) {
                item.setCreativeTab(creativeTab);
            }
            this.registerItemForBlock();
        }
    }

    U getItem();

    default void registerItemForBlock() {
        InternalReflectionLambdas.callStatic_Item$registerItemBlock.accept(this.getBlock(), this.getItem());
    }
}

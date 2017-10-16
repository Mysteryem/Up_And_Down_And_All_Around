package uk.co.mysterymayhem.mystlib.setup.singletons;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

/**
 * Created by Mysteryem on 2016-12-07.
 */
public interface IModBlockWithItem<T extends Block & IModBlockWithItem<T, U>, U extends Item> extends IModBlock<T> {

    U getItem();

    @SideOnly(Side.CLIENT)
    default void registerItemClient(IForgeRegistry<Item> registry) {
        U itemBlock = this.getItem();
        if (itemBlock != null) {
            ModelLoader.setCustomModelResourceLocation(itemBlock, 0, new ModelResourceLocation(itemBlock.getRegistryName(), "inventory"));
        }
    }

    default void registerItem(IForgeRegistry<Item> registry) {
        CreativeTabs creativeTab = this.getModCreativeTab();
        U item = this.getItem();
        if (item != null) {
            if (creativeTab != null) {
                item.setCreativeTab(creativeTab);
            }
            T block = this.getBlock();
//            BiMap<Block, Item> blockItemMap = GameData.getBlockItemMap();
//            blockItemMap.put(block, item);
            registry.register(item.setRegistryName(block.getRegistryName()));
//            InternalReflectionLambdas.callStatic_Item$registerItemBlock.accept(this.getBlock(), item);
        }
    }
}

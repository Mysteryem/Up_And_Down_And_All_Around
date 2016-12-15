package uk.co.mysterymayhem.gravitymod.common.registries;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import uk.co.mysterymayhem.gravitymod.GravityMod;
import uk.co.mysterymayhem.gravitymod.common.registries.ModBlocks.IModBlock;

import java.util.ArrayList;

/**
 * Created by Mysteryem on 2016-12-07.
 */
public class ModBlocks extends ModObjectRegistry<IModBlock<?>, ArrayList<IModBlock<?>>> {

    public ModBlocks() {
        super(new ArrayList<>());
    }

    /**
     * Created by Mysteryem on 2016-12-07.
     */
    public interface IModBlock<T extends Block & IModBlock<T>> extends IModObject<T> {
        @Override
        @SideOnly(Side.CLIENT)
        default void preInitClient(){
            T cast = this.getCast();
            Item itemBlock = this.getItemBlock();
            if (itemBlock != null) {
                ModelLoader.setCustomModelResourceLocation(itemBlock, 0, new ModelResourceLocation(itemBlock.getRegistryName(), "inventory"));
            }
            //        ModelLoader.setCustomModelResourceLocation(new ItemBlock(thisAsBlock), 0, new ModelResourceLocation(this.getRegistryName(), "inventory"));
        }

        @Override
        default void postInit(){}

        @Override
        default void preInit(){
            T cast = this.getCast();
            cast.setUnlocalizedName(cast.getName());
            cast.setRegistryName(GravityMod.MOD_ID, cast.getName());
            GameRegistry.register(cast);
            Item itemBlock = cast.getItemBlock();
            if (itemBlock != null) {
                GameRegistry.register(itemBlock, cast.getRegistryName());
            }
        }

        default Item getItemBlock() {
            return null;
        }
    }
}

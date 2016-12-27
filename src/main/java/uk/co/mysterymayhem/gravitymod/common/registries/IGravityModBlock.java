package uk.co.mysterymayhem.gravitymod.common.registries;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import uk.co.mysterymayhem.gravitymod.GravityMod;
import uk.co.mysterymayhem.mystlib.setup.singletons.IModBlock;

/**
 * Created by Mysteryem on 2016-12-24.
 */
public interface IGravityModBlock<T extends Block & IGravityModBlock<T, U>, U extends Item> extends IModBlock<T, U> {
    @Override
    default CreativeTabs getModCreativeTab() {
        return ModItems.UP_AND_DOWN_CREATIVE_TAB;
    }

    @Override
    default String getModID() {
        return GravityMod.MOD_ID;
    }

    default U createItem(T block) {
        return null;
    }
}

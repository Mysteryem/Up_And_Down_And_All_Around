package uk.co.mysterymayhem.gravitymod.common.registries;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import uk.co.mysterymayhem.gravitymod.GravityMod;
import uk.co.mysterymayhem.mystlib.setup.singletons.IModItem;

/**
 * Created by Mysteryem on 2016-12-24.
 */
public interface IGravityModItem<T extends Item & IGravityModItem<T>> extends IModItem<T> {
    @Override
    default String getModID() {
        return GravityMod.MOD_ID;
    }

    @Override
    default CreativeTabs getModCreativeTab() {
        return ModItems.UP_AND_DOWN_CREATIVE_TAB;
    }
}

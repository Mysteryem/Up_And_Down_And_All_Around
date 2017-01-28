package uk.co.mysterymayhem.gravitymod.common.registries;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import uk.co.mysterymayhem.mystlib.setup.singletons.IModBlockWithItem;

/**
 * Created by Mysteryem on 2016-12-24.
 */
public interface IGravityModBlockWithItem<T extends Block & IGravityModBlockWithItem<T, U>, U extends Item> extends IModBlockWithItem<T, U>, IGravityModBlock<T> {
    default U createItem(T block) {
        return null;
    }
}

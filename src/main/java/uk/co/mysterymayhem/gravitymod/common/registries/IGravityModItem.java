package uk.co.mysterymayhem.gravitymod.common.registries;

import net.minecraft.item.Item;
import uk.co.mysterymayhem.mystlib.setup.singletons.IModItem;

/**
 * Created by Mysteryem on 2016-12-24.
 */
public interface IGravityModItem<T extends Item & IGravityModItem<T>> extends IModItem<T>, IGravityModCommon {
}

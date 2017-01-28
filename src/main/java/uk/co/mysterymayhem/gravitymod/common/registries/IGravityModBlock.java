package uk.co.mysterymayhem.gravitymod.common.registries;

import net.minecraft.block.Block;
import uk.co.mysterymayhem.mystlib.setup.singletons.IModBlock;

/**
 * Created by Mysteryem on 2016-12-24.
 */
public interface IGravityModBlock<T extends Block & IGravityModBlock<T>> extends IModBlock<T>, IGravityModCommon {

}

package uk.co.mysterymayhem.gravitymod.common.registries;

import net.minecraft.tileentity.TileEntity;
import uk.co.mysterymayhem.mystlib.setup.singletons.IModTileEntityClassWrapper;

/**
 * Created by Mysteryem on 2016-12-25.
 */
public interface IGravityModTileEntityClassWrapper<T extends TileEntity> extends IModTileEntityClassWrapper<T>, IGravityModCommon {
}

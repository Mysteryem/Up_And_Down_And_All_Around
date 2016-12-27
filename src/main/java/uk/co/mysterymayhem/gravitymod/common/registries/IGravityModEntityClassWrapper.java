package uk.co.mysterymayhem.gravitymod.common.registries;

import net.minecraft.entity.Entity;
import uk.co.mysterymayhem.gravitymod.GravityMod;
import uk.co.mysterymayhem.mystlib.setup.singletons.IModEntityClassWrapper;

/**
 * Created by Mysteryem on 2016-12-24.
 */
public interface IGravityModEntityClassWrapper<T extends Entity> extends IModEntityClassWrapper<T> {
    @Override
    default String getModID() {
        return GravityMod.MOD_ID;
    }

    @Override
    default Object getModInstance() {
        return GravityMod.INSTANCE;
    }

    @Override
    default int getUniqueID() {
        return GravityMod.getNextEntityID();
    }
}

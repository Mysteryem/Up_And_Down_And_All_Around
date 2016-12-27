package uk.co.mysterymayhem.mystlib.setup.singletons;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * Created by Mysteryem on 2016-12-25.
 */
public interface IModTileEntityClassWrapper<T extends TileEntity> extends IModObject<Class<T>> {
    Class<T> getEntityClass();

    @Override
    default Class<T> getCast() {
        return this.getEntityClass();
    }

    @Override
    default void preInit() {
        GameRegistry.registerTileEntity(this.getCast(), this.getModID() + this.getName());
    }
}

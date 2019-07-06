package uk.co.mysterymayhem.mystlib.setup.singletons;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * Created by Mysteryem on 2016-12-25.
 */
public interface IModTileEntityClassWrapper<T extends TileEntity> extends IModObject {
    @Override
    default void preInit() {
        GameRegistry.registerTileEntity(this.getTileEntityClass(), new ResourceLocation(this.getModID(), this.getModObjectName()));
        IModObject.super.preInit();
    }

    Class<T> getTileEntityClass();
}

package uk.co.mysterymayhem.gravitymod.common.registries;

import uk.co.mysterymayhem.gravitymod.common.tileentities.TileGravityGenerator;
import uk.co.mysterymayhem.mystlib.setup.registries.AbstractTileEntityRegistry;
import uk.co.mysterymayhem.mystlib.setup.singletons.IModTileEntityClassWrapper;

import java.util.ArrayList;

/**
 * Created by Mysteryem on 2016-12-25.
 */
public class ModTileEntities extends AbstractTileEntityRegistry<IModTileEntityClassWrapper<?>, ArrayList<IModTileEntityClassWrapper<?>>> {
    public ModTileEntities() {
        super(new ArrayList<>());
    }

    @Override
    protected void addToCollection(ArrayList<IModTileEntityClassWrapper<?>> modObjects) {
        modObjects.add(new TileGravityGenerator.Wrapper());
    }
}

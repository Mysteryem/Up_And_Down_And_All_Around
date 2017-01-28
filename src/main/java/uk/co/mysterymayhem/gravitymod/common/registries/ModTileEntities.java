package uk.co.mysterymayhem.gravitymod.common.registries;

import uk.co.mysterymayhem.gravitymod.common.blocks.gravitygenerator.TileWrapper;
import uk.co.mysterymayhem.mystlib.setup.registries.AbstractTileEntityRegistry;
import uk.co.mysterymayhem.mystlib.setup.singletons.IModTileEntityClassWrapper;

import java.util.ArrayList;

/**
 * Created by Mysteryem on 2016-12-25.
 */
public class ModTileEntities extends AbstractTileEntityRegistry<IModTileEntityClassWrapper<?>, ArrayList<IModTileEntityClassWrapper<?>>> {
    static boolean STATIC_SETUP_ALLOWED = false;

    static TileWrapper tileGravityGenerator;

    public ModTileEntities() {
        super(new ArrayList<>());
    }

    @Override
    protected void addToCollection(ArrayList<IModTileEntityClassWrapper<?>> modObjects) {
        modObjects.add(tileGravityGenerator = new TileWrapper());
        STATIC_SETUP_ALLOWED = true;
    }
}

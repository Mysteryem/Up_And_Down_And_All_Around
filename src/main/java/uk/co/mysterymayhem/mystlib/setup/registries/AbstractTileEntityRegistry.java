package uk.co.mysterymayhem.mystlib.setup.registries;

import uk.co.mysterymayhem.mystlib.setup.singletons.IModTileEntityClassWrapper;

import java.util.Collection;

/**
 * Created by Mysteryem on 2016-12-25.
 */
public abstract class AbstractTileEntityRegistry<SINGLETON extends IModTileEntityClassWrapper<?>, COLLECTION extends Collection<SINGLETON>> extends AbstractModObjectRegistry<SINGLETON, COLLECTION> {
    public AbstractTileEntityRegistry(COLLECTION modObjects) {
        super(modObjects);
    }
}

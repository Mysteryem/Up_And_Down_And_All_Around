package uk.co.mysterymayhem.mystlib.setup.registries;

import uk.co.mysterymayhem.mystlib.setup.singletons.IModEntityClassWrapper;

import java.util.Collection;

/**
 * Created by Mysteryem on 2016-12-24.
 */
public abstract class AbstractEntityRegistry<SINGLETON extends IModEntityClassWrapper<?>, COLLECTION extends Collection<SINGLETON>> extends AbstractModObjectRegistry<SINGLETON, COLLECTION> {
    public AbstractEntityRegistry(COLLECTION modObjects) {
        super(modObjects);
    }

}

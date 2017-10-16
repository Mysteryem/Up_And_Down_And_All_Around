package uk.co.mysterymayhem.mystlib.setup.registries;

import uk.co.mysterymayhem.mystlib.setup.singletons.IModObject;

import java.util.Collection;

/**
 * Created by Mysteryem on 2016-12-14.
 */
public abstract class AbstractModObjectRegistry<SINGLETON extends IModObject, COLLECTION extends Collection<SINGLETON>> extends
        AbstractIFMLStagedRegistry<SINGLETON, COLLECTION> {
    public AbstractModObjectRegistry(COLLECTION modObjects) {
        super(modObjects);
    }
}

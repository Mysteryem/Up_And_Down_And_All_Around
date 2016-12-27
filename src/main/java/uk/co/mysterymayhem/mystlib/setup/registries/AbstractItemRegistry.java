package uk.co.mysterymayhem.mystlib.setup.registries;

import uk.co.mysterymayhem.mystlib.setup.singletons.IModItem;

import java.util.Collection;

/**
 * Created by Mysteryem on 2016-12-24.
 */
public abstract class AbstractItemRegistry<SINGLETON extends IModItem<?>, COLLECTION extends Collection<SINGLETON>> extends AbstractModObjectRegistry<SINGLETON, COLLECTION> {
    public AbstractItemRegistry(COLLECTION modObjects) {
        super(modObjects);
    }
}

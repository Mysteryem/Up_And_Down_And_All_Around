package uk.co.mysterymayhem.mystlib.setup.registries;

import uk.co.mysterymayhem.mystlib.setup.singletons.IModBlock;

import java.util.Collection;

/**
 * Created by Mysteryem on 2016-12-24.
 */
public abstract class AbstractBlockRegistry<SINGLETON extends IModBlock<?>, COLLECTION extends Collection<SINGLETON>> extends AbstractModObjectRegistry<SINGLETON, COLLECTION> {
    public AbstractBlockRegistry(COLLECTION modObjects) {
        super(modObjects);
    }
}

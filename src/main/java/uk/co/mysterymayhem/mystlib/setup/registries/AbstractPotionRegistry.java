package uk.co.mysterymayhem.mystlib.setup.registries;

import uk.co.mysterymayhem.mystlib.setup.singletons.IModPotion;

import java.util.Collection;

/**
 * Created by Mysteryem on 15/03/2017.
 */
public abstract class AbstractPotionRegistry<SINGLETON extends IModPotion<?>, COLLECTION extends Collection<SINGLETON>> extends AbstractModObjectRegistry<SINGLETON, COLLECTION> {
    public AbstractPotionRegistry(COLLECTION modObjects) {
        super(modObjects);
    }
}

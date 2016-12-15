package uk.co.mysterymayhem.gravitymod.common.registries;

import uk.co.mysterymayhem.gravitymod.IFMLStaged;

/**
 * Created by Mysteryem on 2016-12-07.
 */
public interface IModObject<T> extends IFMLStaged {

    String getName();
    @SuppressWarnings("unchecked")
    default T getCast() {
        return (T)this;
    }
}

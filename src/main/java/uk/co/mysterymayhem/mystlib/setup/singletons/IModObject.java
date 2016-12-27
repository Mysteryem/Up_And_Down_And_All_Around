package uk.co.mysterymayhem.mystlib.setup.singletons;

import net.minecraft.creativetab.CreativeTabs;
import uk.co.mysterymayhem.mystlib.setup.IFMLStaged;

/**
 * Created by Mysteryem on 2016-12-07.
 */
public interface IModObject<T> extends IFMLStaged {

    String getName();
    String getModID();
    @SuppressWarnings("unchecked")
    default T getCast() {
        return (T)this;
    }
    default CreativeTabs getModCreativeTab() {
        return null;
    }
}

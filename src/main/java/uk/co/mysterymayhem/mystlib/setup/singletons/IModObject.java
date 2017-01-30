package uk.co.mysterymayhem.mystlib.setup.singletons;

import net.minecraft.creativetab.CreativeTabs;
import uk.co.mysterymayhem.mystlib.setup.IFMLStaged;

/**
 * Created by Mysteryem on 2016-12-07.
 */
public interface IModObject extends IFMLStaged {

    /**
     * Get a name of this IModObject, often you would ensure this is unique, but it depends on the circumstances
     *
     * @return
     */
    String getName();
//    default String getName() {
//        return this.getClass().getSimpleName().toLowerCase(Locale.ENGLISH);
//    }

    /**
     * Get the String ID of your mod
     *
     * @return
     */
    String getModID();

    /**
     * Get your mod's instance. Use @Mod.Instance(&lt;modID&gt;) on a field and return that field in this method.
     *
     * @return
     */
    Object getModInstance();

    /**
     * Get the creative tab for this item/block, not really useful for other types of IModObject,
     * but cleaner than having a separate Interface for IModBlock and IModItem to extend.
     *
     * @return
     */
    default CreativeTabs getModCreativeTab() {
        return null;
    }
}

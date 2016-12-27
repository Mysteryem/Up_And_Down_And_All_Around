package uk.co.mysterymayhem.mystlib.setup;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by Mysteryem on 2016-12-07.
 */
public interface IFMLStaged {
    default void preInit() {}
    default void init() {}
    default void postInit() {}

    @SideOnly(Side.CLIENT)
    default void preInitClient() {}
    @SideOnly(Side.CLIENT)
    default void initClient() {}
    @SideOnly(Side.CLIENT)
    default void postInitClient() {}
}

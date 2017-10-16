package uk.co.mysterymayhem.mystlib.setup.singletons;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

/**
 * Created by Mysteryem on 15/10/2017.
 */
public interface IModRegistryEntry<T extends IForgeRegistryEntry<T>> {
    void register(IForgeRegistry<T> registry);

    @SideOnly(Side.CLIENT)
    default void registerClient(IForgeRegistry<T> registry) {}
}

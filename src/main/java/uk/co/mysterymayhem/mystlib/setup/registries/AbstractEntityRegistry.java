package uk.co.mysterymayhem.mystlib.setup.registries;

import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import uk.co.mysterymayhem.mystlib.setup.singletons.IModEntityClassWrapper;

import java.util.Collection;

/**
 * Created by Mysteryem on 2016-12-24.
 */
public abstract class AbstractEntityRegistry<SINGLETON extends IModEntityClassWrapper<?>, COLLECTION extends Collection<SINGLETON>> extends AbstractRegistrableModObjectRegistry<SINGLETON, COLLECTION> {
    public AbstractEntityRegistry(COLLECTION modObjects) {
        super(modObjects);
    }

    @Override
    public void onRegisterEntityEntries(RegistryEvent.Register<EntityEntry> event) {
        this.addToCollection(this.getCollection());
        this.getCollection().forEach(wrapper -> wrapper.register(event.getRegistry()));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void onRegisterEntityEntriesClient(RegistryEvent.Register<EntityEntry> event) {
        this.getCollection().forEach(wrapper -> wrapper.registerClient(event.getRegistry()));
    }
}

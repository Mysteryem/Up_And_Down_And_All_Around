package uk.co.mysterymayhem.mystlib.setup.registries;

import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import uk.co.mysterymayhem.mystlib.setup.singletons.IModItem;

import java.util.Collection;

/**
 * Created by Mysteryem on 2016-12-24.
 */
public abstract class AbstractItemRegistry<SINGLETON extends IModItem<?>, COLLECTION extends Collection<SINGLETON>> extends AbstractRegistrableModObjectRegistry<SINGLETON, COLLECTION> {
    public AbstractItemRegistry(COLLECTION modObjects) {
        super(modObjects);
    }

    @Override
    public void onRegisterItems(RegistryEvent.Register<Item> event) {
        this.addToCollection(this.getCollection());
        this.getCollection().forEach(item -> item.register(event.getRegistry()));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void onRegisterItemsClient(RegistryEvent.Register<Item> event) {
        this.getCollection().forEach(item -> item.registerClient(event.getRegistry()));
    }
}

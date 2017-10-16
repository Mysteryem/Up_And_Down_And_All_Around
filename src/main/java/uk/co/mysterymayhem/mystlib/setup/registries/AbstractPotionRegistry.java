package uk.co.mysterymayhem.mystlib.setup.registries;

import net.minecraft.potion.Potion;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import uk.co.mysterymayhem.mystlib.setup.singletons.IModPotion;

import java.util.Collection;

/**
 * Created by Mysteryem on 15/03/2017.
 */
public abstract class AbstractPotionRegistry<SINGLETON extends IModPotion<?>, COLLECTION extends Collection<SINGLETON>> extends AbstractRegistrableModObjectRegistry<SINGLETON, COLLECTION> {
    public AbstractPotionRegistry(COLLECTION modObjects) {
        super(modObjects);
    }

    @Override
    public void onRegisterPotions(RegistryEvent.Register<Potion> event) {
        this.addToCollection(this.getCollection());
        this.getCollection().forEach(potion -> potion.register(event.getRegistry()));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void onRegisterPotionsClient(RegistryEvent.Register<Potion> event) {
        this.getCollection().forEach(potion -> potion.registerClient(event.getRegistry()));
    }
}

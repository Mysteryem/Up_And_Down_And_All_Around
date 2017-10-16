package uk.co.mysterymayhem.mystlib.setup.registries;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import uk.co.mysterymayhem.mystlib.setup.IFMLStaged;
import uk.co.mysterymayhem.mystlib.setup.singletons.IModBlock;
import uk.co.mysterymayhem.mystlib.setup.singletons.IModItem;
import uk.co.mysterymayhem.mystlib.setup.singletons.IModPotion;

import java.util.Collection;

/**
 * Created by Mysteryem on 2016-12-25.
 */
public abstract class AbstractIFMLStagedRegistry<SINGLETON extends IFMLStaged, COLLECTION extends Collection<SINGLETON>> implements IFMLStaged {
    private final COLLECTION modObjects;

    public AbstractIFMLStagedRegistry(COLLECTION modObjects) {
        this.modObjects = modObjects;
    }

    @Override
    public void preInit() {
        MinecraftForge.EVENT_BUS.register(this);

        this.addToCollection(this.getCollection());
        this.getCollection().forEach(IFMLStaged::preInit);
    }

    @SubscribeEvent
    public void onRegisterBlocks(RegistryEvent.Register<Block> event) {
        this.getCollection().stream().filter((a) -> a instanceof IModBlock).forEach((a) -> ((IModBlock) a).register(event.getRegistry()));
    }

    @SubscribeEvent
    public void onRegisterItems(RegistryEvent.Register<Item> event) {
        this.getCollection().stream().filter((a) -> a instanceof IModItem).forEach((a) -> ((IModItem) a).register(event.getRegistry()));
    }

    @SubscribeEvent
    public void onRegisterPotions(RegistryEvent.Register<Potion> event) {
        this.getCollection().stream().filter((a) -> a instanceof IModPotion).forEach((a) -> ((IModPotion) a).register(event.getRegistry()));
    }

    protected abstract void addToCollection(COLLECTION modObjects);

    public COLLECTION getCollection() {
        return this.modObjects;
    }

    @Override
    public void init() {
        this.getCollection().forEach(IFMLStaged::init);
    }

    @Override
    public void postInit() {
        this.getCollection().forEach(IFMLStaged::postInit);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void initClient() {
        this.getCollection().forEach(IFMLStaged::initClient);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void preInitClient() {
        this.getCollection().forEach(IFMLStaged::preInitClient);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void postInitClient() {
        this.getCollection().forEach(IFMLStaged::postInitClient);
    }
}

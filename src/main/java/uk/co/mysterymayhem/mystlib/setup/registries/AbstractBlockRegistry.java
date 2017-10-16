package uk.co.mysterymayhem.mystlib.setup.registries;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import uk.co.mysterymayhem.mystlib.setup.singletons.IModBlock;
import uk.co.mysterymayhem.mystlib.setup.singletons.IModBlockWithItem;

import java.util.Collection;

/**
 * Created by Mysteryem on 2016-12-24.
 */
public abstract class AbstractBlockRegistry<SINGLETON extends IModBlock<?>, COLLECTION extends Collection<SINGLETON>> extends
        AbstractRegistrableModObjectRegistry<SINGLETON, COLLECTION> {
    public AbstractBlockRegistry(COLLECTION modObjects) {
        super(modObjects);
    }

    @Override
    public void onRegisterBlocks(RegistryEvent.Register<Block> event) {
        this.addToCollection(this.getCollection());
        this.getCollection().forEach(block -> block.register(event.getRegistry()));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void onRegisterBlocksClient(RegistryEvent.Register<Block> event) {
        this.getCollection().forEach(block -> block.registerClient(event.getRegistry()));
    }

    @Override
    public void onRegisterItems(RegistryEvent.Register<Item> event) {
        this.getCollection().forEach(block -> {
            if (block instanceof IModBlockWithItem) {
                ((IModBlockWithItem<?,?>)block).registerItem(event.getRegistry());
            }
        });
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void onRegisterItemsClient(RegistryEvent.Register<Item> event) {
        this.getCollection().forEach(block -> {
            if (block instanceof IModBlockWithItem) {
                ((IModBlockWithItem<?,?>)block).registerItemClient(event.getRegistry());
            }
        });
    }
}

package uk.co.mysterymayhem.mystlib.setup.registries;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import uk.co.mysterymayhem.mystlib.setup.singletons.IModGUIProvider;

import java.util.List;

/**
 * Created by Mysteryem on 2017-01-03.
 */
public abstract class AbstractGUIHandlerRegistry<SINGLETON extends IModGUIProvider<?>, COLLECTION extends List<SINGLETON>> extends AbstractModObjectRegistry<SINGLETON, COLLECTION> implements IGuiHandler {

    public abstract Object getModInstance();

    public AbstractGUIHandlerRegistry(COLLECTION serverSide) {
        super(serverSide);
    }

    @Override
    public void preInit() {
        NetworkRegistry.INSTANCE.registerGuiHandler(this.getModInstance(), this);
        super.preInit();

        COLLECTION collection = this.getCollection();

        for (int i = 0, length = collection.size(); i < length; i++) {
            collection.get(i).setModGUIID(i);
        }
    }

    private boolean indexInRange(int index) {
        return index < this.getCollection().size() && index >= 0;
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (indexInRange(ID)) {
            return this.getCollection().get(ID).getServerSide(player, world, x, y, z);
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (indexInRange(ID)) {
            return this.getCollection().get(ID).getClientSide(player, world, x, y, z);
        }
        return null;
    }
}

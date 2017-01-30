package uk.co.mysterymayhem.mystlib.setup.singletons;

import net.minecraft.inventory.Container;

/**
 * Created by Mysteryem on 2017-01-08.
 */
public abstract class AbstractGuiProvider<S extends Container> implements IModGUIProvider<S> {
    private int modGUIID = -1;

    @Override
    public int getModGUIID() {
        return modGUIID;
    }

    @Override
    public void setModGUIID(int modGUIID) {
        this.modGUIID = modGUIID;
    }
}

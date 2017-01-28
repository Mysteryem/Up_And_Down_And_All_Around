package uk.co.mysterymayhem.gravitymod.common.registries;

import uk.co.mysterymayhem.gravitymod.GravityMod;
import uk.co.mysterymayhem.gravitymod.common.blocks.gravitygenerator.GUIProviderGravityGenerator;
import uk.co.mysterymayhem.mystlib.setup.registries.AbstractGUIHandlerRegistry;
import uk.co.mysterymayhem.mystlib.setup.singletons.IModGUIProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mysteryem on 2017-01-03.
 */
public class ModGUIs extends AbstractGUIHandlerRegistry<IModGUIProvider<?>, List<IModGUIProvider<?>>> {
    static boolean STATIC_SETUP_ALLOWED = false;

    static GUIProviderGravityGenerator GUI_GRAVITY_GENERATOR;

    public ModGUIs() {
        super(new ArrayList<>());
    }

    @Override
    public Object getModInstance() {
        return GravityMod.INSTANCE;
    }

    @Override
    protected void addToCollection(List<IModGUIProvider<?>> modObjects) {
        modObjects.add(GUI_GRAVITY_GENERATOR = new GUIProviderGravityGenerator());
        STATIC_SETUP_ALLOWED = true;
    }
}

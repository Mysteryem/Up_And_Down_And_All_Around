package uk.co.mysterymayhem.gravitymod.common.registries;

import uk.co.mysterymayhem.gravitymod.common.blocks.gravitygenerator.GUIProviderGravityGenerator;

/**
 * Created by Mysteryem on 2017-01-08.
 */
public class StaticGUIs {
    public static final GUIProviderGravityGenerator GUI_GRAVITY_GENERATOR;

    static {
        if (ModGUIs.STATIC_SETUP_ALLOWED) {
            GUI_GRAVITY_GENERATOR = ModGUIs.GUI_GRAVITY_GENERATOR;
        }
        else {
            throw new RuntimeException("Static GUI provider registry setup occurring too early");
        }
    }
}

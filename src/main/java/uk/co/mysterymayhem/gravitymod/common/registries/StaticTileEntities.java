package uk.co.mysterymayhem.gravitymod.common.registries;

import uk.co.mysterymayhem.gravitymod.common.blocks.gravitygenerator.TileWrapper;

/**
 * Created by Mysteryem on 2017-01-03.
 */
public class StaticTileEntities {
    public static final TileWrapper GRAVITY_GENERATOR;
    static {
        if (ModTileEntities.STATIC_SETUP_ALLOWED) {
            GRAVITY_GENERATOR = ModTileEntities.tileGravityGenerator;
        }
        else {
            throw new RuntimeException("Static tile entity wrapper registry setup occurring too early");
        }
    }
}

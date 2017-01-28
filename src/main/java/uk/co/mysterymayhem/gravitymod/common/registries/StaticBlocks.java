package uk.co.mysterymayhem.gravitymod.common.registries;

import uk.co.mysterymayhem.gravitymod.common.blocks.BlockLiquidAntiMass;
import uk.co.mysterymayhem.gravitymod.common.blocks.BlockRestabilisedGravityDust;
import uk.co.mysterymayhem.gravitymod.common.blocks.gravitygenerator.BlockGravityGenerator;

/**
 * Created by Mysteryem on 2017-01-26.
 */
public class StaticBlocks {
    public static final BlockGravityGenerator GRAVITY_GENERATOR;
    public static final BlockLiquidAntiMass LIQUID_ANTI_MASS;
    public static final BlockRestabilisedGravityDust RESTABILISED_GRAVITY_DUST_BLOCK;

    static {
        if (ModBlocks.REGISTRY_SETUP_ALLOWED) {
            GRAVITY_GENERATOR = ModBlocks.gravityGenerator;
            LIQUID_ANTI_MASS = ModBlocks.liquidAntiMass;
            RESTABILISED_GRAVITY_DUST_BLOCK = ModBlocks.restabilisedGravityDust_Block;
        }
        else {
            throw new RuntimeException("Static block registry setup occurring too early");
        }
    }
}

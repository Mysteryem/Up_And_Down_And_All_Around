package uk.co.mysterymayhem.gravitymod.common.registries;

import uk.co.mysterymayhem.gravitymod.common.blocks.BlockLiquidAntiMass;
import uk.co.mysterymayhem.gravitymod.common.blocks.BlockRestabilisedGravityDust;
import uk.co.mysterymayhem.gravitymod.common.blocks.gravitygenerator.BlockGravityGenerator;
import uk.co.mysterymayhem.mystlib.setup.registries.AbstractBlockRegistry;

import java.util.ArrayList;

/**
 * Created by Mysteryem on 2016-12-07.
 */
public class ModBlocks extends AbstractBlockRegistry<IGravityModBlock<?>, ArrayList<IGravityModBlock<?>>> {

    static boolean REGISTRY_SETUP_ALLOWED = false;

    static BlockGravityGenerator gravityGenerator;
    static BlockLiquidAntiMass liquidAntiMass;
    static BlockRestabilisedGravityDust restabilisedGravityDust_Block;

    public ModBlocks() {
        super(new ArrayList<>());
    }

    @Override
    protected void addToCollection(ArrayList<IGravityModBlock<?>> modObjects) {
        modObjects.add(gravityGenerator = new BlockGravityGenerator());
        modObjects.add(liquidAntiMass = new BlockLiquidAntiMass());
        modObjects.add(restabilisedGravityDust_Block = new BlockRestabilisedGravityDust());
        REGISTRY_SETUP_ALLOWED = true;
    }

}

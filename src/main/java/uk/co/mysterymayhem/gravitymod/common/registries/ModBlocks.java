package uk.co.mysterymayhem.gravitymod.common.registries;

import uk.co.mysterymayhem.gravitymod.api.EnumGravityTier;
import uk.co.mysterymayhem.gravitymod.common.blocks.BlockGravityPlate;
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
    static BlockGravityPlate weakGravityPlate;
    static BlockGravityPlate normalGravityPlate;
    static BlockGravityPlate strongGravityPlate;

    public ModBlocks() {
        super(new ArrayList<>());
    }

    @Override
    protected void addToCollection(ArrayList<IGravityModBlock<?>> modObjects) {
        modObjects.add(gravityGenerator = new BlockGravityGenerator());
        modObjects.add(liquidAntiMass = new BlockLiquidAntiMass());
        modObjects.add(restabilisedGravityDust_Block = new BlockRestabilisedGravityDust());
        modObjects.add(weakGravityPlate = new BlockGravityPlate(EnumGravityTier.WEAK));
        modObjects.add(normalGravityPlate = new BlockGravityPlate(EnumGravityTier.NORMAL));
        modObjects.add(strongGravityPlate = new BlockGravityPlate(EnumGravityTier.STRONG));
        REGISTRY_SETUP_ALLOWED = true;
    }

}

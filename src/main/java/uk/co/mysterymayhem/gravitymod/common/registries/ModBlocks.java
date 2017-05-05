package uk.co.mysterymayhem.gravitymod.common.registries;

import uk.co.mysterymayhem.gravitymod.api.EnumGravityTier;
import uk.co.mysterymayhem.gravitymod.common.blocks.BlockGravityOre;
import uk.co.mysterymayhem.gravitymod.common.blocks.BlockGravityPlate;
import uk.co.mysterymayhem.gravitymod.common.blocks.BlockLiquidAntiMass;
import uk.co.mysterymayhem.gravitymod.common.blocks.BlockRestabilisedGravityDust;
import uk.co.mysterymayhem.gravitymod.common.blocks.gravitygenerator.BlockGravityGenerator;
import uk.co.mysterymayhem.mystlib.setup.registries.AbstractBlockRegistry;
import uk.co.mysterymayhem.mystlib.setup.singletons.IModBlock;

import java.util.ArrayList;

/**
 * Created by Mysteryem on 2016-12-07.
 */
public class ModBlocks extends AbstractBlockRegistry<IModBlock<? extends IGravityModCommon>, ArrayList<IModBlock<? extends IGravityModCommon>>> {

    static boolean REGISTRY_SETUP_ALLOWED = false;

    static BlockGravityGenerator gravityGenerator;
    static BlockLiquidAntiMass liquidAntiMass;
    static BlockRestabilisedGravityDust restabilisedGravityDust_Block;
    static BlockGravityPlate weakGravityPlate;
    static BlockGravityPlate normalGravityPlate;
    static BlockGravityPlate strongGravityPlate;
    static BlockGravityOre blockGravityOre;

    public ModBlocks() {
        super(new ArrayList<>());
    }

    @Override
    protected void addToCollection(ArrayList<IModBlock<? extends IGravityModCommon>> modObjects) {
        modObjects.add(gravityGenerator = new BlockGravityGenerator());
        modObjects.add(liquidAntiMass = new BlockLiquidAntiMass());
        modObjects.add(restabilisedGravityDust_Block = new BlockRestabilisedGravityDust());
        modObjects.add(weakGravityPlate = new BlockGravityPlate(EnumGravityTier.WEAK));
        modObjects.add(normalGravityPlate = new BlockGravityPlate(EnumGravityTier.NORMAL));
        modObjects.add(strongGravityPlate = new BlockGravityPlate(EnumGravityTier.STRONG));
        modObjects.add(blockGravityOre = new BlockGravityOre());
        REGISTRY_SETUP_ALLOWED = true;
    }

}

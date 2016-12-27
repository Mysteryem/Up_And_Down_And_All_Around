package uk.co.mysterymayhem.gravitymod.common.registries;

import uk.co.mysterymayhem.gravitymod.common.blocks.BlockGravityGenerator;
import uk.co.mysterymayhem.mystlib.setup.registries.AbstractBlockRegistry;

import java.util.ArrayList;

/**
 * Created by Mysteryem on 2016-12-07.
 */
public class ModBlocks extends AbstractBlockRegistry<IGravityModBlock<?,?>, ArrayList<IGravityModBlock<?,?>>> {

    static boolean REGISTRY_SETUP_ALLOWED = false;

//    static BlockGravityGenerator WEAK_GRAVITY_GENERATOR;
//    static BlockGravityGenerator NORMAL_GRAVITY_GENERATOR;
//    static BlockGravityGenerator STRONG_GRAVITY_GENERATOR;
    static BlockGravityGenerator GRAVITY_GENERATOR;

    public ModBlocks() {
        super(new ArrayList<>());
    }

    @Override
    protected void addToCollection(ArrayList<IGravityModBlock<?, ?>> modObjects) {
        modObjects.add(GRAVITY_GENERATOR = new BlockGravityGenerator());
//        modObjects.add(WEAK_GRAVITY_GENERATOR = new BlockGravityGenerator(EnumGravityTier.WEAK));
//        modObjects.add(NORMAL_GRAVITY_GENERATOR = new BlockGravityGenerator(EnumGravityTier.NORMAL));
//        modObjects.add(STRONG_GRAVITY_GENERATOR = new BlockGravityGenerator(EnumGravityTier.STRONG));
        REGISTRY_SETUP_ALLOWED = true;
    }

}

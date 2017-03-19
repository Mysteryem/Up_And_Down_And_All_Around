package uk.co.mysterymayhem.gravitymod.common.registries;

import uk.co.mysterymayhem.gravitymod.common.potions.AbstractGravityModPotion;
import uk.co.mysterymayhem.gravitymod.common.potions.BloodBoilPotion;
import uk.co.mysterymayhem.gravitymod.common.potions.NoEffectPotion;
import uk.co.mysterymayhem.mystlib.setup.registries.AbstractPotionRegistry;

import java.util.ArrayList;

/**
 * Created by Mysteryem on 16/03/2017.
 */
public class ModPotions extends AbstractPotionRegistry<AbstractGravityModPotion<?>, ArrayList<AbstractGravityModPotion<?>>> {

    static boolean REGISTRY_SETUP_ALLOWED = false;
    static NoEffectPotion asphyxiation;
    static NoEffectPotion freezing;
    static BloodBoilPotion bloodboil;

    public ModPotions() {
        super(new ArrayList<>());
    }

    @Override
    protected void addToCollection(ArrayList<AbstractGravityModPotion<?>> modObjects) {
        modObjects.add(asphyxiation = new NoEffectPotion("asphyxiation", true, 1).setIconIndex(0));
        modObjects.add(freezing = new NoEffectPotion("freezing", true, 1).setIconIndex(1));
        modObjects.add(bloodboil = new BloodBoilPotion(true, 1).setIconIndex(2));
        REGISTRY_SETUP_ALLOWED = true;
    }
}

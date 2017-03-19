package uk.co.mysterymayhem.gravitymod.common.registries;

import uk.co.mysterymayhem.gravitymod.common.potions.BloodBoilPotion;
import uk.co.mysterymayhem.gravitymod.common.potions.NoEffectPotion;

/**
 * Created by Mysteryem on 16/03/2017.
 */
public class StaticPotions {
    public static final NoEffectPotion ASPHYXIATION;
    public static final NoEffectPotion FREEZING;
    public static final BloodBoilPotion BLOODBOIL;
    static {
        if (ModPotions.REGISTRY_SETUP_ALLOWED) {
            ASPHYXIATION = ModPotions.asphyxiation;
            FREEZING = ModPotions.freezing;
            BLOODBOIL = ModPotions.bloodboil;
        }
        else {
            throw new RuntimeException("Static potion registry accessed too early");
        }
    }
}

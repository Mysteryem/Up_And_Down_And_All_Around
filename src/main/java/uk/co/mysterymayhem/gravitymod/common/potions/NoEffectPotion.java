package uk.co.mysterymayhem.gravitymod.common.potions;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Created by Mysteryem on 16/03/2017.
 */
public class NoEffectPotion extends AbstractGravityModPotion<NoEffectPotion> {

    public NoEffectPotion(String modObjectName, boolean isBadEffectIn, int liquidColorIn) {
        super(modObjectName, isBadEffectIn, liquidColorIn);
    }

    @ParametersAreNonnullByDefault
    @Override
    public void affectEntity(@Nullable Entity source, @Nullable Entity indirectSource, EntityLivingBase entityLivingBaseIn, int amplifier, double health) {
//        super.affectEntity(source, indirectSource, entityLivingBaseIn, amplifier, health);
    }

    @ParametersAreNonnullByDefault
    @Override
    public void performEffect(EntityLivingBase entityLivingBaseIn, int p_76394_2_) {
//        super.performEffect(entityLivingBaseIn, p_76394_2_);
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return false;
    }
}

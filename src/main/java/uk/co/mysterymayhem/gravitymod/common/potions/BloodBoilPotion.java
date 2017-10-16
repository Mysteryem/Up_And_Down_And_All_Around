package uk.co.mysterymayhem.gravitymod.common.potions;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import uk.co.mysterymayhem.gravitymod.GravityMod;
import uk.co.mysterymayhem.gravitymod.common.config.ConfigHandler;
import uk.co.mysterymayhem.gravitymod.common.listeners.FallOutOfWorldUpwardsListenerCommon;

import javax.annotation.Nullable;

/**
 * Created by Mysteryem on 16/03/2017.
 */
public class BloodBoilPotion extends AbstractGravityModPotion<BloodBoilPotion> {
    public BloodBoilPotion(boolean isBadEffectIn, int liquidColorIn) {
        super("bloodboil", isBadEffectIn, liquidColorIn);
    }

    @Override
    public void affectEntity(@Nullable Entity source, @Nullable Entity indirectSource, EntityLivingBase entityLivingBaseIn, int amplifier, double health) {
        GravityMod.logInfo("affectingentity");
    }

    @Override
    public void performEffect(EntityLivingBase entityLivingBaseIn, int p_76394_2_) {
        entityLivingBaseIn.attackEntityFrom(FallOutOfWorldUpwardsListenerCommon.getBloodBoilDamageSource(), ConfigHandler.bloodBoilDamage);
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return true;
    }
}

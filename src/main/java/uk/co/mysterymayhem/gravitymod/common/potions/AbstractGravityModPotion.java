package uk.co.mysterymayhem.gravitymod.common.potions;

import net.minecraft.util.ResourceLocation;
import uk.co.mysterymayhem.gravitymod.GravityMod;
import uk.co.mysterymayhem.gravitymod.common.registries.IGravityModCommon;
import uk.co.mysterymayhem.mystlib.setup.singletons.AbstractPotion;

/**
 * Created by Mysteryem on 16/03/2017.
 */
public abstract class AbstractGravityModPotion<POTION extends AbstractGravityModPotion<POTION>> extends AbstractPotion<POTION> implements IGravityModCommon {
    private static final ResourceLocation TEXTURE = new ResourceLocation(GravityMod.MOD_ID, "textures/gui/potions.png");
    private final String modObjectName;

    public AbstractGravityModPotion(String modObjectName, boolean isBadEffectIn, int liquidColorIn) {
        super(isBadEffectIn, liquidColorIn);
        this.modObjectName = modObjectName;
    }

    @Override
    public String getModObjectName() {
        return this.modObjectName;
    }

    @Override
    public ResourceLocation getTextureResource() {
        return TEXTURE;
    }
}

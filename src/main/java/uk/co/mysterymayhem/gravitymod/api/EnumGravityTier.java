package uk.co.mysterymayhem.gravitymod.api;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.IStringSerializable;
import uk.co.mysterymayhem.gravitymod.common.listeners.GravityManagerCommon;
import uk.co.mysterymayhem.gravitymod.common.registries.GravityPriorityRegistry;

/**
 * Created by Mysteryem on 2016-12-25.
 */
public enum EnumGravityTier implements IStringSerializable {
    WEAK("weak", new float[]{1, 1, 1}) {
        @Override
        public int getPriority(double percent) {
            return GravityPriorityRegistry.interpolateWeak(percent);
        }

        @Override
        public boolean isPlayerAffected(EntityPlayerMP playerMP) {
            return GravityManagerCommon.playerIsAffectedByWeakGravity(playerMP);
        }
    },
    NORMAL("normal", new float[]{0.66796875f, 0, 0.66796875f}) {
        @Override
        public int getPriority(double percent) {
            return GravityPriorityRegistry.interpolateNormal(percent);
        }

        @Override
        public boolean isPlayerAffected(EntityPlayerMP playerMP) {
            return GravityManagerCommon.playerIsAffectedByNormalGravity(playerMP);
        }
    },
    STRONG("strong", new float[]{0.3359375f, 0.3359375f, 1}) {
        @Override
        public int getPriority(double percent) {
            return GravityPriorityRegistry.interpolateStrong(percent);
        }

        @Override
        public boolean isPlayerAffected(EntityPlayerMP playerMP) {
            return GravityManagerCommon.playerIsAffectedByStrongGravity(playerMP);
        }
    };

    private final float[] colour;
    private final String internalPrefix;

    EnumGravityTier(String internalPrefix, float[] colour) {
        this.internalPrefix = internalPrefix;
        this.colour = colour;
    }

    public abstract int getPriority(double percent);

    public abstract boolean isPlayerAffected(EntityPlayerMP playerMP);

    public float[] getColour() {
        return colour;
    }

    @Override
    public String getName() {
        return this.getInternalPrefix();
    }

    public String getInternalPrefix() {
        return internalPrefix;
    }
}

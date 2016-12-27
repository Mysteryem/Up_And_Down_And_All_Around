package uk.co.mysterymayhem.gravitymod.api;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.IStringSerializable;
import uk.co.mysterymayhem.gravitymod.common.listeners.GravityManagerCommon;
import uk.co.mysterymayhem.gravitymod.common.registries.GravityPriorityRegistry;

/**
 * Created by Mysteryem on 2016-12-25.
 */
public enum EnumGravityTier implements IStringSerializable {
    WEAK("weak") {
        @Override
        public int getPriority(double percent) {
            return GravityPriorityRegistry.interpolateWeak(percent);
        }

        @Override
        public boolean isPlayerAffected(EntityPlayerMP playerMP) {
            return GravityManagerCommon.playerIsAffectedByWeakGravity(playerMP);
        }
    },
    NORMAL("normal") {
        @Override
        public int getPriority(double percent) {
            return GravityPriorityRegistry.interpolateNormal(percent);
        }

        @Override
        public boolean isPlayerAffected(EntityPlayerMP playerMP) {
            return GravityManagerCommon.playerIsAffectedByNormalGravity(playerMP);
        }
    },
    STRONG("strong") {
        @Override
        public int getPriority(double percent) {
            return GravityPriorityRegistry.interpolateStrong(percent);
        }

        @Override
        public boolean isPlayerAffected(EntityPlayerMP playerMP) {
            return GravityManagerCommon.playerIsAffectedByStrongGravity(playerMP);
        }
    };

    public abstract int getPriority(double percent);

    public abstract boolean isPlayerAffected(EntityPlayerMP playerMP);

    private final String internalPrefix;

    EnumGravityTier(String internalPrefix) {
        this.internalPrefix = internalPrefix;
    }

    public String getInternalPrefix() {
        return internalPrefix;
    }

    @Override
    public String getName() {
        return this.getInternalPrefix();
    }
}

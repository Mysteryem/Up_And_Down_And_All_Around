package uk.co.mysterymayhem.gravitymod.common.modsupport.prepostmodifier;

import uk.co.mysterymayhem.gravitymod.asm.EntityPlayerWithGravity;
import uk.co.mysterymayhem.gravitymod.asm.Hooks;

import java.util.HashMap;
import java.util.Locale;

/**
 * All the possible PrePostModifiers, all absolute is the default state, so is not included.
 *
 * Created by Mysteryem on 2016-10-23.
 */
public enum EnumPrePostModifier implements IPrePostModifier<EntityPlayerWithGravity> {
    ALL_MOTION_RELATIVE(true, "relativemotionall") {
        @Override
        public void preModify(EntityPlayerWithGravity player) {
            player.makeMotionRelative();
        }

        @Override
        public void postModify(EntityPlayerWithGravity player) {
            player.popMotionStack();
        }
    },
    ABSOLUTE_X(true, "absolutemotionx") {
        @Override
        public void preModify(EntityPlayerWithGravity player) {
            player.makeMotionRelative();
            player.storeMotionX();
        }

        @Override
        public void postModify(EntityPlayerWithGravity player) {
            double motionXChange = player.undoMotionXChange();
            player.popMotionStack();
            player.motionX += motionXChange;
        }
    },
    RELATIVE_Z(true, "relativemotionz") {
        @Override
        public void preModify(EntityPlayerWithGravity player) {
            player.makeMotionRelative();
            player.storeMotionX();
            player.storeMotionY();
        }

        @Override
        public void postModify(EntityPlayerWithGravity player) {
            double motionXChange = player.undoMotionXChange();
            double motionYChange = player.undoMotionYChange();
            player.popMotionStack();
            player.motionX += motionXChange;
            player.motionY += motionYChange;
        }
    },
    RELATIVE_Y(true, "relativemotiony") {
        @Override
        public void preModify(EntityPlayerWithGravity player) {
            player.makeMotionRelative();
            player.storeMotionX();
            player.storeMotionZ();
        }

        @Override
        public void postModify(EntityPlayerWithGravity player) {
            double motionXChange = player.undoMotionXChange();
            double motionZChange = player.undoMotionZChange();
            player.popMotionStack();
            player.motionX += motionXChange;
            player.motionZ += motionZChange;
        }
    },
    ABSOLUTE_Y(true, "absolutemotiony") {
        @Override
        public void preModify(EntityPlayerWithGravity player) {
            player.makeMotionRelative();
            player.storeMotionY();
        }

        @Override
        public void postModify(EntityPlayerWithGravity player) {
            double motionYChange = player.undoMotionYChange();
            player.popMotionStack();
            player.motionY += motionYChange;
        }
    },
    RELATIVE_X(true, "relativemotionx") {
        @Override
        public void preModify(EntityPlayerWithGravity player) {
            player.makeMotionRelative();
            player.storeMotionY();
            player.storeMotionZ();
        }

        @Override
        public void postModify(EntityPlayerWithGravity player) {
            double motionYChange = player.undoMotionYChange();
            double motionZChange = player.undoMotionZChange();
            player.popMotionStack();
            player.motionY += motionYChange;
            player.motionZ += motionZChange;
        }
    },
    ABSOLUTE_Z(true, "absolutemotionz") {
        @Override
        public void preModify(EntityPlayerWithGravity player) {
            player.makeMotionRelative();
            player.storeMotionZ();
        }

        @Override
        public void postModify(EntityPlayerWithGravity player) {
            double motionZChange = player.undoMotionZChange();
            player.popMotionStack();
            player.motionZ += motionZChange;
        }
    },
    ROTATION_RELATIVE(false, "relativerotation") {
        @Override
        public void preModify(EntityPlayerWithGravity player) {
            player.makeRotationRelative();
        }

        @Override
        public void postModify(EntityPlayerWithGravity player) {
            player.popRotationStack();
        }
    };

    private static final HashMap<String, EnumPrePostModifier> lowerCaseConfigStringToEnumPrePostModifier = new HashMap<>();
    static {
        for (EnumPrePostModifier modifier : EnumPrePostModifier.values()) {
            lowerCaseConfigStringToEnumPrePostModifier.put(modifier.configString, modifier);
        }
    }

    public static EnumPrePostModifier getFromConfigString(String configString) {
        return lowerCaseConfigStringToEnumPrePostModifier.get(configString.toLowerCase(Locale.ENGLISH));
    }

    private final boolean isMotionModifier;
    private final String configString;

    EnumPrePostModifier(boolean isMotionModifier, String configString) {
        this.isMotionModifier = isMotionModifier;
        this.configString = configString;
    }

    @Override
    public int getUniqueID() {
        return this.ordinal();
    }

    public boolean isMotionModifier() {
        return this.isMotionModifier;
    }
}

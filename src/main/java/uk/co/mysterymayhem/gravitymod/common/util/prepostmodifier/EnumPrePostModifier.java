package uk.co.mysterymayhem.gravitymod.common.util.prepostmodifier;

import uk.co.mysterymayhem.gravitymod.asm.EntityPlayerWithGravity;
import uk.co.mysterymayhem.gravitymod.asm.Hooks;

import java.util.HashMap;
import java.util.Locale;

/**
 * All the possible PrePostModifiers, all relative is the default state during a player tick, so is not included.
 * Outside of a player tick, the default state will be ALL_ABSOLUTE, unless called from within a method that is always
 * relative, e.g. moveEntity() or jump()
 *
 * Created by Mysteryem on 2016-10-23.
 */
public enum EnumPrePostModifier implements IPrePostModifier<EntityPlayerWithGravity> {
    ALL_MOTION_RELATIVE(true, "relativemotionall") {
        @Override
        public void preModify(EntityPlayerWithGravity player) {
            Hooks.makeMotionRelative(player);
        }

        @Override
        public void postModify(EntityPlayerWithGravity player) {
            Hooks.popMotionStack(player);
        }
    },
    ABSOLUTE_X(true, "absolutemotionx") {
        @Override
        public void preModify(EntityPlayerWithGravity player) {
            Hooks.makeMotionRelative(player);
            player.storeMotionX();
        }

        @Override
        public void postModify(EntityPlayerWithGravity player) {
            double motionXChange = player.undoMotionXChange();
            Hooks.popMotionStack(player);
            player.motionX += motionXChange;
        }
    },
    RELATIVE_Z(true, "relativemotionz") {
        @Override
        public void preModify(EntityPlayerWithGravity player) {
            Hooks.makeMotionRelative(player);
            player.storeMotionX();
            player.storeMotionY();
        }

        @Override
        public void postModify(EntityPlayerWithGravity player) {
            double motionXChange = player.undoMotionXChange();
            double motionYChange = player.undoMotionYChange();
            Hooks.popMotionStack(player);
            player.motionX += motionXChange;
            player.motionY += motionYChange;
        }
    },
    RELATIVE_Y(true, "relativemotiony") {
        @Override
        public void preModify(EntityPlayerWithGravity player) {
            Hooks.makeMotionRelative(player);
            player.storeMotionX();
            player.storeMotionZ();
        }

        @Override
        public void postModify(EntityPlayerWithGravity player) {
            double motionXChange = player.undoMotionXChange();
            double motionZChange = player.undoMotionZChange();
            Hooks.popMotionStack(player);
            player.motionX += motionXChange;
            player.motionZ += motionZChange;
        }
    },
    ABSOLUTE_Y(true, "absolutemotiony") {
        @Override
        public void preModify(EntityPlayerWithGravity player) {
            Hooks.makeMotionRelative(player);
            player.storeMotionY();
        }

        @Override
        public void postModify(EntityPlayerWithGravity player) {
            double motionYChange = player.undoMotionYChange();
            Hooks.popMotionStack(player);
            player.motionY += motionYChange;
        }
    },
    RELATIVE_X(true, "relativemotionx") {
        @Override
        public void preModify(EntityPlayerWithGravity player) {
            Hooks.makeMotionRelative(player);
            player.storeMotionY();
            player.storeMotionZ();
        }

        @Override
        public void postModify(EntityPlayerWithGravity player) {
            double motionYChange = player.undoMotionYChange();
            double motionZChange = player.undoMotionZChange();
            Hooks.popMotionStack(player);
            player.motionY += motionYChange;
            player.motionZ += motionZChange;
        }
    },
    ABSOLUTE_Z(true, "absolutemotionz") {
        @Override
        public void preModify(EntityPlayerWithGravity player) {
            Hooks.makeMotionRelative(player);
            player.storeMotionZ();
        }

        @Override
        public void postModify(EntityPlayerWithGravity player) {
            double motionZChange = player.undoMotionZChange();
            Hooks.popMotionStack(player);
            player.motionZ += motionZChange;
        }
    },
    ROTATION_RELATIVE(false, "relativerotation") {
        @Override
        public void preModify(EntityPlayerWithGravity thing) {
            Hooks.makeRotationRelative(thing);
        }

        @Override
        public void postModify(EntityPlayerWithGravity thing) {
            Hooks.popRotationStack(thing);
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

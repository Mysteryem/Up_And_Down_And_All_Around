package uk.co.mysterymayhem.gravitymod.common.registries;

import net.minecraft.util.math.MathHelper;

/**
 * Created by Mysteryem on 2016-11-08.
 */
public class GravityPriorityRegistry {
    private static final int NUM_PRIORITIES = 3;
    private static final int NUM_EXTRA_VALUES = 4;

    private static final int PRIORITY_SIZE =
            (int)(    //(max unsigned int - extra values) / number of priorities
                    (((long)Integer.MAX_VALUE - (long)Integer.MIN_VALUE) - NUM_EXTRA_VALUES)
                            / NUM_PRIORITIES
            );

    public static final int WEAK_MIN = Integer.MIN_VALUE;
    public static final int WEAK_MAX = WEAK_MIN + PRIORITY_SIZE;

    public static final int WEAK_GRAVITY_CONTROLLER = WEAK_MAX + 1;

    public static final int NORMAL_MIN = WEAK_GRAVITY_CONTROLLER + 1;
    public static final int NORMAL_MAX = NORMAL_MIN + PRIORITY_SIZE;

    public static final int PERSONAL_GRAVITY_CONTROLLER = NORMAL_MAX + 1;

    public static final int GRAVITY_ANCHOR = PERSONAL_GRAVITY_CONTROLLER + 1;

    public static final int STRONG_MIN = GRAVITY_ANCHOR + 1;
    public static final int STRONG_MAX = STRONG_MIN + PRIORITY_SIZE;

    public static final int ULTIMATE_GRAVITY_CONTROLLER = STRONG_MAX + 1; //Integer.MAX_VALUE

    public static int interpolateNormal(double percent) {
        percent = MathHelper.clamp_double(percent, 0.0, 1.0);
        return (int)(NORMAL_MIN + PRIORITY_SIZE * percent);
    }

    public static int interpolateNormal(float percent) {
        percent = MathHelper.clamp_float(percent, 0.0f, 1.0f);
        return (int)(NORMAL_MIN + PRIORITY_SIZE * percent);
    }

    public static int interpolateStrong(double percent) {
        percent = MathHelper.clamp_double(percent, 0.0, 1.0);
        return (int)(STRONG_MIN + PRIORITY_SIZE * percent);
    }

    public static int interpolateStrong(float percent) {
        percent = MathHelper.clamp_float(percent, 0.0f, 1.0f);
        return (int)(STRONG_MIN + PRIORITY_SIZE * percent);
    }

    public static int interpolateWeak(double percent) {
        percent = MathHelper.clamp_double(percent, 0.0, 1.0);
        return (int)(WEAK_MIN + PRIORITY_SIZE * percent);
    }

    public static int interpolateWeak(float percent) {
        percent = MathHelper.clamp_float(percent, 0.0f, 1.0f);
        return (int)(WEAK_MIN + PRIORITY_SIZE * percent);
    }
}

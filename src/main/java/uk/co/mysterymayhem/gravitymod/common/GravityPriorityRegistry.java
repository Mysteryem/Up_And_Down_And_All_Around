package uk.co.mysterymayhem.gravitymod.common;

/**
 * Created by Mysteryem on 2016-11-08.
 */
public class GravityPriorityRegistry {
    private static final int PRIORITY_SIZE = 2_000_000_000/3;
    public static final int WEAK_MIN = 1;
    public static final int WEAK_MAX = WEAK_MIN + PRIORITY_SIZE - 1;
    public static final int WEAK_GRAVITY_CONTROLLER = WEAK_MAX + 1;
    public static final int PERSONAL_MIN = WEAK_GRAVITY_CONTROLLER + 1;
    public static final int PERSONAL_MAX = PERSONAL_MIN + PRIORITY_SIZE - 1;
    public static final int PERSONAL_GRAVITY_CONTROLLER = PERSONAL_MAX + 1;

    public static final int GRAVITY_ANCHOR = PERSONAL_GRAVITY_CONTROLLER + 1;
    public static final int ULTIMATE_MIN = GRAVITY_ANCHOR + 1;
    public static final int ULTIMATE_MAX = ULTIMATE_MIN + PRIORITY_SIZE - 1;

    public static final int ULTIMATE_GRAVITY_CONTROLLER = Integer.MAX_VALUE;
}

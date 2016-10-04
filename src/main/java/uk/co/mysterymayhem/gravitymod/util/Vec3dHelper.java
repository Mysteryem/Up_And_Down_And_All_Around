package uk.co.mysterymayhem.gravitymod.util;

import net.minecraft.util.math.Vec3d;

/**
 * Created by Mysteryem on 2016-10-01.
 */
public class Vec3dHelper {
    private static final double PI_OVER_180 = Math.PI/180d;
    private static final double ONE_HUNDRED_EIGHTY_OVER_PI = 180d/Math.PI;
    public static final int PITCH = 0;
    public static final int YAW = 1;

    public static float getYaw(Vec3d lookVec) {
        return (float)(Math.atan2(-lookVec.xCoord, lookVec.zCoord) * (180D/Math.PI));
    }

    public static float getPitch(Vec3d lookVec) {
        return (float)-(Math.asin(lookVec.yCoord) * (180D/Math.PI));
    }

    public static double getAbsoluteYawPrecise(Vec3d lookVec) {
        return (Math.atan2(-lookVec.xCoord, lookVec.zCoord) * (180D/Math.PI));
    }

    public static double getAbsolutePitchPrecise(Vec3d lookVec) {
        return -(Math.asin(lookVec.yCoord) * (180D/Math.PI));
    }

    public static Vec3d getPreciseVectorForRotation(double pitch, double yaw) {
            double f = Math.cos(-yaw * PI_OVER_180 - Math.PI);
            double f1 = Math.sin(-yaw * PI_OVER_180 - Math.PI);
            double f2 = -Math.cos(-pitch * PI_OVER_180);
            double f3 = Math.sin(-pitch * PI_OVER_180);
            return new Vec3d((f1 * f2), f3, (f * f2));
    }

    public static double[] getPrecisePitchAndYawFromVector(Vec3d xyz) {
        double pitch = -(Math.asin(xyz.yCoord) * ONE_HUNDRED_EIGHTY_OVER_PI);
        double yaw = Math.atan2(-xyz.xCoord, xyz.zCoord) * ONE_HUNDRED_EIGHTY_OVER_PI;
        return new double[]{pitch, yaw};
    }
}

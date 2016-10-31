package uk.co.mysterymayhem.gravitymod.common.capabilities.gravitydirection;

import net.minecraft.util.math.Vec3d;
import uk.co.mysterymayhem.gravitymod.api.EnumGravityDirection;

/**
 * Created by Mysteryem on 2016-10-10.
 */
public interface IGravityDirectionCapability {
    EnumGravityDirection getDirection();
    EnumGravityDirection getPrevDirection();
    EnumGravityDirection getPendingDirection();
    int getTimeoutTicks();

    void setDirection(EnumGravityDirection direction);
    void setTransitionAngle(double angle);
    void setEyePosChangeVector(Vec3d vec3d);
    Vec3d getEyePosChangeVector();
    double getTransitionAngle();
    boolean hasTransitionAngle();

    /**
     * Used to set client gravity when logging into a server.
     * Possibly also on both sides when a dimension change/respawn occurs
     * @param direction
     */
    void setDirectionNoTimeout(EnumGravityDirection direction);
    void setTimeoutTicks(int newTimeout);
    void setPendingDirection(EnumGravityDirection direction, int priority);
    void tick();
}

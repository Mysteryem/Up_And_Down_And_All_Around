package uk.co.mysterymayhem.gravitymod.common.capabilities.gravitydirection;

import net.minecraft.util.math.Vec3d;
import uk.co.mysterymayhem.gravitymod.api.EnumGravityDirection;

import javax.annotation.Nonnull;

/**
 * Created by Mysteryem on 2016-10-10.
 */
public interface IGravityDirectionCapability {
    EnumGravityDirection getDirection();

    void setDirection(@Nonnull EnumGravityDirection direction);

    @Nonnull Vec3d getEyePosChangeVector();

    void setEyePosChangeVector(@Nonnull Vec3d vec3d);

    EnumGravityDirection getPendingDirection();

    EnumGravityDirection getPrevDirection();

    int getTimeoutTicks();

    int getReverseTimeoutTicks();

    void setReverseTimeoutTicks(int newReverseTimeout);

    void setTimeoutTicks(int newTimeout);

    double getTransitionAngle();

    void setTransitionAngle(double angle);

    boolean hasTransitionAngle();

    boolean timeoutComplete();

    /**
     * Used to set client gravity when logging into a server.
     * Possibly also on both sides when a dimension change/respawn occurs
     *
     * @param direction
     */
    void setDirectionNoTimeout(@Nonnull EnumGravityDirection direction);

    void setPendingDirection(@Nonnull EnumGravityDirection direction, int priority);
    void forceSetPendingDirection(@Nonnull EnumGravityDirection direction, int priority);

    int getPendingPriority();

    int getPreviousTickPriority();

    default void tickCommon() {}

    default void tickServer() {}

    default void tickClient() {}
}

package uk.co.mysterymayhem.gravitymod.common.capabilities.gravitydirection;

import net.minecraft.util.math.Vec3d;
import uk.co.mysterymayhem.gravitymod.api.EnumGravityDirection;

/**
 * Created by Mysteryem on 2016-10-10.
 */
public class GravityDirectionCapabilityImpl implements IGravityDirectionCapability {
    // The direction for this tick
    private EnumGravityDirection direction;
    private Vec3d eyePosChangeVec = null;
    private boolean hasTransitionAngle = false;
    //
    private EnumGravityDirection pendingDirection;
    private int pendingPriority = GravityDirectionCapability.MIN_PRIORITY;
    // Updated when the direction changes. Used to do smooth transitions between
    private EnumGravityDirection prevDirection;
    private int timeoutTicks = 0;
    private double transitionRotationAmount = 0;

    public GravityDirectionCapabilityImpl() {
        this(GravityDirectionCapability.DEFAULT_GRAVITY);
    }

    public GravityDirectionCapabilityImpl(EnumGravityDirection direction) {
        this.direction = direction;
        //TODO: Safe to assume not null?
//        this.direction = Objects.requireNonNull(direction, "Gravity direction cannot be null");
        this.prevDirection = direction;
        this.pendingDirection = direction;
    }

    public EnumGravityDirection getDirection() {
        return this.direction;
    }

    @Override
    public void setDirection(EnumGravityDirection direction) {
        updateDirection(direction);
        this.timeoutTicks = GravityDirectionCapability.DEFAULT_TIMEOUT;
    }

    private void updateDirection(EnumGravityDirection direction) {
        this.prevDirection = this.direction;
        this.direction = direction;
        this.transitionRotationAmount = 0;
        this.hasTransitionAngle = false;
        //TODO: Safe to assume not null?
        //this.direction = Objects.requireNonNull(direction, "New gravity direction cannot be null");
    }

    @Override
    public EnumGravityDirection getPrevDirection() {
        return this.prevDirection;
    }

    @Override
    public EnumGravityDirection getPendingDirection() {
        return this.pendingDirection;
    }

    @Override
    public int getTimeoutTicks() {
        return this.timeoutTicks;
    }

    @Override
    public void setTimeoutTicks(int timeoutTicks) {
        this.timeoutTicks = timeoutTicks;
    }

    @Override
    public void setPendingDirection(EnumGravityDirection direction, int priority) {
        if (this.timeoutTicks == 0 && priority > this.pendingPriority) {
            this.pendingPriority = priority;
            this.pendingDirection = direction;
        }
    }

    @Override
    public void tick() {
        if (this.timeoutTicks > 0) {
            this.timeoutTicks--;
        }
        this.pendingDirection = GravityDirectionCapability.DEFAULT_GRAVITY;
        this.pendingPriority = GravityDirectionCapability.MIN_PRIORITY;
    }

    @Override
    public boolean hasTransitionAngle() {
        return this.hasTransitionAngle;
    }

    @Override
    public double getTransitionAngle() {
        return this.transitionRotationAmount;
    }

    @Override
    public void setTransitionAngle(double angle) {
        this.transitionRotationAmount = angle;
        this.hasTransitionAngle = true;
    }

    @Override
    public Vec3d getEyePosChangeVector() {
        return eyePosChangeVec;
    }

    @Override
    public void setEyePosChangeVector(Vec3d vec3d) {
        this.eyePosChangeVec = vec3d;
    }

    @Override
    public void setDirectionNoTimeout(EnumGravityDirection direction) {
        updateDirection(direction);
        this.timeoutTicks = 0;
    }
}

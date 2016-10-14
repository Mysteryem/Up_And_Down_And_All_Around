package uk.co.mysterymayhem.gravitymod.capabilities;

import uk.co.mysterymayhem.gravitymod.api.EnumGravityDirection;

/**
 * Created by Mysteryem on 2016-10-10.
 */
public class GravityDirectionCapabilityImpl implements IGravityDirectionCapability {
    // Updated when the direction changes. Used to do smooth transitions between
    private EnumGravityDirection prevDirection;
    // The direction for this tick
    private EnumGravityDirection direction;
    //
    private EnumGravityDirection pendingDirection;
    private int pendingPriority = GravityDirectionCapability.MIN_PRIORITY;
    private int timeoutTicks = 0;

    public GravityDirectionCapabilityImpl() {
        this(GravityDirectionCapability.DEFAULT_GRAVITY);
    }

    public GravityDirectionCapabilityImpl(EnumGravityDirection direction) {
        this.direction = direction;
        this.prevDirection = direction;
        this.pendingDirection = direction;
    }

    public EnumGravityDirection getDirection() {
        return this.direction;
    }

    @Override
    public EnumGravityDirection getPrevDirection() {
        return this.prevDirection;
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
    public void setDirection(EnumGravityDirection direction) {
        updateDirection(direction);
        this.timeoutTicks = GravityDirectionCapability.DEFAULT_TIMEOUT;
    }

    @Override
    public void setDirectionNoTimeout(EnumGravityDirection direction) {
        updateDirection(direction);
        this.timeoutTicks = 0;
    }

    private void updateDirection(EnumGravityDirection direction) {
        this.prevDirection = this.direction;
        this.direction = direction;
    }

    @Override
    public EnumGravityDirection getPendingDirection() {
        return this.pendingDirection;
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

//    public void updateCurrentDirection() {
//        if (this.timeoutTicks == 0) {
//            if (this.pendingDirection != this.direction) {
//                this.direction = this.pendingDirection;
//                this.timeoutTicks = GravityDirectionCapability.DEFAULT_TIMEOUT;
//            }
//            this.pendingDirection = GravityDirectionCapability.DEFAULT_GRAVITY;
//            this.pendingPriority = GravityDirectionCapability.MIN_PRIORITY;
//        }
//        else {
//            this.timeoutTicks--;
//        }
//    }
}
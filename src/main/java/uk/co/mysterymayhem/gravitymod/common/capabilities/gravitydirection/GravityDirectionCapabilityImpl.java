package uk.co.mysterymayhem.gravitymod.common.capabilities.gravitydirection;

import net.minecraft.util.math.Vec3d;
import uk.co.mysterymayhem.gravitymod.api.EnumGravityDirection;

import javax.annotation.Nonnull;

/**
 * Created by Mysteryem on 2016-10-10.
 */
public class GravityDirectionCapabilityImpl implements IGravityDirectionCapability {
    // The direction for this tick
    private EnumGravityDirection direction;
    private Vec3d eyePosChangeVec = Vec3d.ZERO;
    private boolean hasTransitionAngle = false;
    //
    private EnumGravityDirection pendingDirection;
    private int pendingPriority = GravityDirectionCapability.MIN_PRIORITY;
    private boolean previouslySetPriorityWasDefault = true;
    // Updated when the direction changes. Used to do smooth transitions between
    private EnumGravityDirection prevDirection;
    // After changing gravity direction, there's a certain number of ticks that must occur before your gravity direction can change again, this is done so
    // that the 'gravity transition animation' can completely finish
    private int timeoutTicks = 0;
    private double transitionRotationAmount = 0;
    // TODO: Think about a config option that delays the count-up start until after the timeout ticks are over
    // 'Count up' that must hit a certain threshold before a change in gravity direction can occur
    // Either:
    //  - New gravity direction must be held for X consecutive ticks before it can take effect (use instance EnumGravityDirection field to store current
    //    tick's new direction (without actually changing the direction). On the next tick, if it has the same new direction, increment the counter. If when
    //    updating, the counter is above its threshold, instead, reset the counter and actually change gravity. If we get a different new direction than we
    //    were expecting, reset the counter (and still don't actually change gravity).
    //     - Can we re-use the 'pendingDirection' field?
    //     - This approach has a problem that if two different new directions fight over the new direction (i.e. alternate each tick), the player can leave the
    //       current gravity field and maintain its effects forever.
    //  - Old gravity direction must not occur for X consecutive ticks before another direction can take effect. Similar to previous, but we increment the
    //    counter if the new direction is not the current direction, we don't care about the new direction being the same for X consecutive ticks, just so
    //    long as it isn't the current direction. If it is the current direction, then reset the counter to 0
    //  - A more complex solution could involve a separate counter for each direction. Increment the corresponding counter for each tick that a new direction
    //    is chosen, every other tick, decrement all counters if they're above zero. This could allow multiple directions to 'fight it out' instead of one of
    //    them being lucky to occur when the counter reached its maximum.
    //  - An even more complicated system could expand on this, whereby instead of storing just one pending direction, we instead store multiple and use the
    //    ordering of priorities of the gravity change attempts to assign 'points' to individual counters.
    private int reverseTimeoutTicks = 0; // 'Count up' or 'Timeout' both work fine, we just want to count ticks

    public GravityDirectionCapabilityImpl() {
        this(GravityDirectionCapability.DEFAULT_GRAVITY);
    }

    public GravityDirectionCapabilityImpl(@Nonnull EnumGravityDirection direction) {
        this.direction = direction;
        //TODO: Safe to assume not null?
//        this.direction = Objects.requireNonNull(direction, "Gravity direction cannot be null");
        this.prevDirection = direction;
        this.pendingDirection = direction;
    }

    @Nonnull
    public EnumGravityDirection getDirection() {
        return this.direction;
    }

    @Override
    public boolean timeoutComplete() {
        return this.timeoutTicks == 0;
    }

    @Override
    public void setDirection(@Nonnull EnumGravityDirection direction) {
        updateDirection(direction);
        this.timeoutTicks = GravityDirectionCapability.DEFAULT_TIMEOUT;
        this.reverseTimeoutTicks = GravityDirectionCapability.DEFAULT_REVERSE_TIMEOUT;
    }

    private void updateDirection(@Nonnull EnumGravityDirection direction) {
        this.prevDirection = this.direction;
        this.direction = direction;
        this.transitionRotationAmount = 0;
        this.hasTransitionAngle = false;
        //TODO: Safe to assume not null?
        //this.direction = Objects.requireNonNull(direction, "New gravity direction cannot be null");
    }

    @Nonnull
    @Override
    public EnumGravityDirection getPrevDirection() {
        return this.prevDirection;
    }

    @Nonnull
    @Override
    public EnumGravityDirection getPendingDirection() {
        return this.pendingDirection;
    }

    @Override
    public int getTimeoutTicks() {
        return this.timeoutTicks;
    }

    @Override
    public int getReverseTimeoutTicks() {
        return this.reverseTimeoutTicks;
    }

    @Override
    public void setReverseTimeoutTicks(int reverseTimeoutTicks) {
        this.reverseTimeoutTicks = reverseTimeoutTicks;
    }

    @Override
    public void setTimeoutTicks(int timeoutTicks) {
        this.timeoutTicks = timeoutTicks;
    }

    @Override
    public void setPendingDirection(@Nonnull EnumGravityDirection direction, int priority) {
        if (priority > this.pendingPriority) {
            this.pendingPriority = priority;
            this.pendingDirection = direction;
        }
    }

    @Override
    public void forceSetPendingDirection(@Nonnull EnumGravityDirection direction, int priority) {
        this.pendingPriority = priority;
        this.pendingDirection = direction;
    }

    @Override
    public int getPendingPriority() {
        return this.pendingPriority;
    }

    @Override
    public int getPreviousTickPriority() {
        return this.previousTickPriority;
    }

    private int previousTickPriority = GravityDirectionCapability.MIN_PRIORITY;

    @Override
    public void tickCommon() {
        if (this.timeoutTicks > 0) {
            this.timeoutTicks--;
        }
    }

    @Override
    public void tickServer() {
        this.previousTickPriority = this.pendingPriority;
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

    @Nonnull
    @Override
    public Vec3d getEyePosChangeVector() {
        return eyePosChangeVec;
    }

    @Override
    public void setEyePosChangeVector(@Nonnull Vec3d vec3d) {
        this.eyePosChangeVec = vec3d;
    }

    @Override
    public void setDirectionNoTimeout(@Nonnull EnumGravityDirection direction) {
        updateDirection(direction);
        this.timeoutTicks = 0;
    }
}

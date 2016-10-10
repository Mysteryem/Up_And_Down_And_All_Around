package uk.co.mysterymayhem.gravitymod.capabilities;

import uk.co.mysterymayhem.gravitymod.api.EnumGravityDirection;

/**
 * Created by Mysteryem on 2016-10-10.
 */
public class GravityDirectionCapabilityImpl implements IGravityDirectionCapability {
    private EnumGravityDirection direction;

    public GravityDirectionCapabilityImpl() {
        this.direction = GravityDirectionCapability.DEFAULT_GRAVITY;
    }

    public GravityDirectionCapabilityImpl(EnumGravityDirection direction) {
        this.direction = direction;
    }

    public EnumGravityDirection getDirection() {
        return direction;
    }

    public void setDirection(EnumGravityDirection direction) {
        this.direction = direction;
    }
}

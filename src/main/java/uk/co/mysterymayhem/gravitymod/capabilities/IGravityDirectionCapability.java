package uk.co.mysterymayhem.gravitymod.capabilities;

import uk.co.mysterymayhem.gravitymod.api.EnumGravityDirection;

/**
 * Created by Mysteryem on 2016-10-10.
 */
public interface IGravityDirectionCapability {
    EnumGravityDirection getDirection();

    void setDirection(EnumGravityDirection direction);
}

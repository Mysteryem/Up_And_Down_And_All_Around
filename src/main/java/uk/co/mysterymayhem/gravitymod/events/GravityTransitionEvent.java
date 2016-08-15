package uk.co.mysterymayhem.gravitymod.events;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.relauncher.Side;
import uk.co.mysterymayhem.gravitymod.EnumGravityDirection;
import uk.co.mysterymayhem.gravitymod.capabilities.GravityCapability;

/**
 * Created by Mysteryem on 2016-08-04.
 */
public class GravityTransitionEvent extends Event {

    public final EnumGravityDirection newGravityDirection;
    public final EntityPlayer player;
    public final Side side;

    public GravityTransitionEvent(EnumGravityDirection newGravityDirection, EntityPlayer player, Side side) {
        this.newGravityDirection = newGravityDirection;
        this.player = player;
        this.side = side;
    }
}

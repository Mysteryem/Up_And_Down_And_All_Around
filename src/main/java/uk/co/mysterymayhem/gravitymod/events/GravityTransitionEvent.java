package uk.co.mysterymayhem.gravitymod.events;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Created by Mysteryem on 2016-08-04.
 */
public class GravityTransitionEvent extends Event {

    public final boolean newGravityIsUpsideDown;
    public final EntityPlayer player;
    public final Side side;

    public GravityTransitionEvent(boolean newGravityIsUpsideDown, EntityPlayer player, Side side) {
        this.newGravityIsUpsideDown = newGravityIsUpsideDown;
        this.player = player;
        this.side = side;
    }
}

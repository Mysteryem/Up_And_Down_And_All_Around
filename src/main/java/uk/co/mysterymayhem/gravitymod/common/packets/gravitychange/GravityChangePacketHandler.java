package uk.co.mysterymayhem.gravitymod.common.packets.gravitychange;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import uk.co.mysterymayhem.gravitymod.GravityMod;
import uk.co.mysterymayhem.gravitymod.api.EnumGravityDirection;
import uk.co.mysterymayhem.gravitymod.common.capabilities.gravitydirection.GravityDirectionCapability;

/**
 * Created by Mysteryem on 2016-10-13.
 */
public class GravityChangePacketHandler implements IMessageHandler<GravityChangeMessage, IMessage> {

    @Override
    public IMessage onMessage(GravityChangeMessage message, MessageContext ctx) {
        if (message.getPacketType() == EnumChangePacketType.CLIENT_REQUEST_GRAVITY_OF_PLAYER) {
            if (GravityMod.GENERAL_DEBUG) {
                GravityMod.logInfo("Received gravity data request for %s, from %s", message.toSend, ctx.getServerHandler().player);
            }
            EnumGravityDirection gravityDirection = GravityDirectionCapability.getGravityDirection(message.getStringData(), ctx.getServerHandler().player
                    .getEntityWorld());
            if (GravityMod.GENERAL_DEBUG) {
                GravityMod.logInfo("Responding with gravity data for %s to %s", message.toSend, ctx.getServerHandler().player);
            }
            return new GravityChangeMessage(message.getStringData(), gravityDirection, true);
        }
        FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> GravityMod.proxy.getGravityManager().handlePacket(message, ctx));
        return null;
    }
}

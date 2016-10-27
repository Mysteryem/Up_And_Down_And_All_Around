package uk.co.mysterymayhem.gravitymod.packets.gravitychange;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import uk.co.mysterymayhem.gravitymod.GravityMod;
import uk.co.mysterymayhem.gravitymod.api.EnumGravityDirection;
import uk.co.mysterymayhem.gravitymod.capabilities.GravityDirectionCapability;

/**
 * Created by Mysteryem on 2016-10-13.
 */
public class GravityChangePacketHandler implements IMessageHandler<GravityChangeMessage, IMessage> {

    public GravityChangePacketHandler() {
    }

    @Override
    public IMessage onMessage(GravityChangeMessage message, MessageContext ctx) {
        if (message.getPacketType() == EnumChangePacketType.CLIENT_REQUEST_GRAVITY_OF_PLAYER) {
            //DEBUG
            //FMLLog.info("Received gravity data request for %s, from %s", message.toSend, ctx.getServerHandler().playerEntity);
            EnumGravityDirection gravityDirection = GravityDirectionCapability.getGravityDirection(message.getStringData(), ctx.getServerHandler().playerEntity.getEntityWorld());
//                if (gravityDirection == null) {
//                    gravityDirection = EnumGravityDirection.DOWN;
//                }
            //DEBUG
            //FMLLog.info("Responding with gravity data for %s to %s", message.toSend, ctx.getServerHandler().playerEntity);
            return new GravityChangeMessage(message.getStringData(), gravityDirection, true);
        }
        FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> GravityMod.proxy.getGravityManager().handlePacket(message, ctx));
        return null;
    }
}

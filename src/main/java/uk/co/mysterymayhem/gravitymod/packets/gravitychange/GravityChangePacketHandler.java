package uk.co.mysterymayhem.gravitymod.packets.gravitychange;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import uk.co.mysterymayhem.gravitymod.GravityMod;
import uk.co.mysterymayhem.gravitymod.api.EnumGravityDirection;
import uk.co.mysterymayhem.gravitymod.capabilities.GravityDirectionCapability;

/**
 * Created by Mysteryem on 2016-10-13.
 */
public class GravityChangePacketHandler implements IMessageHandler<GravityChangeMessage, IMessage> {
    private static int packetID = 0;

    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(GravityMod.MOD_ID);

    public static int nextID() {
        return packetID++;
    }

    public static void registerMessages() {
        INSTANCE.registerMessage(GravityChangePacketHandler.class, GravityChangeMessage.class, nextID(), Side.CLIENT);
        INSTANCE.registerMessage(GravityChangePacketHandler.class, GravityChangeMessage.class, nextID(), Side.SERVER);
    }

    public GravityChangePacketHandler() {
    }

    @Override
    public IMessage onMessage(GravityChangeMessage message, MessageContext ctx) {
        if (message.getPacketType() == EnumPacketType.CLIENT_REQUEST_GRAVITY_OF_PLAYER) {
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

package uk.co.mysterymayhem.gravitymod.common.packets;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import uk.co.mysterymayhem.gravitymod.GravityMod;
import uk.co.mysterymayhem.gravitymod.common.packets.config.ModCompatConfigCheckMessage;
import uk.co.mysterymayhem.gravitymod.common.packets.config.ModCompatConfigCheckPacketHandler;
import uk.co.mysterymayhem.gravitymod.common.packets.gravitychange.GravityChangeMessage;
import uk.co.mysterymayhem.gravitymod.common.packets.gravitychange.GravityChangePacketHandler;

/**
 * Created by Mysteryem on 2016-10-26.
 */
public class PacketHandler {
    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(GravityMod.MOD_ID);

    private static int packetID = 0;

    public static <REQ extends IMessage, REPLY extends IMessage> void registerMessage(Class<? extends IMessageHandler<REQ, REPLY>> messageHandler, Class<REQ> requestMessageType, Side processingSide) {
        INSTANCE.registerMessage(messageHandler, requestMessageType, nextID(), processingSide);
    }

    private static int nextID() {
        return packetID++;
    }

    public static void registerMessages() {
        GravityChangePacketHandler gravityChangePacketHandler = new GravityChangePacketHandler();
        ModCompatConfigCheckPacketHandler modCompatConfigCheckPacketHandler = new ModCompatConfigCheckPacketHandler();

        INSTANCE.registerMessage(gravityChangePacketHandler, GravityChangeMessage.class, nextID(), Side.CLIENT);
        INSTANCE.registerMessage(gravityChangePacketHandler, GravityChangeMessage.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(modCompatConfigCheckPacketHandler, ModCompatConfigCheckMessage.class, nextID(), Side.CLIENT);
        INSTANCE.registerMessage(modCompatConfigCheckPacketHandler, ModCompatConfigCheckMessage.class, nextID(), Side.SERVER);
    }
}

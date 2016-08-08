package uk.co.mysterymayhem.gravitymod.packets;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import uk.co.mysterymayhem.gravitymod.GravityMod;

/**
 * Created by Mysteryem on 2016-08-07.
 */
public class WhatsUpPacketHandler {
    private static int packetID = 0;

    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(GravityMod.MOD_ID);

    public WhatsUpPacketHandler() {
    }

    public static int nextID() {
        return packetID++;
    }

    public static void registerMessages() {
        INSTANCE.registerMessage(WhatsUpMessage.WhatsUpMessageHandler.class, WhatsUpMessage.class, nextID(), Side.CLIENT);
    }
}

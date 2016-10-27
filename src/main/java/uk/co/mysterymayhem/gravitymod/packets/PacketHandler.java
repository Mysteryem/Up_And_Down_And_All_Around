package uk.co.mysterymayhem.gravitymod.packets;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import uk.co.mysterymayhem.gravitymod.GravityMod;
import uk.co.mysterymayhem.gravitymod.packets.config.ModCompatConfigCheckMessage;
import uk.co.mysterymayhem.gravitymod.packets.config.ModCompatConfigCheckPacketHandler;
import uk.co.mysterymayhem.gravitymod.packets.gravitychange.GravityChangeMessage;
import uk.co.mysterymayhem.gravitymod.packets.gravitychange.GravityChangePacketHandler;

/**
 * Created by Mysteryem on 2016-10-26.
 */
public class PacketHandler {
    private static int packetID = 0;

    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(GravityMod.MOD_ID);

    private static int nextID() {
        return packetID++;
    }

    public static void registerMessages() {
        INSTANCE.registerMessage(GravityChangePacketHandler.class, GravityChangeMessage.class, nextID(), Side.CLIENT);
        INSTANCE.registerMessage(GravityChangePacketHandler.class, GravityChangeMessage.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(ModCompatConfigCheckPacketHandler.class, ModCompatConfigCheckMessage.class, nextID(), Side.CLIENT);
        INSTANCE.registerMessage(ModCompatConfigCheckPacketHandler.class, ModCompatConfigCheckMessage.class, nextID(), Side.SERVER);
    }
}

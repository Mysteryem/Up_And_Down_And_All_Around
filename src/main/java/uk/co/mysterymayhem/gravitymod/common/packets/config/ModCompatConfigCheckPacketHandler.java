package uk.co.mysterymayhem.gravitymod.common.packets.config;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import uk.co.mysterymayhem.gravitymod.GravityMod;
import uk.co.mysterymayhem.gravitymod.common.config.ConfigHandler;
import uk.co.mysterymayhem.gravitymod.common.listeners.ItemStackUseListener;

/**
 * Created by Mysteryem on 2016-10-26.
 */
public class ModCompatConfigCheckPacketHandler implements IMessageHandler<ModCompatConfigCheckMessage, ModCompatConfigCheckMessage> {

    private static final boolean DEBUG = false;

    @Override
    public ModCompatConfigCheckMessage onMessage(final ModCompatConfigCheckMessage message, final MessageContext ctx) {
        // 'break;' within the switch leads to 'return null;' and nothing else
        switch (message.getPacketType()) {
            case SERVER_TO_CLIENT_SERVER_HASH_CHECK:
                if (ctx.side == Side.CLIENT) {
                    if (message.getModCompatHashcode() != ItemStackUseListener.getHashCode()) {
                        GravityMod.logInfo("Server has different config hashcode to me. Notifying the server");
                        return new ModCompatConfigCheckMessage(EnumConfigPacketType.CLIENT_TO_SERVER_WRONG_CLIENT_SIDE_HASH);
                    }
                    else if (DEBUG) {
                        GravityMod.logInfo("Server has the same hashcode as me, everything is ok");
                    }
                }
                else {
                    GravityMod.logWarning("Received SERVER_TO_CLIENT_SERVER_HASH_CHECK packet on the server side");
                }
                break;
            case CLIENT_TO_SERVER_WRONG_CLIENT_SIDE_HASH:
                if (ctx.side == Side.SERVER) {
                    String playerName = ctx.getServerHandler().player.getName();
                    if (ConfigHandler.kickPlayersWithMismatchingModCompatHashes) {
                        GravityMod.logInfo("%s has a different mod compatibility config to the server, kicking them", playerName);
                        FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(()
                                -> ctx.getServerHandler().disconnect("UpAndDown mod compatibility config doesn't match server"));
                    }
                    else {
                        GravityMod.logWarning("%s has a different mod compatibility config to the server", playerName);
                    }
                }
                else {
                    GravityMod.logWarning("Received CLIENT_TO_SERVER_WRONG_CLIENT_SIDE_HASH on the client side");
                }
                break;
        }
        return null;
    }
}

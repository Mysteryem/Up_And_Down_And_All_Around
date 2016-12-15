package uk.co.mysterymayhem.gravitymod.common.packets.config;

import gnu.trove.map.hash.TIntObjectHashMap;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import uk.co.mysterymayhem.gravitymod.GravityMod;
import uk.co.mysterymayhem.gravitymod.asm.EntityPlayerWithGravity;
import uk.co.mysterymayhem.gravitymod.common.config.ConfigHandler;
import uk.co.mysterymayhem.gravitymod.common.listeners.ItemStackUseListener;
import uk.co.mysterymayhem.gravitymod.common.modsupport.prepostmodifier.CombinedPrePostModifier;
import uk.co.mysterymayhem.gravitymod.common.modsupport.prepostmodifier.EnumPrePostModifier;
import uk.co.mysterymayhem.gravitymod.common.modsupport.prepostmodifier.IPrePostModifier;

/**
 * Created by Mysteryem on 2016-10-26.
 */
public class ModCompatConfigCheckPacketHandler implements IMessageHandler<ModCompatConfigCheckMessage, ModCompatConfigCheckMessage> {

    private static IPrePostModifier<EntityPlayerWithGravity> getModifierFromID(int id) {
        EnumPrePostModifier[] enumValues = EnumPrePostModifier.values();
        int numEnumModifiers = enumValues.length;
        if (id < numEnumModifiers) {
            return enumValues[id];
        }
        else {
            return CombinedPrePostModifier.COMBINED_REGISTRY.get(id - numEnumModifiers);
        }
    }

    public ModCompatConfigCheckPacketHandler() {
    }

    @Override
    public ModCompatConfigCheckMessage onMessage(final ModCompatConfigCheckMessage message, final MessageContext ctx) {
        // 'break;' within the switch leads to 'return null;' and nothing else
        switch (message.getPacketType()) {
            case SERVER_TO_CLIENT_SERVER_HASH_CHECK:
                if (ctx.side == Side.CLIENT) {
                    if (message.getModCompatHashcode() != ItemStackUseListener.getHashCode()) {
                        GravityMod.logInfo("Server has different config hashcode to me. Requesting data from server");
                        return new ModCompatConfigCheckMessage(EnumConfigPacketType.CLIENT_TO_SERVER_WRONG_CLIENT_SIDE_HASH);
                    }
                    else if (GravityMod.GENERAL_DEBUG){
                        GravityMod.logInfo("Server has the same hashcode as me, everything is ok");
                    }
                }
                else {
                    GravityMod.logWarning("Received SERVER_TO_CLIENT_SERVER_HASH_CHECK packet on the server side");
                }
                break;
            case CLIENT_TO_SERVER_WRONG_CLIENT_SIDE_HASH:
                if (ctx.side == Side.SERVER) {
                    String playerName = ctx.getServerHandler().playerEntity.getName();
                    GravityMod.logInfo("%s has a different mod compatibility config to the server, sending them the correct data", playerName);

                    FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
                        //TODO: Send a chat message to the client if they're an OP based on config setting on the server
                        ctx.getServerHandler().playerEntity.addChatComponentMessage(
                                new TextComponentString("[UpAndDown] Your mod compatibility config does not match the server's. It will be automatically downloaded for this session."));
                    });
                    return new ModCompatConfigCheckMessage(EnumConfigPacketType.SERVER_TO_CLIENT_CONFIG_DOWNLOAD);
                }
                else {
                    GravityMod.logWarning("Received CLIENT_TO_SERVER_WRONG_CLIENT_SIDE_HASH on the client side");
                }
                break;
            case SERVER_TO_CLIENT_CONFIG_DOWNLOAD:
                if (ctx.side == Side.CLIENT) {
                    GravityMod.logInfo("Received server config data from server, will schedule the client data to be updated");

                    final ByteBuf data = message.getConfigData();

                    //Can't modify the maps while we could be using them, so going to schedule a task
                    FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {

                        if (GravityMod.GENERAL_DEBUG) {
                            GravityMod.logInfo("Updating client maps based on server packet");
                        }
                        ItemStackUseListener.reset();
                        CombinedPrePostModifier.reset();

                        int numberOfUniqueStrings = data.readInt();

                        TIntObjectHashMap<String> intToStringMap = new TIntObjectHashMap<>();

                        for (int i = 0; i < numberOfUniqueStrings; i++) {
                            intToStringMap.put(i, ByteBufUtils.readUTF8String(data));
                        }

                        int numberOfCombinedModifiers = data.readInt();

                        for (int i = 0; i < numberOfCombinedModifiers; i++) {
                            CombinedPrePostModifier.ProcessingOrder processingOrder = data.readBoolean() ?
                                    CombinedPrePostModifier.ProcessingOrder.PRE_FIRST_SECOND_POST_FIRST_SECOND :
                                    CombinedPrePostModifier.ProcessingOrder.PRE_FIRST_SECOND_POST_SECOND_FIRST;
                            int firstModifierID = data.readInt();
                            IPrePostModifier<EntityPlayerWithGravity> firstModifier = getModifierFromID(firstModifierID);
                            int secondModifierID = data.readInt();
                            IPrePostModifier<EntityPlayerWithGravity> secondModifier = getModifierFromID(secondModifierID);
                            //CombinedPrePostModifier combinedModifier =
                            CombinedPrePostModifier.getModifierFor(firstModifier, secondModifier, processingOrder);
                        }


                        // TODO: Change this to work better if the order changes or more maps are added (i.e. remove the hardcoded stuff)
                        // Order is BLOCK, GENERAL, ONSTOPPEDUSEING
                        for (ConfigHandler.EnumItemStackUseCompat itemUseType : ConfigHandler.EnumItemStackUseCompat.values()) {
                            int numItemMappings = data.readInt();
                            for (int i = 0; i < numItemMappings; i++) {
                                int domainStringIndex = data.readInt();
                                String domainString = intToStringMap.get(domainStringIndex);
                                int pathStringIndex = data.readInt();
                                String pathString = intToStringMap.get(pathStringIndex);

                                int numDamageValuesThatHaveModifiers = data.readInt();

                                for (int j = 0; j < numDamageValuesThatHaveModifiers; j++) {
                                    int damageValue = data.readInt();
                                    int modifierID = data.readInt();
                                    IPrePostModifier<EntityPlayerWithGravity> modifier = getModifierFromID(modifierID);

                                    // Add the modifier
                                    ItemStackUseListener.addPrePostModifier(domainString, pathString, modifier, itemUseType, damageValue);
                                }
                            }
                        }
                        int hashCode = ItemStackUseListener.getHashCode();

                        if (GravityMod.GENERAL_DEBUG) {
                            GravityMod.logInfo("Hashcode before: " + hashCode);
                        }
                        ItemStackUseListener.makeHash();

                        hashCode = ItemStackUseListener.getHashCode();

                        if (GravityMod.GENERAL_DEBUG) {
                            GravityMod.logInfo("Hashcode after: " + hashCode);
                        }
                    });
                }
                else {
                    GravityMod.logWarning("Received SERVER_TO_CLIENT_CONFIG_DOWNLOAD on the client side");
                }
                break;
        }
        return null;
    }
}

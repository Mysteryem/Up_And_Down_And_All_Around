package uk.co.mysterymayhem.gravitymod.packets;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import uk.co.mysterymayhem.gravitymod.EnumGravityDirection;
import uk.co.mysterymayhem.gravitymod.GravityMod;
import uk.co.mysterymayhem.gravitymod.capabilities.GravityCapability;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.BiConsumer;

/**
 * Created by Mysteryem on 2016-08-07.
 */
public class GravityChangeMessage implements IMessage {

    public enum PacketType {
        SINGLE((gravityChangeMessage, buf) -> {
            char[] stringAsChars = gravityChangeMessage.toSend.toCharArray();
            buf.writeInt(stringAsChars.length);
            for (char c : stringAsChars) {
                buf.writeChar(c);
            }
            buf.writeInt(gravityChangeMessage.newGravityDirection.ordinal());
        }, (gravityChangeMessage, buf) -> {
            int charsToRead = buf.readInt();
            char[] readChars = new char[charsToRead];
            for (int i = 0; i < charsToRead; i++) {
                readChars[i] = buf.readChar();
            }
            gravityChangeMessage.toSend = String.valueOf(readChars);
            gravityChangeMessage.newGravityDirection = EnumGravityDirection.values()[buf.readInt()];
        }),
        ALL_UPSIDE_DOWN((gravityChangeMessage, buf) -> {
            int numberOfStrings = gravityChangeMessage.toSendMultiple.size();
            buf.writeInt(numberOfStrings);
            for(String string : gravityChangeMessage.toSendMultiple) {
                char[] stringAsChars = string.toCharArray();
                buf.writeInt(stringAsChars.length);
                for (char c : stringAsChars) {
                    buf.writeChar(c);
                }
            }
        }, (gravityChangeMessage, buf) -> {
            int numberOfStrings = buf.readInt();
            ArrayList<String> names = new ArrayList<>(numberOfStrings);
            for (int i = 0; i < numberOfStrings; i++) {
                int charsToRead = buf.readInt();
                char[] readChars = new char[charsToRead];
                for (int j = 0; j < charsToRead; j++) {
                    readChars[j] = buf.readChar();
                }
                names.add(String.valueOf(readChars));
            }
            gravityChangeMessage.toSendMultiple = names;
        }),
        CLIENT_REQUEST_GRAVITY_OF_PLAYER((gravityChangeMessage, buf) -> {
            char[] stringAsChars = gravityChangeMessage.toSend.toCharArray();
            buf.writeInt(stringAsChars.length);
            for (char c : stringAsChars) {
                buf.writeChar(c);
            }
        }, (gravityChangeMessage, buf) -> {
            int charsToRead = buf.readInt();
            char[] readChars = new char[charsToRead];
            for (int i = 0; i < charsToRead; i++) {
                readChars[i] = buf.readChar();
            }
            gravityChangeMessage.toSend = String.valueOf(readChars);
        }),
        SERVER_RESPONSE_TO_GRAVITY_REQUEST((gravityChangeMessage, buf) -> {

        }, (gravityChangeMessage, buf) -> {

        });

        private final BiConsumer<GravityChangeMessage, ByteBuf> toBytesFunction;
        private final BiConsumer<GravityChangeMessage, ByteBuf> fromBytesFunction;

        private PacketType(BiConsumer<GravityChangeMessage, ByteBuf> toBytes, BiConsumer<GravityChangeMessage, ByteBuf> fromBytes) {
            this.toBytesFunction = toBytes;
            this.fromBytesFunction = fromBytes;
        }
    }

    public GravityChangeMessage(){

    }

    private String toSend;
    private EnumGravityDirection newGravityDirection;
    private PacketType packetType;
    private Collection<String> toSendMultiple;

    public PacketType getPacketType() {
        return this.packetType;
    }

    public String getStringData() {
        return this.toSend;
    }

    public EnumGravityDirection getNewGravityDirection() {
        return this.newGravityDirection;
    }

    public GravityChangeMessage(String stringToSend, EnumGravityDirection newGravityDirection) {
        this.toSend = stringToSend;
        this.newGravityDirection = newGravityDirection;
        this.packetType = PacketType.SINGLE;
    }

    public GravityChangeMessage(Collection<String> stringsToSend) {
        this.toSendMultiple = stringsToSend;
        this.packetType = PacketType.ALL_UPSIDE_DOWN;
    }

    public GravityChangeMessage(String playerToRequest) {
        this.toSend = playerToRequest;
        this.packetType = PacketType.CLIENT_REQUEST_GRAVITY_OF_PLAYER;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.packetType.ordinal());
        this.packetType.toBytesFunction.accept(this, buf);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        //(PacketType.values()[buf.readInt()]).fromBytesFunction.accept(this, buf);
        int packetTypeOrdinal = buf.readInt();
        PacketType type = PacketType.values()[packetTypeOrdinal];
        this.packetType = type;
        type.fromBytesFunction.accept(this, buf);
    }

    public static class WhatsUpMessageHandler implements IMessageHandler<GravityChangeMessage, IMessage> {
        @Override
        public IMessage onMessage(GravityChangeMessage message, MessageContext ctx) {
            if (message.packetType == PacketType.CLIENT_REQUEST_GRAVITY_OF_PLAYER) {
                //DEBUG
                //FMLLog.info("Received gravity data request for %s, from %s", message.toSend, ctx.getServerHandler().playerEntity);
                EnumGravityDirection gravityDirection = GravityCapability.getGravityDirection(message.toSend, ctx.getServerHandler().playerEntity.getEntityWorld());
                if (gravityDirection == null) {
                    gravityDirection = EnumGravityDirection.DOWN;
                }
                //DEBUG
                //FMLLog.info("Responding with gravity data for %s to %s", message.toSend, ctx.getServerHandler().playerEntity);
                return new GravityChangeMessage(message.toSend, gravityDirection);
            }
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> GravityMod.proxy.getGravityManager().handlePacket(message, ctx));
            //FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

//        private void handle(GravityChangeMessage message, MessageContext ctx) {
//            switch(message.packetType) {
//                case SINGLE:
//                    GravityMod.proxy.gravityManagerCommon.handleSinglePlayerUpdateServerPacket(message.toSend, message.newGravityDirection);
//                    break;
//                case ALL_UPSIDE_DOWN:
//                    GravityMod.proxy.gravityManagerCommon.handleMultiplePlayerInitialiseServerPacket(message.toSendMultiple);
//                    break;
//            }
//        }
    }
}

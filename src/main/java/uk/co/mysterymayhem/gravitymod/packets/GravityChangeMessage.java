package uk.co.mysterymayhem.gravitymod.packets;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import uk.co.mysterymayhem.gravitymod.api.EnumGravityDirection;
import uk.co.mysterymayhem.gravitymod.GravityMod;
import uk.co.mysterymayhem.gravitymod.capabilities.GravityDirectionCapability;

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
            buf.writeBoolean(gravityChangeMessage.noTimeout);
        }, (gravityChangeMessage, buf) -> {
            int charsToRead = buf.readInt();
            char[] readChars = new char[charsToRead];
            for (int i = 0; i < charsToRead; i++) {
                readChars[i] = buf.readChar();
            }
            gravityChangeMessage.toSend = String.valueOf(readChars);
            gravityChangeMessage.newGravityDirection = EnumGravityDirection.values()[buf.readInt()];
            gravityChangeMessage.noTimeout = buf.readBoolean();
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
    private boolean noTimeout;

    public PacketType getPacketType() {
        return this.packetType;
    }

    public String getStringData() {
        return this.toSend;
    }

    public boolean getNoTimeout() {
        return this.noTimeout;
    }

    public EnumGravityDirection getNewGravityDirection() {
        return this.newGravityDirection;
    }

    public GravityChangeMessage(String stringToSend, EnumGravityDirection newGravityDirection, boolean noTimeout) {
        this.toSend = stringToSend;
        this.newGravityDirection = newGravityDirection;
        this.noTimeout = noTimeout;
        this.packetType = PacketType.SINGLE;
    }

    public GravityChangeMessage(Collection<String> stringsToSend, boolean noTimeout) {
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
                EnumGravityDirection gravityDirection = GravityDirectionCapability.getGravityDirection(message.toSend, ctx.getServerHandler().playerEntity.getEntityWorld());
//                if (gravityDirection == null) {
//                    gravityDirection = EnumGravityDirection.DOWN;
//                }
                //DEBUG
                //FMLLog.info("Responding with gravity data for %s to %s", message.toSend, ctx.getServerHandler().playerEntity);
                return new GravityChangeMessage(message.toSend, gravityDirection, true);
            }
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> GravityMod.proxy.getGravityManager().handlePacket(message, ctx));
            return null;
        }
    }
}

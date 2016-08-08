package uk.co.mysterymayhem.gravitymod.packets;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import uk.co.mysterymayhem.gravitymod.GravityMod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.BiConsumer;

/**
 * Created by Mysteryem on 2016-08-07.
 */
public class GravityChangeMessage implements IMessage {

    public static enum PacketType {
        SINGLE((gravityChangeMessage, buf) -> {
            char[] stringAsChars = gravityChangeMessage.toSend.toCharArray();
            buf.writeInt(stringAsChars.length);
            for (char c : stringAsChars) {
                buf.writeChar(c);
            }
            buf.writeBoolean(gravityChangeMessage.gravityUpsideDownNow);
        }, (gravityChangeMessage, buf) -> {
            int charsToRead = buf.readInt();
            char[] readChars = new char[charsToRead];
            for (int i = 0; i < charsToRead; i++) {
                readChars[i] = buf.readChar();
            }
            gravityChangeMessage.toSend = String.valueOf(readChars);
            gravityChangeMessage.gravityUpsideDownNow = buf.readBoolean();
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
    private boolean gravityUpsideDownNow;
    private PacketType packetType;
    private Collection<String> toSendMultiple;

    public GravityChangeMessage(String stringToSend, boolean gravityUpsideDownNow) {
        this.toSend = stringToSend;
        this.gravityUpsideDownNow = gravityUpsideDownNow;
        this.packetType = PacketType.SINGLE;
    }

    public GravityChangeMessage(Collection<String> stringsToSend) {
        this.toSendMultiple = stringsToSend;
        this.packetType = PacketType.ALL_UPSIDE_DOWN;
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
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(GravityChangeMessage message, MessageContext ctx) {
            switch(message.packetType) {
                case SINGLE:
                    GravityMod.proxy.gravityManagerCommon.handleSinglePlayerUpdateServerPacket(message.toSend, message.gravityUpsideDownNow);
                    break;
                case ALL_UPSIDE_DOWN:
                    GravityMod.proxy.gravityManagerCommon.handleMultiplePlayerInitialiseServerPacket(message.toSendMultiple);
                    break;
            }
        }
    }
}

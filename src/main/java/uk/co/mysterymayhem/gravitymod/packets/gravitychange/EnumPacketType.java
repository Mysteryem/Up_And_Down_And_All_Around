package uk.co.mysterymayhem.gravitymod.packets.gravitychange;

import io.netty.buffer.ByteBuf;
import uk.co.mysterymayhem.gravitymod.api.EnumGravityDirection;

import java.util.ArrayList;
import java.util.function.BiConsumer;

/**
 * Created by Mysteryem on 2016-10-13.
 */
public enum EnumPacketType {
    SINGLE(
            (gravityChangeMessage, buf) -> {
                char[] stringAsChars = gravityChangeMessage.toSend.toCharArray();
                buf.writeInt(stringAsChars.length);
                for (char c : stringAsChars) {
                    buf.writeChar(c);
                }
                buf.writeInt(gravityChangeMessage.newGravityDirection.ordinal());
                buf.writeBoolean(gravityChangeMessage.noTimeout);
            },

            (gravityChangeMessage, buf) -> {
                int charsToRead = buf.readInt();
                char[] readChars = new char[charsToRead];
                for (int i = 0; i < charsToRead; i++) {
                    readChars[i] = buf.readChar();
                }
                gravityChangeMessage.toSend = String.valueOf(readChars);
                gravityChangeMessage.newGravityDirection = EnumGravityDirection.values()[buf.readInt()];
                gravityChangeMessage.noTimeout = buf.readBoolean();
            }
    ),
    ALL_UPSIDE_DOWN(
            (gravityChangeMessage, buf) -> {
                int numberOfStrings = gravityChangeMessage.toSendMultiple.size();
                buf.writeInt(numberOfStrings);
                for (String string : gravityChangeMessage.toSendMultiple) {
                    char[] stringAsChars = string.toCharArray();
                    buf.writeInt(stringAsChars.length);
                    for (char c : stringAsChars) {
                        buf.writeChar(c);
                    }
                }
            },

            (gravityChangeMessage, buf) -> {
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
            }
    ),
    CLIENT_REQUEST_GRAVITY_OF_PLAYER(
            (gravityChangeMessage, buf) -> {
                char[] stringAsChars = gravityChangeMessage.toSend.toCharArray();
                buf.writeInt(stringAsChars.length);
                for (char c : stringAsChars) {
                    buf.writeChar(c);
                }
            },

            (gravityChangeMessage, buf) -> {
                int charsToRead = buf.readInt();
                char[] readChars = new char[charsToRead];
                for (int i = 0; i < charsToRead; i++) {
                    readChars[i] = buf.readChar();
                }
                gravityChangeMessage.toSend = String.valueOf(readChars);
            }
    );

    public final BiConsumer<GravityChangeMessage, ByteBuf> toBytesFunction;
    public final BiConsumer<GravityChangeMessage, ByteBuf> fromBytesFunction;

    EnumPacketType(BiConsumer<GravityChangeMessage, ByteBuf> toBytes, BiConsumer<GravityChangeMessage, ByteBuf> fromBytes) {
        this.toBytesFunction = toBytes;
        this.fromBytesFunction = fromBytes;
    }
}

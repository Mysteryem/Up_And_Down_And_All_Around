package uk.co.mysterymayhem.gravitymod.packets.gravitychange;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import uk.co.mysterymayhem.gravitymod.api.EnumGravityDirection;
import uk.co.mysterymayhem.gravitymod.packets.IMessageHelper;

import java.util.function.BiConsumer;

/**
 * Created by Mysteryem on 2016-10-13.
 */
enum EnumChangePacketType implements IMessageHelper<GravityChangeMessage>{
    SINGLE {
        @Override
        public void writeToBuff(GravityChangeMessage message, ByteBuf buf) {
            ByteBufUtils.writeUTF8String(buf, message.toSend);
            buf.writeInt(message.newGravityDirection.ordinal());
            buf.writeBoolean(message.noTimeout);
        }

        @Override
        public void readFromBuff(GravityChangeMessage message, ByteBuf buf) {
            message.toSend = ByteBufUtils.readUTF8String(buf);
            message.newGravityDirection = EnumGravityDirection.values()[buf.readInt()];
            message.noTimeout = buf.readBoolean();
        }
    },
    CLIENT_REQUEST_GRAVITY_OF_PLAYER {
        @Override
        public void writeToBuff(GravityChangeMessage message, ByteBuf buf) {
            ByteBufUtils.writeUTF8String(buf, message.toSend);
        }

        @Override
        public void readFromBuff(GravityChangeMessage message, ByteBuf buf) {
            message.toSend = ByteBufUtils.readUTF8String(buf);
        }
    }
}

package uk.co.mysterymayhem.gravitymod.common.packets.config;

import io.netty.buffer.ByteBuf;
import uk.co.mysterymayhem.gravitymod.common.packets.IMessageHelper;

/**
 * Created by Mysteryem on 2016-10-27.
 */
enum EnumConfigPacketType implements IMessageHelper<ModCompatConfigCheckMessage> {
    SERVER_TO_CLIENT_SERVER_HASH_CHECK {
        @Override
        public void writeToBuff(ModCompatConfigCheckMessage message, ByteBuf buf) {
            buf.writeInt(message.getModCompatHashcode());
        }

        @Override
        public void readFromBuff(ModCompatConfigCheckMessage message, ByteBuf buf) {
            message.setModCompatHashcode(buf.readInt());
        }
    },
    CLIENT_TO_SERVER_WRONG_CLIENT_SIDE_HASH {
        @Override
        public void writeToBuff(ModCompatConfigCheckMessage message, ByteBuf buf) {
            //nothing to do, the packet being sent with this ordinal is all we need to know
        }

        @Override
        public void readFromBuff(ModCompatConfigCheckMessage message, ByteBuf buf) {
            //nothing to do, the packet being received with this ordinal is all we need to know
        }
    }
}

package uk.co.mysterymayhem.gravitymod.common.packets;

/**
 * Created by Mysteryem on 2016-10-27.
 */

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public interface IMessageHelper<T extends IMessage> {
    void writeToBuff(T message, ByteBuf buf);
    void readFromBuff(T message, ByteBuf buf);
}

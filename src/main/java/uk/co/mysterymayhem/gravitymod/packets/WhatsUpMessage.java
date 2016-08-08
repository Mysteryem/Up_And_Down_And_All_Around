package uk.co.mysterymayhem.gravitymod.packets;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import uk.co.mysterymayhem.gravitymod.GravityManagerClient;
import uk.co.mysterymayhem.gravitymod.GravityMod;

/**
 * Created by Mysteryem on 2016-08-07.
 */
public class WhatsUpMessage implements IMessage {

    public WhatsUpMessage(){

    }

    private String toSend;
    private boolean gravityUpsideDownNow;

    public WhatsUpMessage(String stringToSend, boolean gravityUpsideDownNow) {
        this.toSend = stringToSend;
        this.gravityUpsideDownNow = gravityUpsideDownNow;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        char[] stringAsChars = this.toSend.toCharArray();
        buf.writeInt(stringAsChars.length);
        for (char c : this.toSend.toCharArray()) {
            buf.writeChar(c);
        }
        buf.writeBoolean(this.gravityUpsideDownNow);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int charsToRead = buf.readInt();
        char[] readChars = new char[charsToRead];
        for (int i = 0; i < charsToRead; i++) {
            readChars[i] = buf.readChar();
        }
        this.toSend = String.valueOf(readChars);
        this.gravityUpsideDownNow = buf.readBoolean();
    }

    public static class WhatsUpMessageHandler implements IMessageHandler<WhatsUpMessage, IMessage> {
        @Override
        public IMessage onMessage(WhatsUpMessage message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(WhatsUpMessage message, MessageContext ctx) {
            GravityMod.proxy.gravityManagerServer.handleServerPacket(message.toSend, message.gravityUpsideDownNow);
        }
    }
}

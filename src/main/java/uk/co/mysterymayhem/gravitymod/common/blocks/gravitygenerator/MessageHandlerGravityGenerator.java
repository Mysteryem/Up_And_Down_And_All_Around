package uk.co.mysterymayhem.gravitymod.common.blocks.gravitygenerator;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Created by Mysteryem on 2017-01-10.
 */
public class MessageHandlerGravityGenerator implements IMessageHandler<MessageGravityGenerator, IMessage> {
    @Override
    public IMessage onMessage(MessageGravityGenerator message, MessageContext ctx) {
        EntityPlayerMP playerEntity = ctx.getServerHandler().playerEntity;
        FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(
                () -> message.process(playerEntity)
        );
        return null;
    }
}

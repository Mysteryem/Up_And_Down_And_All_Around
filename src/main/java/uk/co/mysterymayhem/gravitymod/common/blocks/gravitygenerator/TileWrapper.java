package uk.co.mysterymayhem.gravitymod.common.blocks.gravitygenerator;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import uk.co.mysterymayhem.gravitymod.client.blocks.gravitygenerator.ClientTickListener;
import uk.co.mysterymayhem.gravitymod.common.packets.PacketHandler;
import uk.co.mysterymayhem.gravitymod.common.registries.IGravityModTileEntityClassWrapper;

/**
 * Created by Mysteryem on 2017-01-08.
 */
public class TileWrapper implements IGravityModTileEntityClassWrapper<TileGravityGenerator> {

    @Override
    public String getModObjectName() {
        return "tilegravitygenerator";
    }

    @Override
    public Class<TileGravityGenerator> getTileEntityClass() {
        return TileGravityGenerator.class;
    }

    @Override
    public void preInit() {
        PacketHandler.registerMessage(MessageHandlerGravityGenerator.class, MessageGravityGenerator.class, Side.SERVER);
        IGravityModTileEntityClassWrapper.super.preInit();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void preInitClient() {
        MinecraftForge.EVENT_BUS.register(ClientTickListener.class);
    }
}

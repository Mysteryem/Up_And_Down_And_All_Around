package uk.co.mysterymayhem.gravitymod;

import net.minecraftforge.common.MinecraftForge;
import scala.collection.parallel.ParIterableLike;
import uk.co.mysterymayhem.gravitymod.packets.WhatsUpPacketHandler;

/**
 * Created by Mysteryem on 2016-08-04.
 */
public class CommonProxy {
    public GravityManagerServer gravityManagerServer;

    public void preInit() {
        this.registerGravityManager();
        WhatsUpPacketHandler.registerMessages();
    }

    public void init() {
        this.registerListeners();
    }

    public void registerGravityManager() {
        this.gravityManagerServer = new GravityManagerServer();
    }

    public void registerListeners() {
        MinecraftForge.EVENT_BUS.register(this.getGravityManager());
        MinecraftForge.EVENT_BUS.register(new DebugHelperListener());
        MinecraftForge.EVENT_BUS.register(new MovementInterceptionListener());
    }

    public GravityManagerServer getGravityManager() {
        return this.gravityManagerServer;
    }
}

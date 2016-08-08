package uk.co.mysterymayhem.gravitymod;

import net.minecraftforge.common.MinecraftForge;

/**
 * Created by Mysteryem on 2016-08-04.
 */
public class ClientProxy extends CommonProxy{
    @Override
    public void registerListeners() {
        super.registerListeners();
        MinecraftForge.EVENT_BUS.register(new MouseInterceptionListener());
        //MinecraftForge.EVENT_BUS.register(new MovementInterceptionListener());
        MinecraftForge.EVENT_BUS.register(new PlayerCameraListener());
        MinecraftForge.EVENT_BUS.register(new PlayerRenderListener());
    }

    @Override
    public void registerGravityManager() {
        this.gravityManagerServer = new GravityManagerClient();
    }
}

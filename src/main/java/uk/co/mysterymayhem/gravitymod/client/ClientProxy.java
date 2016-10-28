package uk.co.mysterymayhem.gravitymod.client;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import uk.co.mysterymayhem.gravitymod.client.listeners.GravityManagerClient;
import uk.co.mysterymayhem.gravitymod.client.listeners.PlayerCameraListener;
import uk.co.mysterymayhem.gravitymod.client.listeners.PlayerRenderListener;
import uk.co.mysterymayhem.gravitymod.common.CommonProxy;
import uk.co.mysterymayhem.gravitymod.common.ModItems;

/**
 * Created by Mysteryem on 2016-08-04.
 */
@SuppressWarnings("unused")
@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    @Override
    public void preInit() {
        super.preInit();
        ModItems.initModels();
    }

    @Override
    public void registerListeners() {
        super.registerListeners();
        MinecraftForge.EVENT_BUS.register(new PlayerCameraListener());
        MinecraftForge.EVENT_BUS.register(new PlayerRenderListener());
    }

    @Override
    public void registerGravityManager() {
        this.gravityManagerCommon = new GravityManagerClient();
    }
}

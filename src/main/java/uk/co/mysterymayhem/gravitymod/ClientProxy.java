package uk.co.mysterymayhem.gravitymod;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import uk.co.mysterymayhem.gravitymod.client.listeners.EntityRenderListener;
import uk.co.mysterymayhem.gravitymod.client.listeners.GravityManagerClient;
import uk.co.mysterymayhem.gravitymod.client.listeners.ItemTooltipListener;
import uk.co.mysterymayhem.gravitymod.client.listeners.PlayerCameraListener;

/**
 * Created by Mysteryem on 2016-08-04.
 */
@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    @Override
    public void preInit() {
        super.preInit();
        this.setupObjects.forEach(IFMLStaged::preInitClient);
    }

    @Override
    public void init() {
        super.init();
        this.setupObjects.forEach(IFMLStaged::initClient);
    }

    @Override
    public void postInit() {
        super.postInit();
        this.setupObjects.forEach(IFMLStaged::postInitClient);
    }

    @Override
    public void registerListeners() {
        super.registerListeners();
        MinecraftForge.EVENT_BUS.register(new PlayerCameraListener());
        MinecraftForge.EVENT_BUS.register(new EntityRenderListener());
        MinecraftForge.EVENT_BUS.register(new ItemTooltipListener());
    }

    @Override
    public void registerGravityManager() {
        this.gravityManagerCommon = new GravityManagerClient();
    }
}

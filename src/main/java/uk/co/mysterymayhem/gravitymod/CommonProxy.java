package uk.co.mysterymayhem.gravitymod;

import net.minecraftforge.common.MinecraftForge;
import uk.co.mysterymayhem.gravitymod.capabilities.GravityDirectionCapability;
import uk.co.mysterymayhem.gravitymod.packets.gravitychange.GravityChangePacketHandler;

/**
 * Created by Mysteryem on 2016-08-04.
 */
public class CommonProxy {
    public GravityManagerCommon gravityManagerCommon;

    public void preInit() {
        GravityDirectionCapability.registerCapability();
        this.registerGravityManager();
        GravityChangePacketHandler.registerMessages();
        ModItems.initItems();
        ModItems.initRecipes();
    }

    public void init() {
        this.registerListeners();
    }

    public void registerGravityManager() {
        this.gravityManagerCommon = new GravityManagerCommon();
    }

    public void registerListeners() {
        MinecraftForge.EVENT_BUS.register(this.getGravityManager());
//        MinecraftForge.EVENT_BUS.register(new DebugHelperListener());
//        MinecraftForge.EVENT_BUS.register(new MovementInterceptionListener());
    }

    public GravityManagerCommon getGravityManager() {
        return this.gravityManagerCommon;
    }
}

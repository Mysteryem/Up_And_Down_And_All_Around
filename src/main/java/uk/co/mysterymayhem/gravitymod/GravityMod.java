package uk.co.mysterymayhem.gravitymod;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

/**
 * Created by Mysteryem on 2016-08-04.
 */
@Mod(modid = GravityMod.MOD_ID, version = GravityMod.VERSION, acceptedMinecraftVersions = GravityMod.MINECRAFT_VERSION)
public class GravityMod {
    public static final String MOD_ID = "mysttmtgravitymod";
    public static final String VERSION = "0.1";
    public static final String MINECRAFT_VERSION = "1.10.2";
    public static boolean DEBUG = true;

    @SidedProxy(clientSide = "uk.co.mysterymayhem.gravitymod.ClientProxy", serverSide = "uk.co.mysterymayhem.gravitymod.CommonProxy")
    public static CommonProxy proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit();
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        // Register listeners, blocks etc.
        proxy.init();
    }

}

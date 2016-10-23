package uk.co.mysterymayhem.gravitymod;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import uk.co.mysterymayhem.gravitymod.events.ItemStackRightClickEvent;

import java.io.File;

/**
 * Created by Mysteryem on 2016-08-04.
 */
@Mod(modid = GravityMod.MOD_ID, version = GravityMod.VERSION, acceptedMinecraftVersions = GravityMod.MINECRAFT_VERSION)
public class GravityMod {
    public static final String MOD_ID = "mysttmtgravitymod";
    public static final String VERSION = "2.0";
    public static final String MINECRAFT_VERSION = "1.10.2";

    static boolean MOTION_IS_RELATIVE_WHEN_PLAYERS_USE_ITEMS = true;

    @SidedProxy(clientSide = "uk.co.mysterymayhem.gravitymod.ClientProxy", serverSide = "uk.co.mysterymayhem.gravitymod.CommonProxy")
    public static CommonProxy proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        //TODO: config stuff instead of hardcoding for just the Botania rod (or just for the air sigil
//        File configFile = event.getSuggestedConfigurationFile();
//        Configuration config = new Configuration(configFile);
//        config.load();
//
//        MOTION_IS_RELATIVE_WHEN_PLAYERS_USE_ITEMS = config.getBoolean(
//                "relative_motion_when_using_items",
//                Configuration.CATEGORY_GENERAL,
//                true,
//                "true if player motion should be relative when players use items, this will make it so that items such" +
//                        " as Botania's Rod of the Skies will send the player upwards relative to their view. But it will" +
//                        " make any items which, when used, adjust the player's motion based off of their rotation, send" +
//                        " the player in the wrong direction, such as Blood.");

        proxy.preInit();
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        // Register listeners, blocks etc.
        proxy.init();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        //TODO:'Un-hardcode' this and instead use a config file
        // Hardcoded for the time being
        if (ItemStackRightClickEvent.addRelativeMotionItem("botania", "tornadoRod")) {
            FMLLog.info("[UpAndDownAndAllAround] Added support for botania:tornadoRod");
        }
        else {
            FMLLog.info("[UpAndDownAndAllAround] Could not add support for botania:tornadoRod (does the specified item exist?)");
        }
    }

}

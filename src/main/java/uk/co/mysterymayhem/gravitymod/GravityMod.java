package uk.co.mysterymayhem.gravitymod;

import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import uk.co.mysterymayhem.gravitymod.common.ItemStackUseListener;
import uk.co.mysterymayhem.gravitymod.util.prepostmodifier.CombinedPrePostModifier;
import uk.co.mysterymayhem.gravitymod.util.prepostmodifier.EnumPrePostModifier;

/**
 * Created by Mysteryem on 2016-08-04.
 */
@Mod(modid = GravityMod.MOD_ID, version = GravityMod.VERSION, acceptedMinecraftVersions = GravityMod.MINECRAFT_VERSION)
public class GravityMod {
    public static final String MOD_ID = "mysttmtgravitymod";
    public static final String VERSION = "2.0";
    public static final String MINECRAFT_VERSION = "1.10.2";

    public static final boolean GENERAL_DEBUG = false;

    public static void logInfo(String formattableString, Object... objects) {
        FMLLog.info("[UpAndDownAndAllAround] " + formattableString, objects);
    }

    public static void logWarning(String formattableString, Object... objects) {
        FMLLog.warning("[UpAndDownAndAllAround] " + formattableString, objects);
    }

    @Mod.Instance(GravityMod.MOD_ID)
    public static GravityMod INSTANCE;

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
        ItemStackUseListener.addPrePostModifier("tconstruct:longsword", CombinedPrePostModifier.getModifierFor(EnumPrePostModifier.ALL_MOTION_RELATIVE, EnumPrePostModifier.ROTATION_RELATIVE), ItemStackUseListener.EnumItemStackUseCompat.STOPPED_USING);
        ItemStackUseListener.addPrePostModifier("tconstruct:rapier", CombinedPrePostModifier.getModifierFor(EnumPrePostModifier.ALL_MOTION_RELATIVE, EnumPrePostModifier.ROTATION_RELATIVE), ItemStackUseListener.EnumItemStackUseCompat.GENERAL);
//        ItemStackUseListener.addPrePostModifier("minecraft:stick", CombinedPrePostModifier.getModifierFor(EnumPrePostModifier.ROTATION_RELATIVE, EnumPrePostModifier.ABSOLUTE_Z), ItemStackUseListener.EnumItemStackUseCompat.GENERAL, 1, 52, 12, 18, 19);
//        ItemStackUseListener.addPrePostModifier("minecraft:coal", EnumPrePostModifier.RELATIVE_Y, ItemStackUseListener.EnumItemStackUseCompat.BLOCK, 9, 8, 7, 6, 0);
        ItemStackUseListener.makeHash();
        ItemStackUseListener.buildPacketData();
        FMLLog.info("HashCode: " + ItemStackUseListener.getHashCode());
    }

}

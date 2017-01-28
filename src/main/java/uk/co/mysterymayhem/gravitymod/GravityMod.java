package uk.co.mysterymayhem.gravitymod;

import net.minecraft.item.EnumRarity;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import uk.co.mysterymayhem.gravitymod.common.config.ConfigHandler;
import uk.co.mysterymayhem.gravitymod.common.listeners.ItemStackUseListener;
import uk.co.mysterymayhem.gravitymod.common.modsupport.ModSupport;

/**
 * Created by Mysteryem on 2016-08-04.
 */
@SuppressWarnings("WeakerAccess")
@Mod(
        modid = GravityMod.MOD_ID,
        version = GravityMod.VERSION,
        acceptedMinecraftVersions = GravityMod.MINECRAFT_VERSION,
        dependencies = GravityMod.DEPENDENCIES_LIST,
        name = GravityMod.USER_FRIENDLY_NAME,
        acceptableRemoteVersions = GravityMod.ACCEPTABLE_VERSIONS)
public class GravityMod {
    public static final String MOD_ID = "mysttmtgravitymod";
    public static final String VERSION = "2.4";
    public static final String ACCEPTABLE_VERSIONS = "[2.4,2.5)";
    public static final String MINECRAFT_VERSION = "1.10.2";
    public static final String DEPENDENCIES_LIST = "after:" + ModSupport.BAUBLES_MOD_ID;
    public static final String USER_FRIENDLY_NAME = "Up And Down And All Around";

    public static final EnumRarity RARITY_WEAK = EnumHelper.addRarity("WEAK_GRAVITY",TextFormatting.WHITE, "Weak Strength");
    public static final EnumRarity RARITY_NORMAL = EnumHelper.addRarity("NORMAL_GRAVITY", TextFormatting.DARK_PURPLE, "Normal Strength");
    public static final EnumRarity RARITY_STRONG = EnumHelper.addRarity("STRONG_GRAVITY", TextFormatting.BLUE, "Strong Strength");

    static {
        FluidRegistry.enableUniversalBucket();
    }

    public static final boolean GENERAL_DEBUG = false;

    public static void logInfo(String formattableString, Object... objects) {
        FMLLog.info("[UpAndDownAndAllAround] " + formattableString, objects);
    }

    public static void logWarning(String formattableString, Object... objects) {
        FMLLog.warning("[UpAndDownAndAllAround] " + formattableString, objects);
    }

    private static int currentEntityID = 0;

    public static int getNextEntityID() {
        return currentEntityID++;
    }

    @Mod.Instance(GravityMod.MOD_ID)
    public static GravityMod INSTANCE;

    @SidedProxy(clientSide = "uk.co.mysterymayhem.gravitymod.ClientProxy", serverSide = "uk.co.mysterymayhem.gravitymod.CommonProxy")
    public static CommonProxy proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        //TODO: config stuff instead of hardcoding for just the Botania rod (or just for the air sigil
        ConfigHandler.loadConfig(event);
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
        ConfigHandler.processLateConfig();
        ConfigHandler.processModCompatConfig();
        ItemStackUseListener.makeHash();
        ItemStackUseListener.buildPacketData();
        if (GravityMod.GENERAL_DEBUG) {
            GravityMod.logInfo("HashCode: " + ItemStackUseListener.getHashCode());
        }
        proxy.postInit();
    }

}

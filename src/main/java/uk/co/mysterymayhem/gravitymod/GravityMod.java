package uk.co.mysterymayhem.gravitymod;

import net.minecraft.item.EnumRarity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.StringFormatterMessageFactory;
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
        acceptableRemoteVersions = GravityMod.ACCEPTABLE_VERSIONS,
        guiFactory = GravityMod.GUI_FACTORY
)
public class GravityMod {
    // Major changes, 1 was used for TMT, 2 is main development, 3 will likely be for initial full release and beyond
    private static final int MAJOR_VERSION = 2;
    // Indicates a breaking change in the mod, requiring both client and server updates
    private static final int MINOR_VERSION = 9;
    // Indicates a non-breaking change in the mod
    // Different patch numbers but the same major and minor version should be compatible with one another
    private static final int PATCH_NUMBER = 1;

    // M.m.p
    public static final String VERSION = "" + MAJOR_VERSION + '.' + MINOR_VERSION + '.' + PATCH_NUMBER;
    // [M.m,M.m+1), e.g. [2.5,2.6)
    public static final String ACCEPTABLE_VERSIONS = "[" + MAJOR_VERSION + '.' + MINOR_VERSION + ',' + MAJOR_VERSION + '.' + (MINOR_VERSION + 1) + ")";
    public static final String MOD_ID = "mysttmtgravitymod";
    public static final String MINECRAFT_VERSION = "1.12.2";
    //Forge 12.18.2.2107 adds Block#getStateForPlacement
    //Baubles 1.3.BETA9 adds default methods to IRenderBauble
    public static final String DEPENDENCIES_LIST = "required-after:forge@[14.23.4.2744,];after:" + ModSupport.BAUBLES_MOD_ID + "@[1.3.BETA9,]";
    public static final String USER_FRIENDLY_NAME = "Up And Down And All Around";
    public static final String GUI_FACTORY = "uk.co.mysterymayhem.gravitymod.client.config.GravityModGuiFactory";

    public static final EnumRarity RARITY_WEAK = EnumHelper.addRarity("WEAK_GRAVITY", TextFormatting.WHITE, "Weak Strength");
    public static final EnumRarity RARITY_NORMAL = EnumHelper.addRarity("NORMAL_GRAVITY", TextFormatting.DARK_PURPLE, "Normal Strength");
    public static final EnumRarity RARITY_STRONG = EnumHelper.addRarity("STRONG_GRAVITY", TextFormatting.BLUE, "Strong Strength");

    private static final Logger logger = LogManager.getLogger("UpAndDownAndAllAround", StringFormatterMessageFactory.INSTANCE);

    public static final boolean GENERAL_DEBUG = false;

    @Mod.Instance(GravityMod.MOD_ID)
    public static GravityMod INSTANCE;

    @SidedProxy(clientSide = "uk.co.mysterymayhem.gravitymod.ClientProxy", serverSide = "uk.co.mysterymayhem.gravitymod.CommonProxy")
    public static CommonProxy proxy;

    private static int currentEntityID = 0;

    static {
        FluidRegistry.enableUniversalBucket();
    }

    public static int getNextEntityID() {
        return currentEntityID++;
    }

    public static void logWarning(String formattableString, Object... objects) {
        logger.warn(formattableString, objects);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        // Register listeners, blocks etc.
        proxy.init();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        ConfigHandler.processLateConfig();
        if (GravityMod.GENERAL_DEBUG) {
            GravityMod.logInfo("HashCode: " + ItemStackUseListener.getHashCode());
        }
        proxy.postInit();
    }

    public static void logInfo(String formattableString, Object... objects) {
        logger.info(formattableString, objects);
    }

    public static ResourceLocation resource(String path) {return new ResourceLocation(GravityMod.MOD_ID, path);}

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        //TODO: config stuff instead of hardcoding for just the Botania rod (or just for the air sigil
        ConfigHandler.initialConfigLoad(event);
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

}

package uk.co.mysterymayhem.gravitymod.common.config;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import uk.co.mysterymayhem.gravitymod.GravityMod;
import uk.co.mysterymayhem.gravitymod.asm.EntityPlayerWithGravity;
import uk.co.mysterymayhem.gravitymod.common.listeners.ItemStackUseListener;
import uk.co.mysterymayhem.gravitymod.common.util.prepostmodifier.CombinedPrePostModifier;
import uk.co.mysterymayhem.gravitymod.common.util.prepostmodifier.EnumPrePostModifier;
import uk.co.mysterymayhem.gravitymod.common.util.prepostmodifier.IPrePostModifier;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Main config file class
 * Created by Mysteryem on 2016-10-23.
 */
public class ConfigHandler {
    public enum EnumItemStackUseCompat {
        BLOCK(
                "modCompat.onUseOnBlock",

                "Adding an item to this list will apply the compatibility when the item is right clicked on a block.\n" +
                "\tSpecifically when the item's \"onItemUse\" method is called from within ItemStack::onItemUse.\n"
        ),
        GENERAL(
                "modCompat.onUseGeneral",

                "Adding an item to this list will apply the compatibility when the item is right clicked on air, or on\n" +
                        "\ta block that doesn't open a chest/machine GUI or otherwise change state, such as right clicking \n" +
                        "\ton a vanilla lever. Specifically when the item's \"onItemRightClick\" method is called from\n" +
                        "\twithin ItemStack::useItemRightClick.\n\n" +

                        "The Tinkers' Construct Rapier makes the player jump upwards and backwards\n" +
                        "\tslightly when right clicked. We want the player to jump upwards relative to their view of the\n" +
                        "\tworld, so we will need at least use relative Y motion for this compatibility. We also want the\n" +
                        "\tplayer to jump backwards relative to their view of the world. The rapier uses the player's\n" +
                        "\tpitch and yaw rotations to calculate the backwards direction and modifies X and Z motion\n" +
                        "\taccordingly, so we need them to be relative too.\n",

                "tconstruct:rapier,relativeMotionAll:relativeRotation"
        ),

        STOPPED_USING(
                "modCompat.onStoppedUsing",
                "Adding an item to this list will apply the compatibility when a player stops 'using' an item. For bows,\n" +
                        "\tthis is when you release right click to fire an arrow.\n",
                "tconstruct:longsword,relativeMotionAll:relativeRotation");

        private final String configString;
        private final String[] defaults;
        private final String comment;

        EnumItemStackUseCompat(String configString, String comment, String... defaults) {
            this.configString = configString;
            this.comment = comment;
            this.defaults = defaults;
        }
    }

    private static final String MAIN_COMMENT =
            "Up And Down changes how player motion is used to move the player. Some items may move the player in\n" +
                    "\tunexpected ways when used. If that's the case, try adding them to one of the below lists. The\n" +
                    "format is (text in square brackets is optional)\n\n" +

                    "\t\"<mod id>:<item name>[:damage][:damage][...],<compatibility modifier>:[compatibility modifier]\"\n\n" +

                    "Where a compatibility modifier is case insensitive and one of:\n" +
                    "relativeRotation, relativeMotionAll, relativeMotionX, relativeMotionY, relativeMotionZ, absoluteMotionX,\n" +
                    "\tabsoluteMotionY or absoluteMotionZ\n\n" +

                    "These modify the player's rotation and/or motion before the item is used (and then puts the rotation/motion\n" +
                    "\tback to normal, taking into account any changes to motion, !!but not taking into account any changes to\n" +
                    "\trotation!!).\n\n" +

                    "You may use a max of one type of modifier per entry (up to 1 motion modifier and up to 1 rotation modifier).\n\n" +

                    "The default state when using items is that both rotation and motion are absolute (as if the player currently\n" +
                    "\thas downwards gravity).\n\n" +

                    "Using relativeMotionX implies that Z and Y motion will be absolute.\n\n" +

                    "Likewise, using absoluteMotionY implies that X and Z motion will be relative.\n\n" +

                    "So if you wanted both X and Y motion to be relative, you would use absoluteMotionZ.\n\n" +

                    "Examples:\n" +
                    "'minecraft:stick:0:4,relativeMotionX' - Adds a relative X motion modifier to vanilla sticks with damage\n" +
                    "\tvalues 0 and 4.\n" +
                    "'tconstruct:longsword,relativeMotionAll:relativeRotation' - Adds a relative X, Y and Z motion modifier\n" +
                    "\tcombined with a relative rotation modifier to all damage values of Tinkers' Construct Longswords.";

    private static final String CONFIG_DIRECTORY_NAME = "UpAndDownAndAllAround";
    private static final String MOD_COMPATIBILITY_CONFIG_FILE_NAME = "modCompat.cfg";
    private static final String GENERAL_CONFIG = "config.cfg";

    private static Configuration modCompatibilityConfig;
    private static Configuration generalConfig;

    public static double animationRotationSpeed = 1d;
    public static float oppositeDirectionFallDistanceMultiplier = 0f;
    public static float otherDirectionFallDistanceMultiplier = 0.5f;

    public static void loadConfig(FMLPreInitializationEvent event) {
        File modConfigurationDirectory = event.getModConfigurationDirectory().toPath().resolve(CONFIG_DIRECTORY_NAME).toFile();
        if (!modConfigurationDirectory.exists()) {
            if (!modConfigurationDirectory.mkdirs()) {
                throw new RuntimeException(new IOException("Could not create config folder"));
            }
        }
        if (!modConfigurationDirectory.isDirectory()) {
            throw new RuntimeException(new IOException("Config folder already exists, but is a file and not a folder"));
        }

        Path configDirectoryPath = modConfigurationDirectory.toPath();
        File modCompatConfigFile = configDirectoryPath.resolve(MOD_COMPATIBILITY_CONFIG_FILE_NAME).toFile();
        if (!modCompatConfigFile.exists()) {
            try {
                if (!modCompatConfigFile.createNewFile()) {
                    throw new RuntimeException(new IOException("Unable to create mod compatibility config file"));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        ConfigHandler.modCompatibilityConfig = new Configuration(modCompatConfigFile);

        File generalConfigFile = configDirectoryPath.resolve(GENERAL_CONFIG).toFile();
        if (!generalConfigFile.exists()) {
            try {
                if (!generalConfigFile.createNewFile()) {
                    throw new RuntimeException(new IOException("Unable to create general config file"));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        ConfigHandler.generalConfig = new Configuration(generalConfigFile);

        processGeneralConfig();
    }

    private static void processGeneralConfig() {
        Configuration config = ConfigHandler.generalConfig;
        config.load();

        config.addCustomCategoryComment(Configuration.CATEGORY_CLIENT, "Client only config options");
        config.addCustomCategoryComment(Configuration.CATEGORY_GENERAL, "Common config options");

        ConfigHandler.animationRotationSpeed = config.get(
                Configuration.CATEGORY_CLIENT,
                "transition.animationSpeed",
                1.5d,
                "Animation speed for gravity transition.\nAnimation takes 1 second divided by the config value.\nMin value 1.0",
                1.0d,
                1000d).getDouble();


        ConfigHandler.oppositeDirectionFallDistanceMultiplier = config.getFloat(
                "transition.fallDistanceMultiplier.opposite", Configuration.CATEGORY_GENERAL, 0f, 0f, 1f,
                "When a player's gravity direction changes to the opposite direction," +
                        "\n\ttheir accrued fall distance will be multiplied by this value." +
                        "\n\nFall damage is performed server side, so it won't matter much if the client's values don't match.");
        ConfigHandler.otherDirectionFallDistanceMultiplier = config.getFloat(
                "transition.fallDistanceMultiplier.other", Configuration.CATEGORY_GENERAL, 0.5f, 0f, 1f,
                "When a player's gravity direction changes to a direction other than the opposite direction," +
                        "\n\ttheir accrued fall distance will be multiplied by this value." +
                        "\n\nFall damage is performed server side, so it won't matter much if the client's values don't match.");

        if (config.hasChanged()) {
            config.save();
        }
    }

    public static void processModCompatConfig() {
        Configuration config = ConfigHandler.modCompatibilityConfig;
        config.load();

        config.addCustomCategoryComment(Configuration.CATEGORY_GENERAL, MAIN_COMMENT);

        for (EnumItemStackUseCompat compatType : EnumItemStackUseCompat.values()) {
            processStringList(compatType);
        }

        if (config.hasChanged()) {
            config.save();
        }
    }

    private static void processStringList(EnumItemStackUseCompat compatType) {
        String[] stringList = ConfigHandler.modCompatibilityConfig.getStringList(
                compatType.configString, Configuration.CATEGORY_GENERAL, compatType.defaults, compatType.comment);

        toNextEntry:
        for (String entry : stringList) {
            String[] sections = entry.split(",");
            if (sections.length != 2) {
                GravityMod.logWarning("Invalid config line in %s: %s. Each line should have a single comma " +
                        "separating item information and the compatibility information.", compatType.configString, entry);
                continue;
            }

            String[] itemResourceAndDamageValues = sections[0].split(":");
            if (itemResourceAndDamageValues.length < 2) {
                GravityMod.logWarning("Invalid config line in %s: %s. A colon is required between the item's mod ID and the item's name.", compatType.configString, entry);
                continue;
            }
            String modID = itemResourceAndDamageValues[0];
            String itemName = itemResourceAndDamageValues[1];
            int[] damageValues = new int[itemResourceAndDamageValues.length - 2];
            for (int i = 2; i < itemResourceAndDamageValues.length; i++) {
                try {
                    damageValues[i - 2] = Integer.parseInt(itemResourceAndDamageValues[i]);
                } catch (NumberFormatException numberFormatException) {
                    GravityMod.logWarning("Invalid config line in %s: %s. Could not parse item damage value \"%s\".",
                            compatType.configString, entry, itemResourceAndDamageValues[i]);
                    continue toNextEntry;
                }
            }

            IPrePostModifier<EntityPlayerWithGravity> modifier;

            String[] modifierData = sections[1].split(":");
            switch (modifierData.length) {
                case 1:
                    EnumPrePostModifier fromConfigString = EnumPrePostModifier.getFromConfigString(modifierData[0]);
                    if (fromConfigString == null) {
                        GravityMod.logWarning("Invalid config line in %s: %s. Invalid compatibility modifier \"%s\"",
                                compatType.configString, entry, modifierData[0]);
                        continue toNextEntry;
                    }
                    modifier = fromConfigString;
                    break;
                case 2:
                    EnumPrePostModifier first = EnumPrePostModifier.getFromConfigString(modifierData[0]);
                    if (first == null) {
                        GravityMod.logWarning("Invalid config line in %s: %s. Invalid compatibility modifier \"%s\"",
                                compatType.configString, entry, modifierData[0]);
                        continue toNextEntry;
                    }
                    EnumPrePostModifier second = EnumPrePostModifier.getFromConfigString(modifierData[1]);
                    if (second == null) {
                        GravityMod.logWarning("Invalid config line in %s: %s. Invalid compatibility modifier \"%s\"",
                                compatType.configString, entry, modifierData[1]);
                        continue toNextEntry;
                    }
                    if (first.isMotionModifier() == second.isMotionModifier()) {
                        GravityMod.logWarning("Invalid config line in %s: %s. Only up to 1 motion modifier and up to 1 rotation modifier may be used.",
                                compatType.configString, entry);
                        continue toNextEntry;
                    }
                    modifier = CombinedPrePostModifier.getModifierFor(first, second);
                    break;
                default:
                    GravityMod.logWarning("Invalid config line in %s: %s. Expecting 1 or 2 compatibility modifiers only.", compatType.configString, entry);
                    continue toNextEntry;
            }
            ItemStackUseListener.addPrePostModifier(modID, itemName, modifier, compatType, damageValues);
        }
    }
}

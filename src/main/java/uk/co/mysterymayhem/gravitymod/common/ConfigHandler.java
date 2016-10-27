package uk.co.mysterymayhem.gravitymod.common;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
 * Created by Thomas on 2016-10-23.
 */
public class ConfigHandler {
    private static Configuration forgeConfig;

    public static void loadConfig(FMLPreInitializationEvent event) {
        Configuration config = new Configuration(event.getSuggestedConfigurationFile());
        ConfigHandler.forgeConfig = config;
        config.load();

        String[] useOnBlockList = config.getStringList(
                "modCompat.onUseOnBlock",
                Configuration.CATEGORY_GENERAL,
                new String[]{
                        "botania:tornadoRod"},
                "Up And Down changes how player motion is used to move the player. Some items may move the player in " +
                        "unexpected ways when used. If that's the case, add them to this list. The Rod of the Skies " +
                        "(botania:tornadoRod) needs to be in this list so that it sends the player upwards from their" +
                        "own perspective, rather than from the perspective of the world");
        String[] useGeneralList = config.getStringList(
                "modCompat.onUseGeneral",
                Configuration.CATEGORY_GENERAL,
                new String[]{
                        "botania:tornadoRod"},
                "Up And Down changes how player motion is used to move the player. Some items may move the player in " +
                        "unexpected ways when used. If that's the case, add them to this list. The Rod of the Skies " +
                        "(botania:tornadoRod) needs to be in this list so that it sends the player upwards from their" +
                        "own perspective, rather than from the perspective of the world");
        String[] onStoppedUsingList = config.getStringList(
                "modCompat.onStoppedUsing",
                Configuration.CATEGORY_GENERAL,
                new String[0],
                "comment");

        if (config.hasChanged()) {
            config.save();
        }

    }

}

package uk.co.mysterymayhem.gravitymod.client.config;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.config.DummyConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import uk.co.mysterymayhem.gravitymod.GravityMod;
import uk.co.mysterymayhem.gravitymod.common.config.ConfigHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mysteryem on 2017-02-07.
 */
@SideOnly(Side.CLIENT)
public class GravityModGuiScreen extends GuiConfig {
    public GravityModGuiScreen(GuiScreen parent) {
        super(parent, getConfigElements(), GravityMod.MOD_ID, false, false, I18n.format("config.mysttmtgravitymod.title"));

    }

    private static List<IConfigElement> getConfigElements() {

        ArrayList<IConfigElement> highestLevel = new ArrayList<>();
        ArrayList<IConfigElement> blockAndItemMenu = new ArrayList<>();
//        ArrayList<IConfigElement> blockMenu = new ArrayList<>();
//        ArrayList<IConfigElement> entityMenu = new ArrayList<>();
//
//        highestLevel.add(new DummyConfigElement.DummyCategoryElement("block", "mysthighlights.config.block", blockMenu));
//        highestLevel.add(new DummyConfigElement.DummyCategoryElement("entity", "mysthighlights.config.entity", entityMenu));

        highestLevel.add(getCategory(ConfigHandler.CATEGORY_GRAVITY));
        highestLevel.add(getCategory(ConfigHandler.CATEGORY_WORLD_GEN));
        highestLevel.add(getCategory(ConfigHandler.CATEGORY_LOOT));
        highestLevel.add(getCategory(ConfigHandler.CATEGORY_MOD_COMPAT));
        highestLevel.add(new DummyConfigElement.DummyCategoryElement("blocksAndItems", "config.mysttmtgravitymod.menu.blocksAndItems", blockAndItemMenu));
        highestLevel.add(getCategory(ConfigHandler.CATEGORY_FALLOUTOFWORLD));
        highestLevel.add(getCategory(ConfigHandler.CATEGORY_CLIENT));
        highestLevel.add(getCategory(ConfigHandler.CATEGORY_SERVER));

        blockAndItemMenu.add(getCategory(ConfigHandler.CATEGORY_GRAVITON_PEARL));
        blockAndItemMenu.add(getCategory(ConfigHandler.CATEGORY_ANTIMASS_INDUCER));
        blockAndItemMenu.add(getCategory(ConfigHandler.CATEGORY_DESTABILISED_ANTIMASS));
        blockAndItemMenu.add(getCategory(ConfigHandler.CATEGORY_GRAVITY_GENERATOR));

        return highestLevel;
    }

    private static IConfigElement getCategory(String categoryName) {
        String categoryNameNoPrefix = categoryName.substring(categoryName.indexOf('_') + 1).replace('_', '.');
        DummyConfigElement.DummyCategoryElement dummyCategoryElement = new DummyConfigElement.DummyCategoryElement(categoryNameNoPrefix, "config." + GravityMod.MOD_ID + "." + categoryNameNoPrefix,
                getElementsForCategory(categoryName));
        dummyCategoryElement.setRequiresMcRestart(ConfigHandler.config.getCategory(categoryName).requiresMcRestart());
        dummyCategoryElement.setRequiresWorldRestart(ConfigHandler.config.getCategory(categoryName).requiresWorldRestart());
        return dummyCategoryElement;
    }

    private static List<IConfigElement> getElementsForCategory(String categoryName) {
        return new ConfigElement(ConfigHandler.config.getCategory(categoryName)).getChildElements();
    }

    private static IConfigElement getCategory(String name, String langKey, String categoryName) {
        return new DummyConfigElement.DummyCategoryElement(name, langKey, getElementsForCategory(categoryName));
    }
}

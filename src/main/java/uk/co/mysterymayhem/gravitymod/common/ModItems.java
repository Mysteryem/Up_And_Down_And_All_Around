package uk.co.mysterymayhem.gravitymod.common;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import uk.co.mysterymayhem.gravitymod.GravityMod;
import uk.co.mysterymayhem.gravitymod.common.items.armour.ItemGravityBoots;
import uk.co.mysterymayhem.gravitymod.common.items.armour.ItemGravityChestplate;
import uk.co.mysterymayhem.gravitymod.common.items.armour.ItemGravityHelmet;
import uk.co.mysterymayhem.gravitymod.common.items.armour.ItemGravityLeggings;
import uk.co.mysterymayhem.gravitymod.common.items.baubles.ItemGravityBauble;
import uk.co.mysterymayhem.gravitymod.common.items.materials.*;
import uk.co.mysterymayhem.gravitymod.common.items.misc.ItemCreativeTabIcon;
import uk.co.mysterymayhem.gravitymod.common.items.shared.IModItem;
import uk.co.mysterymayhem.gravitymod.common.items.tools.ItemGravityAnchor;
import uk.co.mysterymayhem.gravitymod.common.items.tools.ItemPersonalGravityController;
import uk.co.mysterymayhem.gravitymod.common.items.tools.ItemUltimateGravityController;
import uk.co.mysterymayhem.gravitymod.common.items.tools.ItemWeakGravityController;

import java.util.ArrayList;

/**
 * Created by Mysteryem on 2016-08-08.
 */
public class ModItems {

    public static final CreativeTabs UP_AND_DOWN_CREATIVE_TAB;
    //'fake' tab for JEI compat
    public static final CreativeTabs FAKE_TAB_FOR_CONTROLLERS;
    static {
        UP_AND_DOWN_CREATIVE_TAB = new CreativeTabs(GravityMod.MOD_ID) {
            @Override
            public Item getTabIconItem() {
                return CREATIVE_TAB_ICON;
            }
        };

        // Get the current array of creative tabs
        CreativeTabs[] creativeTabArray = CreativeTabs.CREATIVE_TAB_ARRAY;

        // Add our 'fake' tab (this creates a new array with an increased size)
        FAKE_TAB_FOR_CONTROLLERS = new CreativeTabs(GravityMod.MOD_ID + "allcontrollers") {
            @Override
            public Item getTabIconItem() {
                return CREATIVE_TAB_ICON;
            }
        };

        // We don't want our 'fake' tab in the array, so we restore the array of creative tabs to what it was before we
        // added the 'fake' tab
        CreativeTabs.CREATIVE_TAB_ARRAY = creativeTabArray;
    }

    private static ItemCreativeTabIcon CREATIVE_TAB_ICON;
    public static ItemPersonalGravityController personalGravityController;
    public static ItemWeakGravityController weakGravityController;
    public static ItemUltimateGravityController ultimateGravityController;
    public static ItemGravityBauble gravityBauble;
    public static ItemGravityAnchor gravityAnchor;
    public static ItemGravityIngot gravityIngot;
    public static ItemGravityPearl gravityPearl;
    public static ItemGravityDust gravityDust;
    public static ItemSpacetimeAnomaly spacetimeAnomaly;
    public static ItemArmourPaste armourPaste;
    public static ItemGravityBoots gravityBoots;
    public static ItemGravityChestplate gravityChestplate;
    public static ItemGravityHelmet gravityHelmet;
    public static ItemGravityLeggings gravityLeggings;

    private static ArrayList<IModItem> allModItems = new ArrayList<>();

    public static void preInitItems() {
        preInitItem(CREATIVE_TAB_ICON = new ItemCreativeTabIcon());
        preInitItem(personalGravityController = new ItemPersonalGravityController());
        preInitItem(weakGravityController = new ItemWeakGravityController());
        preInitItem(ultimateGravityController = new ItemUltimateGravityController());
        preInitItem(gravityBauble = new ItemGravityBauble());
        preInitItem(gravityAnchor = new ItemGravityAnchor());
        preInitItem(gravityIngot = new ItemGravityIngot());
        preInitItem(gravityPearl = new ItemGravityPearl());
        preInitItem(gravityDust = new ItemGravityDust());
        preInitItem(spacetimeAnomaly = new ItemSpacetimeAnomaly());
        preInitItem(armourPaste = new ItemArmourPaste());
        preInitItem(gravityBoots = new ItemGravityBoots());
        preInitItem(gravityChestplate = new ItemGravityChestplate());
        preInitItem(gravityHelmet = new ItemGravityHelmet());
        preInitItem(gravityLeggings = new ItemGravityLeggings());
    }

    private static <T extends Item & IModItem> T preInitItem(T modItem) {
        GravityMod.proxy.preInitItem(modItem);
        allModItems.add(modItem);
        return modItem;
    }

    public static void postInitRecipes() {
        allModItems.forEach(IModItem::postInitRecipes);
    }
}

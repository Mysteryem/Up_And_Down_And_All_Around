package uk.co.mysterymayhem.gravitymod.common.registries;

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
import uk.co.mysterymayhem.gravitymod.common.items.tools.ItemGravityAnchor;
import uk.co.mysterymayhem.gravitymod.common.items.tools.ItemPersonalGravityController;
import uk.co.mysterymayhem.gravitymod.common.items.tools.ItemUltimateGravityController;
import uk.co.mysterymayhem.gravitymod.common.items.tools.ItemWeakGravityController;
import uk.co.mysterymayhem.mystlib.setup.registries.AbstractItemRegistry;

import java.util.ArrayList;

/**
 * Created by Mysteryem on 2016-08-08.
 */
public class ModItems extends AbstractItemRegistry<IGravityModItem<?>, ArrayList<IGravityModItem<?>>> {

    public static final CreativeTabs UP_AND_DOWN_CREATIVE_TAB;
    //'fake' tab for JEI compat
    public static final CreativeTabs FAKE_TAB_FOR_CONTROLLERS;
    static {
        UP_AND_DOWN_CREATIVE_TAB = new CreativeTabs(GravityMod.MOD_ID) {
            @Override
            public Item getTabIconItem() {
                return creativeTabIcon;
            }
        };

        // Get the current array of creative tabs
        CreativeTabs[] creativeTabArray = CreativeTabs.CREATIVE_TAB_ARRAY;

        // Add our 'fake' tab (this creates a new array with an increased size)
        FAKE_TAB_FOR_CONTROLLERS = new CreativeTabs(GravityMod.MOD_ID + "allcontrollers") {
            @Override
            public Item getTabIconItem() {
                return creativeTabIcon;
            }
        };

        // We don't want our 'fake' tab in the array, so we restore the array of creative tabs to what it was before we
        // added the 'fake' tab
        CreativeTabs.CREATIVE_TAB_ARRAY = creativeTabArray;
    }

    @SuppressWarnings("WeakerAccess")
    static ItemCreativeTabIcon creativeTabIcon;
    static ItemPersonalGravityController personalGravityController;
    static ItemWeakGravityController weakGravityController;
    static ItemUltimateGravityController ultimateGravityController;
    static ItemGravityBauble gravityBauble;
    static ItemGravityAnchor gravityAnchor;
    static ItemGravityIngot gravityIngot;
    static ItemGravityPearl gravityPearl;
    static ItemGravityDust gravityDust;
    static ItemSpacetimeAnomaly spacetimeAnomaly;
    static ItemArmourPaste armourPaste;
    static ItemGravityBoots gravityBoots;
    static ItemGravityChestplate gravityChestplate;
    static ItemGravityHelmet gravityHelmet;
    static ItemGravityLeggings gravityLeggings;

    static boolean REGISTRY_SETUP_ALLOWED = false;

    public ModItems() {
        super(new ArrayList<>());
    }

    @Override
    protected void addToCollection(ArrayList<IGravityModItem<?>> modObjects) {
        modObjects.add(creativeTabIcon             = new ItemCreativeTabIcon());
        modObjects.add(personalGravityController   = new ItemPersonalGravityController());
        modObjects.add(weakGravityController       = new ItemWeakGravityController());
        modObjects.add(ultimateGravityController   = new ItemUltimateGravityController());
        modObjects.add(gravityBauble               = new ItemGravityBauble());
        modObjects.add(gravityAnchor               = new ItemGravityAnchor());
        modObjects.add(gravityIngot                = new ItemGravityIngot());
        modObjects.add(gravityPearl                = new ItemGravityPearl());
        modObjects.add(gravityDust                 = new ItemGravityDust());
        modObjects.add(spacetimeAnomaly            = new ItemSpacetimeAnomaly());
        modObjects.add(armourPaste                 = new ItemArmourPaste());
        modObjects.add(gravityBoots                = new ItemGravityBoots());
        modObjects.add(gravityChestplate           = new ItemGravityChestplate());
        modObjects.add(gravityHelmet               = new ItemGravityHelmet());
        modObjects.add(gravityLeggings             = new ItemGravityLeggings());
        REGISTRY_SETUP_ALLOWED = true;
    }

}

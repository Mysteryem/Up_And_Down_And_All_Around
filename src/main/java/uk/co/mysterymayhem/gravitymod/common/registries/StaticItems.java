package uk.co.mysterymayhem.gravitymod.common.registries;

import net.minecraft.item.ItemStack;
import uk.co.mysterymayhem.gravitymod.common.items.armour.ItemGravityBoots;
import uk.co.mysterymayhem.gravitymod.common.items.armour.ItemGravityChestplate;
import uk.co.mysterymayhem.gravitymod.common.items.armour.ItemGravityHelmet;
import uk.co.mysterymayhem.gravitymod.common.items.armour.ItemGravityLeggings;
import uk.co.mysterymayhem.gravitymod.common.items.baubles.ItemGravityBauble;
import uk.co.mysterymayhem.gravitymod.common.items.baubles.ItemGravityFieldGoggles;
import uk.co.mysterymayhem.gravitymod.common.items.materials.*;
import uk.co.mysterymayhem.gravitymod.common.items.tools.ItemGravityAnchor;
import uk.co.mysterymayhem.gravitymod.common.items.tools.ItemPersonalGravityController;
import uk.co.mysterymayhem.gravitymod.common.items.tools.ItemUltimateGravityController;
import uk.co.mysterymayhem.gravitymod.common.items.tools.ItemWeakGravityController;

/**
 * Registry of singletons used in the mod, static and final for speed purposes.
 * <p>
 * The static block aka &lt;clinit&gt; is run when this class is referenced for the first time (usually when some code
 * tries to access one of the static fields)
 * <p>
 * Created by Mysteryem on 2016-12-07.
 */
public class StaticItems {
    public static final ItemPersonalGravityController PERSONAL_GRAVITY_CONTROLLER;
    public static final ItemWeakGravityController WEAK_GRAVITY_CONTROLLER;
    public static final ItemUltimateGravityController ULTIMATE_GRAVITY_CONTROLLER;
    public static final ItemGravityBauble GRAVITY_BAUBLE;
    public static final ItemGravityAnchor GRAVITY_ANCHOR;
    public static final ItemGravityIngot GRAVITY_INGOT;
    public static final ItemGravityPearl GRAVITY_PEARL;
    public static final ItemGravityDust GRAVITY_DUST;
    public static final ItemSpacetimeAnomaly SPACETIME_ANOMALY;
    public static final ItemArmourPaste ARMOUR_PASTE;
    public static final ItemGravityBoots GRAVITY_BOOTS;
    public static final ItemGravityChestplate GRAVITY_CHESTPLATE;
    public static final ItemGravityHelmet GRAVITY_HELMET;
    public static final ItemGravityLeggings GRAVITY_LEGGINGS;
    public static final ItemGravityFieldGoggles GRAVITY_FIELD_GOGGLES;
    public static final ItemDestabilisedGravityDust DESTABILISED_GRAVITY_DUST;
    public static final ItemRestabilisedGravityDust RESTABILISED_GRAVITY_DUST;
    public static final ItemStack LIQUID_ANTI_MASS_BUCKET;

    static {
        if (ModItems.STATIC_SETUP_ALLOWED) {
            PERSONAL_GRAVITY_CONTROLLER = ModItems.personalGravityController;
            WEAK_GRAVITY_CONTROLLER = ModItems.weakGravityController;
            ULTIMATE_GRAVITY_CONTROLLER = ModItems.ultimateGravityController;
            GRAVITY_BAUBLE = ModItems.gravityBauble;
            GRAVITY_ANCHOR = ModItems.gravityAnchor;
            GRAVITY_INGOT = ModItems.gravityIngot;
            GRAVITY_PEARL = ModItems.gravityPearl;
            GRAVITY_DUST = ModItems.gravityDust;
            SPACETIME_ANOMALY = ModItems.spacetimeAnomaly;
            ARMOUR_PASTE = ModItems.armourPaste;
            GRAVITY_BOOTS = ModItems.gravityBoots;
            GRAVITY_CHESTPLATE = ModItems.gravityChestplate;
            GRAVITY_HELMET = ModItems.gravityHelmet;
            GRAVITY_LEGGINGS = ModItems.gravityLeggings;
            GRAVITY_FIELD_GOGGLES = ModItems.gravityFieldGoggles;
            DESTABILISED_GRAVITY_DUST = ModItems.destabilisedGravityDust;
            RESTABILISED_GRAVITY_DUST = ModItems.restabilisedGravityDust;
            LIQUID_ANTI_MASS_BUCKET = ModItems.liquidAntiMassBucket;
        }
        else {
            throw new RuntimeException("Static item registry setup occurring too early");
        }
    }
}

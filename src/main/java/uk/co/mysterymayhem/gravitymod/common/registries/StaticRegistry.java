package uk.co.mysterymayhem.gravitymod.common.registries;

import uk.co.mysterymayhem.gravitymod.common.items.armour.ItemGravityBoots;
import uk.co.mysterymayhem.gravitymod.common.items.armour.ItemGravityChestplate;
import uk.co.mysterymayhem.gravitymod.common.items.armour.ItemGravityHelmet;
import uk.co.mysterymayhem.gravitymod.common.items.armour.ItemGravityLeggings;
import uk.co.mysterymayhem.gravitymod.common.items.baubles.ItemGravityBauble;
import uk.co.mysterymayhem.gravitymod.common.items.materials.*;
import uk.co.mysterymayhem.gravitymod.common.items.tools.ItemGravityAnchor;
import uk.co.mysterymayhem.gravitymod.common.items.tools.ItemPersonalGravityController;
import uk.co.mysterymayhem.gravitymod.common.items.tools.ItemUltimateGravityController;
import uk.co.mysterymayhem.gravitymod.common.items.tools.ItemWeakGravityController;

/**
 * Registry of singletons used in the mod, static and final for speed purposes.
 *
 * The static block aka &lt;clinit&gt; is run when this class is referenced for the first time (usually when some code
 * tries to access one of the static fields)
 *
 * Created by Mysteryem on 2016-12-07.
 */
public class StaticRegistry {
    public static final ItemPersonalGravityController personalGravityController;
    public static final ItemWeakGravityController weakGravityController;
    public static final ItemUltimateGravityController ultimateGravityController;
    public static final ItemGravityBauble gravityBauble;
    public static final ItemGravityAnchor gravityAnchor;
    public static final ItemGravityIngot gravityIngot;
    public static final ItemGravityPearl gravityPearl;
    public static final ItemGravityDust gravityDust;
    public static final ItemSpacetimeAnomaly spacetimeAnomaly;
    public static final ItemArmourPaste armourPaste;
    public static final ItemGravityBoots gravityBoots;
    public static final ItemGravityChestplate gravityChestplate;
    public static final ItemGravityHelmet gravityHelmet;
    public static final ItemGravityLeggings gravityLeggings;

    static {
        if (ModItems.REGISTRY_SETUP_ALLOWED) {
            personalGravityController = ModItems.personalGravityController;
            weakGravityController = ModItems.weakGravityController;
            ultimateGravityController = ModItems.ultimateGravityController;
            gravityBauble = ModItems.gravityBauble;
            gravityAnchor = ModItems.gravityAnchor;
            gravityIngot = ModItems.gravityIngot;
            gravityPearl = ModItems.gravityPearl;
            gravityDust = ModItems.gravityDust;
            spacetimeAnomaly = ModItems.spacetimeAnomaly;
            armourPaste = ModItems.armourPaste;
            gravityBoots = ModItems.gravityBoots;
            gravityChestplate = ModItems.gravityChestplate;
            gravityHelmet = ModItems.gravityHelmet;
            gravityLeggings = ModItems.gravityLeggings;
        }
        else {
            throw new RuntimeException("Static item registry setup occurring too early");
        }
    }
}

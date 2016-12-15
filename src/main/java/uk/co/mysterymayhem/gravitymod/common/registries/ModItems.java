package uk.co.mysterymayhem.gravitymod.common.registries;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
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
import uk.co.mysterymayhem.gravitymod.common.registries.ModItems.IModItem;

import java.util.ArrayList;

/**
 * Created by Mysteryem on 2016-08-08.
 */
public class ModItems extends ModObjectRegistry<IModItem<?>, ArrayList<IModItem<?>>> {

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
    public void preInit() {
        registerItems();
        super.preInit();
    }

    private void registerItems() {
        ArrayList<IModItem<?>> allModItems = this.getModObjects();
        allModItems.add(creativeTabIcon             = new ItemCreativeTabIcon());
        allModItems.add(personalGravityController   = new ItemPersonalGravityController());
        allModItems.add(weakGravityController       = new ItemWeakGravityController());
        allModItems.add(ultimateGravityController   = new ItemUltimateGravityController());
        allModItems.add(gravityBauble               = new ItemGravityBauble());
        allModItems.add(gravityAnchor               = new ItemGravityAnchor());
        allModItems.add(gravityIngot                = new ItemGravityIngot());
        allModItems.add(gravityPearl                = new ItemGravityPearl());
        allModItems.add(gravityDust                 = new ItemGravityDust());
        allModItems.add(spacetimeAnomaly            = new ItemSpacetimeAnomaly());
        allModItems.add(armourPaste                 = new ItemArmourPaste());
        allModItems.add(gravityBoots                = new ItemGravityBoots());
        allModItems.add(gravityChestplate           = new ItemGravityChestplate());
        allModItems.add(gravityHelmet               = new ItemGravityHelmet());
        allModItems.add(gravityLeggings             = new ItemGravityLeggings());
        REGISTRY_SETUP_ALLOWED = true;
    }

    /**
     * It may be frowned upon, but this is so I can have some item classes that extend Item and some that extend ItemArmor
     * whilst still having common default code for both of them.
     *
     * Created by Mysteryem on 2016-11-05.
     */
    public interface IModItem<T extends Item & IModItem<T>> extends IModObject<T>{
        @Override
        @SideOnly(Side.CLIENT)
        default void preInitClient(){
            T cast = this.getCast();
            ModelLoader.setCustomModelResourceLocation((Item)this, 0, new ModelResourceLocation(cast.getRegistryName(), "inventory"));
        }

        @Override
        default void preInit() {
            T cast = this.getCast();
            cast.setUnlocalizedName(GravityMod.MOD_ID + "." + this.getName());
            cast.setRegistryName(new ResourceLocation(GravityMod.MOD_ID, this.getName()));
            cast.setCreativeTab(UP_AND_DOWN_CREATIVE_TAB);
            GameRegistry.register(cast);
        }
    }
}

package uk.co.mysterymayhem.gravitymod.common.items.materials;

import baubles.api.IBauble;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import uk.co.mysterymayhem.gravitymod.GravityMod;
import uk.co.mysterymayhem.gravitymod.api.IWeakGravityEnabler;
import uk.co.mysterymayhem.gravitymod.common.registries.ModItems;
import uk.co.mysterymayhem.gravitymod.common.registries.StaticRegistry;
import uk.co.mysterymayhem.gravitymod.common.modsupport.ModSupport;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;

/**
 * Craft with armour to enable [no prefix] gravity field interaction
 * Created by Mysteryem on 2016-11-11.
 */
public class ItemArmourPaste extends Item implements ModItems.IModItem {
    private static final String NBT_KEY = "mystgravity_paste";

    private static final EnumSet<EntityEquipmentSlot> armourSlots = EnumSet.of(
            EntityEquipmentSlot.CHEST, EntityEquipmentSlot.FEET, EntityEquipmentSlot.HEAD, EntityEquipmentSlot.LEGS);

    private static boolean isItemArmour(@Nonnull ItemStack stack, @Nonnull Item item) {
        if (item instanceof ItemArmor) {
            return true;
        }

        for (EntityEquipmentSlot slot : armourSlots) {
            if (item.isValidArmor(stack, slot, null)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isItemBauble(@Nullable Item item) {
        return ModSupport.isModLoaded(ModSupport.BAUBLES_MOD_ID) && item instanceof IBauble;
    }

    public static boolean hasPasteTag(@Nonnull ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        return tagCompound != null && tagCompound.hasKey(NBT_KEY);
    }


    private class ArmourPasteRecipe implements IRecipe {

        @Override
        public boolean matches(InventoryCrafting inv, World worldIn) {
            int nonPasteItemsFound = 0;
            int pasteItemsFound = 0;
            for (int i = 0; i < inv.getSizeInventory(); i++) {
                ItemStack stack = inv.getStackInSlot(i);
                if (stack != null) {
                    Item item = stack.getItem();
                    if (item == ItemArmourPaste.this) {
                        if (++pasteItemsFound > 1) {
                            return false;
                        }
                    }
                    else if (isItemArmour(stack, item) || isItemBauble(item)) {
                        if (hasPasteTag(stack) || item instanceof IWeakGravityEnabler) {
                            return false;
                        }
                        else {
                            if (++nonPasteItemsFound > 1) {
                                return false;
                            }
                        }
                    }
                    else {
                        return false;
                    }
                }
            }
            return nonPasteItemsFound == 1 && pasteItemsFound == 1;
        }

        @Nullable
        @Override
        public ItemStack getCraftingResult(InventoryCrafting inv) {
            ItemStack armourStack = null;
            for (int i = 0; i < inv.getSizeInventory(); i++) {
                armourStack = inv.getStackInSlot(i);
                if (armourStack != null && armourStack.getItem() != ItemArmourPaste.this) {
                    break;
                }
            }

            ItemStack copy = armourStack.copy();
            NBTTagCompound tagCompound = copy.getTagCompound();
            if (tagCompound == null) {
                tagCompound = new NBTTagCompound();
                copy.setTagCompound(tagCompound);
            }
            tagCompound.setBoolean(NBT_KEY, true);
            //
            copy.stackSize = 1;
            return copy;

        }

        @Override
        public int getRecipeSize() {
            return 2;
        }

        @Nullable
        @Override
        public ItemStack getRecipeOutput() {
            return null;
        }

        @Override
        public ItemStack[] getRemainingItems(InventoryCrafting inv) {
            return ForgeHooks.defaultRecipeGetRemainingItems(inv);
        }
    }

    @Override
    public String getName() {
        return "armourpaste";
    }

    @Override
    public void postInit() {
        //TODO: Replace with ore dictionary entry for slime ball
        GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(this), StaticRegistry.gravityDust, "slimeball", "dustGlowstone"));
//        GameRegistry.addShapelessRecipe(new ItemStack(this), ModItems.gravityDust, Items.SLIME_BALL, Items.GLOWSTONE_DUST);

        GameRegistry.addRecipe(new ArmourPasteRecipe());
        RecipeSorter.register(GravityMod.MOD_ID + ":" + this.getName(), ArmourPasteRecipe.class, RecipeSorter.Category.SHAPELESS, "");
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
        if (ModSupport.isModLoaded(ModSupport.BAUBLES_MOD_ID)) {
            tooltip.add(I18n.format("mouseovertext.mysttmtgravitymod.armourpaste.infobaubles"));
        }
        else {
            tooltip.add(I18n.format("mouseovertext.mysttmtgravitymod.armourpaste.info"));
        }
    }
}

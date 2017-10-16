package uk.co.mysterymayhem.gravitymod.common.items.materials;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;
import org.lwjgl.input.Keyboard;
import uk.co.mysterymayhem.gravitymod.GravityMod;
import uk.co.mysterymayhem.gravitymod.common.registries.IGravityModItem;
import uk.co.mysterymayhem.gravitymod.common.registries.StaticItems;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by Mysteryem on 21/02/2017.
 */
public class ItemGravityDustInducer extends Item implements IGravityModItem<ItemGravityDustInducer> {
    private static final String NBT_KEY = "mystgravity_distort";

    public static boolean hasDistorterTag(@Nonnull ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        return tagCompound != null && tagCompound.hasKey(NBT_KEY);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
        KeyBinding keyBindSneak = Minecraft.getMinecraft().gameSettings.keyBindSneak;
        if (Keyboard.isKeyDown(keyBindSneak.getKeyCode())) {
            tooltip.add(I18n.format("mouseovertext.mysttmtgravitymod.gravitydustinducer.line1"));
            tooltip.add(I18n.format("mouseovertext.mysttmtgravitymod.gravitydustinducer.line2"));
        }
        else {
            tooltip.add(keyBindSneak.getDisplayName() + I18n.format("mouseovertext.mysttmtgravitymod.presskeyfordetails"));
        }
    }

    @Override
    public String getModObjectName() {
        return "gravitydustinducer";
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerClient(IForgeRegistry<Item> registry) {
        //TODO: Change to different translation strings
        String ANY_NON_STACKABLE_TEXT = I18n.format("crafting.mysttmtgravitymod.inducerinfo.noinducer");
        String ANY_NON_STACKABLE_WITH_INDUCER_TEXT = I18n.format("crafting.mysttmtgravitymod.inducerinfo.inducer");
        GravityDustInducerRemoval.DUMMY_RECIPE_INPUT.setStackDisplayName(ANY_NON_STACKABLE_WITH_INDUCER_TEXT);
        GravityDustInducerRemoval.DUMMY_RECIPE_OUTPUT.setStackDisplayName(ANY_NON_STACKABLE_TEXT);
        GravityDustInducerRecipe.DUMMY_RECIPE_INPUT.setStackDisplayName(ANY_NON_STACKABLE_TEXT);
        GravityDustInducerRecipe.DUMMY_RECIPE_OUTPUT.setStackDisplayName(ANY_NON_STACKABLE_WITH_INDUCER_TEXT);
        IGravityModItem.super.registerClient(registry);
    }

    @Override
    public void postInit() {
        ForgeRegistries.RECIPES.register(new GravityDustInducerRecipe(new ResourceLocation(GravityMod.MOD_ID, "gravity_dust_inducer")).setRegistryName(new ResourceLocation(GravityMod.MOD_ID, "gravity_dust_inducer_recipe")));
        ForgeRegistries.RECIPES.register(new GravityDustInducerRemoval(new ResourceLocation(GravityMod.MOD_ID, "gravity_dust_inducer")).setRegistryName(new ResourceLocation(GravityMod.MOD_ID, "gravity_dust_inducer_removal")));
    }

    private static class GravityDustInducerRecipe extends ShapelessRecipes {

        static final ItemStack DUMMY_RECIPE_INPUT = new ItemStack(Items.DIAMOND_PICKAXE);
        static final ItemStack DUMMY_RECIPE_OUTPUT = new ItemStack(Items.DIAMOND_PICKAXE);

        static {
            NBTTagCompound tagCompound = DUMMY_RECIPE_OUTPUT.getTagCompound();
            if (tagCompound == null) {
                tagCompound = new NBTTagCompound();
                DUMMY_RECIPE_OUTPUT.setTagCompound(tagCompound);
            }
            tagCompound.setBoolean(NBT_KEY, true);
        }

        public GravityDustInducerRecipe(ResourceLocation group) {
            super(
                    group.toString(),
                    DUMMY_RECIPE_OUTPUT,
                    NonNullList.from(Ingredient.EMPTY, Ingredient.fromStacks(DUMMY_RECIPE_INPUT), Ingredient.fromItem(StaticItems.SPACETIME_DISTORTER)));
        }

        @Override
        public boolean matches(InventoryCrafting inv, World worldIn) {
            int nonDistorterItemsFound = 0;
            int distorterItemsFound = 0;
            for (int i = 0; i < inv.getSizeInventory(); i++) {
                ItemStack stack = inv.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    Item item = stack.getItem();
                    if (item == StaticItems.SPACETIME_DISTORTER) {
                        if (++distorterItemsFound > 1) {
                            return false;
                        }
                    }
                    else if (isItemValidForInducer(stack, item)) {
                        if (stack.getCount() != 1 || hasDistorterTag(stack)) {
                            return false;
                        }
                        else if (++nonDistorterItemsFound > 1) {
                            return false;
                        }
                    }
                    else {
                        return false;
                    }
                }
            }
            return nonDistorterItemsFound == 1 && distorterItemsFound == 1;
        }

        private static boolean isItemValidForInducer(@Nonnull ItemStack stack, @Nonnull Item item) {
            return (!stack.isStackable() || stack.getMaxStackSize() == 1);
        }

        @Override
        public ItemStack getCraftingResult(InventoryCrafting inv) {
            ItemStack toolStack = ItemStack.EMPTY;
            for (int i = 0; i < inv.getSizeInventory(); i++) {
                toolStack = inv.getStackInSlot(i);
                if (!toolStack.isEmpty() && toolStack.getItem() != StaticItems.SPACETIME_DISTORTER) {
                    break;
                }
            }

            ItemStack copy = toolStack.copy();

            NBTTagCompound tagCompound = copy.getTagCompound();
            if (tagCompound == null) {
                tagCompound = new NBTTagCompound();
                copy.setTagCompound(tagCompound);
            }
            tagCompound.setBoolean(NBT_KEY, true);
            //
//            copy.stackSize = 1;
            return copy;

        }

        @Override
        public ItemStack getRecipeOutput() {
            return super.getRecipeOutput();
        }

        @Override
        public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
            return ForgeHooks.defaultRecipeGetRemainingItems(inv);
        }
    }

    private static class GravityDustInducerRemoval extends ShapelessRecipes {

        static final ItemStack DUMMY_RECIPE_INPUT = new ItemStack(Items.STONE_PICKAXE);
        static final ItemStack DUMMY_RECIPE_OUTPUT = new ItemStack(Items.STONE_PICKAXE);

        static {
            NBTTagCompound tagCompound = DUMMY_RECIPE_INPUT.getTagCompound();
            if (tagCompound == null) {
                tagCompound = new NBTTagCompound();
                DUMMY_RECIPE_INPUT.setTagCompound(tagCompound);
            }
            tagCompound.setBoolean(NBT_KEY, true);
        }

        public GravityDustInducerRemoval(ResourceLocation group) {
            super(
                    group.toString(),
                    DUMMY_RECIPE_OUTPUT,
                    NonNullList.from(Ingredient.EMPTY, Ingredient.fromStacks(DUMMY_RECIPE_INPUT), Ingredient.fromItem(Items.WATER_BUCKET)));
        }

        @Nullable
        @Override
        public ItemStack getRecipeOutput() {
            return super.getRecipeOutput();
        }

        @Override
        public boolean matches(InventoryCrafting inv, World worldIn) {
            int pasteItemsFound = 0;
            int waterBucketItemsFound = 0;
            for (int i = 0; i < inv.getSizeInventory(); i++) {
                ItemStack stack = inv.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    Item item = stack.getItem();
                    if (item == Items.WATER_BUCKET) {
                        if (++waterBucketItemsFound > 1) {
                            return false;
                        }
                    }
                    else if (stack.getCount() == 1 && hasDistorterTag(stack)) {
                        if (++pasteItemsFound > 1) {
                            // Found too many paste items
                            return false;
                        }
                    }
                    else {
                        // Found an item that isn't a water bucket or an armour piece/bauble with a paste tag
                        return false;
                    }
                }
            }
            // Necessary to check as either could be zero
            return pasteItemsFound == 1 && waterBucketItemsFound == 1;
        }

        @Override
        @MethodsReturnNonnullByDefault
        public ItemStack getCraftingResult(InventoryCrafting inv) {
            ItemStack armourStack = ItemStack.EMPTY;
            for (int i = 0; i < inv.getSizeInventory(); i++) {
                armourStack = inv.getStackInSlot(i);
                if (!armourStack.isEmpty() && armourStack.getItem() != Items.WATER_BUCKET) {
                    break;
                }
            }

            ItemStack copy = armourStack.copy();
            NBTTagCompound tagCompound = copy.getTagCompound();
            if (tagCompound != null) {
                tagCompound.removeTag(NBT_KEY);
                if (tagCompound.hasNoTags()) {
                    // Prevent leaving behind empty tags
                    copy.setTagCompound(null);
                }
            }
//            copy.stackSize = 1;
            return copy;
        }

        @Override
        public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
            return ForgeHooks.defaultRecipeGetRemainingItems(inv);
        }
    }
}

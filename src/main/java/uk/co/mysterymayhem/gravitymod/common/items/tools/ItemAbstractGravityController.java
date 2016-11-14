package uk.co.mysterymayhem.gravitymod.common.items.tools;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import uk.co.mysterymayhem.gravitymod.api.API;
import uk.co.mysterymayhem.gravitymod.api.EnumGravityDirection;
import uk.co.mysterymayhem.gravitymod.common.ModItems;
import uk.co.mysterymayhem.gravitymod.common.items.shared.IModItem;
import uk.co.mysterymayhem.gravitymod.common.util.IConditionallyAffectsGravity;
import uk.co.mysterymayhem.gravitymod.common.util.item.ITickOnMouseCursor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Mysteryem on 2016-10-11.
 */
public abstract class ItemAbstractGravityController extends Item implements ITickOnMouseCursor, IModItem, IConditionallyAffectsGravity {

    //7 values (0-6) -> first 3 bits
    private enum EnumControllerActiveDirection {
        DOWN(EnumGravityDirection.DOWN),
        UP(EnumGravityDirection.UP),
        NORTH(EnumGravityDirection.NORTH),
        EAST(EnumGravityDirection.EAST),
        SOUTH(EnumGravityDirection.SOUTH),
        WEST(EnumGravityDirection.WEST),
        NONE(null);

        public final int mask;
        public final EnumGravityDirection gravityDirection;
        public final String extraText;

        EnumControllerActiveDirection(EnumGravityDirection direction) {
            this.extraText = "." + this.name().toLowerCase(Locale.ENGLISH);
            this.gravityDirection = direction;
            this.mask = this.ordinal();
        }

        public static EnumControllerActiveDirection getFromMeta(int meta) {
            EnumControllerActiveDirection[] values = EnumControllerActiveDirection.values();
            if (meta >= 0 && meta < values.length) {
                return values[meta];
            }
            else {
                return NONE;
            }
        }

        public static EnumControllerActiveDirection getFromCombinedMeta(int combinedMeta) {
            return getFromMeta(ItemAbstractGravityController.getActiveDirectionMetaFromCombinedMeta(combinedMeta));
        }

        public int addToCombinedMeta(int combinedMeta) {
            //clear the lowest 3 bits, then add the mask
            return (0b1111000 & combinedMeta) | this.mask;
        }

    }

    //12 values (0-11) -> next 4 bits
    private enum EnumControllerVisibleState {
        DOWN_OFF("down", EnumControllerActiveDirection.DOWN, null),
        DOWN_ON("down_on", null, EnumControllerActiveDirection.DOWN),
        UP_OFF("up", EnumControllerActiveDirection.UP, null),
        UP_ON("up_on", null, EnumControllerActiveDirection.UP),
        NORTH_OFF("north", EnumControllerActiveDirection.NORTH, null),
        NORTH_ON("north_on", null, EnumControllerActiveDirection.NORTH),
        EAST_OFF("east", EnumControllerActiveDirection.EAST, null),
        EAST_ON("east_on", null, EnumControllerActiveDirection.EAST),
        SOUTH_OFF("south", EnumControllerActiveDirection.SOUTH, null),
        SOUTH_ON("south_on", null, EnumControllerActiveDirection.SOUTH),
        WEST_OFF("west", EnumControllerActiveDirection.WEST, null),
        WEST_ON("west_on", null, EnumControllerActiveDirection.WEST);

        public final int mask;
        public final String unlocalizedName;
        public final EnumControllerActiveDirection illegalDirection;
        public final EnumControllerActiveDirection onlyLegalDirection;

        EnumControllerVisibleState(String unlocalizedName, EnumControllerActiveDirection illegalCombination, EnumControllerActiveDirection onlyLegalCombination) {
            this.illegalDirection = illegalCombination;
            this.onlyLegalDirection = onlyLegalCombination;
            this.unlocalizedName = unlocalizedName;
            this.mask = this.ordinal() << 3;
        }

        public static EnumControllerVisibleState getFromMeta(int meta) {
            EnumControllerVisibleState[] values = EnumControllerVisibleState.values();
            if (meta >= 0 && meta < values.length) {
                return values[meta];
            }
            else {
                return DOWN_OFF;
            }
        }

        public static EnumControllerVisibleState getFromCombinedMeta(int combinedMeta) {
            return getFromMeta(ItemAbstractGravityController.getVisibleStateMetaFromCombinedMeta(combinedMeta));
        }

        public int addToCombinedMeta(int combinedMeta) {
            //clear bits 4-7, then add the mask
            return (0b0000111 & combinedMeta) | this.mask;
        }

        public static EnumControllerVisibleState getOffState(EnumControllerVisibleState other) {
            int ordinal = other.ordinal();
            if (ordinal % 2 == 1) {
                return EnumControllerVisibleState.values()[ordinal - 1];
            }
            return other;
        }

        public String getUnlocalizedName() {
            return this.unlocalizedName;
        }

//        public EnumControllerVisibleState next() {
//            // 0/2 = 0; 0*2 = 0; 0+2 = 2
//            // 1/2 = 0; 0*2 = 0; 0+2 = 2
//            // 2/2 = 1; 1*2 = 2; 2+2 = 4
//            int next = ((this.ordinal()/2)*2+2);
//            EnumControllerVisibleState[] values = EnumControllerVisibleState.values();
//            if (next < values.length) {
//                return values[next];
//            }
//            else {
//                return DOWN_OFF;
//            }
//        }
    }

    static final int DEFAULT_META = getCombinedMetaFor(EnumControllerActiveDirection.NONE, EnumControllerVisibleState.DOWN_OFF);

    private static final int[] LEGAL_METADATA;
    static {
        ArrayList<Integer> legalMetaList = new ArrayList<>();
        for (EnumControllerVisibleState visibleState : EnumControllerVisibleState.values()) {
            if (visibleState.onlyLegalDirection != null) {
                legalMetaList.add(getCombinedMetaFor(visibleState.onlyLegalDirection, visibleState));
            }
            else {
                for (EnumControllerActiveDirection activeDirection : EnumControllerActiveDirection.values()) {
                    if (visibleState.illegalDirection == activeDirection) {
                        continue;
                    }
                    legalMetaList.add(getCombinedMetaFor(activeDirection, visibleState));
                }
            }
        }

        int listSize = legalMetaList.size();
        LEGAL_METADATA = new int[listSize];
        for (int i = 0; i < listSize; i++) {
            LEGAL_METADATA[i] = legalMetaList.get(i);
        }
    }

    private static int getCombinedMetaFor(EnumControllerActiveDirection active, EnumControllerVisibleState visible) {
        return active.mask | visible.mask;
    }

    private static int getActiveDirectionMetaFromCombinedMeta(int combinedMeta) {
        return combinedMeta & 0b111;
    }

    private static int getVisibleStateMetaFromCombinedMeta(int combinedMeta) {
        return combinedMeta >>> 3;
    }

    @Override
    public void preInit() {
        this.setMaxStackSize(1);
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
        IModItem.super.preInit();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
        KeyBinding keyBindSneak = Minecraft.getMinecraft().gameSettings.keyBindSneak;
        if (Keyboard.isKeyDown(keyBindSneak.getKeyCode())) {
            tooltip.add(I18n.format("mouseovertext.mysttmtgravitymod.gravitycontroller.sneak.line1"));
            tooltip.add(I18n.format("mouseovertext.mysttmtgravitymod.gravitycontroller.sneak.line2"));
        }
        else {
            tooltip.add(keyBindSneak.getDisplayName() + I18n.format("mouseovertext.mysttmtgravitymod.presskeyfordetails"));
        }
        if (advanced) {
            int meta = stack.getItemDamage();
            EnumControllerActiveDirection activeDirection = EnumControllerActiveDirection.getFromCombinedMeta(meta);
            EnumControllerVisibleState visibleState = EnumControllerVisibleState.getFromCombinedMeta(meta);
            tooltip.add("Visible direction: " + visibleState.name().toLowerCase(Locale.ENGLISH));
            tooltip.add("Active direction: " + activeDirection.name().toLowerCase(Locale.ENGLISH));
        }
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        int meta = stack.getItemDamage();
        EnumControllerActiveDirection fromCombinedMeta = EnumControllerActiveDirection.getFromCombinedMeta(meta);

        return super.getUnlocalizedName() + fromCombinedMeta.extraText;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        if (oldStack.getItemDamage() != newStack.getItemDamage()) {
            return true;
        }
        return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
        if (tab == ModItems.UP_AND_DOWN_CREATIVE_TAB) {
            subItems.add(new ItemStack(this, 1, DEFAULT_META));
        }
        else if (tab == ModItems.FAKE_TAB_FOR_CONTROLLERS) {
            for (int legalMeta : LEGAL_METADATA) {
                subItems.add(new ItemStack(this, 1, legalMeta));
            }
        }
    }

    // Fake tab for JEI compat
    @Override
    public CreativeTabs[] getCreativeTabs() {
        return new CreativeTabs[]{ModItems.FAKE_TAB_FOR_CONTROLLERS, ModItems.UP_AND_DOWN_CREATIVE_TAB};
    }

    @Override
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (entityIn instanceof EntityPlayerMP) {
            if (this.affectsPlayer((EntityPlayerMP)entityIn)) {
                int meta = stack.getItemDamage();
                EnumControllerActiveDirection activeDirection = EnumControllerActiveDirection.getFromCombinedMeta(meta);
                if (activeDirection != EnumControllerActiveDirection.NONE) {
                    API.setPlayerGravity(activeDirection.gravityDirection, (EntityPlayerMP) entityIn, this.getPriority(entityIn));
                }
            }
        }
    }

    // Returns true, even though, we return null in the createEntity method, so that we can modify the item meta/damage when an item entity gets spawned
    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return true;
    }

    // We could alternatively return a new EntityItem instance
    @Override
    public Entity createEntity(World world, Entity location, ItemStack itemstack) {
        int meta = itemstack.getItemDamage();
        EnumControllerActiveDirection activeDirection = EnumControllerActiveDirection.getFromCombinedMeta(meta);
        if (activeDirection != EnumControllerActiveDirection.NONE) {
            activeDirection = EnumControllerActiveDirection.NONE;
            EnumControllerVisibleState offStateFromMeta = EnumControllerVisibleState.getOffState(EnumControllerVisibleState.getFromCombinedMeta(meta));
            meta = getCombinedMetaFor(activeDirection, offStateFromMeta);
            itemstack.setItemDamage(meta);
        }
        return null;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World worldIn, EntityPlayer playerIn, EnumHand hand) {
        if (playerIn != null) {
            int i = stack.getItemDamage();

            if (playerIn.isSneaking()) {
                //Change displayed direction
                EnumControllerVisibleState currentVisible = EnumControllerVisibleState.getFromCombinedMeta(i);
                EnumControllerActiveDirection currentActive = EnumControllerActiveDirection.getFromCombinedMeta(i);
                switch (currentVisible) {
                    case DOWN_OFF:
                        if (currentActive == EnumControllerActiveDirection.UP) {
                            currentVisible = EnumControllerVisibleState.UP_ON;
                            break;
                        }
                    case DOWN_ON:
                        currentVisible = EnumControllerVisibleState.UP_OFF;
                        break;
                    case UP_OFF:
                        if (currentActive == EnumControllerActiveDirection.NORTH) {
                            currentVisible = EnumControllerVisibleState.NORTH_ON;
                            break;
                        }
                    case UP_ON:
                        currentVisible = EnumControllerVisibleState.NORTH_OFF;
                        break;
                    case NORTH_OFF:
                        if (currentActive == EnumControllerActiveDirection.EAST) {
                            currentVisible = EnumControllerVisibleState.EAST_ON;
                            break;
                        }
                    case NORTH_ON:
                        currentVisible = EnumControllerVisibleState.EAST_OFF;
                        break;
                    case EAST_OFF:
                        if (currentActive == EnumControllerActiveDirection.SOUTH) {
                            currentVisible = EnumControllerVisibleState.SOUTH_ON;
                            break;
                        }
                    case EAST_ON:
                        currentVisible = EnumControllerVisibleState.SOUTH_OFF;
                        break;
                    case SOUTH_OFF:
                        if (currentActive == EnumControllerActiveDirection.WEST) {
                            currentVisible = EnumControllerVisibleState.WEST_ON;
                            break;
                        }
                    case SOUTH_ON:
                        currentVisible = EnumControllerVisibleState.WEST_OFF;
                        break;
                    case WEST_OFF:
                        if (currentActive == EnumControllerActiveDirection.DOWN) {
                            currentVisible = EnumControllerVisibleState.DOWN_ON;
                            break;
                        }
                    case WEST_ON:
                        currentVisible = EnumControllerVisibleState.DOWN_OFF;
                        break;
                }
                stack.setItemDamage(getCombinedMetaFor(currentActive, currentVisible));
            }
            else {
                //Change active direction
                EnumControllerVisibleState currentVisible = EnumControllerVisibleState.getFromCombinedMeta(i);
                EnumControllerActiveDirection currentActive = EnumControllerActiveDirection.getFromCombinedMeta(i);
                switch (currentVisible) {
                    case DOWN_OFF:
                        currentActive = EnumControllerActiveDirection.DOWN;
                        currentVisible = EnumControllerVisibleState.DOWN_ON;
                        break;
                    case DOWN_ON:
                        currentActive = EnumControllerActiveDirection.NONE;
                        currentVisible = EnumControllerVisibleState.DOWN_OFF;
                        break;
                    case UP_OFF:
                        currentActive = EnumControllerActiveDirection.UP;
                        currentVisible = EnumControllerVisibleState.UP_ON;
                        break;
                    case UP_ON:
                        currentActive = EnumControllerActiveDirection.NONE;
                        currentVisible = EnumControllerVisibleState.UP_OFF;
                        break;
                    case NORTH_OFF:
                        currentActive = EnumControllerActiveDirection.NORTH;
                        currentVisible = EnumControllerVisibleState.NORTH_ON;
                        break;
                    case NORTH_ON:
                        currentActive = EnumControllerActiveDirection.NONE;
                        currentVisible = EnumControllerVisibleState.NORTH_OFF;
                        break;
                    case EAST_OFF:
                        currentActive = EnumControllerActiveDirection.EAST;
                        currentVisible = EnumControllerVisibleState.EAST_ON;
                        break;
                    case EAST_ON:
                        currentActive = EnumControllerActiveDirection.NONE;
                        currentVisible = EnumControllerVisibleState.EAST_OFF;
                        break;
                    case SOUTH_OFF:
                        currentActive = EnumControllerActiveDirection.SOUTH;
                        currentVisible = EnumControllerVisibleState.SOUTH_ON;
                        break;
                    case SOUTH_ON:
                        currentActive = EnumControllerActiveDirection.NONE;
                        currentVisible = EnumControllerVisibleState.SOUTH_OFF;
                        break;
                    case WEST_OFF:
                        currentActive = EnumControllerActiveDirection.WEST;
                        currentVisible = EnumControllerVisibleState.WEST_ON;
                        break;
                    case WEST_ON:
                        currentActive = EnumControllerActiveDirection.NONE;
                        currentVisible = EnumControllerVisibleState.WEST_OFF;
                        break;
                }
                stack.setItemDamage(getCombinedMetaFor(currentActive, currentVisible));
            }
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
//        return super.onItemRightClick(stack, worldIn, playerIn, hand);
    }

    @SideOnly(Side.CLIENT)
    private class MeshDefinitions implements ItemMeshDefinition {

        final ArrayList<ModelResourceLocation> list;

        MeshDefinitions(ItemAbstractGravityController item) {
            list = new ArrayList<>();
            for (EnumControllerVisibleState visibleState : EnumControllerVisibleState.values()) {
                list.add(new ModelResourceLocation(item.getRegistryName() + "_" + visibleState.getUnlocalizedName(), "inventory"));
            }
        }

        @Override
        public ModelResourceLocation getModelLocation(ItemStack stack) {
            int metadata = stack.getMetadata();
            int ordinal = EnumControllerVisibleState.getFromCombinedMeta(metadata).ordinal();
            return list.get(ordinal);
        }
    }

    @SideOnly(Side.CLIENT)
    public void preInitModel() {
        MeshDefinitions meshDefinitions = new MeshDefinitions(this);
        ModelBakery.registerItemVariants(this, meshDefinitions.list.toArray(new ModelResourceLocation[meshDefinitions.list.size()]));
        ModelLoader.setCustomMeshDefinition(this, meshDefinitions);
    }

}

package uk.co.mysterymayhem.gravitymod.common.blocks.gravitygenerator;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;

/**
 * Created by Mysteryem on 2017-01-03.
 */
public class ContainerGravityGenerator extends Container {

    private static final int HOTBAR_ITEM_COUNT = 9;
    private static final int MAIN_INV_ITEMS_PER_ROW = HOTBAR_ITEM_COUNT;
    private static final int HOTBAR_X_START = 8;
    private static final int HOTBAR_Y_START = 119;
    private static final int MAININV_X_START = HOTBAR_X_START;
    private static final int MAININV_Y_START = 61;
    private static final int SLOT_SPACING = 18;

    private final TileGravityGenerator tileGravityGenerator;

    public ContainerGravityGenerator(EntityPlayer player, TileGravityGenerator tileGravityGenerator) {
        this.tileGravityGenerator = tileGravityGenerator;

        IItemHandler playerInv;
        if (player.hasCapability(ITEM_HANDLER_CAPABILITY, EnumFacing.UP)) {
            playerInv = player.getCapability(ITEM_HANDLER_CAPABILITY, EnumFacing.UP);
        }
        else if (player.hasCapability(ITEM_HANDLER_CAPABILITY, EnumFacing.DOWN)) {
            playerInv = player.getCapability(ITEM_HANDLER_CAPABILITY, EnumFacing.DOWN);
        }
        else if (player.hasCapability(ITEM_HANDLER_CAPABILITY, null)) {
            playerInv = player.getCapability(ITEM_HANDLER_CAPABILITY, null);
        }
        else {
            playerInv = EmptyHandler.INSTANCE;
        }

        final int invSize = playerInv.getSlots();
//        int rows = invSize / 9;

        for (int i = 0; i < HOTBAR_ITEM_COUNT; i++) {
            int xExtra = (i % MAIN_INV_ITEMS_PER_ROW) * SLOT_SPACING;
            int yExtra = (i / MAIN_INV_ITEMS_PER_ROW) * SLOT_SPACING;
            this.addSlotToContainer(new SlotItemHandler(playerInv, i, HOTBAR_X_START + xExtra, HOTBAR_Y_START + yExtra));
        }

        for (int i = HOTBAR_ITEM_COUNT; i < invSize; i++) {
            int j = i - HOTBAR_ITEM_COUNT;
            int xExtra = (j % MAIN_INV_ITEMS_PER_ROW) * SLOT_SPACING;
            int yExtra = (j / MAIN_INV_ITEMS_PER_ROW) * SLOT_SPACING;
            this.addSlotToContainer(new SlotItemHandler(playerInv, i, MAININV_X_START + xExtra, MAININV_Y_START + yExtra));
        }

        // Possible future feature whereby items must be inserted in order to unlock additional features of the gravity
        // generator
//        IItemHandler tileInv = this.tileGravityGenerator.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
//
//        yPos = 36;
//
//        int tileSlots = tileInv.getSlots();
//        if (tileSlots == 1) {
//            this.addSlotToContainer(new SlotItemHandler(tileInv, 0, xPos + (MAIN_INV_ITEMS_PER_ROW / 2) * SLOT_SPACING, yPos));
//        }
    }

    private static final int MAX_DISTANCE = 8;
    private static final int MAX_DISTANCE_SQUARED = MAX_DISTANCE * MAX_DISTANCE;

    @Override
    public boolean canInteractWith(@Nonnull EntityPlayer playerIn) {
        BlockPos pos = this.tileGravityGenerator.getPos();
        return playerIn.worldObj.getTileEntity(pos) == this.tileGravityGenerator
                && playerIn.getDistanceSqToCenter(pos) < MAX_DISTANCE_SQUARED;
    }

    @Nullable
    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        return null;
    }

    public TileGravityGenerator getTileGravityGenerator() {
        return tileGravityGenerator;
    }
}

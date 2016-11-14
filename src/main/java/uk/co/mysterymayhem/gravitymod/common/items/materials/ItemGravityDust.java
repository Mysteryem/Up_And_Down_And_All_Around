package uk.co.mysterymayhem.gravitymod.common.items.materials;

import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.hash.TIntHashSet;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import uk.co.mysterymayhem.gravitymod.GravityMod;
import uk.co.mysterymayhem.gravitymod.asm.Hooks;
import uk.co.mysterymayhem.gravitymod.common.ModItems;
import uk.co.mysterymayhem.gravitymod.common.config.ConfigHandler;
import uk.co.mysterymayhem.gravitymod.common.entities.EntityFloatingItem;
import uk.co.mysterymayhem.gravitymod.common.items.shared.IModItem;

import java.util.*;

/**
 * Created by Mysteryem on 2016-11-10.
 */
public class ItemGravityDust extends Item implements IModItem {

    public static class FallDamageListener {

        @SubscribeEvent(priority = EventPriority.LOWEST)
        public void onLivingFall(LivingFallEvent event) {
            if (event.getDistance() <= 3) {
                return;
            }
            EntityLivingBase entityLiving = event.getEntityLiving();
            if (entityLiving instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) entityLiving;
//                player.inventory
                List<ItemStack> stacks = player.inventoryContainer.inventoryItemStacks;
                int amountOfDust = 0;
                for (ItemStack stack : stacks) {
                    if (stack == null) {
                        continue;
                    }
                    Item item = stack.getItem();
                    if (item == ModItems.gravityDust) {
                        amountOfDust += stack.stackSize;
                    }
                }
                double upwardsMotionFromStacks = UPWARDS_MOTION_CHANGE_PER_ITEM * amountOfDust;
                double multiplier = 1 - (upwardsMotionFromStacks / VANILLA_DOWNWARDS_MOTION);
                if (multiplier < 0) {
                    multiplier = 0;
                }
                event.setDistance((float) (event.getDistance() * multiplier));
            }
        }
    }

    public static class BlockBreakListener {

        private static final HashSet<String> ACCEPTABLE_DROPS_ORE_NAMES = new HashSet<>();
        private static final HashSet<String> ACCEPTABLE_BLOCK_ORE_NAMES = new HashSet<>();
        private static final HashSet<Block> ACCEPTABLE_BLOCKS = new HashSet<>();
        private static final HashMap<Block, TIntHashSet> ACCEPTABLE_BLOCKS_WITH_META = new HashMap<>();
        private static final HashSet<Item> ACCEPTABLE_DROPS = new HashSet<>();
        private static final HashMap<Item, TIntHashSet> ACCEPTABLE_DROPS_WITH_META = new HashMap<>();

        @SubscribeEvent(priority = EventPriority.LOWEST)
        public void onBlockDropItems(BlockEvent.HarvestDropsEvent event) {
            EntityPlayer harvester = event.getHarvester();
            if (harvester != null/* && !(harvester instanceof FakePlayer)*/) {

                World world = event.getWorld();
                IBlockState blockState = event.getState();
                Block block = blockState.getBlock();
                if (block == Blocks.LIT_REDSTONE_ORE) {
                    block = Blocks.REDSTONE_ORE;
                }

                boolean blockIsAcceptable = false;

                if (!ACCEPTABLE_BLOCKS.contains(block)) {
                    TIntHashSet metadataSet = ACCEPTABLE_BLOCKS_WITH_META.get(block);
                    if (metadataSet != null) {
                        if (metadataSet.contains(block.getMetaFromState(blockState))) {
                            blockIsAcceptable = true;
                        }
                    }

                    if (!blockIsAcceptable) {

                        Item itemFromBlock = Item.getItemFromBlock(block);

                        // OreDictionary will throw an exception if the Item from a Block is null
                        // This is the case with lit redstone ore
                        if (itemFromBlock != null) {
                            int[] oreIDs = OreDictionary.getOreIDs(new ItemStack(itemFromBlock));

                            for (int oreID : oreIDs) {
                                String oreName = OreDictionary.getOreName(oreID);
                                if (ACCEPTABLE_BLOCK_ORE_NAMES.contains(oreName)) {
                                    blockIsAcceptable = true;
                                    break;
                                }
                            }
                            if (!blockIsAcceptable) {
                                return;
                            }
                        }
                    }
                }
                // Block broken is ok


                List<ItemStack> drops = event.getDrops();
                for (ListIterator<ItemStack> it = drops.listIterator(); it.hasNext(); ) {
                    ItemStack stack = it.next();
                    boolean itemIsOk = false;
                    Item item = stack.getItem();

                    if (ACCEPTABLE_DROPS.contains(item)) {
                        itemIsOk = true;
                    }
                    else {
                        TIntHashSet metadataSet = ACCEPTABLE_DROPS_WITH_META.get(item);
                        if (metadataSet != null) {
                            if (metadataSet.contains(stack.getItemDamage())) {
                                itemIsOk = true;
                            }
                        }

                        if (!itemIsOk) {
                            int[] oreIDs = OreDictionary.getOreIDs(stack);
                            for (int oreID : oreIDs) {
                                String oreName = OreDictionary.getOreName(oreID);
                                if (ACCEPTABLE_DROPS_ORE_NAMES.contains(oreName)) {
                                    itemIsOk = true;
                                    break;
                                }
                            }
                        }
                    }
                    // If item is OK
                    if (itemIsOk) {
                        // The order of checks and results of each check have been carefully organised such that
                        // the chance of the item dropping from the block remains the same as normal
                        if (world.rand.nextFloat() < ConfigHandler.gravityDustDropChance) {
                            // Vanilla uses <=, which technically means there is a 1/(pretty big) chance for an item to still drop
                            // even with a drop chance of 0, in the case that the pseudo-random number generator generates exactly zero
                            if (world.rand.nextFloat() < event.getDropChance()) {
                                //drop special item
                                BlockBreakListener.spawnAsSpecialEntity(world, event.getPos(), stack);
                            }
                            else {
                                //no drop at all
                            }
                            it.remove();
                        }
                        else {
                            // Item should drop as per normal (we'll let vanilla handle if the item drops or not)
                        }
                        if (ConfigHandler.gravityDustChanceOncePerBrokenBlock){
                            // Only one attempt allowed per block broken
                            return;
                        }
                    }

                }
            }
        }

        public static void addDropsFromConfig(String... configStrings) {
            for (String configString : configStrings) {
                String[] split = configString.split(":");
                if (split.length != 2) {
                    if (split.length == 3) {
                        int meta;
                        try {
                            meta = Integer.parseInt(split[2]);
                            if (!addItemDropWithMeta(split[0], split[1], meta)) {
                                GravityMod.logWarning("Could not find item/block for \"anti-mass.drops\" entry: %s", configString);
                            }
                        } catch (NumberFormatException ex) {
                            GravityMod.logWarning("Invalid \"anti-mass.drops\" entry: %s, could not parse \"%s\" as an integer", configString, split[2]);
                        }
                    }
                    else {
                        GravityMod.logWarning("Invalid \"anti-mass.drops\" entry: %s", configString);
                    }
                }
                else if (split[0].equals("ore")) {
                    addDropsOreName(split[1]);
                }
                else {
                    if(!addItemDrop(split[0], split[1])) {
                        GravityMod.logWarning("Could not find item/block for \"anti-mass.drops\" entry: %s", configString);
                    }
                }
            }
        }

        public static void addBlocksFromConfig(String... configStrings) {
            for (String configString : configStrings) {
                String[] split = configString.split(":");
                if (split.length != 2) {
                    if (split.length == 3) {
                        int meta;
                        try {
                            meta = Integer.parseInt(split[2]);
                            if (!addBlockWithMeta(split[0], split[1], meta)) {
                                GravityMod.logWarning("Could not find block for \"anti-mass.drops\" entry: %s", configString);
                            }
                        } catch (NumberFormatException ex) {
                            GravityMod.logWarning("Invalid \"anti-mass.blocks\" entry: %s, could not parse \"%s\" as an integer", configString, split[2]);
                        }
                    }
                    else {
                        GravityMod.logWarning("Invalid \"anti-mass.blocks\" entry: %s", configString);
                    }
                }
                else if (split[0].equals("ore")) {
                    addBlockOreName(split[1]);
                }
                else {
                    if(!addBlock(split[0], split[1])) {
                        GravityMod.logWarning("Could not find block for \"anti-mass.drops\" entry: %s", configString);
                    }
                }
            }
        }

        static void addDropsOreName(String name) {
            ACCEPTABLE_DROPS_ORE_NAMES.add(name);
        }

        static void addBlockOreName(String name) {
            ACCEPTABLE_BLOCK_ORE_NAMES.add(name);
        }

        static boolean addBlock(Block block) {
            return ACCEPTABLE_BLOCKS.add(block);
        }

        static void addBlockWithMeta(Block block, int meta) {
            TIntHashSet tIntHashSet = ACCEPTABLE_BLOCKS_WITH_META.get(block);
            if (tIntHashSet == null) {
                tIntHashSet = new TIntHashSet();
                ACCEPTABLE_BLOCKS_WITH_META.put(block, tIntHashSet);
            }
            tIntHashSet.add(meta);
        }

        static boolean addBlock(String modID, String blockName) {
            Block block = Block.REGISTRY.getObject(new ResourceLocation(modID, blockName));
            // Default value is air, as opposed to null
            if (block == Blocks.AIR) {
                return false;
            }
            addBlock(block);
            return true;
        }

        static boolean addBlockWithMeta(String modID, String blockName, int meta) {
            Block block = Block.REGISTRY.getObject(new ResourceLocation(modID, blockName));
            if (block == Blocks.AIR) {
                return false;
            }
            addBlockWithMeta(block, meta);
            return true;
        }

        static boolean addItemDrop(Item item) {
            return ACCEPTABLE_DROPS.add(item);
        }

        static boolean addItemDrop(String modID, String itemName) {
            Item item = Item.REGISTRY.getObject(new ResourceLocation(modID, itemName));
            // No default value/default value is null
            return item != null && addItemDrop(item);
        }

        static boolean addItemDropWithMeta(String modID, String itemName, int meta) {
            Item item = Item.REGISTRY.getObject(new ResourceLocation(modID, itemName));
            // No default value/default value is null
            if (item == null) {
                return false;
            }
            addItemDropWithMeta(item, meta);
            return true;
        }

        static void addItemDropWithMeta(Item item, int meta) {
            TIntHashSet tIntHashSet = ACCEPTABLE_DROPS_WITH_META.get(item);
            if (tIntHashSet == null) {
                tIntHashSet = new TIntHashSet();
                ACCEPTABLE_DROPS_WITH_META.put(item, tIntHashSet);
            }
            tIntHashSet.add(meta);
        }

        /**
         * Spawns the given ItemStack as an EntityItem into the World at the given position
         */
        static void spawnAsSpecialEntity(World worldIn, BlockPos pos, ItemStack stack) {
            if (!worldIn.isRemote && worldIn.getGameRules().getBoolean("doTileDrops") && !worldIn.restoringBlockSnapshots) // do not drop items while restoring blockstates, prevents item dupe
            {
                //            if (captureDrops.get())
                //            {
                //                capturedDrops.get().add(stack);
                //                return;
                //            }
                float f = 0.5F;
                double d0 = (double) (worldIn.rand.nextFloat() * f) + 0.25D;
                double d1 = (double) (worldIn.rand.nextFloat() * f) + 0.25D;
                double d2 = (double) (worldIn.rand.nextFloat() * f) + 0.25D;
                EntityItem entityitem = new EntityFloatingItem(worldIn, (double) pos.getX() + d0, (double) pos.getY() + d1, (double) pos.getZ() + d2, stack);
                entityitem.setDefaultPickupDelay();
                worldIn.spawnEntityInWorld(entityitem);
            }
        }
    }

    private static final double VANILLA_DOWNWARDS_MOTION = 0.03999999910593033D;
    private static final int VANILLA_INVENTORY_SLOTS = 36;
    private static final int STACK_SIZE = 64;
    private static final int MAX_ITEMS_IN_INVENTORY = VANILLA_INVENTORY_SLOTS * STACK_SIZE;
    private static final double MAXUPWARDS_MOTION = VANILLA_DOWNWARDS_MOTION - 0.002;
//    private static final double MAXUPWARDS_MOTION_PER_ITEM = MAXUPWARDS_MOTION / (double)(VANILLA_INVENTORY_SLOTS);
    private static final double UPWARDS_MOTION_CHANGE_PER_ITEM = MAXUPWARDS_MOTION / (double)(MAX_ITEMS_IN_INVENTORY);

    @Override
    public void preInit() {
        MinecraftForge.EVENT_BUS.register(new FallDamageListener());
        IModItem.super.preInit();
    }

    @Override
    public String getName() {
        return "gravitydust";
    }

    @Override
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        Hooks.makeMotionRelative(entityIn);
        double upwardsMotion = UPWARDS_MOTION_CHANGE_PER_ITEM * stack.stackSize;
        if (entityIn.motionY + upwardsMotion < 0) {
            entityIn.motionY += upwardsMotion;
        }
        Hooks.popMotionStack(entityIn);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("mouseovertext.mysttmtgravitymod.gravitydust.line1"));
    }

    @Override
    public boolean onEntityItemUpdate(EntityItem entityItem) {
//        entityItem.motionX *= 0.95;
        entityItem.motionY += 0.000005 * entityItem.getEntityItem().stackSize;
//        entityItem.motionZ *= 0.95;
        boolean toReturn = super.onEntityItemUpdate(entityItem);
//        if (entityItem.getAge() >= entityItem.lifespan - 1) {
//            entityItem.setDead();
//            if (entityItem.worldObj.isRemote) {
//                BlockPos blockPosIn = new BlockPos(entityItem);
//                World world = entityItem.worldObj;
////                entityItem.worldObj.playEvent(2003, new BlockPos(entityItem), 0);
//                double d0 = (double)blockPosIn.getX() + 0.5D;
//                double d1 = (double)blockPosIn.getY();
//                double d2 = (double)blockPosIn.getZ() + 0.5D;
//
//                for (int j = 0; j < 8; ++j)
//                {
//                    world.spawnParticle(EnumParticleTypes.ITEM_CRACK, d0, d1, d2, world.rand.nextGaussian() * 0.15D, world.rand.nextDouble() * 0.2D, world.rand.nextGaussian() * 0.15D, new int[] {Item.getIdFromItem(ModItems.gravityDust)});
//                }
//
//                for (double d11 = 0.0D; d11 < (Math.PI * 2D); d11 += 0.15707963267948966D)
//                {
//                    world.spawnParticle(EnumParticleTypes.PORTAL, d0 + Math.cos(d11) * 5.0D, d1 - 0.4D, d2 + Math.sin(d11) * 5.0D, Math.cos(d11) * -5.0D, 0.0D, Math.sin(d11) * -5.0D, new int[0]);
//                    world.spawnParticle(EnumParticleTypes.PORTAL, d0 + Math.cos(d11) * 5.0D, d1 - 0.4D, d2 + Math.sin(d11) * 5.0D, Math.cos(d11) * -7.0D, 0.0D, Math.sin(d11) * -7.0D, new int[0]);
//                }
//            }
//        }
        return toReturn;
    }

    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return true;
    }

    @Override
    public Entity createEntity(World world, Entity location, ItemStack itemstack) {
        if (location instanceof EntityItem) {
            location.setNoGravity(true);
            location.motionY -= 0.1;
        }
        return null;
    }
}

package uk.co.mysterymayhem.gravitymod.common.items.materials;

import gnu.trove.set.hash.TIntHashSet;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import uk.co.mysterymayhem.gravitymod.GravityMod;
import uk.co.mysterymayhem.gravitymod.common.config.ConfigHandler;
import uk.co.mysterymayhem.gravitymod.common.entities.EntityFloatingItem;
import uk.co.mysterymayhem.gravitymod.common.registries.IGravityModItem;
import uk.co.mysterymayhem.gravitymod.common.registries.StaticItems;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by Mysteryem on 2016-11-10.
 */
public class ItemGravityDust extends Item implements IGravityModItem<ItemGravityDust> {

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("mouseovertext.mysttmtgravitymod.gravitydust.line1"));
    }

    // True so we can access the normally created EntityItem and disable its gravity
    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return true;
    }

    // Returning null uses 'location', returning 'location' would cause the game to kill 'location' and add it to the
    // world a second time, which would probably cause some issues
    @Override
    public Entity createEntity(World world, Entity location, ItemStack itemstack) {
        if (location instanceof EntityItem) {
            location.setNoGravity(true);
            location.motionY -= 0.1;
        }
        return null;
    }

    @Override
    public String getName() {
        return "gravitydust";
    }

    public static class BlockBreakListener {

        private static final HashSet<Block> ACCEPTABLE_BLOCKS = new HashSet<>();
        private static final HashMap<Block, TIntHashSet> ACCEPTABLE_BLOCKS_WITH_META = new HashMap<>();
        private static final HashSet<String> ACCEPTABLE_BLOCK_ORE_NAMES = new HashSet<>();
        private static final HashSet<Item> ACCEPTABLE_DROPS = new HashSet<>();
        private static final HashSet<String> ACCEPTABLE_DROPS_ORE_NAMES = new HashSet<>();
        private static final HashMap<Item, TIntHashSet> ACCEPTABLE_DROPS_WITH_META = new HashMap<>();

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
                    if (!addBlock(split[0], split[1])) {
                        GravityMod.logWarning("Could not find block for \"anti-mass.drops\" entry: %s", configString);
                    }
                }
            }
        }

        static boolean addBlockWithMeta(String modID, String blockName, int meta) {
            Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(modID, blockName));
            if (block == Blocks.AIR) {
                return false;
            }
            addBlockWithMeta(block, meta);
            return true;
        }

        static void addBlockOreName(String name) {
            ACCEPTABLE_BLOCK_ORE_NAMES.add(name);
        }

        static boolean addBlock(String modID, String blockName) {
            Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(modID, blockName));
            // Default value is air, as opposed to null
            if (block == Blocks.AIR) {
                return false;
            }
            addBlock(block);
            return true;
        }

        static void addBlockWithMeta(Block block, int meta) {
            TIntHashSet tIntHashSet = ACCEPTABLE_BLOCKS_WITH_META.get(block);
            if (tIntHashSet == null) {
                tIntHashSet = new TIntHashSet();
                ACCEPTABLE_BLOCKS_WITH_META.put(block, tIntHashSet);
            }
            tIntHashSet.add(meta);
        }

        static boolean addBlock(Block block) {
            return ACCEPTABLE_BLOCKS.add(block);
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
                    if (!addItemDrop(split[0], split[1])) {
                        GravityMod.logWarning("Could not find item/block for \"anti-mass.drops\" entry: %s", configString);
                    }
                }
            }
        }

        static boolean addItemDropWithMeta(String modID, String itemName, int meta) {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(modID, itemName));
            // No default value/default value is null
            if (item == null) {
                return false;
            }
            addItemDropWithMeta(item, meta);
            return true;
        }

        static void addDropsOreName(String name) {
            ACCEPTABLE_DROPS_ORE_NAMES.add(name);
        }

        static boolean addItemDrop(String modID, String itemName) {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(modID, itemName));
            // No default value/default value is null
            return item != null && addItemDrop(item);
        }

        static void addItemDropWithMeta(Item item, int meta) {
            TIntHashSet tIntHashSet = ACCEPTABLE_DROPS_WITH_META.get(item);
            if (tIntHashSet == null) {
                tIntHashSet = new TIntHashSet();
                ACCEPTABLE_DROPS_WITH_META.put(item, tIntHashSet);
            }
            tIntHashSet.add(meta);
        }

        static boolean addItemDrop(Item item) {
            return ACCEPTABLE_DROPS.add(item);
        }

        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void onBlockDropItems(BlockEvent.HarvestDropsEvent event) {
            EntityPlayer harvester = event.getHarvester();
            // FakePlayers are allowed, custom item entities are not spawned in that case, instead, the anti-mass item is directly added to the drops
            if (harvester != null) {

                World world = event.getWorld();
                IBlockState blockState = event.getState();
                Block block = blockState.getBlock();
                if (block == Blocks.LIT_REDSTONE_ORE) {
                    block = Blocks.REDSTONE_ORE;
                }

                boolean blockIsAcceptable = ACCEPTABLE_BLOCKS.contains(block);

                if (!blockIsAcceptable) {
                    TIntHashSet metadataSet = ACCEPTABLE_BLOCKS_WITH_META.get(block);
                    if (metadataSet != null) {
                        blockIsAcceptable = metadataSet.contains(block.getMetaFromState(blockState));
                    }

                    if (!blockIsAcceptable) {

                        Item itemFromBlock = Item.getItemFromBlock(block);

                        // OreDictionary will throw an exception if the Item from a Block is null
                        if (itemFromBlock != null) {
                            int[] oreIDs = OreDictionary.getOreIDs(new ItemStack(itemFromBlock));

                            for (int oreID : oreIDs) {
                                String oreName = OreDictionary.getOreName(oreID);
                                if (ACCEPTABLE_BLOCK_ORE_NAMES.contains(oreName)) {
                                    blockIsAcceptable = true;
                                    break;
                                }
                            }
                        }
                    }
                }

                if (blockIsAcceptable) {
                    // Block broken is ok, check each of the drops to see if they're acceptable, we may return after the first acceptable drop, dependent on
                    // config

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
                                    if (!(harvester instanceof FakePlayer)) {
                                        // drop special item
                                        BlockBreakListener.spawnAsSpecialEntity(world, event.getPos(), stack);
                                        it.remove();
                                    }
                                    else {
                                        // add dust directly to drops (don't want a quarry to spawn floating items. Quarries
                                        // should be able to collect the GRAVITY_DUST and send it straight into a storage system
                                        it.add(new ItemStack(StaticItems.GRAVITY_DUST, ConfigHandler.gravityDustAmountDropped));
                                    }
                                }
                                else {
                                    if (!(harvester instanceof FakePlayer)) {
                                        // no drop at all
                                        it.remove();
                                    }
                                    else {
                                        // do nothing
                                    }
                                }
                            }
                            else {
                                // Item should drop as per normal (we'll let vanilla handle if the item drops or not)
                            }
                            if (ConfigHandler.gravityDustChanceOncePerBrokenBlock) {
                                // Only one attempt allowed per block broken
                                return;
                            }
                        }
                    }
                }
            }
        }

        /**
         * Spawns the given ItemStack as an EntityItem into the World at the given position
         */
        static void spawnAsSpecialEntity(World worldIn, BlockPos pos, ItemStack stack) {
            if (!worldIn.isRemote && worldIn.getGameRules().getBoolean("doTileDrops") && !worldIn.restoringBlockSnapshots) // do not drop items while restoring blockstates, prevents item dupe
            {
                float f = 0.5F;
                double d0 = (double)(worldIn.rand.nextFloat() * f) + 0.25D;
                double d1 = (double)(worldIn.rand.nextFloat() * f) + 0.25D;
                double d2 = (double)(worldIn.rand.nextFloat() * f) + 0.25D;
                EntityItem entityitem = new EntityFloatingItem(worldIn, (double)pos.getX() + d0, (double)pos.getY() + d1, (double)pos.getZ() + d2, stack);
                entityitem.setDefaultPickupDelay();
                worldIn.spawnEntityInWorld(entityitem);
            }
        }
    }
}

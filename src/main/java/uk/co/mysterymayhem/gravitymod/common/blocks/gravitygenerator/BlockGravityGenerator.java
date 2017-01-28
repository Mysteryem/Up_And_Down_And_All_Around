package uk.co.mysterymayhem.gravitymod.common.blocks.gravitygenerator;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.oredict.ShapedOreRecipe;
import uk.co.mysterymayhem.gravitymod.GravityMod;
import uk.co.mysterymayhem.gravitymod.api.EnumGravityTier;
import uk.co.mysterymayhem.gravitymod.common.blocks.AbstractModBlock;
import uk.co.mysterymayhem.gravitymod.common.blocks.GenericItemBlock;
import uk.co.mysterymayhem.gravitymod.common.registries.StaticGUIs;
import uk.co.mysterymayhem.gravitymod.common.registries.StaticItems;
import uk.co.mysterymayhem.mystlib.block.metaconverters.AbstractMetaMapper.MetaHelper;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

/**
 * Created by Mysteryem on 2016-10-10.
 */
public class BlockGravityGenerator extends AbstractModBlock<BlockGravityGenerator, GenericItemBlock<BlockGravityGenerator>> /*implements ITileEntityProvider*/ {
//    private static final String name = "gravitygenerator";

    public static final PropertyEnum<EnumGravityTier> TIER = PropertyEnum.create("tier", EnumGravityTier.class);

    // Direction the block is faced
    public static final PropertyDirection FACING = BlockDirectional.FACING;

    // Used with get actual state when block is powered
    public static final PropertyBool ENABLED = PropertyBool.create("active");

//    public final EnumGravityTier gravityTier;

    public BlockGravityGenerator(/*EnumGravityTier gravityTier*/) {
        super(Material.IRON, MapColor.IRON, new MetaHelper().addMeta(TIER).addNonMeta(FACING, ENABLED));
        this.setHardness(5F).setResistance(10f).setSoundType(SoundType.METAL);
    }

    @Override
    public GenericItemBlock<BlockGravityGenerator> createItem(BlockGravityGenerator block) {
        GenericItemBlock<BlockGravityGenerator> itemBlock = new GenericItemBlock<BlockGravityGenerator>(block) {
            @Override
            public String getUnlocalizedName(ItemStack stack) {
                int itemDamage = stack.getItemDamage();
                IBlockState stateFromMeta = getStateFromMeta(itemDamage);
                String extra = TIER.getName(stateFromMeta.getValue(TIER));
                return this.getBlock().getUnlocalizedName() + '.' + extra;
//                return super.getUnlocalizedName(stack);
            }

            @Override
            public EnumRarity getRarity(ItemStack stack) {
                if (stack.isItemEnchanted()) {
                    return EnumRarity.RARE;
                }
                int itemDamage = stack.getItemDamage();
                IBlockState stateFromMeta = getStateFromMeta(itemDamage);
                switch(stateFromMeta.getValue(TIER)) {
                    case WEAK:
                        return GravityMod.RARITY_WEAK;
                    case NORMAL:
                        return GravityMod.RARITY_NORMAL;
                    default://case STRONG:
                        return GravityMod.RARITY_STRONG;
                }
            }
        };
        itemBlock.setHasSubtypes(true);
        return itemBlock;
    }

    @Override
    public int damageDropped(IBlockState state) {
        return this.getMetaFromState(state);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            TileEntity tileEntity = worldIn.getTileEntity(pos);

//            if (tileEntity instanceof TileGravityGenerator && playerIn instanceof EntityPlayerMP) {
//                EntityPlayerMP playerMP = (EntityPlayerMP)playerIn;
//                playerMP.connection.sendPacket(new SPacketOpenWindow(playerMP.currentWindowId, ));
//            }
            if (tileEntity instanceof TileGravityGenerator) {
                StaticGUIs.GUI_GRAVITY_GENERATOR.openGUI(playerIn, worldIn, pos);
            }
        }
        return true;
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof TileGravityGenerator && tileEntity.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
            IItemHandler itemHandler = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
            int slots = itemHandler.getSlots();
            for (int i = 0; i < slots; i++) {
                // Extracting items may not be necessary
                ItemStack stackInSlot = itemHandler.extractItem(i, Integer.MAX_VALUE, false);
                if (stackInSlot != null) {
                    Block.spawnAsEntity(worldIn, pos, stackInSlot);
                }
            }
        }

        super.breakBlock(worldIn, pos, state);
    }

    //    @Override
//    public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis) {
//        IBlockState blockState = world.getBlockState(pos);
//        Boolean value = blockState.getValue(ENABLED);
//        if (value) {
//            return false;
//        }
//
//        EnumFacing facing = blockState.getValue(FACING);
//        EnumFacing newFacing = facing.rotateAround(axis.getAxis());
//
//        if (newFacing != facing) {
//            TileEntity tileEntity = world.getTileEntity(pos);
//            if (tileEntity instanceof TileGravityGenerator) {
//                world.setBlockState(pos, blockState.withProperty(FACING, newFacing));
//                ((TileGravityGenerator) tileEntity).updateOnFacingChange(newFacing);
//                return true;
//            }
//            else {
//                GravityMod.logWarning("Tried to rotate BlockGravityGenerator with non/the wrong tile entity");
//                return false;
//            }
//        }
//        else {
//            return false;
//        }
//    }

//    // Why is this deprecated?..
//    @SuppressWarnings("deprecation")
//    @Override
//    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn) {
//        int powered = worldIn.isBlockIndirectlyGettingPowered(pos);
//        worldIn.setBlockState(pos, state.withProperty(ENABLED, powered > 0), 1 | 2);
//    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof TileGravityGenerator) {
            TileGravityGenerator generator = (TileGravityGenerator)tileEntity;
            generator.setFacing(BlockPistonBase.getFacingFromEntity(pos, placer));
        }
//        worldIn.setBlockState(pos, state.withProperty(FACING, BlockPistonBase.getFacingFromEntity(pos, placer)), 2);
//        String ad = "";
    }

//    @Override
//    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
//        if (!worldIn.isRemote) {
//            if (worldIn.isPowered(pos)) {
//                worldIn.setBlockState(pos, state.withProperty(ENABLED, true), 1 | 2);
//            }
//        }
//    }

    @SideOnly(Side.CLIENT)
    @Override
    public void preInitClient() {
//        ItemMeshDefinition meshDefinitions = stack -> {
//            int metadata = stack.getMetadata();
//            IBlockState stateFromMeta = BlockGravityGenerator.this.getStateFromMeta(metadata);
//            return new ModelResourceLocation(BlockGravityGenerator.this.getRegistryName(), TIER.getName() + "=" + TIER.getName(stateFromMeta.getValue(TIER)));
//        };
//        ArrayList<ResourceLocation> mrls = new ArrayList<>();
//        IBlockState defaultState = this.getDefaultState();
//        for (EnumGravityTier tier : EnumGravityTier.values()) {
//            int meta = this.getMetaFromState(defaultState.withProperty(TIER, tier));
//            mrls.add(meshDefinitions.getModelLocation(new ItemStack(this, 1, meta)));
//        }
//
//        ResourceLocation[] modelResourceLocations = mrls.toArray(new ResourceLocation[mrls.size()]);
//
//        ItemBlock item = this.getItem();
//
//        ModelBakery.registerItemVariants(item, modelResourceLocations);
//        ModelLoader.setCustomMeshDefinition(item, meshDefinitions);

        for (EnumGravityTier tier : EnumGravityTier.values()) {
            int meta = this.getMetaFromState(this.getDefaultState().withProperty(TIER, tier));
//            ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(this.getRegistryName(), "inventory"));
            ModelResourceLocation modelResourceLocation = new ModelResourceLocation(this.getRegistryName(), ENABLED.getName() + "=" + ENABLED.getName(false) + "," + FACING.getName() + "=" + FACING.getName(EnumFacing.NORTH) + "," + TIER.getName() + "=" + TIER.getName(tier));
            ModelLoader.registerItemVariants(item, modelResourceLocation);
            ModelLoader.setCustomModelResourceLocation(item, meta, modelResourceLocation);
        }
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, ItemStack stack) {
        if (placer != null) {
            facing = BlockPistonBase.getFacingFromEntity(pos, placer);
        }
        int metadata = stack.getMetadata();
        return this.getStateFromMeta(metadata).withProperty(FACING, facing);
//        return super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, stack);
    }

    @Override
    public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list) {
//        IBlockState defaultState = this.getDefaultState();
        for (EnumGravityTier tier : EnumGravityTier.values()) {
            list.add(new ItemStack(this, 1, tier.ordinal()/*this.getMetaFromState(defaultState.withProperty(TIER, tier))*/));
        }
//        list.add(new ItemStack(this, 1, this.getMetaFromState(defaultState.withProperty(TIER, EnumGravityTier.WEAK))));
//        list.add(new ItemStack(this, 1, this.getMetaFromState(defaultState.withProperty(TIER, EnumGravityTier.NORMAL))));
//        list.add(new ItemStack(this, 1, this.getMetaFromState(defaultState.withProperty(TIER, EnumGravityTier.STRONG))));
//        super.getSubBlocks(itemIn, tab, list);
    }

    @SuppressWarnings("deprecation")
    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        TileEntity tileEntity;
        if (worldIn instanceof ChunkCache) {
            tileEntity = ((ChunkCache) worldIn).getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);
        }
        else {
            tileEntity = worldIn.getTileEntity(pos);
        }
        EnumFacing facing = EnumFacing.UP;
        boolean blockPowered = false;
        if (tileEntity instanceof TileGravityGenerator) {
            TileGravityGenerator tileGravityGenerator = (TileGravityGenerator)tileEntity;
            World world = tileGravityGenerator.getWorld();
            if (world != null) {
                blockPowered = world.isBlockPowered(pos);
            }
            else {
                // fallback I guess
                blockPowered = tileGravityGenerator.isPowered();
            }
            facing = tileGravityGenerator.getFacing();
        }
        return state.withProperty(FACING, facing).withProperty(ENABLED, blockPowered);
//        return super.getActualState(state, worldIn, pos);
    }

    public static int getRedstonePower(IBlockAccess access, BlockPos pos, EnumFacing facing)
    {
        IBlockState iblockstate = access.getBlockState(pos);
        return iblockstate.getBlock().shouldCheckWeakPower(iblockstate, access, pos, facing) ? access.getStrongPower(pos, facing) : iblockstate.getWeakPower(access, pos, facing);
    }

    public static boolean isBlockPowered(IBlockAccess access, BlockPos pos)
    {
        return getRedstonePower(access, pos.down(), EnumFacing.DOWN) > 0
                || getRedstonePower(access, pos.up(), EnumFacing.UP) > 0
                || getRedstonePower(access, pos.north(), EnumFacing.NORTH) > 0
                || getRedstonePower(access, pos.south(), EnumFacing.SOUTH) > 0
                || getRedstonePower(access, pos.west(), EnumFacing.WEST) > 0
                || getRedstonePower(access, pos.east(), EnumFacing.EAST) > 0;
    }

    public static EnumFacing getFacingFromEntity(BlockPos clickedBlock, EntityLivingBase entity) {
        return BlockPistonBase.getFacingFromEntity(clickedBlock, entity);
//        //TODO: Surely you'd need to add 0.5 to each of the clickedBlock values
//        return EnumFacing.getFacingFromVector(
//                (float) (entity.posX - clickedBlock.getX()),
//                (float) (entity.posY - clickedBlock.getY()),
//                (float) (entity.posZ - clickedBlock.getZ()));
    }

    /*

        From IModBlock

     */

    @Override
    public void preInit() {
        this.setDefaultState(this.getDefaultState().withProperty(FACING, EnumFacing.UP));

        super.preInit();
    }

    @Override
    public String getName() {
        return "gravitygenerator";
    }

    @Override
    public GenericItemBlock<BlockGravityGenerator> getItem() {
        return this.item;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        Collection<IProperty<?>> propertyNames = state.getPropertyNames();
        if (propertyNames.contains(FACING)) {
            return new TileGravityGenerator(state.getValue(TIER), state.getValue(FACING));
        }
        else {
            GravityMod.logWarning("Failed to create tile entity in %s due to invalid blockstate %s", world, state);
            return null;
        }
    }

    @Override
    public void postInit() {
        super.postInit();

//        ItemStack antimassBucket = UniversalBucket.getFilledBucket(ForgeModContainer.getInstance().universalBucket, LiquidAntiMass.INSTANCE);
        ItemStack weakGenerator = new ItemStack(this, 1, this.getMetaFromState(this.getDefaultState().withProperty(TIER, EnumGravityTier.WEAK)));
        ItemStack normalGenerator = new ItemStack(this, 1, this.getMetaFromState(this.getDefaultState().withProperty(TIER, EnumGravityTier.NORMAL)));
        ItemStack strongGenerator = new ItemStack(this, 1, this.getMetaFromState(this.getDefaultState().withProperty(TIER, EnumGravityTier.STRONG)));

        GameRegistry.addRecipe(new ShapedOreRecipe(
                weakGenerator,
                "SCS",
                "CRC",
                "SCS",
                'S', "stone",
                'C', "cobblestone",
                'R', StaticItems.RESTABILISED_GRAVITY_DUST));

        GameRegistry.addRecipe(new ShapedOreRecipe(
                normalGenerator,
                "GWG",
                "WIW",
                "GWG",
                'G', StaticItems.GRAVITY_INGOT,
                'W', weakGenerator,
                'I', "ingotIron"));

        GameRegistry.addRecipe(new ShapedOreRecipe(
                strongGenerator,
                "GNG",
                "NLN",
                "GNG",
                'G', "ingotGold",
                'N', normalGenerator,
                'L', "blockLapis"));
    }
}

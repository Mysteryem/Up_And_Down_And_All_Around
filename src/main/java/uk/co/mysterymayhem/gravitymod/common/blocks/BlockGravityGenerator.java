package uk.co.mysterymayhem.gravitymod.common.blocks;

import net.minecraft.block.BlockDirectional;
import net.minecraft.block.BlockPistonBase;
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
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import uk.co.mysterymayhem.gravitymod.GravityMod;
import uk.co.mysterymayhem.gravitymod.api.EnumGravityTier;
import uk.co.mysterymayhem.gravitymod.common.tileentities.TileGravityGenerator;
import uk.co.mysterymayhem.mystlib.block.metaconverters.AbstractMetaMapper.MetaHelper;

import java.util.Collection;
import java.util.List;

/**
 * Created by Mysteryem on 2016-10-10.
 */
public class BlockGravityGenerator extends AbstractModBlock<BlockGravityGenerator, ItemBlock> /*implements ITileEntityProvider*/ {
//    private static final String name = "gravitygenerator";

    public static final PropertyEnum<EnumGravityTier> TIER = PropertyEnum.create("tier", EnumGravityTier.class);

    // Direction the block is faced
    public static final PropertyDirection FACING = BlockDirectional.FACING;

    // Used with get actual state when block is powered
    public static final PropertyBool ENABLED = PropertyBool.create("active");

//    public final EnumGravityTier gravityTier;
    public static ItemBlock getItemBlock (BlockGravityGenerator block) {
        ItemBlock itemBlock = new ItemBlock(block);
        itemBlock.setHasSubtypes(true);
        return itemBlock;
    }

    public BlockGravityGenerator(/*EnumGravityTier gravityTier*/) {
        super(Material.ROCK, MapColor.QUARTZ, new MetaHelper().addMeta(TIER).addNonMeta(FACING, ENABLED));
//        this.gravityTier = gravityTier;
    }

    @Override
    public ItemBlock createItem(BlockGravityGenerator block) {
        ItemBlock itemBlock = new ItemBlock(block) {
            @Override
            public String getUnlocalizedName(ItemStack stack) {
                int itemDamage = stack.getItemDamage();
                IBlockState stateFromMeta = getStateFromMeta(itemDamage);
                String extra = TIER.getName(stateFromMeta.getValue(TIER));
                return this.getBlock().getUnlocalizedName() + '.' + extra;
//                return super.getUnlocalizedName(stack);
            }
        };
        itemBlock.setHasSubtypes(true);
        return itemBlock;
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
//            if (worldIn.isBlockPowered(pos)) {
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
        tileEntity = worldIn.getTileEntity(pos);
        EnumFacing facing = EnumFacing.UP;
        boolean blockPowered = false;
        if (tileEntity instanceof TileGravityGenerator) {
            TileGravityGenerator generator = (TileGravityGenerator)tileEntity;
            facing = generator.getFacing();
            if (worldIn instanceof World) {
                blockPowered = ((World) worldIn).isBlockPowered(pos);
            }
            else {
                blockPowered = isBlockPowered(worldIn, pos);
            }

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
    public ItemBlock getItem() {
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
}

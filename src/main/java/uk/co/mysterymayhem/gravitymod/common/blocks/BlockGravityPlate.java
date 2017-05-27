package uk.co.mysterymayhem.gravitymod.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import uk.co.mysterymayhem.gravitymod.GravityMod;
import uk.co.mysterymayhem.gravitymod.api.API;
import uk.co.mysterymayhem.gravitymod.api.EnumGravityDirection;
import uk.co.mysterymayhem.gravitymod.api.EnumGravityTier;
import uk.co.mysterymayhem.gravitymod.common.registries.GravityPriorityRegistry;
import uk.co.mysterymayhem.gravitymod.common.registries.IGravityModCommon;
import uk.co.mysterymayhem.gravitymod.common.registries.StaticItems;
import uk.co.mysterymayhem.mystlib.setup.singletons.AbstractModBlockWithItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by Mysteryem on 20/02/2017.
 */
public class BlockGravityPlate extends AbstractModBlockWithItem<BlockGravityPlate, GenericItemBlock<BlockGravityPlate>> implements IGravityModCommon {

    //0.03125 = 1/32
    private static final AxisAlignedBB DOWN = new AxisAlignedBB(0, 1 - 0.03125, 0, 1, 1, 1);
    private static final AxisAlignedBB UP = new AxisAlignedBB(0, 0, 0, 1, 0.03125, 1);
    private static final AxisAlignedBB NORTH = new AxisAlignedBB(0, 0, 1 - 0.03125, 1, 1, 1);
    private static final AxisAlignedBB EAST = new AxisAlignedBB(0, 0, 0, 0.03125, 1, 1);
    private static final AxisAlignedBB SOUTH = new AxisAlignedBB(0, 0, 0, 1, 1, 0.03125);
    private static final AxisAlignedBB WEST = new AxisAlignedBB(1 - 0.03125, 0, 0, 1, 1, 1);

    private static final PropertyBool GLOWING = PropertyBool.create("glowing");

    private static final Material PLATE_MATERIAL = new Material(MapColor.AIR) {

        @Override
        public boolean isSolid() {
            return false;
        }

        /**
         * Will prevent grass from growing on dirt underneath and kill any grass below it if it returns true
         */
        @Override
        public boolean blocksLight() {
            return false;
        }

        /**
         * Returns if this material is considered solid or not
         */
        @Override
        public boolean blocksMovement() {
            return false;
        }
    };

    private final EnumGravityTier tier;
    private final int priorityValue;

    public BlockGravityPlate(EnumGravityTier tier) {
        super(PLATE_MATERIAL, MapColor.AIR, BlockDirectional.FACING, GLOWING);
        this.setHardness(1.5F).setResistance(10f);
        this.tier = tier;
        switch (this.tier) {
            case WEAK:
                this.setSoundType(SoundType.STONE);
                this.priorityValue = GravityPriorityRegistry.WEAK_MAX;
                break;
            case NORMAL:
                this.setSoundType(SoundType.METAL);
                this.priorityValue = GravityPriorityRegistry.NORMAL_MAX;
                break;
            default://case STRONG:
                this.setSoundType(SoundType.METAL);
                this.priorityValue = GravityPriorityRegistry.STRONG_MAX;
                break;
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void preInitClient() {
        super.preInitClient();
        ModelLoader.setCustomModelResourceLocation(this.getItem(), 1, new ModelResourceLocation(this.getItem().getRegistryName(), "inventory"));
    }

    @Override
    public void postInit() {
        super.postInit();
        // Maybe I should add a slime ball to each recipe
        // For weak, 4 anti-mass gets 11x11 (121) cross sectional coverage with a generator, 1 anti-mass would be equivalent to ~30
        switch (this.tier) {
            case WEAK:
                GameRegistry.addShapelessRecipe(new ItemStack(this, 32),
                        StaticItems.DESTABILISED_GRAVITY_DUST, Blocks.STONE_PRESSURE_PLATE);
                break;
            case NORMAL:
                GameRegistry.addShapelessRecipe(new ItemStack(this, 32),
                        StaticItems.RESTABILISED_GRAVITY_DUST, Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE);
                break;
            case STRONG:
                GameRegistry.addShapelessRecipe(new ItemStack(this, 32),
                        StaticItems.RESTABILISED_GRAVITY_DUST, StaticItems.RESTABILISED_GRAVITY_DUST,
                        StaticItems.RESTABILISED_GRAVITY_DUST, StaticItems.RESTABILISED_GRAVITY_DUST, Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE);
                break;
        }
        GameRegistry.addShapelessRecipe(new ItemStack(this, 1, 1),
                this, Items.GLOWSTONE_DUST, Items.GLOWSTONE_DUST, Items.GLOWSTONE_DUST, Items.GLOWSTONE_DUST);
        GameRegistry.addShapelessRecipe(new ItemStack(this, 1, 1),
                this, Blocks.GLOWSTONE);
        GameRegistry.addShapelessRecipe(new ItemStack(this, 1, 1),
                this, Blocks.TORCH);
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        IBlockState other = world.getBlockState(pos);
        if (other.getBlock() != this) {
            return other.getLightValue(world, pos);
        }
        return other.getValue(GLOWING) ? 15 : this.lightValue;
    }

    @Override
    public GenericItemBlock<BlockGravityPlate> createItem(BlockGravityPlate block) {
        return new ItemGravityPlate(this);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getSubBlocks(Item itemIn, CreativeTabs tab, NonNullList<ItemStack> list) {
        super.getSubBlocks(itemIn, tab, list);
        if (itemIn == this.getItem()) {
            list.add(new ItemStack(itemIn, 1, 1));
        }
    }

    @Override
    public boolean isToolEffective(String type, IBlockState state) {
        return type.equals("pickaxe");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }

    @SuppressWarnings("deprecation")
    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return NULL_AABB;
    }

    @Override
    public int damageDropped(IBlockState state) {
        return state.getValue(GLOWING) ? 1 : 0;
    }

    /**
     * Used to determine ambient occlusion and culling when rebuilding chunks for render
     */
    @SuppressWarnings("deprecation")
    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean blocksMovement(IBlockAccess worldIn, BlockPos pos) {
        return false;
    }

    /**
     * Return true if an entity can be spawned inside the block (used to get the player's bed spawn location)
     */
    @Override
    public boolean canSpawnInBlock() {
        return true;
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase
            placer, EnumHand hand) {
        if (this.canBlockStay(world, pos, facing)) {
            return this.getDefaultState().withProperty(BlockDirectional.FACING, facing).withProperty(GLOWING, placer.getHeldItem(hand).getItemDamage() != 0);
        }
        else {
            for (EnumFacing enumfacing : EnumFacing.values()) {
                if (this.canBlockStay(world, pos, enumfacing)) {
                    return this.getDefaultState().withProperty(BlockDirectional.FACING, enumfacing).withProperty(GLOWING, placer.getHeldItem(hand).getItemDamage() != 0);
                }
            }

            return this.getDefaultState();
        }
    }

    private boolean canBlockStay(World worldIn, BlockPos pos, EnumFacing facing) {
        return worldIn.getBlockState(pos.offset(facing.getOpposite())).isSideSolid(worldIn, pos.offset(facing.getOpposite()), facing);
    }

    //Should look into allowing placement on glass (will need to change other methods than just this one)
    @Override
    public boolean canPlaceBlockAt(World worldIn, @Nonnull BlockPos pos) {
        for (EnumFacing facing : EnumFacing.values()) {
            if (this.canBlockStay(worldIn, pos, facing)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Called when a neighboring block was changed and marks that this state should perform any checks during a neighbor
     * change. Cases may include when redstone power is updated, cactus blocks popping off due to a neighboring solid
     * block, etc.
     */
    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos changedPos) {
        EnumFacing enumfacing = state.getValue(BlockDirectional.FACING);

        if (!this.canBlockStay(worldIn, pos, enumfacing)) {
            this.dropBlockAsItem(worldIn, pos, state, 0);
            worldIn.setBlockToAir(pos);
        }

        super.neighborChanged(state, worldIn, pos, blockIn, changedPos);
    }

    @SuppressWarnings("deprecation")
    @Nonnull
    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        EnumFacing value = state.getValue(BlockDirectional.FACING);
        return getBBFromFacing(value);
    }

    private static AxisAlignedBB getBBFromFacing(EnumFacing facing) {
        switch (facing) {
            case DOWN:
                return DOWN;
            case UP:
                return UP;
            case NORTH:
                return NORTH;
            case SOUTH:
                return SOUTH;
            case WEST:
                return WEST;
            default://case EAST:
                return EAST;
        }
    }

    /**
     * Called When an Entity enters the 1*1*1 space the block occupies in the world
     */
    @Override
    public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
        if (!worldIn.isRemote) {
            EnumFacing facing = state.getValue(BlockDirectional.FACING);
            AxisAlignedBB axisalignedbb = getBBFromFacing(facing).offset(pos);
            List<EntityPlayerMP> list;

            list = worldIn.getEntitiesWithinAABB(EntityPlayerMP.class, axisalignedbb);

            if (!list.isEmpty()) {
                EnumGravityDirection direction = EnumGravityDirection.fromEnumFacing(facing.getOpposite());
                for (EntityPlayerMP player : list) {
                    if (this.tier.isPlayerAffected(player)) {
                        API.setPlayerGravity(direction, player, this.priorityValue);
                    }
                }
            }
        }
    }

    @Override
    public String getModObjectName() {
        return this.tier.getInternalPrefix() + "gravityplate";
    }

    private static class ItemGravityPlate extends GenericItemBlock<BlockGravityPlate> {

        public ItemGravityPlate(BlockGravityPlate block) {
            super(block);
            this.setHasSubtypes(true);
        }

        @Override
        public EnumRarity getRarity(ItemStack stack) {
            switch (this.getBlock().tier) {
                case WEAK:
                    return GravityMod.RARITY_WEAK;
                case NORMAL:
                    return GravityMod.RARITY_NORMAL;
                default://case STRONG:
                    return GravityMod.RARITY_STRONG;
            }
        }

        @Override
        public String getUnlocalizedName(ItemStack stack) {
            if (stack.getItemDamage() != 0) {
                return super.getUnlocalizedName(stack) + ".glowing";
            }
            return super.getUnlocalizedName(stack);
        }
    }
}

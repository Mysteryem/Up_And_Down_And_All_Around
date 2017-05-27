package uk.co.mysterymayhem.gravitymod.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import uk.co.mysterymayhem.gravitymod.common.config.ConfigHandler;
import uk.co.mysterymayhem.gravitymod.common.registries.IGravityModCommon;
import uk.co.mysterymayhem.gravitymod.common.registries.StaticItems;
import uk.co.mysterymayhem.mystlib.setup.singletons.AbstractModBlockWithItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Locale;
import java.util.Random;

/**
 * Created by Mysteryem on 21/02/2017.
 */
public class BlockGravityOre extends AbstractModBlockWithItem<BlockGravityOre, GenericItemBlock<BlockGravityOre>> implements IGravityModCommon {

    public static final PropertyEnum<Type> TYPE = PropertyEnum.create("type", Type.class);

    public BlockGravityOre() {
        super(Material.ROCK, TYPE);
        this.setHarvestLevel("pickaxe", 2);
        this.setHardness(3f).setResistance(5f);
        this.setDefaultState(this.getDefaultState().withProperty(TYPE, Type.STONE));
    }

    @Override
    public GenericItemBlock<BlockGravityOre> createItem(BlockGravityOre block) {
        GenericItemBlock<BlockGravityOre> item = new GenericItemBlock<>(this);
        item.setHasSubtypes(true);
        return item;
    }

    @Override
    public void preInit() {
        super.preInit();
        OreDictionary.registerOre("oreGravity", new ItemStack(this, 1, OreDictionary.WILDCARD_VALUE));
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase
            placer, EnumHand hand) {
        ItemStack stack = placer.getHeldItem(hand);
        if (stack.getMetadata() == 0) {
            IBlockState placedAgainstState = world.getBlockState(pos.offset(facing.getOpposite()));
            Block placedAgainstBlock = placedAgainstState.getBlock();
            Type placedType;
            if (placedAgainstBlock == this) {
                placedType = placedAgainstState.getValue(TYPE);
            }
            else if (placedAgainstBlock == Blocks.END_STONE) {
                placedType = Type.END_STONE;
            }
            else if (placedAgainstBlock == Blocks.NETHERRACK) {
                placedType = Type.NETHERRACK;
            }
            else if (placedAgainstBlock == Blocks.STONE) {
                placedType = Type.STONE;
            }
            else {
                switch (world.provider.getDimensionType()) {
                    case NETHER:
                        placedType = Type.NETHERRACK;
                        break;
                    case THE_END:
                        placedType = Type.END_STONE;
                        break;
                    default:
                        placedType = Type.STONE;
                        break;
                }
            }
            return this.getDefaultState().withProperty(TYPE, placedType);
        }
        else {
            int metadata = stack.getMetadata();
            return this.getStateFromMeta(metadata);
        }
    }


    @SideOnly(Side.CLIENT)
    @Override
    public void preInitClient() {
        for (Type blockType : Type.values()) {
            int meta = this.getMetaFromState(this.getDefaultState().withProperty(TYPE, blockType));
            ModelResourceLocation modelResourceLocation = new ModelResourceLocation(this.getRegistryName(),
                    TYPE.getName() + "=" + TYPE.getName(blockType));
            ModelLoader.registerItemVariants(this.item, modelResourceLocation);
            ModelLoader.setCustomModelResourceLocation(this.item, meta, modelResourceLocation);
        }
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return new ItemStack(this, 1, this.getMetaFromState(state));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getSubBlocks(Item itemIn, CreativeTabs tab, NonNullList<ItemStack> list) {
        for (Type blockType : Type.values()) {
            list.add(new ItemStack(this, 1, this.getMetaFromState(this.getDefaultState().withProperty(TYPE, blockType))));
        }
    }

    @Override
    public String getModObjectName() {
        return "gravityore";
    }

    @Override
    public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        return super.canSilkHarvest(world, pos, state, player);
//        return false;
    }

    @Nullable
    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return StaticItems.GRAVITY_DUST;
    }

    //TODO: Re-test, these values are no longer correct
    // ~7.2526 drops with no fortune (1.7M trials), ~15.1809 drops with fortune 3 (1.9M trials)
    @Override
    public int quantityDropped(IBlockState state, int fortune, Random random) {
        int baseAmount = this.quantityDropped(random);

        // From BlockOre
        if (fortune > 0) {
            float i = (random.nextFloat() * (fortune + 2)) - 1;
//            int i = random.nextInt(fortune + 2) - 1;

            if (i < 0) {
                i = 0;
            }

            return (int)(baseAmount * (i + 1) + random.nextInt(baseAmount + 1));
        }
        else {
            // Amount dropped from config plus [0,baseAmount], total = [baseAmount, 2*baseAmount]
            // This is done to make the actual amount a bit more random
            return baseAmount + random.nextInt(baseAmount + 1);
        }
    }

    @Override
    public int quantityDropped(Random random) {
        return ConfigHandler.gravityDustAmountDropped;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.SOLID;
    }

    @Nullable
    @Override
    protected ItemStack getSilkTouchDrop(IBlockState state) {
        return new ItemStack(this);
    }

    public enum Type implements IStringSerializable {
        STONE, NETHERRACK, END_STONE;

        private final String niceName;

        Type() {
            this.niceName = this.name().toLowerCase(Locale.ENGLISH);
        }

        @Nonnull
        @Override
        public String getName() {
            return this.niceName;
        }
    }
}

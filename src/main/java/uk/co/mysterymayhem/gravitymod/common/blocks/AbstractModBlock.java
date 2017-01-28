package uk.co.mysterymayhem.gravitymod.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import uk.co.mysterymayhem.gravitymod.common.registries.IGravityModBlockWithItem;
import uk.co.mysterymayhem.mystlib.block.metaconverters.AbstractMetaMapper;
import uk.co.mysterymayhem.mystlib.block.metaconverters.AbstractMetaMapper.MetaHelper;

import javax.annotation.Nonnull;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by Mysteryem on 2016-12-17.
 */
@SuppressWarnings("unchecked")
public abstract class AbstractModBlock<BLOCK extends AbstractModBlock<BLOCK, ITEM>, ITEM extends Item> extends Block implements IGravityModBlockWithItem<BLOCK, ITEM> {

    protected final ITEM item;
    protected final AbstractMetaMapper<AbstractModBlock<BLOCK, ITEM>> metaConverter;
    // hiding the field in Block.class on purpose
    protected final BlockStateContainer blockState;

    // I heard you like constructors

    public AbstractModBlock(Function<AbstractModBlock<BLOCK, ITEM>, ITEM> itemFunction, Material blockMaterialIn, MapColor blockMapColorIn, MetaHelper metaHelper) {
        super(blockMaterialIn, blockMapColorIn);
        this.item = itemFunction.apply(this);
        this.metaConverter = AbstractMetaMapper.get(this, metaHelper);
        this.blockState = this.metaConverter.createBlockState();
        this.setDefaultState(this.getBlockState().getBaseState());
    }

    public AbstractModBlock(Function<AbstractModBlock<BLOCK, ITEM>, ITEM> itemFunction, Material materialIn, MetaHelper metaHelper) {
        super(materialIn);
        this.item = itemFunction.apply(this);
        this.metaConverter = AbstractMetaMapper.get(this, metaHelper);
        this.blockState = this.metaConverter.createBlockState();
        this.setDefaultState(this.getBlockState().getBaseState());
    }

    public AbstractModBlock(Supplier<ITEM> itemSupplier, Material blockMaterialIn, MapColor blockMapColorIn, MetaHelper metaHelper) {
        super(blockMaterialIn, blockMapColorIn);
        this.item = itemSupplier.get();
        this.metaConverter = AbstractMetaMapper.get(this, metaHelper);
        this.blockState = this.metaConverter.createBlockState();
        this.setDefaultState(this.getBlockState().getBaseState());
    }

    public AbstractModBlock(Supplier<ITEM> itemSupplier, Material materialIn, MetaHelper metaHelper) {
        super(materialIn);
        this.item = itemSupplier.get();
        this.metaConverter = AbstractMetaMapper.get(this, metaHelper);
        this.blockState = this.metaConverter.createBlockState();
        this.setDefaultState(this.getBlockState().getBaseState());
    }

    public AbstractModBlock(Material blockMaterialIn, MapColor blockMapColorIn, MetaHelper metaHelper) {
        super(blockMaterialIn, blockMapColorIn);
        this.item = this.createItem(this.getBlock());
        this.metaConverter = AbstractMetaMapper.get(this, metaHelper);
        this.blockState = this.metaConverter.createBlockState();
        this.setDefaultState(this.getBlockState().getBaseState());
    }

    public AbstractModBlock(Material materialIn, MetaHelper metaHelper) {
        super(materialIn);
        this.item = this.createItem(this.getBlock());
        this.metaConverter = AbstractMetaMapper.get(this, metaHelper);
        this.blockState = this.metaConverter.createBlockState();
        this.setDefaultState(this.getBlockState().getBaseState());
    }

    public AbstractModBlock(Function<AbstractModBlock<BLOCK, ITEM>, ITEM> itemFunction, Material blockMaterialIn, MapColor blockMapColorIn, IProperty<?>[] metaProperties, IProperty<?>... nonMetaProperties) {
        this(itemFunction, blockMaterialIn, blockMapColorIn, MetaHelper.with(metaProperties, nonMetaProperties));
    }

    public AbstractModBlock(Function<AbstractModBlock<BLOCK, ITEM>, ITEM> itemFunction, Material materialIn, IProperty<?>[] metaProperties, IProperty<?>... nonMetaProperties) {
        this(itemFunction, materialIn, MetaHelper.with(metaProperties, nonMetaProperties));
    }

    public AbstractModBlock(Supplier<ITEM> itemSupplier, Material blockMaterialIn, MapColor blockMapColorIn, IProperty<?>[] metaProperties, IProperty<?>... nonMetaProperties) {
        this(itemSupplier, blockMaterialIn, blockMapColorIn, MetaHelper.with(metaProperties, nonMetaProperties));
    }

    public AbstractModBlock(Supplier<ITEM> itemSupplier, Material materialIn, IProperty<?>[] metaProperties, IProperty<?>... nonMetaProperties) {
        this(itemSupplier, materialIn, MetaHelper.with(metaProperties, nonMetaProperties));
    }

    public AbstractModBlock(Material blockMaterialIn, MapColor blockMapColorIn, IProperty<?>[] metaProperties, IProperty<?>... nonMetaProperties) {
        this(blockMaterialIn, blockMapColorIn, MetaHelper.with(metaProperties, nonMetaProperties));
    }

    public AbstractModBlock(Material materialIn, IProperty<?>[] metaProperties, IProperty<?>... nonMetaProperties) {
        this(materialIn, MetaHelper.with(metaProperties, nonMetaProperties));
    }

    public AbstractModBlock(Function<AbstractModBlock<BLOCK, ITEM>, ITEM> itemFunction, Material blockMaterialIn, MapColor blockMapColorIn, IProperty<?>... metaProperties) {
        this(itemFunction, blockMaterialIn, blockMapColorIn, MetaHelper.withMeta(metaProperties));
    }

    public AbstractModBlock(Function<AbstractModBlock<BLOCK, ITEM>, ITEM> itemFunction, Material materialIn, IProperty<?>... metaProperties) {
        this(itemFunction, materialIn, MetaHelper.withMeta(metaProperties));
    }

    public AbstractModBlock(Supplier<ITEM> itemSupplier, Material blockMaterialIn, MapColor blockMapColorIn, IProperty<?>... metaProperties) {
        this(itemSupplier, blockMaterialIn, blockMapColorIn, MetaHelper.withMeta(metaProperties));
    }

    public AbstractModBlock(Supplier<ITEM> itemSupplier, Material materialIn, IProperty<?>... metaProperties) {
        this(itemSupplier, materialIn, MetaHelper.withMeta(metaProperties));
    }

    public AbstractModBlock(Material blockMaterialIn, MapColor blockMapColorIn, IProperty<?>... metaProperties) {
        this(blockMaterialIn, blockMapColorIn, MetaHelper.withMeta(metaProperties));
    }

    public AbstractModBlock(Material materialIn, IProperty<?>... metaProperties) {
        this(materialIn, MetaHelper.withMeta(metaProperties));
    }

    @Override
    @Nonnull
    public BLOCK setLightOpacity(int opacity) {
        super.setLightOpacity(opacity);
        return this.getBlock();
    }

    @Override
    @Nonnull
    public BLOCK setLightLevel(float value) {
        super.setLightLevel(value);
        return this.getBlock();
    }

    @Override
    @Nonnull
    public BLOCK setBlockUnbreakable() {
        super.setBlockUnbreakable();
        return this.getBlock();
    }

    @Override
    @Nonnull
    public BLOCK setTickRandomly(boolean shouldTick) {
        super.setTickRandomly(shouldTick);
        return this.getBlock();
    }

    @Override
    @Nonnull
    public BLOCK setUnlocalizedName(@Nonnull String name) {
        super.setUnlocalizedName(name);
        return this.getBlock();
    }

    @Override
    @Nonnull
    public BLOCK setHardness(float hardness) {
        super.setHardness(hardness);
        return this.getBlock();
    }

    @Override
    @Nonnull
    public BLOCK setResistance(float resistance) {
        super.setResistance(resistance);
        return this.getBlock();
    }

    @Override
    @Nonnull
    public BLOCK setSoundType(@Nonnull SoundType sound) {
        super.setSoundType(sound);
        return this.getBlock();
    }

    @Override
    @Nonnull
    public BLOCK disableStats() {
        super.disableStats();
        return this.getBlock();
    }

    @Override
    @Nonnull
    public BLOCK setCreativeTab(@Nonnull CreativeTabs tab) {
        super.setCreativeTab(tab);
        return this.getBlock();
    }

    @SuppressWarnings("deprecation")
    @Override
    @Nonnull
    public IBlockState getStateFromMeta(int meta) {
        return this.metaConverter.apply(meta);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return this.metaConverter.applyAsInt(state);
    }

    @Override
    @Nonnull
    public BlockStateContainer getBlockState() {
        return this.blockState;
    }

    @Override
    public ITEM getItem() {
        return this.item;
    }
}

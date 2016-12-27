package uk.co.mysterymayhem.gravitymod.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import uk.co.mysterymayhem.gravitymod.common.registries.IGravityModBlock;
import uk.co.mysterymayhem.mystlib.block.metaconverters.AbstractMetaMapper;
import uk.co.mysterymayhem.mystlib.block.metaconverters.AbstractMetaMapper.MetaHelper;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by Mysteryem on 2016-12-17.
 */
public abstract class AbstractModBlock<BLOCK extends AbstractModBlock<BLOCK, ITEM>, ITEM extends Item> extends Block implements IGravityModBlock<BLOCK, ITEM> {

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

    @SuppressWarnings("deprecation")
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.metaConverter.apply(meta);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return this.metaConverter.applyAsInt(state);
    }

    @Override
    public BlockStateContainer getBlockState() {
        return this.blockState;
    }

    @Override
    public ITEM getItem() {
        return this.item;
    }
}

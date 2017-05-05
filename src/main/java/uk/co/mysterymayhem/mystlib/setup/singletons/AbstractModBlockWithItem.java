package uk.co.mysterymayhem.mystlib.setup.singletons;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.item.Item;
import uk.co.mysterymayhem.mystlib.block.metaconverters.AbstractMetaMapper.MetaHelper;

import javax.annotation.Nonnull;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by Mysteryem on 2016-12-17.
 */
@SuppressWarnings("unchecked")
public abstract class AbstractModBlockWithItem<BLOCK extends AbstractModBlockWithItem<BLOCK, ITEM>, ITEM extends Item> extends AbstractModBlock<BLOCK>
        implements
        IModBlockWithItem<BLOCK, ITEM> {

    protected final ITEM item;

    // I heard you like constructors

    public AbstractModBlockWithItem(Function<AbstractModBlockWithItem<BLOCK, ITEM>, ITEM> itemFunction, Material blockMaterialIn, MapColor blockMapColorIn, IProperty<?>[] metaProperties, IProperty<?>... nonMetaProperties) {
        this(itemFunction, blockMaterialIn, blockMapColorIn, MetaHelper.with(metaProperties, nonMetaProperties));
    }

    public AbstractModBlockWithItem(Function<AbstractModBlockWithItem<BLOCK, ITEM>, ITEM> itemFunction, Material blockMaterialIn, MapColor blockMapColorIn, MetaHelper metaHelper) {
        super(blockMaterialIn, blockMapColorIn);
        this.item = itemFunction.apply(this);
    }

    @Override
    @Nonnull
    public BlockStateContainer getBlockState() {
        return this.blockState;
    }

    public AbstractModBlockWithItem(Function<AbstractModBlockWithItem<BLOCK, ITEM>, ITEM> itemFunction, Material materialIn, IProperty<?>[] metaProperties, IProperty<?>... nonMetaProperties) {
        this(itemFunction, materialIn, MetaHelper.with(metaProperties, nonMetaProperties));
    }

    public AbstractModBlockWithItem(Function<AbstractModBlockWithItem<BLOCK, ITEM>, ITEM> itemFunction, Material materialIn, MetaHelper metaHelper) {
        super(materialIn, metaHelper);
        this.item = itemFunction.apply(this);
    }

    public AbstractModBlockWithItem(Supplier<ITEM> itemSupplier, Material blockMaterialIn, MapColor blockMapColorIn, IProperty<?>[] metaProperties, IProperty<?>... nonMetaProperties) {
        this(itemSupplier, blockMaterialIn, blockMapColorIn, MetaHelper.with(metaProperties, nonMetaProperties));
    }

    public AbstractModBlockWithItem(Supplier<ITEM> itemSupplier, Material blockMaterialIn, MapColor blockMapColorIn, MetaHelper metaHelper) {
        super(blockMaterialIn, blockMapColorIn, metaHelper);
        this.item = itemSupplier.get();
    }

    public AbstractModBlockWithItem(Supplier<ITEM> itemSupplier, Material materialIn, IProperty<?>[] metaProperties, IProperty<?>... nonMetaProperties) {
        this(itemSupplier, materialIn, MetaHelper.with(metaProperties, nonMetaProperties));
    }

    public AbstractModBlockWithItem(Supplier<ITEM> itemSupplier, Material materialIn, MetaHelper metaHelper) {
        super(materialIn, metaHelper);
        this.item = itemSupplier.get();
    }

    public AbstractModBlockWithItem(Material blockMaterialIn, MapColor blockMapColorIn, IProperty<?>[] metaProperties, IProperty<?>... nonMetaProperties) {
        this(blockMaterialIn, blockMapColorIn, MetaHelper.with(metaProperties, nonMetaProperties));
    }

    public AbstractModBlockWithItem(Material blockMaterialIn, MapColor blockMapColorIn, MetaHelper metaHelper) {
        super(blockMaterialIn, blockMapColorIn, metaHelper);
        this.item = this.createItem(this.getBlock());
    }

    public AbstractModBlockWithItem(Material materialIn, IProperty<?>[] metaProperties, IProperty<?>... nonMetaProperties) {
        this(materialIn, MetaHelper.with(metaProperties, nonMetaProperties));
    }

    public AbstractModBlockWithItem(Material materialIn, MetaHelper metaHelper) {
        super(materialIn, metaHelper);
        this.item = this.createItem(this.getBlock());
    }

    public AbstractModBlockWithItem(Function<AbstractModBlockWithItem<BLOCK, ITEM>, ITEM> itemFunction, Material blockMaterialIn, MapColor blockMapColorIn, IProperty<?>... metaProperties) {
        this(itemFunction, blockMaterialIn, blockMapColorIn, MetaHelper.withMeta(metaProperties));
    }

    public AbstractModBlockWithItem(Function<AbstractModBlockWithItem<BLOCK, ITEM>, ITEM> itemFunction, Material materialIn, IProperty<?>... metaProperties) {
        this(itemFunction, materialIn, MetaHelper.withMeta(metaProperties));
    }

    public AbstractModBlockWithItem(Supplier<ITEM> itemSupplier, Material blockMaterialIn, MapColor blockMapColorIn, IProperty<?>... metaProperties) {
        this(itemSupplier, blockMaterialIn, blockMapColorIn, MetaHelper.withMeta(metaProperties));
    }

    public AbstractModBlockWithItem(Supplier<ITEM> itemSupplier, Material materialIn, IProperty<?>... metaProperties) {
        this(itemSupplier, materialIn, MetaHelper.withMeta(metaProperties));
    }

    public AbstractModBlockWithItem(Material blockMaterialIn, MapColor blockMapColorIn, IProperty<?>... metaProperties) {
        this(blockMaterialIn, blockMapColorIn, MetaHelper.withMeta(metaProperties));
    }

    public AbstractModBlockWithItem(Material materialIn, IProperty<?>... metaProperties) {
        this(materialIn, MetaHelper.withMeta(metaProperties));
    }

    @Override
    public ITEM getItem() {
        return this.item;
    }

    public ITEM createItem(BLOCK block) {
        return null;
    }
}

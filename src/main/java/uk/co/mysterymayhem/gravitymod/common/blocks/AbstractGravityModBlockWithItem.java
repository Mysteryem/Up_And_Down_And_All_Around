package uk.co.mysterymayhem.gravitymod.common.blocks;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.item.Item;
import uk.co.mysterymayhem.gravitymod.common.registries.IGravityModCommon;
import uk.co.mysterymayhem.mystlib.block.metaconverters.AbstractMetaMapper;
import uk.co.mysterymayhem.mystlib.setup.singletons.AbstractModBlockWithItem;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by Mysteryem on 05/05/2017.
 */
public abstract class AbstractGravityModBlockWithItem<BLOCK extends AbstractGravityModBlockWithItem<BLOCK, ITEM>, ITEM extends Item> extends
        AbstractModBlockWithItem<BLOCK, ITEM> implements IGravityModCommon {
    public AbstractGravityModBlockWithItem(Function<AbstractModBlockWithItem<BLOCK, ITEM>, ITEM> abstractModBlockWithItemITEMFunction, Material blockMaterialIn, MapColor blockMapColorIn, IProperty<?>[] metaProperties, IProperty<?>[] nonMetaProperties) {
        super(abstractModBlockWithItemITEMFunction, blockMaterialIn, blockMapColorIn, metaProperties, nonMetaProperties);
    }

    public AbstractGravityModBlockWithItem(Function<AbstractModBlockWithItem<BLOCK, ITEM>, ITEM> abstractModBlockWithItemITEMFunction, Material blockMaterialIn, MapColor blockMapColorIn, AbstractMetaMapper.MetaHelper metaHelper) {
        super(abstractModBlockWithItemITEMFunction, blockMaterialIn, blockMapColorIn, metaHelper);
    }

    public AbstractGravityModBlockWithItem(Function<AbstractModBlockWithItem<BLOCK, ITEM>, ITEM> abstractModBlockWithItemITEMFunction, Material materialIn, IProperty<?>[] metaProperties, IProperty<?>[] nonMetaProperties) {
        super(abstractModBlockWithItemITEMFunction, materialIn, metaProperties, nonMetaProperties);
    }

    public AbstractGravityModBlockWithItem(Function<AbstractModBlockWithItem<BLOCK, ITEM>, ITEM> abstractModBlockWithItemITEMFunction, Material materialIn, AbstractMetaMapper.MetaHelper metaHelper) {
        super(abstractModBlockWithItemITEMFunction, materialIn, metaHelper);
    }

    public AbstractGravityModBlockWithItem(Supplier<ITEM> itemSupplier, Material blockMaterialIn, MapColor blockMapColorIn, IProperty<?>[] metaProperties, IProperty<?>[] nonMetaProperties) {
        super(itemSupplier, blockMaterialIn, blockMapColorIn, metaProperties, nonMetaProperties);
    }

    public AbstractGravityModBlockWithItem(Supplier<ITEM> itemSupplier, Material blockMaterialIn, MapColor blockMapColorIn, AbstractMetaMapper.MetaHelper metaHelper) {
        super(itemSupplier, blockMaterialIn, blockMapColorIn, metaHelper);
    }

    public AbstractGravityModBlockWithItem(Supplier<ITEM> itemSupplier, Material materialIn, IProperty<?>[] metaProperties, IProperty<?>[] nonMetaProperties) {
        super(itemSupplier, materialIn, metaProperties, nonMetaProperties);
    }

    public AbstractGravityModBlockWithItem(Supplier<ITEM> itemSupplier, Material materialIn, AbstractMetaMapper.MetaHelper metaHelper) {
        super(itemSupplier, materialIn, metaHelper);
    }

    public AbstractGravityModBlockWithItem(Material blockMaterialIn, MapColor blockMapColorIn, IProperty<?>[] metaProperties, IProperty<?>[] nonMetaProperties) {
        super(blockMaterialIn, blockMapColorIn, metaProperties, nonMetaProperties);
    }

    public AbstractGravityModBlockWithItem(Material blockMaterialIn, MapColor blockMapColorIn, AbstractMetaMapper.MetaHelper metaHelper) {
        super(blockMaterialIn, blockMapColorIn, metaHelper);
    }

    public AbstractGravityModBlockWithItem(Material materialIn, IProperty<?>[] metaProperties, IProperty<?>[] nonMetaProperties) {
        super(materialIn, metaProperties, nonMetaProperties);
    }

    public AbstractGravityModBlockWithItem(Material materialIn, AbstractMetaMapper.MetaHelper metaHelper) {
        super(materialIn, metaHelper);
    }

    public AbstractGravityModBlockWithItem(Function<AbstractModBlockWithItem<BLOCK, ITEM>, ITEM> abstractModBlockWithItemITEMFunction, Material blockMaterialIn, MapColor blockMapColorIn, IProperty<?>[] metaProperties) {
        super(abstractModBlockWithItemITEMFunction, blockMaterialIn, blockMapColorIn, metaProperties);
    }

    public AbstractGravityModBlockWithItem(Function<AbstractModBlockWithItem<BLOCK, ITEM>, ITEM> abstractModBlockWithItemITEMFunction, Material materialIn, IProperty<?>[] metaProperties) {
        super(abstractModBlockWithItemITEMFunction, materialIn, metaProperties);
    }

    public AbstractGravityModBlockWithItem(Supplier<ITEM> itemSupplier, Material blockMaterialIn, MapColor blockMapColorIn, IProperty<?>[] metaProperties) {
        super(itemSupplier, blockMaterialIn, blockMapColorIn, metaProperties);
    }

    public AbstractGravityModBlockWithItem(Supplier<ITEM> itemSupplier, Material materialIn, IProperty<?>[] metaProperties) {
        super(itemSupplier, materialIn, metaProperties);
    }

    public AbstractGravityModBlockWithItem(Material blockMaterialIn, MapColor blockMapColorIn, IProperty<?>[] metaProperties) {
        super(blockMaterialIn, blockMapColorIn, metaProperties);
    }

    public AbstractGravityModBlockWithItem(Material materialIn, IProperty<?>[] metaProperties) {
        super(materialIn, metaProperties);
    }
}

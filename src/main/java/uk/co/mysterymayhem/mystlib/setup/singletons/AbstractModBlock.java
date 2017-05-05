package uk.co.mysterymayhem.mystlib.setup.singletons;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import uk.co.mysterymayhem.mystlib.block.metaconverters.AbstractMetaMapper;

import javax.annotation.Nonnull;

/**
 * Created by Mysteryem on 05/05/2017.
 */
public abstract class AbstractModBlock<BLOCK extends AbstractModBlock<BLOCK>> extends Block implements IModBlock<BLOCK> {
    // hiding the field in Block.class on purpose
    protected final BlockStateContainer blockState;
    protected final AbstractMetaMapper<AbstractModBlock<BLOCK>> metaConverter;

    // I heard you like constructors

    public AbstractModBlock(Material blockMaterialIn, MapColor blockMapColorIn, IProperty<?>[] metaProperties, IProperty<?>... nonMetaProperties) {
        this(blockMaterialIn, blockMapColorIn, AbstractMetaMapper.MetaHelper.with(metaProperties, nonMetaProperties));
    }

    public AbstractModBlock(Material blockMaterialIn, MapColor blockMapColorIn, AbstractMetaMapper.MetaHelper metaHelper) {
        super(blockMaterialIn, blockMapColorIn);
        this.metaConverter = AbstractMetaMapper.get(this, metaHelper);
        this.blockState = this.metaConverter.createBlockState();
        this.setDefaultState(this.getBlockState().getBaseState());
    }

    @Override
    @Nonnull
    public BlockStateContainer getBlockState() {
        return this.blockState;
    }

    public AbstractModBlock(Material materialIn, IProperty<?>[] metaProperties, IProperty<?>... nonMetaProperties) {
        this(materialIn, AbstractMetaMapper.MetaHelper.with(metaProperties, nonMetaProperties));
    }

    public AbstractModBlock(Material materialIn, AbstractMetaMapper.MetaHelper metaHelper) {
        super(materialIn);
        this.metaConverter = AbstractMetaMapper.get(this, metaHelper);
        this.blockState = this.metaConverter.createBlockState();
        this.setDefaultState(this.getBlockState().getBaseState());
    }

    public AbstractModBlock(Material blockMaterialIn, MapColor blockMapColorIn, IProperty<?>... metaProperties) {
        this(blockMaterialIn, blockMapColorIn, AbstractMetaMapper.MetaHelper.withMeta(metaProperties));
    }

    public AbstractModBlock(Material materialIn, IProperty<?>... metaProperties) {
        this(materialIn, AbstractMetaMapper.MetaHelper.withMeta(metaProperties));
    }

    @Override
    @Nonnull
    public BLOCK disableStats() {
        super.disableStats();
        return this.getBlock();
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return this.metaConverter.applyAsInt(state);
    }

    @SuppressWarnings("deprecation")
    @Override
    @Nonnull
    public IBlockState getStateFromMeta(int meta) {
        return this.metaConverter.apply(meta);
    }

    @Override
    @Nonnull
    public BLOCK setBlockUnbreakable() {
        super.setBlockUnbreakable();
        return this.getBlock();
    }

    @Override
    @Nonnull
    public BLOCK setCreativeTab(@Nonnull CreativeTabs tab) {
        super.setCreativeTab(tab);
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
    public BLOCK setLightLevel(float value) {
        super.setLightLevel(value);
        return this.getBlock();
    }

    @Override
    @Nonnull
    public BLOCK setLightOpacity(int opacity) {
        super.setLightOpacity(opacity);
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
}

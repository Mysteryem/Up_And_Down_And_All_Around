package uk.co.mysterymayhem.mystlib.block.metaconverters;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;

/**
 * Created by Mysteryem on 2016-12-17.
 */
public abstract class AbstractMetaMapper<BLOCK extends Block> implements ToIntFunction<IBlockState>, IntFunction<IBlockState> {

    public static final IProperty<?>[] EMPTY_PROPERTY_ARRAY = new IProperty<?>[0];
    // Used when calling this.block.getDefaultState() when creating an IBlockState from meta
    protected final BLOCK block;
    protected final IProperty<?>[] metaProperties;
    protected final IProperty<?>[] allProperties;

    public AbstractMetaMapper(BLOCK block, IProperty<?>[] metaProperties, IProperty<?>[] allProperties) {
        this.block = block;
        this.metaProperties = metaProperties;
        this.allProperties = allProperties;
    }

    public static <BLOCK extends Block> AbstractMetaMapper<BLOCK> get(BLOCK block, IProperty<?>... metaProperties) {
        return get(block, metaProperties, metaProperties);
    }

    private static <BLOCK extends Block> AbstractMetaMapper<BLOCK> get(BLOCK block, IProperty<?>[] metaProperties, IProperty<?>[] allProperties) {
        switch (metaProperties.length) {
            case 0:
                return new ZeroMetaMapper<>(block, allProperties);
            case 1:
                return new SingleMetaMapper<>(block, metaProperties[0], allProperties);
            default:
                return new MultiMetaMapper<>(block, metaProperties, allProperties);
        }
    }

    public static <BLOCK extends Block> AbstractMetaMapper<BLOCK> get(BLOCK block, MetaHelper helper) {
        ArrayList<IProperty<?>> orderedMetaProperties = helper.orderedMetaProperties;
        HashSet<IProperty<?>> allProperties = helper.allProperties;
        return get(block, orderedMetaProperties.toArray(new IProperty<?>[orderedMetaProperties.size()]), allProperties.toArray(new IProperty<?>[allProperties.size()]));
    }

    public BlockStateContainer createBlockState() {
        return new BlockStateContainer(this.getBlock(), this.allProperties);
    }

    public BLOCK getBlock() {
        return this.block;
    }

    public IProperty<?>[] getAllProperties() {
        return this.allProperties;
    }

    public IProperty<?>[] getMetaProperties() {
        return this.metaProperties;
    }

    public static class MetaHelper {
        private final ArrayList<IProperty<?>> orderedMetaProperties = new ArrayList<>();
        private final HashSet<IProperty<?>> allProperties = new HashSet<>();

        public static MetaHelper newHelper() {
            return new MetaHelper();
        }

        public static MetaHelper with(IProperty<?>[] metaProperties, IProperty<?>... nonMetaProperties) {
            return new MetaHelper().addMeta(metaProperties).addNonMeta(nonMetaProperties);
        }

        public MetaHelper addNonMeta(IProperty<?>... nonMetaProperties) {
            for (IProperty<?> property : nonMetaProperties) {
                this.allProperties.add(property);
            }
            return this;
        }

        public MetaHelper addMeta(IProperty<?>... metaProperties) {
            for (IProperty<?> property : metaProperties) {
                this.orderedMetaProperties.add(property);
            }
            this.allProperties.addAll(this.orderedMetaProperties);
            return this;
        }

        public static MetaHelper withMeta(IProperty<?>... metaProperties) {
            return new MetaHelper().addMeta(metaProperties);
        }

        public static MetaHelper withNonMeta(IProperty<?>... nonMetaProperties) {
            return new MetaHelper().addNonMeta(nonMetaProperties);
        }

    }
}

package uk.co.mysterymayhem.mystlib.block.metaconverters;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;

/**
 * Created by Mysteryem on 2016-12-17.
 */
public class ZeroMetaMapper<BLOCK extends Block> extends AbstractMetaMapper<BLOCK> {

    ZeroMetaMapper(BLOCK block, IProperty<?>... nonMetaProperties) {
        super(block, EMPTY_PROPERTY_ARRAY, nonMetaProperties);
    }

    @Override
    public IBlockState apply(int value) {
        return this.block.getDefaultState();
    }

    @Override
    public int applyAsInt(IBlockState value) {
        return 0;
    }
}

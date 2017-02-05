package uk.co.mysterymayhem.mystlib.block.metaconverters;

import gnu.trove.impl.Constants;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.fml.common.FMLLog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by Mysteryem on 2016-12-16.
 */
public class SingleMetaMapper<BLOCK extends Block, PROPERTY extends IProperty<VALUE>, VALUE extends Comparable<VALUE>> extends AbstractMetaMapper<BLOCK> {

    private final PROPERTY property;

    private final VALUE[] indexToValueMap;
    private final TObjectIntHashMap<VALUE> valueToIndexMap = new TObjectIntHashMap<>(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, -1);

    @SuppressWarnings("unchecked")
    SingleMetaMapper(BLOCK block, PROPERTY property, IProperty<?>[] allProperties) {
        super(block, new IProperty<?>[]{property}, allProperties);
        this.property = property;

        Collection<VALUE> allowedValues = property.getAllowedValues();

        ArrayList<VALUE> list = new ArrayList<>(allowedValues);
        Collections.sort(list);
        for (int i = 0; i < list.size(); i++) {
            this.valueToIndexMap.put(list.get(i), i);
        }

        // Technically, the VALUE array is actually a Comparable array
        this.indexToValueMap = list.toArray((VALUE[])new Comparable<?>[list.size()]);
    }

    @Override
    public IBlockState apply(int meta) {
        if (meta < 0 || meta >= indexToValueMap.length) {
            FMLLog.warning("MystLib: SingleMetaMapper: Invalid meta passed to 'getStateFromMeta' delegate (SingleMetaMapper::apply). Using default state.");
            return this.block.getDefaultState();
        }
        return this.block.getDefaultState().withProperty(this.property, indexToValueMap[meta]);
    }

    @Override
    public int applyAsInt(IBlockState state) {
        VALUE value = state.getValue(this.property);
        return this.valueToIndexMap.get(value);
    }
}

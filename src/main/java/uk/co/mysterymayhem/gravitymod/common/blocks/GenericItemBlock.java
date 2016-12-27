package uk.co.mysterymayhem.gravitymod.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;

/**
 * Created by Mysteryem on 2016-12-15.
 */
public class GenericItemBlock<T extends Block> extends ItemBlock {

    @SuppressWarnings("unchecked")
    private final T block = (T)super.block;

    public GenericItemBlock(T block) {
        super(block);
    }

    @Override
    public T getBlock() {
        return block;
    }
}

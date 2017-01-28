package uk.co.mysterymayhem.gravitymod.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;

/**
 * Created by Mysteryem on 2016-12-15.
 */
public class GenericItemBlock<T extends Block> extends ItemBlock {

    private final T block;

    public GenericItemBlock(T block) {
        super(block);
        this.block = block;
    }

    @Override
    public T getBlock() {
        return block;
    }
}

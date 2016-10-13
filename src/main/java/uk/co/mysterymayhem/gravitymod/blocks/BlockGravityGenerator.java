package uk.co.mysterymayhem.gravitymod.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;

/**
 * Created by Mysteryem on 2016-10-10.
 */
public class BlockGravityGenerator extends Block {
    public BlockGravityGenerator(Material blockMaterialIn, MapColor blockMapColorIn) {
        super(blockMaterialIn, blockMapColorIn);
    }

    public BlockGravityGenerator(Material materialIn) {
        super(materialIn);
    }
}

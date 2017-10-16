package uk.co.mysterymayhem.gravitymod.common.blocks;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemBlock;
import uk.co.mysterymayhem.gravitymod.common.registries.IGravityModCommon;
import uk.co.mysterymayhem.mystlib.setup.singletons.AbstractModBlockWithItem;

/**
 * Created by Mysteryem on 2017-01-26.
 */
public class BlockRestabilisedGravityDust extends AbstractModBlockWithItem<BlockRestabilisedGravityDust, ItemBlock> implements IGravityModCommon {
    public BlockRestabilisedGravityDust() {
        super(Material.ROCK, MapColor.PURPLE);
        this.setHardness(5.0F);
        this.setResistance(10.0F);
        this.setSoundType(SoundType.STONE);
    }

    @Override
    public ItemBlock createItem(BlockRestabilisedGravityDust block) {
        return new ItemBlock(this);
    }

    @Override
    public String getModObjectName() {
        return "restabilisedgravitydust_block";
    }
}

package uk.co.mysterymayhem.gravitymod.common.blocks;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import uk.co.mysterymayhem.gravitymod.common.registries.StaticItems;

import java.util.Arrays;

/**
 * Created by Mysteryem on 2017-01-26.
 */
public class BlockRestabilisedGravityDust extends AbstractModBlock<BlockRestabilisedGravityDust, ItemBlock> {
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
    public String getName() {
        return "restabilisedgravitydust_block";
    }

    @Override
    public void postInit() {
        Object[] inputs = new Object[9];
        Arrays.fill(inputs, StaticItems.RESTABILISED_GRAVITY_DUST);
        GameRegistry.addShapelessRecipe(new ItemStack(this), inputs);
        GameRegistry.addShapelessRecipe(new ItemStack(StaticItems.RESTABILISED_GRAVITY_DUST, 9), this);
    }
}

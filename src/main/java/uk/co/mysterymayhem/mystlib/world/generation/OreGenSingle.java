package uk.co.mysterymayhem.mystlib.world.generation;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.event.terraingen.OreGenEvent;
import net.minecraftforge.event.terraingen.TerrainGen;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;
import java.util.Random;

/**
 * Created by Mysteryem on 28/02/2017.
 */
public class OreGenSingle extends BlockStateMappedGenerator {
    public OreGenSingle(IBlockState... mapEntries) {
        super(false, mapEntries);
    }

    public OreGenSingle(Map<IBlockState, IBlockState> blockToBlockStateFunctionMap) {
        super(false, blockToBlockStateFunctionMap);
    }

    public OreGenSingle(Helper helper) {
        super(false, helper);
    }

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        this.generate(world, random, this.choosePosFromChunk(random, chunkX, chunkZ));
    }

    @ParametersAreNonnullByDefault
    @Override
    public boolean generate(World worldIn, Random rand, BlockPos position) {
        if (TerrainGen.generateOre(worldIn, rand, this, position, OreGenEvent.GenerateMinable.EventType.CUSTOM)) {
            IBlockState state = worldIn.getBlockState(position);
            if (state.getBlock().isReplaceableOreGen(state, worldIn, position, truePredicate)) {
                IBlockState newState = this.blockToBlockStateFunctionMap.get(state);
                if (newState != null) {
                    this.setBlockAndNotifyAdequately(worldIn, position, newState);
                }
            }
        }
        return true;
    }
}

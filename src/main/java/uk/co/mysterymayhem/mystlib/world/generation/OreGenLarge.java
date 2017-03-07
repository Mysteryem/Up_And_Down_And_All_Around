package uk.co.mysterymayhem.mystlib.world.generation;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.event.terraingen.OreGenEvent;
import net.minecraftforge.event.terraingen.TerrainGen;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;
import java.util.Random;

/**
 * Created by Mysteryem on 25/02/2017.
 */
public class OreGenLarge extends BlockStateMappedGenerator {

    private final int numBlocks;

    public OreGenLarge(IBlockState... mapEntries) {
        this(9, mapEntries);
    }

    public OreGenLarge(int numBlocks, IBlockState... mapEntries) {
        super(false, mapEntries);
        this.numBlocks = numBlocks;
    }

    public OreGenLarge(Map<IBlockState, IBlockState> blockToBlockStateFunctionMap) {
        this(9, blockToBlockStateFunctionMap);
    }

    public OreGenLarge(int numBlocks, Map<IBlockState, IBlockState> blockToBlockStateFunctionMap) {
        super(false, blockToBlockStateFunctionMap);
        this.numBlocks = numBlocks;
    }

    public OreGenLarge(Helper helper) {
        this(9, helper);
    }

    public OreGenLarge(int numBlocks, Helper helper) {
        super(false, helper);
        this.numBlocks = numBlocks;
    }

    public boolean generate(int numberOfBlocks, World worldIn, Random rand, int chunkX, int chunkZ) {
        return this.generate(numberOfBlocks, worldIn, rand, this.choosePosFromChunk(rand, chunkX, chunkZ));
    }

    // Modified from WorldGenMinable, numberOfBlocks at 1 -> no ore, at 2 -> 4 blocks
    public boolean generate(int numberOfBlocks, World worldIn, Random rand, BlockPos position) {
        if (TerrainGen.generateOre(worldIn, rand, this, position, OreGenEvent.GenerateMinable.EventType.CUSTOM)) {
            float zeroToPi = rand.nextFloat() * (float)Math.PI; // [0,PI]

            // Note that we have removed the +8 to both xPos and zPos, as that is nor handled in this::choosePosFromChunk
            int xPos = position.getX();
            int zPos = position.getZ();
            int yPos = position.getY();

            // >= 0
            float sinF = MathHelper.sin(zeroToPi); // /\ 0 -> 1 -> 0 for 0 -> PI

            //
            float cosF = MathHelper.cos(zeroToPi); // '-, 1 -> 0 -> -1 for 0 -> PI

            // Start and end X are effectively mirrored about xPos + 8.
            double startX = xPos + sinF * numberOfBlocks / 8.0F;
            double endX = xPos - sinF * numberOfBlocks / 8.0F;
            // Start and end Z are effectively mirrored about zPos + 8.
            double startZ = zPos + cosF * numberOfBlocks / 8.0F;
            double endZ = zPos - cosF * numberOfBlocks / 8.0F;
//        double startY = yPos + rand.nextInt(3) - 2;
            double startY = yPos + rand.nextInt(3); //[ypos, ypos+2]
            double endY = yPos + rand.nextInt(3) - 2; //[ypos-2, ypos]

            for (int i = 0; i < numberOfBlocks; ++i) {
                float percentOfNumBlocks = (float)i / (float)numberOfBlocks;
                double interpXPos = startX + (endX - startX) * (double)percentOfNumBlocks;
                double interpYPos = startY + (endY - startY) * (double)percentOfNumBlocks;
                double interpZPos = startZ + (endZ - startZ) * (double)percentOfNumBlocks;
                double zeroTo16thNumBlocks = rand.nextDouble() * (double)numberOfBlocks / 16.0D;
                // Always greater than zero?
                double horizontalDouble = 0.5 * (MathHelper.sin((float)Math.PI * percentOfNumBlocks) + 1.0F * zeroTo16thNumBlocks + 1.0D);
                double verticalDouble = 0.5 * (MathHelper.sin((float)Math.PI * percentOfNumBlocks) + 1.0F * zeroTo16thNumBlocks + 1.0D);
                int xMin = MathHelper.floor_double(interpXPos - horizontalDouble);
                int yMin = MathHelper.floor_double(interpYPos - verticalDouble);
                int zMin = MathHelper.floor_double(interpZPos - horizontalDouble);
                int xMax = MathHelper.floor_double(interpXPos + horizontalDouble);
                int yMax = MathHelper.floor_double(interpYPos + verticalDouble);
                int zMax = MathHelper.floor_double(interpZPos + horizontalDouble);

                for (int x = xMin; x <= xMax; ++x) {
                    double d12 = (x + 0.5D - interpXPos) / horizontalDouble;

                    if (d12 * d12 < 1.0D) {
                        for (int y = yMin; y <= yMax; ++y) {
                            double d13 = (y + 0.5D - interpYPos) / verticalDouble;

                            if (d12 * d12 + d13 * d13 < 1.0D) {
                                for (int z = zMin; z <= zMax; ++z) {
                                    double d14 = ((double)z + 0.5D - interpZPos) / horizontalDouble;

                                    if (d12 * d12 + d13 * d13 + d14 * d14 < 1.0D) {
                                        BlockPos blockpos = new BlockPos(x, y, z);

                                        IBlockState state = worldIn.getBlockState(blockpos);
                                        Block block = state.getBlock();
                                        if (block.isReplaceableOreGen(state, worldIn, blockpos, truePredicate)) {
                                            IBlockState newState = this.blockToBlockStateFunctionMap.get(state);
                                            if (newState != null) {
                                                this.setBlockAndNotifyAdequately(worldIn, blockpos, newState);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return true;
    }

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        this.generate(world, random, this.choosePosFromChunk(random, chunkX, chunkZ));
    }

    @ParametersAreNonnullByDefault
    @Override
    public boolean generate(World worldIn, Random rand, BlockPos position) {
        return this.generate(this.getNumBlocks(), worldIn, rand, position);
    }

    public int getNumBlocks() {
        return this.numBlocks;
    }
}

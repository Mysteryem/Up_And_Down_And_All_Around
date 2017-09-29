package uk.co.mysterymayhem.mystlib.world.generation;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.*;

/**
 * Created by Mysteryem on 25/02/2017.
 */
public abstract class BlockStateMappedGenerator extends WorldGenerator implements IWorldGenerator {

    protected static final com.google.common.base.Predicate<IBlockState> truePredicate = com.google.common.base.Predicates.alwaysTrue();
    // We use an IdentityHashMap as the underlying map
    protected final Map<IBlockState, IBlockState> blockToBlockStateFunctionMap;
    private final boolean notify;

    public BlockStateMappedGenerator(boolean notify, IBlockState... mapEntries) {
        this(notify, buildMapFromArray(mapEntries));
    }

    public BlockStateMappedGenerator(boolean notify, Map<IBlockState, IBlockState> blockToBlockStateFunctionMap) {
        super(notify);
        // IdentityHashMap should be good since blockstates can be compared with '=='
        this.blockToBlockStateFunctionMap = Collections.unmodifiableMap(new IdentityHashMap<>(blockToBlockStateFunctionMap));
        this.notify = notify;
    }

    private static Map<IBlockState, IBlockState> buildMapFromArray(IBlockState... mapEntries) {
        if (mapEntries.length % 2 != 0) {
            throw new IllegalArgumentException("Number of IBlockState arguments must be even");
        }
        HashMap<IBlockState, IBlockState> map = new HashMap<>();
        for (int i = 0; i < mapEntries.length; i += 2) {
            map.put(mapEntries[i], mapEntries[i + 1]);
        }
        return map;
    }

    public BlockStateMappedGenerator(boolean notify, Helper helper) {
        this(notify, helper.blockToBlockStateFunctionMap);
    }

    public boolean tryReplaceBlockAt(World world, BlockPos pos) {
        IBlockState blockState = world.getBlockState(pos);
        IBlockState replacement = this.getReplacementBlockState(blockState);
        return replacement != null
                && blockState.getBlock().isReplaceableOreGen(blockState, world, pos, truePredicate)
                && world.setBlockState(pos, replacement, this.notify ? 3 : 2);
    }

    public IBlockState getReplacementBlockState(IBlockState otherState) {
        return this.blockToBlockStateFunctionMap.get(otherState);
    }

    public boolean canReplaceBlockState(IBlockState otherState) {
        return this.blockToBlockStateFunctionMap.containsKey(otherState);
    }

    public BlockPos choosePosFromChunk(Random random, int chunkX, int chunkZ) {
        return this.choosePosFromChunkOrigin(random, new BlockPos(chunkX * 16, 0, chunkZ * 16));
    }

    public BlockPos choosePosFromChunkOrigin(Random random, BlockPos chunkPos) {
        return chunkPos
                .add(
                        random.nextInt(16) + 8,
                        random.nextInt(this.getMaxSpawnHeight() - this.getMinSpawnHeight()) + this.getMinSpawnHeight(),
                        random.nextInt(16) + 8);
    }

    public int getMaxSpawnHeight() {
        return 255;
    }

    public int getMinSpawnHeight() {
        return 1;
    }

    public void generate(int genTries, Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        this.generate(genTries, random, chunkX, chunkZ, world);
    }

    public void generate(int genTries, Random random, int chunkX, int chunkZ, World world) {
        BlockPos chunkOrigin = new BlockPos(chunkX * 16, 0, chunkZ * 16);
        for (int i = 0; i < genTries; i++) {
            this.generate(world, random, this.choosePosFromChunkOrigin(random, chunkOrigin));
        }
    }

    public static class Helper {
        private final Map<IBlockState, IBlockState> blockToBlockStateFunctionMap = new HashMap<>();

        public static Helper withPair(IBlockState origState, IBlockState replacement) {
            return new Helper().addPair(origState, replacement);
        }

        public Helper addPair(IBlockState origState, IBlockState replacement) {
            this.blockToBlockStateFunctionMap.put(origState, replacement);
            return this;
        }
    }
}

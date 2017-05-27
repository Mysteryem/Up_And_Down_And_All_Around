package uk.co.mysterymayhem.gravitymod.common.world.generation.ore;

import net.minecraft.init.Blocks;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.IWorldGenerator;
import uk.co.mysterymayhem.gravitymod.common.blocks.BlockGravityOre.Type;
import uk.co.mysterymayhem.gravitymod.common.config.ConfigHandler;
import uk.co.mysterymayhem.mystlib.world.generation.BlockStateMappedGenerator;
import uk.co.mysterymayhem.mystlib.world.generation.OreGenLarge;
import uk.co.mysterymayhem.mystlib.world.generation.OreGenSingle;

import java.util.Random;
import java.util.WeakHashMap;

import static uk.co.mysterymayhem.gravitymod.common.blocks.BlockGravityOre.TYPE;
import static uk.co.mysterymayhem.gravitymod.common.registries.StaticBlocks.GRAVITY_ORE;

/**
 * Created by Mysteryem on 23/02/2017.
 */
public class Generator implements IWorldGenerator {

    public static final Generator INSTANCE = new Generator();

    private static final OreGenSingle overworldGravityOreGen = new OreGenSingle(
            Blocks.STONE.getDefaultState(),
            GRAVITY_ORE.getDefaultState().withProperty(TYPE, Type.STONE));

    private static final OreGenSingle netherGravityOreGen = new OreGenSingle(
            Blocks.NETHERRACK.getDefaultState(),
            GRAVITY_ORE.getDefaultState().withProperty(TYPE, Type.NETHERRACK)) {
        @Override
        public int getMaxSpawnHeight() {
            return 127;
        }
    };

    private static final OreGenLarge endGravityOreGen = new OreGenLarge(
            5,
            Blocks.END_STONE.getDefaultState(),
            GRAVITY_ORE.getDefaultState().withProperty(TYPE, Type.END_STONE));

    private static final OreGenSingle modWorldGravityOreGen = new OreGenSingle(BlockStateMappedGenerator.Helper
            .withPair(Blocks.STONE.getDefaultState(), GRAVITY_ORE.getDefaultState().withProperty(TYPE, Type.STONE))
            .addPair(Blocks.NETHERRACK.getDefaultState(), GRAVITY_ORE.getDefaultState().withProperty(TYPE, Type.NETHERRACK))
            .addPair(Blocks.END_STONE.getDefaultState(), GRAVITY_ORE.getDefaultState().withProperty(TYPE, Type.END_STONE)));

    // Weak for safety, we don't want to keep strong references to Worlds around
    // And it will help with cleanup (GC) I suppose?
    //
    // Might it be worth using a TIntObjectHashMap<Boolean> instead, keyed on the world.provider.getDimension()?
    private static final WeakHashMap<World, Boolean> VALID_WORLD_CACHE = new WeakHashMap<>();
    private static final boolean DEBUG_GEN = false;

    private static boolean isWorldValidForGeneration(World world) {
        Boolean worldIsValid = VALID_WORLD_CACHE.get(world);
        if (worldIsValid == null) {
            if (world.getWorldType() == WorldType.FLAT) {
                // No generating in flat worlds
                VALID_WORLD_CACHE.put(world, false);
                return false;
            }
            if (ConfigHandler.oreGenDimensionListIsBlackList) {
                if (ConfigHandler.oreGenDimensionIDs.contains(world.provider.getDimension())) {
                    // The dimension is on the blacklist, so it's invalid
                    VALID_WORLD_CACHE.put(world, false);
                    return false;
                }
            }
            else {
                // dimension ID list is a whitelist
                if (!ConfigHandler.oreGenDimensionIDs.contains(world.provider.getDimension())) {
                    // The dimension is not on the whitelist, so it's invalid
                    VALID_WORLD_CACHE.put(world, false);
                    return false;
                }
            }
            // The dimension is valid, so update the cache
            VALID_WORLD_CACHE.put(world, true);
            return true;
        }
        else {
            // Return cached value
            return worldIsValid;
        }
    }

    public void generate(Random random, int chunkX, int chunkZ, World world) {
        if (isWorldValidForGeneration(world)) {
//        WorldType worldType = world.getWorldType();
            switch (world.provider.getDimension()) {
                case 0://overworld
                    // 20 blocks per chunk [1,255]
                    overworldGravityOreGen.generate(20, random, chunkX, chunkZ, world);
                    break;
                case -1://nether
                    // 20 blocks per chunk [1,127]
                    netherGravityOreGen.generate(20, random, chunkX, chunkZ, world);
                    break;
                case 1://the end
                    // Would guess maybe ~30 blocks per chunk [1,255]
                    endGravityOreGen.generate(3, random, chunkX, chunkZ, world);
                    break;
                default://unknown
                    // 20 blocks per chunk [1,255]
                    modWorldGravityOreGen.generate(20, random, chunkX, chunkZ, world);
                    break;
            }
            if (DEBUG_GEN) {
                FMLLog.info("Completed mod population of " + new ChunkRef(chunkX, chunkZ, world));
            }
        }
    }

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        this.generate(random, chunkX, chunkZ, world);
    }

    /**
     * Only used to make logging easier
     */
    private static class ChunkRef extends Vec3i {

        public ChunkRef(Chunk chunk, World world) {
            this(chunk, world.provider);
        }

        public ChunkRef(Chunk chunk, WorldProvider provider) {
            this(chunk.x, chunk.z, provider);
        }

        public ChunkRef(int chunkX, int chunkZ, WorldProvider provider) {
            this(chunkX, chunkZ, provider.getDimension());
        }

        public ChunkRef(int xIn, int zIn, int dimensionIn) {
            super(xIn, dimensionIn, zIn);
        }

        public ChunkRef(ChunkPos chunkPos, World world) {
            this(chunkPos, world.provider);
        }

        public ChunkRef(ChunkPos chunkPos, WorldProvider provider) {
            this(chunkPos.x, chunkPos.z, provider);
        }

        public ChunkRef(int chunkX, int chunkZ, World world) {
            this(chunkX, chunkZ, world.provider);
        }

        @Override
        public String toString() {
            return "[" + this.getX() + ", " + this.getZ() + "][" + this.getDimensionID() + "]";
        }

        public int getDimensionID() {
            return super.getY();
        }

        @Deprecated
        @Override
        public int getY() {return super.getY();}

        @Deprecated
        @Override
        public Vec3i crossProduct(Vec3i vec) {return super.crossProduct(vec);}

        @Deprecated
        @Override
        public double getDistance(int xIn, int yIn, int zIn) {return super.getDistance(xIn, yIn, zIn);}

        @Deprecated
        @Override
        public double distanceSq(double toX, double toY, double toZ) {return super.distanceSq(toX, toY, toZ);}

        @Deprecated
        @Override
        public double distanceSqToCenter(double xIn, double yIn, double zIn) {return super.distanceSqToCenter(xIn, yIn, zIn);}

        @Deprecated
        @Override
        public double distanceSq(Vec3i to) {return super.distanceSq(to);}
    }
}

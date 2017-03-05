package uk.co.mysterymayhem.gravitymod.common.world.generation.ore;

import com.google.common.util.concurrent.ListenableFutureTask;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import uk.co.mysterymayhem.gravitymod.common.blocks.BlockGravityOre.Type;
import uk.co.mysterymayhem.gravitymod.common.config.ConfigHandler;
import uk.co.mysterymayhem.mystlib.world.generation.BlockStateMappedGenerator;
import uk.co.mysterymayhem.mystlib.world.generation.OreGenLarge;
import uk.co.mysterymayhem.mystlib.world.generation.OreGenSingle;

import java.util.Collections;
import java.util.Random;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.Executors;

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
    // Key for main tag compound store in chunk nbt
    private static final String COMPOUND_TAG_KEY = "upanddownretrogen";
    // Key for 'gravity ore' generation status
    private static final String GRAVITY_ORE_KEY = "gravityore";
    // Chunks that have been populated and have yet to be saved
    private static final Set<Chunk> MOD_POPULATED_CHUNKS = Collections.newSetFromMap(new WeakHashMap<>());
    private static final boolean DEBUG_GEN = false;

    @SubscribeEvent
    public static void onChunkDataLoad(ChunkDataEvent.Load event) {

        if (!event.getChunk().isTerrainPopulated()) {
            // Population is yet to be run for this chunk, mod population will run as normal
            if (DEBUG_GEN) {
                FMLLog.info("Loaded chunk that hasn't been vanilla populated (and thus hasn't been mod populated either): "
                        + new ChunkRef(event.getChunk(),
                        event.getWorld()));
            }
            return;
        }

        NBTTagCompound data = event.getData();

        NBTTagCompound compoundTag = data.getCompoundTag(COMPOUND_TAG_KEY);

        if (compoundTag.hasKey(GRAVITY_ORE_KEY, Constants.NBT.TAG_BYTE)) {
            // Mod population has already occurred for this chunk

            // Need to ensure that the data that signifies this is still saved to the chunk nbt when the chunk gets saved
            MOD_POPULATED_CHUNKS.add(event.getChunk());
            if (DEBUG_GEN) {
                FMLLog.info("Loaded chunk that is already mod populated: " + new ChunkRef(event.getChunk(), event.getWorld()));
            }
        }
        else {
            final World world = event.getWorld();
            //Mod population has not yet occurred for this chunk
            if (isWorldValidForGeneration(world)) {
                // Only care if the world is valid, we won't do any generating regardless if it's not

//            ConfigHandler.oreGenRetroGen

                // If the chunk nbt doesn't have the tag, then we haven't populated it
                if (ConfigHandler.oreGenRetroGen) {
                    // Prepare retrogen
                    final ChunkPos chunkPos = event.getChunk().getChunkCoordIntPair();

                    if (DEBUG_GEN) {
                        FMLLog.info("Preparing for retrogen in chunk: " + new ChunkRef(event.getChunk(), world));
                    }
                    // Will stack overflow if we generate from here since the chunk hasn't fully loaded.
                    // Attempting to access the blocks in the chunk would cause a chunk load attempt, re-calling this method.
                    // So we schedule it to run near the start of the next tick.
                    FMLCommonHandler.instance().getMinecraftServerInstance().futureTaskQueue.add(ListenableFutureTask.create(Executors.callable(
                            () -> {
                                if (DEBUG_GEN) {
                                    FMLLog.info("Performing retrogen in chunk: " + new ChunkRef(chunkPos, world));
                                }
                                INSTANCE.generate(world.rand, chunkPos.chunkXPos, chunkPos.chunkZPos, world);
                            }
                    )));
                }
                else if (DEBUG_GEN) {
                    FMLLog.info("Loaded chunk that hasn't been mod populated, but retrogen is disabled, so doing nothing " + new ChunkRef(event.getChunk(),
                            world));
                }
            }
        }
    }

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
            // If no blocks/entities/etc in the chunk change, then the chunk is not re-saved, meaning that the nbt we use to track retrogen status won't get
            // saved. Therefore, we forcefully tell the chunk that it's been modified so that it will get saved.
            Chunk chunkPopulated = world.getChunkFromChunkCoords(chunkX, chunkZ);
            chunkPopulated.setChunkModified();
            MOD_POPULATED_CHUNKS.add(chunkPopulated);
            if (DEBUG_GEN) {
                FMLLog.info("Completed mod population of " + new ChunkRef(chunkX, chunkZ, world));
            }
        }
    }

    //TODO: Test a valid world, then make it invalid and see if the loaded chunks still have the extra nbt data? I.e., we don't have to add the tag every time?
    @SubscribeEvent
    public static void onChunkDataSave(ChunkDataEvent.Save event) {
        Chunk chunk = event.getChunk();

        if (MOD_POPULATED_CHUNKS.contains(chunk)) {
            if (DEBUG_GEN) {
                FMLLog.info("Saving chunk that has completed mod population: " + new ChunkRef(chunk, event.getWorld()));
            }
            NBTTagCompound data = event.getData();
            // Will create a new empty tag if it doesn't already exist
            NBTTagCompound compoundTag = data.getCompoundTag(COMPOUND_TAG_KEY);
            // We still have to add the new tag to the main 'data' NBTTagCompound for some reason
            data.setTag(COMPOUND_TAG_KEY, compoundTag);
            // Value of the boolean tag is irrelevant, it's existence shows that this chunk has been populated (either by normal population or retrogen)
            compoundTag.setBoolean(GRAVITY_ORE_KEY, true);
        }
        else if (DEBUG_GEN) {
            if (!chunk.isTerrainPopulated()) {
                // Population is yet to be run for this chunk
                FMLLog.info("Saving chunk that's not vanilla populated (and thus not mod populated): " + new ChunkRef(chunk, event.getWorld()));
                return;
            }
            FMLLog.info("Saving chunk that's vanilla populated, but not mod populated: " + new ChunkRef(chunk, event.getWorld()));
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
            this(chunk.xPosition, chunk.zPosition, provider);
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
            this(chunkPos.chunkXPos, chunkPos.chunkZPos, provider);
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

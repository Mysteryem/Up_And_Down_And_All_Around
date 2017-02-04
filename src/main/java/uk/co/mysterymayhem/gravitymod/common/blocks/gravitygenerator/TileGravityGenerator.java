package uk.co.mysterymayhem.gravitymod.common.blocks.gravitygenerator;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import uk.co.mysterymayhem.gravitymod.GravityMod;
import uk.co.mysterymayhem.gravitymod.api.API;
import uk.co.mysterymayhem.gravitymod.api.EnumGravityDirection;
import uk.co.mysterymayhem.gravitymod.api.EnumGravityTier;
import uk.co.mysterymayhem.gravitymod.client.blocks.gravitygenerator.ClientTickListener;
import uk.co.mysterymayhem.gravitymod.client.blocks.gravitygenerator.GravityParticle;
import uk.co.mysterymayhem.gravitymod.client.blocks.gravitygenerator.VolumeParticle;
import uk.co.mysterymayhem.gravitymod.common.config.ConfigHandler;
import uk.co.mysterymayhem.gravitymod.common.util.boundingboxes.GravityAxisAlignedBB;
import uk.co.mysterymayhem.mystlib.annotations.UsedReflexively;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

import static net.minecraftforge.common.util.Constants.NBT.TAG_BYTE;
import static net.minecraftforge.common.util.Constants.NBT.TAG_INT;

/**
 * Created by Mysteryem on 2016-12-18.
 */
public class TileGravityGenerator extends TileEntity implements ITickable {

    public static final int MAX_HEIGHT = ConfigHandler.gravityGeneratorMaxHeight;
    public static final int MAX_RADIUS = ConfigHandler.gravityGeneratorMaxRadius;
    public static final int MAX_TICKS_PER_PARTICLE_SPAWN = 10;
    public static final int MAX_VOLUME = MAX_HEIGHT * (2 * MAX_RADIUS + 1) * (2 * MAX_RADIUS + 1);
    public static final int MIN_HEIGHT = 1;
    public static final int MIN_RADIUS = 0;
    public static final int MIN_TICKS_PER_PARTICLE_SPAWN = 1;
    private static final String FACING_NBT_KEY = "facing";
    private static final String REL_HEIGHT_KEY = "relheight";
    private static final String REL_XRADIUS_KEY = "relxrad";
    private static final String REL_ZRADIUS_KEY = "relzrad";
    private static final String TIER_NBT_KEY = "tier";
    private static final String REVERSE_KEY = "reverse";
    private static final Random sharedRandom = new Random();

    private int clientTicksLived = 0;
    // Shouldn't be changed once set
    private EnumFacing facing = EnumFacing.NORTH;
    private EnumGravityDirection gravityDirection = null;
    // Shouldn't be changed once set
    private EnumGravityTier gravityTier = EnumGravityTier.NORMAL;
    private boolean powered = false;
    private int relativeXRadius = MAX_RADIUS;
    private int relativeYHeight = MAX_HEIGHT;
    private int relativeZRadius = MAX_RADIUS;
    private double maxDistance = (relativeXRadius + 0.5) * (relativeZRadius + 0.5) * relativeYHeight;
    private double percentOfMaxVolume = this.getVolume() / MAX_VOLUME;
    //Initial value should never be used
    private AxisAlignedBB searchVolume = Block.FULL_BLOCK_AABB;
    private int ticksPerSpawn = (int)(MAX_TICKS_PER_PARTICLE_SPAWN - percentOfMaxVolume *
            (MAX_TICKS_PER_PARTICLE_SPAWN - MIN_TICKS_PER_PARTICLE_SPAWN));
    // SideOnly fields are nasty
    // It's important that the field is not initialised otherwise constructors will attempt to access the field on the
    // server side
    @SideOnly(Side.CLIENT)
    private VolumeParticle volumeParticle;
    // Effectively final
    private Vec3d volumeSpawnPoint = null;
    private boolean reverseDirection = false;

    public TileGravityGenerator(EnumGravityTier gravityTier, EnumFacing facing, Boolean reverseDirection) {
        this.gravityTier = gravityTier;
        this.facing = facing;
        this.gravityDirection = EnumGravityDirection.fromEnumFacing(facing.getOpposite());
        this.reverseDirection = reverseDirection;
    }

    @UsedReflexively
    public TileGravityGenerator() {/**/}

    public static int clampWidth(int width) {
        return radiusToWidth(clampRadius(widthToRadius(width)));
    }

    public static int radiusToWidth(int radius) {
        return radius * 2 + 1;
    }

    public static int clampRadius(int radius) {
        return Math.min(MAX_RADIUS, Math.max(MIN_RADIUS, radius));
    }

    public static int widthToRadius(int width) {
        return (width - 1) / 2;
    }

    public EnumFacing getFacing() {
        return this.facing;
    }

    public void setFacing(EnumFacing facing) {
        this.facing = facing;
        this.gravityDirection = EnumGravityDirection.fromEnumFacing(facing.getOpposite());
        this.updateVolumeSpawnPos();
        this.updateSearchVolume();
        this.markDirty();
    }

    private void updateVolumeSpawnPos() {
        BlockPos pos = this.getPos();
        switch (this.facing) {
            case DOWN:
                this.volumeSpawnPoint = new Vec3d(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                break;
            case UP:
                this.volumeSpawnPoint = new Vec3d(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
                break;
            case NORTH:
                this.volumeSpawnPoint = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ());
                break;
            case SOUTH:
                this.volumeSpawnPoint = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 1);
                break;
            case WEST:
                this.volumeSpawnPoint = new Vec3d(pos.getX(), pos.getY() + 0.5, pos.getZ() + 0.5);
                break;
            default://case EAST:
                this.volumeSpawnPoint = new Vec3d(pos.getX() + 1, pos.getY() + 0.5, pos.getZ() + 0.5);
                break;
        }
    }

    // when relativeX/ZRadius or relativeYHeight get changed, we need to update the searchvolume
    private void updateSearchVolume() {
//        Vec3d centreBlockPos = new Vec3d(this.getPos()).addVector(0.5, 0.5, 0.5);
        final AxisAlignedBB offset = Block.FULL_BLOCK_AABB.offset(this.getPos());
//        final EnumGravityDirection direction = EnumGravityDirection.fromEnumFacing(this.facing.getOpposite());
        // 0 -> +0 either side, 2 -> +0.5 either side = +1, 3 -> +1 either side = +2 etc.
        double yIncrease = (relativeYHeight - 1) * 0.5d;
        double[] relativeXYZExpansion = this.gravityDirection.adjustXYZValuesMaintainSigns(relativeXRadius, yIncrease,
                relativeZRadius);
        double[] relativeYMovement = this.gravityDirection.adjustXYZValues(0, yIncrease + 1, 0);
        this.searchVolume = offset.expand(relativeXYZExpansion[0], relativeXYZExpansion[1], relativeXYZExpansion[2])
                .offset(relativeYMovement[0], relativeYMovement[1], relativeYMovement[2]);
        this.maxDistance = (relativeXRadius + 0.5) * (relativeZRadius + 0.5) * relativeYHeight;
        this.percentOfMaxVolume = this.getVolume() / (double)MAX_VOLUME;
        this.ticksPerSpawn = (int)(MAX_TICKS_PER_PARTICLE_SPAWN - this.extendPercentageOfMaxVolume() *
                (MAX_TICKS_PER_PARTICLE_SPAWN - MIN_TICKS_PER_PARTICLE_SPAWN));
    }

    public int getVolume() {
        return this.relativeYHeight * (2 * this.relativeXRadius + 1) * (2 * this.relativeZRadius + 1);
    }

    /**
     * <p>Takes the percentage of the maximum volume [0,1] and maps it according to 1-(x-1)^4 (to [0,1]).</p>
     * <p>
     * <p>This is done so that smaller percentages e.g. 1/(11*11*11) = 1/1331 = 0.00075... still spawn a reasonable
     * number</p>
     * of particles.
     *
     * @return
     */
    private double extendPercentageOfMaxVolume() {
//        return Math.pow(this.percentOfMaxVolume, 0.5);
        return 1 - Math.pow(this.percentOfMaxVolume - 1, 4); //1-(x-1)^(4), stick in wolfram alpha/graphic
        // software/calculator
    }

    public EnumGravityTier getGravityTier() {
        return gravityTier;
    }

    public int getRelativeXRadius() {
        return relativeXRadius;
    }

    public void setRelativeXRadius(int relativeXRadius) {
        int newRadius = clampRadius(relativeXRadius);
        if (this.relativeXRadius != newRadius) {
            this.relativeXRadius = newRadius;
            this.updateSearchVolume();
            this.markDirty();
        }
    }

    public int getRelativeYHeight() {
        return relativeYHeight;
    }

    // Possible future feature whereby items must be inserted in order to unlock additional features of the gravity
    // generator
//    private static final String INVENTORY_KEY = "inventory";
//
//    private IItemHandler inventory = new ItemStackHandler(1) {
//        @Override
//        protected void onContentsChanged(int slot) {
//            TileGravityGenerator.this.markDirty();
//        }
//    };
//
//    @Override
//    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
//        if (capability == ITEM_HANDLER_CAPABILITY) {
//            return true;
//        }
//        else {
//            return super.hasCapability(capability, facing);
//        }
//    }
//
//    @Override
//    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
//        if (capability == ITEM_HANDLER_CAPABILITY) {
//            return ITEM_HANDLER_CAPABILITY.cast(this.inventory);
//        }
//        else {
//            return super.getCapability(capability, facing);
//        }
//    }

    public void setRelativeYHeight(int relativeYHeight) {
        int newHeight = clampHeight(relativeYHeight);
        if (this.relativeYHeight != newHeight) {
            this.relativeYHeight = newHeight;
            this.updateSearchVolume();
            this.markDirty();
        }
    }

    public static int clampHeight(int height) {
        return Math.min(MAX_HEIGHT, Math.max(MIN_HEIGHT, height));
    }

    public int getRelativeZRadius() {
        return relativeZRadius;
    }

    public void setRelativeZRadius(int relativeZRadius) {
        int newRadius = clampRadius(relativeZRadius);
        if (this.relativeZRadius != newRadius) {
            this.relativeZRadius = newRadius;
            this.updateSearchVolume();
            this.markDirty();
        }
    }

    public AxisAlignedBB getSearchVolume() {
        return searchVolume;
    }

    @SideOnly(Side.CLIENT)
    public VolumeParticle getVolumeParticle() {
        return this.volumeParticle;
    }

    public boolean isPowered() {
        return this.powered;
    }

    // Can't call this.markDirty() while being read from NBT, so got to store some state so that we know to call this.markDirty() as soon as possible
    // I could schedule a task for the next world tick, but I don't want to schedule multiple tasks
    private boolean missingTagsOnLoad = false;

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
//        if (compound.hasKey(INVENTORY_KEY, TAG_LIST)) {
//            ITEM_HANDLER_CAPABILITY.readNBT(this.inventory, null, compound.getTagList(INVENTORY_KEY, TAG_COMPOUND));
//        }

        // Gravity Tier
        if (compound.hasKey(TIER_NBT_KEY, TAG_BYTE)) {
            int gravityTierOrdinal = compound.getByte(TIER_NBT_KEY);
            EnumGravityTier[] tierValues = EnumGravityTier.values();
            int length = tierValues.length;
            if (gravityTierOrdinal < 0) {
                GravityMod.logWarning("Reading tier from nbt for %s failed. Got %s, expected value between 0 and %s",
                        this, gravityTierOrdinal, length);
                gravityTierOrdinal = 0;
                this.missingTagsOnLoad = true;
            }
            else if (gravityTierOrdinal >= length) {
                GravityMod.logWarning("Reading tier from nbt for %s failed. Got %s, expected value between 0 and %s",
                        this, gravityTierOrdinal, length);
                gravityTierOrdinal = length - 1;
                this.missingTagsOnLoad = true;
            }
            this.gravityTier = tierValues[gravityTierOrdinal];
        }
        else {
            GravityMod.logWarning("Missing \"%s\" tag from loaded nbt for %s", TIER_NBT_KEY, this);
            this.missingTagsOnLoad = true;
        }

        // Facing
        if (compound.hasKey(FACING_NBT_KEY, TAG_BYTE)) {
            int facingOrdinal = compound.getByte(FACING_NBT_KEY);
            EnumFacing[] facingValues = EnumFacing.values();
            int length = facingValues.length;
            if (facingOrdinal < 0) {
                GravityMod.logWarning("Reading facing from nbt for %s failed. Got %s, expected value between 0 and %s",
                        this, gravityTier, length);
                facingOrdinal = 0;
                this.missingTagsOnLoad = true;
            }
            else if (facingOrdinal >= length) {
                GravityMod.logWarning("Reading facing from nbt for %s failed. Got %s, expected value between 0 and %s",
                        this, gravityTier, length);
                facingOrdinal = length - 1;
                this.missingTagsOnLoad = true;
            }
            this.facing = facingValues[facingOrdinal];
            this.gravityDirection = EnumGravityDirection.fromEnumFacing(this.facing.getOpposite());
        }
        else {
            GravityMod.logWarning("Missing \"%s\" tag from loaded nbt for %s", FACING_NBT_KEY, this);
            this.missingTagsOnLoad = true;
        }

        // Relative X radius
        if (compound.hasKey(REL_XRADIUS_KEY, TAG_INT)) {
            this.relativeXRadius = clampRadius(compound.getInteger(REL_XRADIUS_KEY));
        }
        else {
            GravityMod.logWarning("Missing \"%s\" tag from loaded nbt for %s", REL_XRADIUS_KEY, this);
            this.missingTagsOnLoad = true;
        }

        // Relative Z radius
        if (compound.hasKey(REL_ZRADIUS_KEY, TAG_INT)) {
            this.relativeZRadius = clampRadius(compound.getInteger(REL_ZRADIUS_KEY));
        }
        else {
            GravityMod.logWarning("Missing \"%s\" tag from loaded nbt for %s", REL_ZRADIUS_KEY, this);
            this.missingTagsOnLoad = true;
        }

        // Relative Y height
        if (compound.hasKey(REL_HEIGHT_KEY, TAG_INT)) {
            this.relativeYHeight = clampHeight(compound.getInteger(REL_HEIGHT_KEY));
        }
        else {
            GravityMod.logWarning("Missing \"%s\" tag from loaded nbt for %s", REL_HEIGHT_KEY, this);
            this.missingTagsOnLoad = true;
        }

        // Reverse
        if (compound.hasKey(REVERSE_KEY, TAG_BYTE)) {
            this.reverseDirection = compound.getBoolean(REVERSE_KEY);
        }
        else {
            GravityMod.logWarning("Missing \"%s\" tag from loaded nbt for %s", REVERSE_KEY, this);
            this.missingTagsOnLoad = true;
        }

        // Apply any changes
        this.updateSearchVolume();
    }

    @Nullable
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(this.pos, 0, this.getUpdateTag());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return this.writeToNBT(new NBTTagCompound());
    }

    //TODO: Is this safe?
    @Override
    public void onLoad() {
        if (this.missingTagsOnLoad && !this.worldObj.isRemote) {
            Minecraft.getMinecraft().addScheduledTask(this::markDirty);
            this.missingTagsOnLoad = false;
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setByte(TIER_NBT_KEY, (byte)this.gravityTier.ordinal());
        compound.setByte(FACING_NBT_KEY, (byte)this.facing.ordinal());
        compound.setInteger(REL_XRADIUS_KEY, this.relativeXRadius);
        compound.setInteger(REL_ZRADIUS_KEY, this.relativeZRadius);
        compound.setInteger(REL_HEIGHT_KEY, this.relativeYHeight);
        compound.setBoolean(REVERSE_KEY, this.reverseDirection);
//        compound.setTag(INVENTORY_KEY, ITEM_HANDLER_CAPABILITY.writeNBT(this.inventory, null));
        return super.writeToNBT(compound);
    }

    @Override
    public void setPos(BlockPos posIn) {
        super.setPos(posIn);
        this.updateSearchVolume();
        this.updateVolumeSpawnPos();
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        this.handleUpdateTag(pkt.getNbtCompound());
        BlockPos pos = this.pos;
        IBlockState blockState = this.worldObj.getBlockState(pos);
        // Update rendering of the block on the client side
        this.worldObj.notifyBlockUpdate(pos, blockState, blockState, 3);
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        super.handleUpdateTag(tag);
    }

    @Override
    public void update() {
        this.powered = this.worldObj.isBlockPowered(this.getPos());
        if (!this.worldObj.isRemote) {
//            IBlockState blockState = this.worldObj.getBlockState(this.getPos());
//            if (this.setupRequired) {
//                if (!blockState.getPropertyNames().contains(BlockGravityGenerator.FACING)) {
//                    this.invalidate();
//                    return;
//                }
//                else {
//                    EnumFacing facing = blockState.getValue(BlockGravityGenerator.FACING);
//                    this.updateSearchVolumes();
//                    this.updateOnFacingChange(facing);
//                }
//                this.setupRequired = false;
//            }
//            boolean isActive = blockState.getValue(BlockGravityGenerator.ENABLED);

            if (this.powered) {
//                EnumFacing facing = blockState.getValue(BlockGravityGenerator.FACING).getOpposite();
                EnumGravityDirection enumGravityDirection = this.gravityDirection;
                if (this.reverseDirection) {
                    enumGravityDirection = enumGravityDirection.getOpposite();
                }
                AxisAlignedBB volumeToCheck = this.searchVolume;
//                RenderGlobal.renderFilledBox(volumeToCheck, 1f, 1f, 1f, 0.5f);

                List<EntityPlayerMP> entitiesWithinAABB = this.worldObj.getEntitiesWithinAABB(
                        EntityPlayerMP.class,
                        volumeToCheck,
                        input -> (!(input instanceof FakePlayer) && TileGravityGenerator.this.affectsPlayer(input)));
                for (EntityPlayerMP player : entitiesWithinAABB) {
//                    player.getEntityBoundingBox().getCenter();
                    AxisAlignedBB bb = player.getEntityBoundingBox();
                    Vec3d bbOrigin;
//                    Vec3d closestCornerOfBBTo = findClosestCornerOfBBTo(bb, this.volumeSpawnPoint);
//                    double squareDistance = closestCornerOfBBTo.squareDistanceTo(this.volumeSpawnPoint);
                    if (bb instanceof GravityAxisAlignedBB) {
                        bbOrigin = ((GravityAxisAlignedBB)bb).getOrigin();
                    }
                    else {
                        bbOrigin = new Vec3d(bb.minX + (bb.maxX - bb.minX) * 0.5D, bb.minY + (bb.maxY - bb.minY) *
                                0.5D, bb.minZ + (bb.maxZ - bb.minZ) * 0.5D);
                    }
//                    if (!volumeToCheck.isVecInside(bbOrigin)) {
//                        continue;
//                    }
                    double squareDistance = this.volumeSpawnPoint.squareDistanceTo(bbOrigin);
//                    Vec3d bbCentre = new Vec3d(player.posX, player.posY, player.posZ);
                    int priority = this.getPriority(1 - squareDistance / this.maxDistance);
                    API.setPlayerGravity(enumGravityDirection, player, priority);
                }
            }
        }
        else {
            this.updateRangeParticle();
            this.spawnFallingParticles();
            this.clientTicksLived++;
        }
    }

    public boolean affectsPlayer(EntityPlayerMP playerMP) {
        return this.gravityTier.isPlayerAffected(playerMP);
    }

    public int getPriority(double percent) {
        return this.gravityTier.getPriority(percent);
    }

    @SideOnly(Side.CLIENT)
    void updateRangeParticle() {
        if (this.playerCanSeeRange()) {
            if (this.volumeParticle == null || !(this.volumeParticle).isAlive()) {
                float[] colour = this.gravityTier.getColour();
                float red = colour[0];
                float green = colour[1];
                float blue = colour[2];
                //float alpha = this.worldObj.isPowered(this.getPos()) ? 0.4f : 0.2f;
                float alphaPowered = 0.3f;
                float alphaUnpowered = 0.1f;
                VolumeParticle volumeParticle = new VolumeParticle(this, red, green, blue, alphaPowered,
                        alphaUnpowered);
                this.volumeParticle = volumeParticle;
                Minecraft.getMinecraft().effectRenderer.addEffect(volumeParticle);
            }
        }
        else {
            if (this.volumeParticle != null) {
                this.volumeParticle = null;
            }
        }
    }

    @SideOnly(Side.CLIENT)
    private void spawnFallingParticles() {
        int particleSetting = Minecraft.getMinecraft().gameSettings.particleSetting;
        if (particleSetting != 2) {
            if (this.powered) {
//                if (this.playerCanSeeRange()) {
                EnumGravityDirection direction = this.reverseDirection ? this.gravityDirection.getOpposite() : this.gravityDirection;
                if ((this.clientTicksLived + (sharedRandom.nextBoolean() ? 1 : 0)) % (particleSetting == 0 ? this.ticksPerSpawn : this.ticksPerSpawn * 2) == 0) {
                    Minecraft.getMinecraft().effectRenderer.addEffect(new GravityParticle(direction, this
                            .searchVolume, this.gravityTier, this.worldObj));
                }
//                }
//                else {
//                    if ((this.clientTicksLived + (sharedRandom.nextBoolean() ? 1 : 0)) % (particleSetting == 0 ?
// this.ticksPerSpawn * 2 : this.ticksPerSpawn * 4) == 0) {
//                        Minecraft.getMinecraft().effectRenderer.addEffect(new GravityParticle(this
// .gravityDirection, this.searchVolume, this.gravityTier, this.worldObj));
//                    }
//                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public boolean playerCanSeeRange() {
        return ClientTickListener.clientPlayerCanSeeGravityFields();
    }

//    private static Vec3d findClosestCornerOfBBTo(AxisAlignedBB bb, Vec3d pos) {
//        double x = pos.xCoord;
//        double y = pos.yCoord;
//        double z = pos.zCoord;
//
//        double closestX;
//        double closestY;
//        double closestZ;
//
//        if (bb.minX >= x) {
//            closestX = bb.minX;
//        }
//        else if (bb.maxX <= x) {
//            closestX = bb.maxX;
//        }
//        else {
//            double maKSSide = bb.maxX - x;
//            double minSide = x - bb.minX;
//            if (maKSSide < minSide) {
//                closestX = bb.maxX;
//            }
//            else {
//                closestX = bb.minX;
//            }
//        }
//
//        if (bb.minY >= y) {
//            closestY = bb.minY;
//        }
//        else if (bb.maxY <= y) {
//            closestY = bb.maxY;
//        }
//        else {
//            double maxSide = bb.maxY - y;
//            double minSide = y - bb.minY;
//            if (maxSide < minSide) {
//                closestY = bb.maxY;
//            }
//            else {
//                closestY = bb.minY;
//            }
//        }
//
//        if (bb.minZ >= z) {
//            closestZ = bb.minZ;
//        }
//        else if (bb.maxZ <= z) {
//            closestZ = bb.maxZ;
//        }
//        else {
//            double maxSide = bb.maxZ - z;
//            double minSide = z - bb.minZ;
//            if (maxSide < minSide) {
//                closestZ = bb.maxZ;
//            }
//            else {
//                closestZ = bb.minZ;
//            }
//        }
//
//        return new Vec3d(closestX, closestY, closestZ);
//    }
//
//    private static Vec3d findClosestCornerOfBBTo2(AxisAlignedBB bb, Vec3d pos) {
//        double x = pos.xCoord;
//        double y = pos.yCoord;
//        double z = pos.zCoord;
//
//        double closestX;
//        double closestY;
//        double closestZ;
//
//        closestX = Math.abs(x - bb.maxX) < Math.abs(x - bb.minX) ? bb.maxX : bb.minX;
//        closestY = Math.abs(y - bb.maxY) < Math.abs(y - bb.minY) ? bb.maxY : bb.minY;
//        closestZ = Math.abs(z - bb.maxZ) < Math.abs(z - bb.minZ) ? bb.maxZ : bb.minZ;
//
//        return new Vec3d(closestX, closestY, closestZ);
//    }


    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " at (" + this.pos.getX() + ",";
    }
}

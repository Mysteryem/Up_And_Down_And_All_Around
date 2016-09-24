package uk.co.mysterymayhem.gravitymod.asm;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import uk.co.mysterymayhem.gravitymod.api.API;
import uk.co.mysterymayhem.gravitymod.api.EnumGravityDirection;
import uk.co.mysterymayhem.gravitymod.util.GravityAxisAlignedBB;
import uk.co.mysterymayhem.gravitymod.util.reflection.LookupThief;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

/**
 * Created by Mysteryem on 2016-09-04.
 */
public abstract class EntityPlayerWithGravity extends EntityPlayer {

    // TODO: Isn't all motion relative by default? That's part of the reasoning behind GravityAxisAlignedBB right?
    private boolean motionVarsAreRelative;

    // Relative position doesn't exist, but this allows adjustments made in other methods such as 'this.posY+=3',
    // to correspond to whatever we think 'UP' is
    private boolean positionVarsAreRelative;

    private boolean angleVarsAreRelative;

    public EntityPlayerWithGravity(World worldIn, GameProfile gameProfileIn) {
        super(worldIn, gameProfileIn);

        this.motionVarsAreRelative = false;
        this.positionVarsAreRelative = false;
        this.angleVarsAreRelative = false;
    }

    void makeMotionRelative() {
//        if (!this.motionVarsAreRelative) {
//            double[] doubles = API.getGravityDirection(this).getInverseAdjustMentFromDOWNDirection().adjustXYZValues(this.motionX, this.motionY, this.motionZ);
//            this.motionVarsAreRelative = true;
//            this.motionX = doubles[0];
//            this.motionY = doubles[1];
//            this.motionZ = doubles[2];
//        }
    }

    void makeMotionAbsolute() {
//        if (this.motionVarsAreRelative) {
//            double[] doubles = API.getGravityDirection(this).adjustXYZValues(this.motionX, this.motionY, this.motionZ);
//            this.motionVarsAreRelative = false;
//            this.motionX = doubles[0];
//            this.motionY = doubles[1];
//            this.motionZ = doubles[2];
//        }
    }

    void makePositionRelative() {
        if (!this.positionVarsAreRelative) {
            EnumGravityDirection direction = API.getGravityDirection(this).getInverseAdjustMentFromDOWNDirection();

            double[] doubles = direction.adjustXYZValues(this.posX, this.posY, this.posZ);
            this.posX = doubles[0];
            this.posY = doubles[1];
            this.posZ = doubles[2];

            doubles = direction.adjustXYZValues(this.prevPosX, this.prevPosY, this.prevPosZ);
            this.prevPosX = doubles[0];
            this.prevPosY = doubles[1];
            this.prevPosZ = doubles[2];

            this.positionVarsAreRelative = true;
        }
    }

    void makePositionAbsolute() {
        if (this.positionVarsAreRelative) {
            EnumGravityDirection direction = API.getGravityDirection(this);

            double[] doubles = direction.adjustXYZValues(this.posX, this.posY, this.posZ);
            this.posX = doubles[0];
            this.posY = doubles[1];
            this.posZ = doubles[2];

            doubles = direction.adjustXYZValues(this.prevPosX, this.prevPosY, this.prevPosZ);
            this.prevPosX = doubles[0];
            this.prevPosY = doubles[1];
            this.prevPosZ = doubles[2];

            this.positionVarsAreRelative = false;
        }
    }

    @Override
    public Vec3d getLook(float partialTicks) {
        Vec3d vectorForRotation;
        if (partialTicks == 1.0F) {
            vectorForRotation = this.getVectorForRotation(this.rotationPitch, this.rotationYawHead);
    //            return this.getVectorForRotation(this.rotationPitch, this.rotationYaw);
        } else {
            float interpolatedRotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * partialTicks;
            float interpolatedRotationYaw = this.prevRotationYawHead + (this.rotationYawHead - this.prevRotationYawHead) * partialTicks;
            vectorForRotation = this.getVectorForRotation(interpolatedRotationPitch, interpolatedRotationYaw);
    //            return this.getVectorForRotation(interpolatedRotationPitch, interpolatedRotationYaw);

        }
        return API.getGravityDirection(this).adjustLookVec(vectorForRotation);
    }

    @Override
    public void setEntityBoundingBox(AxisAlignedBB bb) {
        bb = Hooks.replaceWithGravityAware(this, bb);
        super.setEntityBoundingBox(bb);
    }

    //TODO: ASM the EntityPlayerMP class instead
    @Override
    protected void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos) {
        if (!this.worldObj.isRemote) {

            BlockPos blockpos;

            AxisAlignedBB offsetAABB = this.getEntityBoundingBox().offset(0, -0.20000000298023224D, 0);
            if (offsetAABB instanceof GravityAxisAlignedBB) {
                GravityAxisAlignedBB offsetAABB_g = (GravityAxisAlignedBB)offsetAABB;
                Vec3d origin = offsetAABB_g.getOrigin();
                blockpos = new BlockPos(origin);
            }
            else {
                // Fallback
                int i, j, k;
                i = MathHelper.floor_double(this.posX);
                j = MathHelper.floor_double(this.posY - 0.20000000298023224D);
                k = MathHelper.floor_double(this.posZ);
                blockpos = new BlockPos(i, j, k);
                FMLLog.warning("Player bounding box is not gravity aware. In [UpAndDownAndAllAround]:EntityPlayerWithGravity::updateFallState");
            }

            IBlockState iblockstate = this.worldObj.getBlockState(blockpos);

            if (iblockstate.getBlock().isAir(iblockstate, this.worldObj, blockpos))
            {
                BlockPos blockpos1;
                switch(API.getGravityDirection(this)) {
                    case UP:
                        blockpos1 = blockpos.up();
                        break;
                    case DOWN:
                        blockpos1 = blockpos.down();
                        break;
                    case SOUTH:
                        blockpos1 = blockpos.south();
                        break;
                    case WEST:
                        blockpos1 = blockpos.west();
                        break;
                    case NORTH:
                        blockpos1 = blockpos.north();
                        break;
//                    case EAST:
                    default:
                        blockpos1 = blockpos.east();
                        break;
                }

                IBlockState iblockstate1 = this.worldObj.getBlockState(blockpos1);
                Block block = iblockstate1.getBlock();

                if (block instanceof BlockFence || block instanceof BlockWall || block instanceof BlockFenceGate)
                {
                    blockpos = blockpos1;
                    iblockstate = iblockstate1;
                }
            }

            super.updateFallState(y, onGroundIn, iblockstate, blockpos);
        }
        else {
            super.updateFallState(y, onGroundIn, state, pos);
        }
    }

    //TODO: ASM?
    @Override
    public boolean isEntityInsideOpaqueBlock()
    {
        if (this.noClip) {
            return false;
        }
        AxisAlignedBB bb = this.getEntityBoundingBox();
        if (bb instanceof GravityAxisAlignedBB) {
            GravityAxisAlignedBB gBB = (GravityAxisAlignedBB)bb;
            EnumGravityDirection direction = gBB.getDirection();
            Vec3d origin = gBB.getOrigin();

            BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain();

            for (int i = 0; i < 8; ++i)
            {
                double yMovement = (double)(((float)((i >> 0) % 2) - 0.5F) * 0.1F) + (double)API.getStandardEyeHeight(this);
                double xMovement = (double)(((float)((i >> 1) % 2) - 0.5F) * this.width * 0.8F);
                double zMovement = (double)(((float)((i >> 2) % 2) - 0.5F) * this.width * 0.8F);
                double[] d = direction.adjustXYZValues(xMovement, yMovement, zMovement);

                int j = MathHelper.floor_double(origin.yCoord + d[1]);
                int k = MathHelper.floor_double(origin.xCoord + d[0]);
                int l = MathHelper.floor_double(origin.zCoord + d[2]);

                if (blockpos$pooledmutableblockpos.getX() != k || blockpos$pooledmutableblockpos.getY() != j || blockpos$pooledmutableblockpos.getZ() != l)
                {
                    blockpos$pooledmutableblockpos.setPos(k, j, l);

                    if (this.worldObj.getBlockState(blockpos$pooledmutableblockpos).getBlock().isVisuallyOpaque())
                    {
                        blockpos$pooledmutableblockpos.release();
                        return true;
                    }
                }
            }

            blockpos$pooledmutableblockpos.release();
            return false;

        }
        else {
            return super.isEntityInsideOpaqueBlock();
        }
//        this.makePositionRelative();
//        boolean entityInsideOpaqueBlock = super.isEntityInsideOpaqueBlock();
//        this.makePositionAbsolute();
//        return entityInsideOpaqueBlock;
    }

    @Override
    public AxisAlignedBB getEntityBoundingBox()
    {
        AxisAlignedBB entityBoundingBox = super.getEntityBoundingBox();
        if (!(entityBoundingBox instanceof GravityAxisAlignedBB)) {
            entityBoundingBox = Hooks.replaceWithGravityAware(this, entityBoundingBox);
            this.setEntityBoundingBox(entityBoundingBox);
        }
        return entityBoundingBox;
    }


    @Override
    public void setPositionAndRotation(double x, double y, double z, float yaw, float pitch) {
        super.setPositionAndRotation(x, y, z, yaw, pitch);
    }

    @Override
    public void addMovementStat(double p_71000_1_, double p_71000_3_, double p_71000_5_) {
        EnumGravityDirection direction = API.getGravityDirection(this);
        super.addMovementStat(p_71000_1_, p_71000_3_, p_71000_5_);
    }

    @Override
    public float getEyeHeight() {
        switch (API.getGravityDirection(this)) {
            case UP:
                return -API.getStandardEyeHeight(this);
            case DOWN:
                return API.getStandardEyeHeight(this);
//            case NORTH:
//            case EAST:
//            case SOUTH:
//            case WEST:
            default:
                return 0f;
        }
    }

    public float super_getEyeHeight() {
        return super.getEyeHeight();
    }

    @Override
    protected void createRunningParticles()
    {
        AxisAlignedBB bb = this.getEntityBoundingBox();
        if (bb instanceof GravityAxisAlignedBB) {
            // Based on the super implementation
            GravityAxisAlignedBB gBB = (GravityAxisAlignedBB)bb;
            Vec3d belowBlock = gBB.addCoord(0, -0.20000000298023224D, 0).getOrigin();
            BlockPos blockpos = new BlockPos(belowBlock);
            IBlockState iblockstate = this.worldObj.getBlockState(blockpos);

            if (iblockstate.getRenderType() != EnumBlockRenderType.INVISIBLE)
            {
                Vec3d particleSpawnPoint = gBB.addCoord(((double) this.rand.nextFloat() - 0.5D) * (double) this.width, 0.1D, ((double) this.rand.nextFloat() - 0.5D) * (double) this.width).getOrigin();
                double[] d = gBB.getDirection().adjustXYZValues(-this.motionX * 4.0D, 1.5D, -this.motionZ * 4.0D);
                this.worldObj.spawnParticle(EnumParticleTypes.BLOCK_CRACK,
                        particleSpawnPoint.xCoord,
                        particleSpawnPoint.yCoord,
                        particleSpawnPoint.zCoord,
                        d[0],
                        d[1],
                        d[2],
                        Block.getStateId(iblockstate));
            }
        }
        else {
            super.createRunningParticles();
        }
    }

    @Override
    protected void updateSize() {
        float f;
        float f1;

        if (this.isElytraFlying()) {
            f = 0.6F;
            f1 = 0.6F;
        } else if (this.isPlayerSleeping()) {
            f = 0.2F;
            f1 = 0.2F;
        } else if (this.isSneaking()) {
            f = 0.6F;
            f1 = 1.65F;
        } else {
            f = 0.6F;
            f1 = 1.8F;
        }

        if (f != this.width || f1 != this.height) {
            AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
            //axisalignedbb = new AxisAlignedBB(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ, axisalignedbb.minX + (double)f, axisalignedbb.minY + (double)f1, axisalignedbb.minZ + (double)f);
            //TODO: ASM
            double heightExpansion = (f1 - this.height)/2d;
            double widthExpansion = (f - this.width)/2d;
            axisalignedbb = axisalignedbb.expand(widthExpansion, heightExpansion, widthExpansion).offset(0, heightExpansion, 0);
//            axisalignedbb = Hooks.getGravityAdjustedHitbox(this, f, f1);

            if (!this.worldObj.collidesWithAnyBlock(axisalignedbb)) {
                this.setSize(f, f1);
            }
        }
        net.minecraftforge.fml.common.FMLCommonHandler.instance().onPlayerPostTick(this);
    }

    /**
     * Sets the width and height of the entity.
     */
    protected void setSize(float width, float height) {
        if (width != this.width || height != this.height) {
            float oldWidth = this.width;
            float oldHeight = this.height;
            this.width = width;
            this.height = height;
            AxisAlignedBB oldAABB = this.getEntityBoundingBox();

            double heightExpansion = (this.height - oldHeight)/2d;
            double widthExpansion = (this.width - oldWidth)/2d;
            AxisAlignedBB newAABB = oldAABB.expand(widthExpansion, heightExpansion, widthExpansion).offset(0, heightExpansion, 0);

            this.setEntityBoundingBox(newAABB);

            if (this.width > oldWidth && !this.firstUpdate && !this.worldObj.isRemote) {
                this.moveEntity((double) (oldWidth - this.width), 0, (double) (oldWidth - this.width));
            }
        }
    }

    //TODO: Move contents of switch into EnumGravityDirection
    @Override
    public void resetPositionToBB() {
        // Oh no you don't vanilla MC
        // I need my weird asymmetrical hitboxes so player.posY + player.getEyeHeight() still get's the player's eye position
        AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
        this.posX = (axisalignedbb.minX + axisalignedbb.maxX) / 2.0D;
        this.posY = axisalignedbb.minY;
        this.posZ = (axisalignedbb.minZ + axisalignedbb.maxZ) / 2.0D;

        switch (API.getGravityDirection(this)) {
            case DOWN:
                super.resetPositionToBB();
                break;
            case UP:
                this.posX = (axisalignedbb.minX + axisalignedbb.maxX) / 2.0D;
                this.posY = axisalignedbb.maxY;
                this.posZ = (axisalignedbb.minZ + axisalignedbb.maxZ) / 2.0D;
                break;
            case SOUTH:
                this.posX = (axisalignedbb.minX + axisalignedbb.maxX) / 2.0D;
                this.posY = (axisalignedbb.minY + axisalignedbb.maxY) / 2.0D;
                this.posZ = axisalignedbb.maxZ - API.getStandardEyeHeight(this);
                break;
            case WEST:
                this.posX = axisalignedbb.minX + API.getStandardEyeHeight(this);
                this.posY = (axisalignedbb.minY + axisalignedbb.maxY) / 2.0D;
                this.posZ = (axisalignedbb.minZ + axisalignedbb.maxZ) / 2.0D;
                break;
            case NORTH:
                this.posX = (axisalignedbb.minX + axisalignedbb.maxX) / 2.0D;
                this.posY = (axisalignedbb.minY + axisalignedbb.maxY) / 2.0D;
                this.posZ = axisalignedbb.minZ + API.getStandardEyeHeight(this);
                break;
            case EAST:
                this.posX = axisalignedbb.maxX - API.getStandardEyeHeight(this);
                this.posY = (axisalignedbb.minY + axisalignedbb.maxY) / 2.0D;
                this.posZ = (axisalignedbb.minZ + axisalignedbb.maxZ) / 2.0D;
                break;

        }
    }

    //TODO: Could ASM or call super.setPosition() and then my own code?
    /**
     * Sets the x,y,z of the entity from the given parameters. Also seems to set up a bounding box.
     */
    @Override
    public void setPosition(double x, double y, double z) {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
//        float f = this.width / 2.0F;
//        float f1 = this.height;
        this.setEntityBoundingBox(Hooks.getGravityAdjustedHitbox(this));
        this.setSize(this.width, this.height);
    }

    //TODO: Fix this, so players are pushed out of blocks in differing direction based on their gravity
    @Override
    protected boolean pushOutOfBlocks(double x, double y, double z) {
        // pushOutOfBlocks directly modifies motion fields
        // Called for non-EntityPlayerSP (it overrides this method and does not call super)
//        this.makeMotionAbsolute();
        boolean result = super.pushOutOfBlocks(x, y, z);
//        this.makeMotionRelative();

        return result;
    }

    //TODO: Fix this, so players are pushed out of blocks in differing direction based on their gravity
    public boolean pushOutOfBlocksDelegate(double x, double y, double z) {
//        this.makeMotionAbsolute();
        boolean result = this.pushOutOfBlocks(x, y, z);
//        this.makeMotionRelative();
        return result;
    }


    //TODO: Work out what's wrong with my Access Transformer
    // MethodHandles used in isOnLadder() as my Access Transformer doesn't want to work...
    private static final MethodHandle nextStepDistance_Get;
    private static final MethodHandle nextStepDistance_Set;

    static {
        MethodHandles.Lookup lookup = LookupThief.INSTANCE.lookup(Entity.class);
        try {
            nextStepDistance_Get = lookup.findGetter(Entity.class, "nextStepDistance", int.class);
            nextStepDistance_Set = lookup.findSetter(Entity.class, "nextStepDistance", int.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void setNextStepDistance(int value){
        try {
            nextStepDistance_Set.invokeExact((Entity)this, value);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    private int getNextStepDistance(){
        try {
            return (int)nextStepDistance_Get.invokeExact((Entity)this);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    /**
     * returns true if this entity is by a ladder, false otherwise
     */
    @Override
    public boolean isOnLadder()
    {
        if (this.isSpectator()) {
            return false;
        }

        boolean isMonkeyBars = false;

        AxisAlignedBB bb = this.getEntityBoundingBox();
        if (bb instanceof GravityAxisAlignedBB) {
            GravityAxisAlignedBB gBB = (GravityAxisAlignedBB)bb;
            BlockPos blockpos = new BlockPos(gBB.offset(0, 0.001, 0).getOrigin());
            IBlockState iblockstate = this.worldObj.getBlockState(blockpos);

            boolean isOnLadder = net.minecraftforge.common.ForgeHooks.isLivingOnLadder(iblockstate, worldObj, blockpos, this);

            if (iblockstate != null) {
                if (iblockstate.getBlock().isLadder(iblockstate, worldObj, blockpos, this)) {
                    if (iblockstate.getPropertyNames().contains(BlockHorizontal.FACING)) {
                        EnumFacing facing = iblockstate.getValue(BlockHorizontal.FACING);
                        EnumGravityDirection direction = gBB.getDirection();
                        switch(direction) {
                            case EAST:
                            case WEST:
                                if (facing == EnumFacing.EAST || facing == EnumFacing.WEST) {
                                    isOnLadder = false;
                                }
                                break;
                            case NORTH:
                            case SOUTH:
                                if (facing == EnumFacing.NORTH || facing == EnumFacing.SOUTH) {
                                    isOnLadder = false;
                                }
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
            if (!isOnLadder) {
                Vec3d topOfHead = gBB.offset(0, this.height, 0).getOrigin();
                blockpos = new BlockPos(topOfHead);
                iblockstate = this.worldObj.getBlockState(blockpos);
                if (iblockstate != null) {
                    if (iblockstate.getBlock().isLadder(iblockstate, worldObj, blockpos, this)) {
                        if (iblockstate.getPropertyNames().contains(BlockHorizontal.FACING)) {
                            EnumFacing facing = iblockstate.getValue(BlockHorizontal.FACING);
                            EnumGravityDirection direction = gBB.getDirection();
                            switch(direction) {
                                case EAST:
                                    isOnLadder = facing == EnumFacing.EAST && net.minecraftforge.common.ForgeHooks.isLivingOnLadder(iblockstate, worldObj, blockpos, this);
                                    break;
                                case WEST:
                                    isOnLadder = facing == EnumFacing.WEST && net.minecraftforge.common.ForgeHooks.isLivingOnLadder(iblockstate, worldObj, blockpos, this);
                                    break;
                                case NORTH:
                                    isOnLadder = facing == EnumFacing.NORTH && net.minecraftforge.common.ForgeHooks.isLivingOnLadder(iblockstate, worldObj, blockpos, this);
                                    break;
                                case SOUTH:
                                    isOnLadder = facing == EnumFacing.SOUTH && net.minecraftforge.common.ForgeHooks.isLivingOnLadder(iblockstate, worldObj, blockpos, this);
                                    break;
                                default:
                                    break;
                            }
                            isMonkeyBars = true;
                        }
                    }
                }
            }
            if (isOnLadder && isMonkeyBars) {
                if (this.distanceWalkedOnStepModified > (float)this.getNextStepDistance()) {
                    this.setNextStepDistance((int)this.distanceWalkedOnStepModified + 1);
                    this.playStepSound(blockpos, iblockstate.getBlock());
                }
            }
            return isOnLadder;
        }
        else {
            return super.isOnLadder();
        }
    }
}

package uk.co.mysterymayhem.gravitymod.asm;

import com.mojang.authlib.GameProfile;
import gnu.trove.stack.array.TDoubleArrayStack;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import uk.co.mysterymayhem.gravitymod.GravityMod;
import uk.co.mysterymayhem.gravitymod.api.API;
import uk.co.mysterymayhem.gravitymod.api.EnumGravityDirection;
import uk.co.mysterymayhem.gravitymod.common.capabilities.gravitydirection.IGravityDirectionCapability;
import uk.co.mysterymayhem.gravitymod.common.util.BlockStateHelper;
import uk.co.mysterymayhem.gravitymod.common.util.Vec3dHelper;
import uk.co.mysterymayhem.gravitymod.common.util.boundingboxes.GravityAxisAlignedBB;

import javax.annotation.Nonnull;
import java.util.ArrayDeque;
import java.util.Deque;

import static uk.co.mysterymayhem.gravitymod.common.util.Vec3dHelper.PITCH;
import static uk.co.mysterymayhem.gravitymod.common.util.Vec3dHelper.YAW;

/**
 * Class inserted into EntityPlayerMP and AbstractClientPlayer's hierarchy in order to reduce the amount of ASM needed
 * Created by Mysteryem on 2016-09-04.
 */
public abstract class EntityPlayerWithGravity extends EntityPlayer {

    private enum FieldState {
        ABSOLUTE,
        RELATIVE
    }

    // Relative position doesn't exist, but this allows adjustments made in other methods such as 'this.posY+=3',
    // to correspond to whatever we think 'UP' is
    private boolean positionVarsAreRelative;

    private boolean superConstructorsFinished = false;

    private final TDoubleArrayStack motionXStore = new TDoubleArrayStack(TDoubleArrayStack.DEFAULT_CAPACITY, 0);
    private final TDoubleArrayStack motionYStore = new TDoubleArrayStack(TDoubleArrayStack.DEFAULT_CAPACITY, 0);
    private final TDoubleArrayStack motionZStore = new TDoubleArrayStack(TDoubleArrayStack.DEFAULT_CAPACITY, 0);
    private float rotationYawStore;
    private float rotationPitchStore;

    public double undoMotionXChange() {
        double storedMotion = this.motionXStore.pop();
        double motionChange = this.motionX - storedMotion;
        this.motionX = storedMotion;
        return motionChange;
    }
    public double undoMotionYChange() {
        double storedMotion = this.motionYStore.pop();
        double motionChange = this.motionY - storedMotion;
        this.motionY = storedMotion;
        return motionChange;
    }
    public double undoMotionZChange() {
        double storedMotion = this.motionZStore.pop();
        double motionChange = this.motionZ - storedMotion;
        this.motionZ = storedMotion;
        return motionChange;
    }
    
    public void storeMotionX() {
        this.motionXStore.push(this.motionX);
    }
    public void storeMotionY() {
        this.motionYStore.push(this.motionY);
    }
    public void storeMotionZ() {
        this.motionZStore.push(this.motionZ);
    }

    private final Deque<FieldState> motionFieldStateStack = new ArrayDeque<>();
    {
        // starting state is ABSOLUTE
        motionFieldStateStack.push(FieldState.ABSOLUTE);
    }
    private final Deque<FieldState> rotationFieldStateStack = new ArrayDeque<>();
    {
        // starting state is ABSOLUTE
        rotationFieldStateStack.push(FieldState.ABSOLUTE);
    }

    public EntityPlayerWithGravity(World worldIn, GameProfile gameProfileIn) {
        super(worldIn, gameProfileIn);
        this.superConstructorsFinished = true;

        // Instead of checking if the capability is null every time we set a boundingbox
        IGravityDirectionCapability capability = API.getGravityDirectionCapability(this);
        if (capability == null) {
            //explode
            throw new RuntimeException("[UpAndDown] Some other mod has blocked UpAndDown from adding its GravityDirectionCapability" +
                    " to a player (or has straight up deleted it), this is very bad behaviour and whatever mod is causing this should be notified" +
                    " immediately");
        }

        // We refuse to do these in setPosition called in the Entity constructor before the capability has been added
        // So we do them now, after the Entity constructor has finished and the GravityDirectionCapability has been added

        this.setEntityBoundingBox(Hooks.getGravityAdjustedHitbox(this));
        this.setSize(this.width, this.height);

        this.positionVarsAreRelative = false;
    }

    // Should only ever be used within makeMotionRelative/Absolute and popMotionStack
    @Deprecated
    private void makeMotionFieldsRelative() {
        double[] doubles = API.getGravityDirection(this).getInverseAdjustmentFromDOWNDirection().adjustXYZValues(this.motionX, this.motionY, this.motionZ);
        this.motionX = doubles[0];
        this.motionY = doubles[1];
        this.motionZ = doubles[2];
    }

    // Should only ever be used within makeMotionRelative/Absolute and popMotionStack
    @Deprecated
    private void makeMotionFieldsAbsolute() {
        double[] doubles = API.getGravityDirection(this).adjustXYZValues(this.motionX, this.motionY, this.motionZ);
        this.motionX = doubles[0];
        this.motionY = doubles[1];
        this.motionZ = doubles[2];
    }

    @SuppressWarnings("deprecation")
    void makeMotionRelative() {
        FieldState top = motionFieldStateStack.peek();
        if (top == FieldState.ABSOLUTE) {
            this.makeMotionFieldsRelative();
        }
        motionFieldStateStack.push(FieldState.RELATIVE);
    }

    @SuppressWarnings("deprecation")
    void makeMotionAbsolute() {
        FieldState top = motionFieldStateStack.peek();
        if (top == FieldState.RELATIVE) {
            this.makeMotionFieldsAbsolute();
        }
        motionFieldStateStack.push(FieldState.ABSOLUTE);
    }

    @SuppressWarnings("deprecation")
    void popMotionStack() {
        FieldState removed = motionFieldStateStack.pop();
        FieldState top = motionFieldStateStack.peek();
        if (top != removed) {
            switch(top) {
                case ABSOLUTE:
                    this.makeMotionFieldsAbsolute();
                    break;
                case RELATIVE:
                    this.makeMotionFieldsRelative();
                    break;
            }
        }
    }

    boolean isMotionRelative() {
        return motionFieldStateStack.peek() == FieldState.RELATIVE;
    }

    boolean isMotionAbsolute() {
        return !this.isMotionRelative();
    }

    /**
     * Should only be called by makeRotationAbsolute and popRotationStack
     */
    @Deprecated
    private void makeRotationFieldsAbsolute() {
        this.rotationYaw = this.rotationYawStore;
        this.rotationPitch = this.rotationPitchStore;
    }

    /**
     * Should only be called by makeRotationRelative and popRotationStack
     */
    @Deprecated
    private void makeRotationFieldsRelative() {
        final Vec3d fastAdjustedVectorForRotation =
                API.getGravityDirection(this)
                        .getInverseAdjustmentFromDOWNDirection()
                        .adjustLookVec(Vec3dHelper.getFastVectorForRotation(this.rotationPitch, this.rotationYaw));
        double[] fastPitchAndYawFromVector = Vec3dHelper.getFastPitchAndYawFromVector(fastAdjustedVectorForRotation);

        // Store the absolute yaw and pitch to instance fields so they can be restored easily
        this.rotationYawStore = this.rotationYaw;
        this.rotationPitchStore = this.rotationPitch;
        this.rotationYaw = (float)fastPitchAndYawFromVector[Vec3dHelper.YAW];
        this.rotationPitch = (float)fastPitchAndYawFromVector[Vec3dHelper.PITCH];
    }


    @SuppressWarnings("deprecation")
    void makeRotationAbsolute() {
        FieldState top = rotationFieldStateStack.peek();
        if (top == FieldState.RELATIVE) {
            this.makeRotationFieldsAbsolute();
        }
        rotationFieldStateStack.push(FieldState.ABSOLUTE);
    }

    @SuppressWarnings("deprecation")
    void makeRotationRelative() {
        FieldState top = rotationFieldStateStack.peek();
        if (top == FieldState.ABSOLUTE) {
            this.makeRotationFieldsRelative();
        }
        rotationFieldStateStack.push(FieldState.RELATIVE);
    }

    @SuppressWarnings("deprecation")
    void popRotationStack() {
        FieldState removed = rotationFieldStateStack.pop();
        FieldState top = rotationFieldStateStack.peek();
        if (top != removed) {
            switch(top) {
                case ABSOLUTE:
                    this.makeRotationFieldsAbsolute();
                    break;
                case RELATIVE:
                    this.makeRotationFieldsRelative();
                    break;
            }
        }
    }

    void makePositionRelative() {
        if (!this.positionVarsAreRelative) {
            EnumGravityDirection direction = API.getGravityDirection(this).getInverseAdjustmentFromDOWNDirection();

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
    public void moveEntity(double x, double y, double z) {
        if (this.isMotionAbsolute()) {
            // If moveEntity is called outside of the player tick, then some external force is moving the player
            double[] doubles = Hooks.inverseAdjustXYZ(this, x, y, z);
            x = doubles[0];
            y = doubles[1];
            z = doubles[2];
            this.makeMotionRelative();
            super.moveEntity(x, y, z);
            this.popMotionStack();
        }
        else {
            super.moveEntity(x, y, z);
        }
    }

    // motion should always be relative when jump is called
    @Override
    public void jump() {
        this.makeMotionRelative();
        super.jump();
        this.popMotionStack();
    }

    @Override
    public boolean handleWaterMovement() {
        this.makeMotionAbsolute();
        boolean toReturn = super.handleWaterMovement();
        this.popMotionStack();
        return toReturn;
    }

    // TODO: use the new pitch and yaw that I calculate to create arguments to pass to the super method (so I don't set any rotation values myself)
    @Override
    public void setAngles(float yaw, float pitch) {
        final float absolutePitch = this.rotationPitch;
        final float absoluteYaw = this.rotationYaw;
        final float f2 = this.prevRotationPitch;
        final float f3 = this.prevRotationYaw;

        // Call super to update ridden entity
        super.setAngles(yaw, pitch);

        final EnumGravityDirection direction = API.getGravityDirection(this);
        final EnumGravityDirection reverseDirection = direction.getInverseAdjustmentFromDOWNDirection();

        final double relativePitchChange = -pitch*0.15d;
        final double relativeYawChange = yaw*0.15d;

        final Vec3d normalLookVec = Vec3dHelper.getPreciseVectorForRotation(absolutePitch, absoluteYaw);

        final Vec3d relativeLookVec = reverseDirection.adjustLookVec(normalLookVec);
        final double[] relativePY = Vec3dHelper.getPrecisePitchAndYawFromVector(relativeLookVec);
        final double relativePitch = relativePY[PITCH];
        final double relativeYaw = relativePY[YAW];

        final double changedRelativeYaw = relativeYaw + relativeYawChange;
        final double changedRelativePitch = relativePitch + relativePitchChange;
        final double clampedRelativePitch;

        // Any closer to -90 or 90 produce tiny values that the trig functions will effectively treat as zero
        // this causes an inability to rotate the camera when looking straight up or down
        // While, it's not a problem for UP and DOWN directions, it causes issues when going from UP/DOWN to a different
        // direction, so I've capped UP and DOWN directions as well
        final double maxRelativeYaw = /*direction == EnumGravityDirection.UP || direction == EnumGravityDirection.DOWN ? 90d :*/ 89.99d;
        final double minRelativeYaw = /*direction == EnumGravityDirection.UP || direction == EnumGravityDirection.DOWN ? -90d :*/ -89.99d;

        if (changedRelativePitch > maxRelativeYaw) {
            clampedRelativePitch = maxRelativeYaw;
        }
        else if (changedRelativePitch < minRelativeYaw) {
            clampedRelativePitch = minRelativeYaw;
        }
        else {
            clampedRelativePitch = changedRelativePitch;
        }

        // Directly set pitch and yaw
        final Vec3d relativeChangedLookVec = Vec3dHelper.getPreciseVectorForRotation(clampedRelativePitch, changedRelativeYaw);

        final Vec3d absoluteLookVec = direction.adjustLookVec(relativeChangedLookVec);
        final double[] absolutePY = Vec3dHelper.getPrecisePitchAndYawFromVector(absoluteLookVec);

        final double changedAbsolutePitch = absolutePY[PITCH];
        final double changedAbsoluteYaw = (absolutePY[YAW] % 360);

        // Yaw calculated through yaw change
        final double absoluteYawChange;
        final double effectiveStartingAbsoluteYaw = absoluteYaw % 360;

        // Limit the change in yaw to 180 degrees each tick
        if (Math.abs(effectiveStartingAbsoluteYaw - changedAbsoluteYaw) > 180) {
            if (effectiveStartingAbsoluteYaw < changedAbsoluteYaw) {
                absoluteYawChange = changedAbsoluteYaw - (effectiveStartingAbsoluteYaw + 360);
            }
            else {
                absoluteYawChange = (changedAbsoluteYaw + 360) - effectiveStartingAbsoluteYaw;
            }
        }
        else {
            absoluteYawChange = changedAbsoluteYaw - effectiveStartingAbsoluteYaw;
        }

        this.rotationYaw = (float)(absoluteYaw + absoluteYawChange);

        this.rotationPitch = (float)changedAbsolutePitch;
        this.prevRotationPitch = f2;
        this.prevRotationPitch += this.rotationPitch - absolutePitch;
        this.prevRotationYaw = f3;
        this.prevRotationYaw += this.rotationYaw - absoluteYaw;
    }

    public Vec3d getSuperLook(float partialTicks) {
        return super.getLook(partialTicks);
    }

    @Override
    public void setEntityBoundingBox(@Nonnull AxisAlignedBB bb) {
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
                GravityMod.logWarning("Player bounding box is not gravity aware. In [UpAndDownAndAllAround]:EntityPlayerWithGravity::updateFallState");
            }

            IBlockState iblockstate = this.worldObj.getBlockState(blockpos);

            if (iblockstate.getBlock().isAir(iblockstate, this.worldObj, blockpos))
            {
                BlockPos blockpos1 = API.getGravityDirection(this).makeRelativeBlockPos(blockpos).down();

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
        if (this.noClip || this.sleeping) {
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
    public float getEyeHeight() {
        return API.getGravityDirection(this).getEntityEyeHeight(this);
    }

    public float super_getEyeHeight() {
        return super.getEyeHeight();
    }

    // TODO: ASM
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

    // TODO: ASM
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
            //TODO: ASM
            //begin
            AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
            //delete line
            //axisalignedbb = new AxisAlignedBB(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ, axisalignedbb.minX + (double)f, axisalignedbb.minY + (double)f1, axisalignedbb.minZ + (double)f);
            double heightExpansion = (f1 - this.height)/2d;
            double widthExpansion = (f - this.width)/2d;
            axisalignedbb = axisalignedbb.expand(widthExpansion, heightExpansion, widthExpansion).offset(0, heightExpansion, 0);
            //end ASM
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
    @Override
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
        API.getGravityDirection(this).resetPositionToBB(this);
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

        // We need to make sure that the AttachCapabilitiesEvent has been fired before trying to set the boundingbox
        if (this.superConstructorsFinished) {
            this.setEntityBoundingBox(Hooks.getGravityAdjustedHitbox(this));
            this.setSize(this.width, this.height);
        }
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
    boolean pushOutOfBlocksDelegate(double x, double y, double z) {
//        this.makeMotionAbsolute();
        boolean result = this.pushOutOfBlocks(x, y, z);
//        this.makeMotionRelative();
        return result;
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

            //iblockstate will never be null (will be air or will throw an exception if stuff isn't right)
//            if (iblockstate != null) {
            if (iblockstate.getBlock().isLadder(iblockstate, worldObj, blockpos, this)) {
                EnumFacing facing = BlockStateHelper.getFacingOfBlockState(iblockstate);
                if (facing != null) {
                    EnumGravityDirection direction = gBB.getDirection();
                    isOnLadder = direction.isValidLadderDirection(facing);
                }
            }
//            }
            if (!isOnLadder) {
                Vec3d topOfHead = gBB.offset(0, this.height, 0).getOrigin();
                blockpos = new BlockPos(topOfHead);
                iblockstate = this.worldObj.getBlockState(blockpos);
                //As before, iblockstate will not be null
//                if (iblockstate != null) {
                if (iblockstate.getBlock().isLadder(iblockstate, worldObj, blockpos, this)) {
                    EnumFacing facing = BlockStateHelper.getFacingOfBlockState(iblockstate);
                    if (facing != null) {
                        EnumGravityDirection direction = gBB.getDirection();
                        isOnLadder = facing == direction.getFacingEquivalent() && net.minecraftforge.common.ForgeHooks.isLivingOnLadder(iblockstate, worldObj, blockpos, this);
                        isMonkeyBars = true;
                    }
                }
//                }
            }
            if (isOnLadder && isMonkeyBars) {
                if (this.distanceWalkedOnStepModified > (float)this.nextStepDistance) {
                    this.nextStepDistance = (int)this.distanceWalkedOnStepModified + 1;
                    this.playStepSound(blockpos, iblockstate.getBlock());
                }
            }
            return isOnLadder;
        }
        else {
            return super.isOnLadder();
        }
    }

    @Override
    protected void playStepSound(BlockPos pos, Block blockIn) {
        AxisAlignedBB entityBoundingBox = this.getEntityBoundingBox();
        if (entityBoundingBox instanceof GravityAxisAlignedBB) {
            EnumGravityDirection direction = ((GravityAxisAlignedBB) entityBoundingBox).getDirection();
            if (direction == EnumGravityDirection.DOWN) {
                super.playStepSound(pos, blockIn);
            }
            else {
                // Bypass check for pos.up().getBlock() == Blocks.SNOW_LAYER
                SoundType soundtype = blockIn.getSoundType(worldObj.getBlockState(pos), worldObj, pos, this);

                if (!blockIn.getDefaultState().getMaterial().isLiquid()) {
                    this.playSound(soundtype.getStepSound(), soundtype.getVolume() * 0.15F, soundtype.getPitch());
                }
            }
        }
        else {
            super.playStepSound(pos, blockIn);
        }
    }

    // This only works in a deobfuscated dev environment and is only used in the dev environment for debugging/coding purposes
    // Outside the dev environment, this method won't be overridden, as the name of the method in this class will not be
    // reobfuscated (probably)
    // Required so we can call this method from inside this package, this will end up calling EntityPlayerSP::updateAutoJump instead
    @SuppressWarnings("WeakerAccess")
    @SideOnly(Side.CLIENT)
    protected void updateAutoJump(float xChange, float zChange) {
        FMLLog.warning("Erroneously tried to call func_189810_i(auto-jump method) from " + this);
//        throw new RuntimeException("Unreachable code reached");
    }


    /**
     * In vanilla code, knockBack is called with arguments usually calculated based off of the attacker's rotation
     * (knockback enchantment) or the difference in position between the attacker and the target (most entity attacks).
     * We try to work out which case has been called and adjust accordingly for this player's current gravity direction.
     * @param attacker
     * @param strength
     * @param xRatio
     * @param zRatio
     */
    @Override
    public void knockBack(Entity attacker, float strength, double xRatio, double zRatio) {
//        FMLLog.info("x:%s, z:%s", xRatio, zRatio);
        this.makeMotionRelative();
        if (attacker != null) {
            // Standard mob attacks, also arrows, not sure about others (checked first
            if (xRatio == attacker.posX - this.posX && zRatio == attacker.posZ - this.posZ) {
//                FMLLog.info("Distance based attack");
                EnumGravityDirection direction = API.getGravityDirection(this).getInverseAdjustmentFromDOWNDirection();
                double[] d_attacker = direction.adjustXYZValues(attacker.posX, attacker.posY, attacker.posZ);
                double[] d_player = direction.adjustXYZValues(this.posX, this.posY, this.posZ);
                xRatio = d_attacker[0] - d_player[0];
                zRatio = d_attacker[2] - d_player[2];
                super.knockBack(attacker, strength, xRatio, zRatio);
            }
            // Usually the knockback enchantment
            else if (xRatio == (double)MathHelper.sin(attacker.rotationYaw * 0.017453292F) && zRatio == (double)-MathHelper.cos(attacker.rotationYaw * 0.017453292F)) {
//                FMLLog.info("Rotation based attack");
                Vec3d lookVec = attacker.getLookVec();
                EnumGravityDirection direction = API.getGravityDirection(this);
                Vec3d adjustedLook = direction.adjustLookVec(lookVec);
                xRatio = -adjustedLook.xCoord;
                zRatio = -adjustedLook.zCoord;
                super.knockBack(attacker, strength, xRatio, zRatio);
            }
            else {
//                FMLLog.info("Unknown attack");
                super.knockBack(attacker, strength, xRatio, zRatio);
            }
        }
        else {
            super.knockBack(attacker, strength, xRatio, zRatio);
        }
        this.popMotionStack();
    }

}

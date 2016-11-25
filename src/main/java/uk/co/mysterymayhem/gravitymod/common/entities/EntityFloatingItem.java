package uk.co.mysterymayhem.gravitymod.common.entities;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import uk.co.mysterymayhem.gravitymod.common.ModItems;
import uk.co.mysterymayhem.gravitymod.common.config.ConfigHandler;

/**
 * Created by Mysteryem on 2016-11-09.
 */
public class EntityFloatingItem extends EntityItem {

    // Using setDead to spawn the item instead of an item pickup listener so that machines and more can cause the dust item to spawn
    @Override
    public void setDead() {
        // Only spawn the new item if setDead wasn't called due to the entity despawning

        if (!this.worldObj.isRemote && this.age < this.lifespan && this.health > 0 && this.getEntityItem() != null /*&& stack.stackSize <= 0*/) {
            World world;
            EntityItem newItem = new EntityItem(world = this.worldObj, this.posX, this.posY, this.posZ, new ItemStack(ModItems.gravityDust, ConfigHandler.gravityDustAmountDropped));
//                newItem.setNoPickupDelay();
//            newItem.lifespan = 20 * 10;
            newItem.motionX *= 0.1;
            newItem.motionY *= 0.1;
            newItem.motionZ *= 0.1;
            newItem.setEntityInvulnerable(true);
            world.spawnEntityInWorld(newItem);
//            this.worldObj.playEvent(2003, new BlockPos(this), 0);
        }
        super.setDead();
    }

    public static final String NAME = "itemfloating";

    // The EntityItem pickup delay is set to infinite so that this entity will not combine with other <? extends EntityItem>s
    private int actualPickupDelay;


    public EntityFloatingItem(World worldIn, double x, double y, double z) {
        super(worldIn, x, y, z);
        this.setNoGravity(true);
        super.setInfinitePickupDelay();
    }

    public EntityFloatingItem(World worldIn, double x, double y, double z, ItemStack stack) {
        super(worldIn, x, y, z, stack);
        this.motionY = (double)((float)(Math.random() * 0.20000000298023224D - 0.10000000149011612D));
        this.setNoGravity(true);
        super.setInfinitePickupDelay();
    }

    public EntityFloatingItem(World worldIn) {
        super(worldIn);
        this.setNoGravity(true);
        super.setInfinitePickupDelay();
    }

    @Override
    public void setDefaultPickupDelay() {
        this.actualPickupDelay = 10;
    }

    @Override
    public void setNoPickupDelay() {
        this.actualPickupDelay = 0;
    }

    @Override
    public void setInfinitePickupDelay() {
        this.actualPickupDelay = 32767;
    }

    @Override
    public void setPickupDelay(int ticks) {
        this.actualPickupDelay = ticks;
    }

    @Override
    public boolean cannotPickup() {
        return this.actualPickupDelay > 0;
    }

    @Override
    public void onCollideWithPlayer(EntityPlayer entityIn) {
        if (!this.worldObj.isRemote) {
            if (!this.cannotPickup()) {
                super.setNoPickupDelay();
                super.onCollideWithPlayer(entityIn);
                super.setInfinitePickupDelay();
            }
        }
    }

    @Override
    public void onUpdate() {
        if (this.firstUpdate) {
            if (this.motionY > 0.04) {
                this.motionY = 0.04;
            }
            if (this.worldObj.isRemote) {
                BlockPos blockPosIn = new BlockPos(this);
                World world = this.worldObj;
//                entityItem.worldObj.playEvent(2003, new BlockPos(entityItem), 0);
                double d0 = this.posX;
                double d1 = this.posY + 0.5;
                double d2 = this.posZ;


                for (double d11 = 0.0D; d11 < (Math.PI * 2D); d11 += 0.15707963267948966D)
                {
                    world.spawnParticle(EnumParticleTypes.SPELL_WITCH, d0, d1 - 0.4D, d2, Math.cos(d11) * -5.0D + this.motionX, 0.0D, Math.sin(d11) * -5.0D + this.motionZ);
                    world.spawnParticle(EnumParticleTypes.SPELL_WITCH, d0, d1 - 0.4D, d2, Math.cos(d11) * -7.0D + this.motionX, 0.0D, Math.sin(d11) * -7.0D + this.motionZ);
                }
            }
        }

        boolean remote = this.worldObj.isRemote;

        if (this.onGround) {
//            this.isCollidedVertically = false;
//            this.isAirBorne = true;
            this.motionY += 0.01;
        }
        else if (this.motionY > -0.1) {
            this.motionY -= 0.00075;
        }

        double xBefore = this.posX;
        double yBefore = this.posY;
        double zBefore = this.posZ;

        double xMotionBefore = this.motionX;
        double yMotionBefore = this.motionY;
        double zMotionBefore = this.motionZ;

        super.onUpdate();

        if (this.actualPickupDelay > 0 && this.actualPickupDelay != 32767)
        {
            --this.actualPickupDelay;
        }

        double xToAdd = (this.posX - xBefore) - xMotionBefore;
        double yToAdd = (this.posY - yBefore) - yMotionBefore;
        double zToAdd = (this.posZ - zBefore) - zMotionBefore;

        this.motionX += xToAdd;
        this.motionY += yToAdd;
        this.motionZ += zToAdd;

        this.motionX /= 0.9810000190734863D;
        this.motionY /= 0.9810000190734863D;
        this.motionZ /= 0.9810000190734863D;

        double minMovement = 0.1;
        double minMovementSquared = minMovement * minMovement;

        Vec3d motionVec = new Vec3d(this.motionX, this.motionY, this.motionZ);
        double squareLength = motionVec.lengthSquared();
        if (squareLength == 0) {
            this.motionY = minMovement;
            squareLength = minMovementSquared;
        }
        if (squareLength < minMovementSquared) {
            motionVec = motionVec.scale(MathHelper.sqrt_double(minMovementSquared/squareLength));
        }
        double maxSpeed = 0.1;
        this.motionX = motionVec.xCoord;
        this.motionY = motionVec.yCoord;
        this.motionZ = motionVec.zCoord;
        this.motionX = MathHelper.clamp_double(this.motionX, -maxSpeed, maxSpeed);
        this.motionY = MathHelper.clamp_double(this.motionY, -maxSpeed, maxSpeed);
        this.motionZ = MathHelper.clamp_double(this.motionZ, -maxSpeed, maxSpeed);

        if (remote) {
            float yOffset = 0.6f;
            double yPos = this.posY - yOffset;
            // Smoke trail effect
//            this.worldObj.spawnParticle(EnumParticleTypes.PORTAL, this.posX, this.posY-0.3, this.posZ, f-this.motionX+f*this.rand.nextFloat(), f-this.motionY+f*this.rand.nextFloat(), f-this.motionZ+f*this.rand.nextFloat());

            this.worldObj.spawnParticle(EnumParticleTypes.PORTAL, this.posX, yPos, this.posZ, -this.motionX+randParticleMotion(), 0.1-this.motionY+randParticleMotion(), -this.motionZ+randParticleMotion());
            this.worldObj.spawnParticle(EnumParticleTypes.PORTAL, this.posX, yPos, this.posZ, -this.motionX+randParticleMotion(), 0.1-this.motionY+randParticleMotion(), -this.motionZ+randParticleMotion());
            this.worldObj.spawnParticle(EnumParticleTypes.PORTAL, this.posX, yPos, this.posZ, -this.motionX+randParticleMotion(), 0.1-this.motionY+randParticleMotion(), -this.motionZ+randParticleMotion());
        }
    }

    private float randParticleMotion() {
        return (this.rand.nextFloat()-0.5f)*1f;
    }

    @Override
    public void moveEntity(double x, double y, double z) {
        double xPosBefore = this.posX;
        double yPosBefore = this.posY;
        double zPosBefore = this.posZ;
        super.moveEntity(x, y, z);

        if (!this.worldObj.isRemote && this.isCollided/*(Math.abs(xToAdd) > 0.001 || Math.abs(yToAdd + 0.03999999910593033D) > 0.001 || Math.abs(zToAdd) > 0.001)*/) {

            double xDiff = this.posX - (xPosBefore + x);
            double yDiff = this.posY - (yPosBefore + y);
            double zDiff = this.posZ - (zPosBefore + z);

            BlockPos current = new BlockPos(this);

            if (xDiff > 0) {
                current = current.west();
                this.playBounceSound(current, this.worldObj.getBlockState(current).getBlock());
            }
            else if (xDiff < 0) {
                current = current.east();
                this.playBounceSound(current, this.worldObj.getBlockState(current).getBlock());
            }
            else if (yDiff > 0) {
                current = current.down();
                // super call so snow layer sounds play
                this.playBounceSoundSnowCheck(current, this.worldObj.getBlockState(current).getBlock());
            }
            else if (yDiff < 0) {
                current = current.up();
                this.playBounceSound(current, this.worldObj.getBlockState(current).getBlock());
            }
            else if (zDiff > 0) {
                current = current.north();
                this.playBounceSound(current, this.worldObj.getBlockState(current).getBlock());
            }
            else if (zDiff < 0) {
                current = current.south();
                this.playBounceSound(current, this.worldObj.getBlockState(current).getBlock());
            }
        }
    }

    // Similar to this.playStepSound
    private void playBounceSound(BlockPos pos, Block blockIn) {
        SoundType soundtype = blockIn.getSoundType(worldObj.getBlockState(pos), worldObj, pos, this);

        if (!blockIn.getDefaultState().getMaterial().isLiquid())
        {
            this.playSound(soundtype.getHitSound(), soundtype.getVolume() * 0.15F, soundtype.getPitch());
        }
    }

    //Similar to this.playStepSound
    private void playBounceSoundSnowCheck(BlockPos pos, Block blockIn) {

        if (this.worldObj.getBlockState(pos.up()).getBlock() == Blocks.SNOW_LAYER)
        {
            SoundType soundtype = Blocks.SNOW_LAYER.getSoundType(worldObj.getBlockState(pos), worldObj, pos, this);
            this.playSound(soundtype.getHitSound(), soundtype.getVolume() * 0.15F, soundtype.getPitch());
        }
        else {
            this.playBounceSound(pos, blockIn);
        }
    }
}

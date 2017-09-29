package uk.co.mysterymayhem.gravitymod.common.entities;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import uk.co.mysterymayhem.gravitymod.common.config.ConfigHandler;
import uk.co.mysterymayhem.gravitymod.common.registries.IGravityModEntityClassWrapper;
import uk.co.mysterymayhem.gravitymod.common.registries.StaticItems;
import uk.co.mysterymayhem.gravitymod.common.util.ReflectionLambdas;
import uk.co.mysterymayhem.mystlib.annotations.UsedReflexively;

/**
 * Created by Mysteryem on 2016-11-09.
 */
public class EntityFloatingItem extends EntityItem {

    // The EntityItem pickup delay is set to infinite so that this entity will not combine with other <? extends EntityItem>s
    private int actualPickupDelay;

    public EntityFloatingItem(World worldIn, double x, double y, double z, ItemStack stack) {
        super(worldIn, x, y, z, stack);
        this.motionY = (double)((float)(Math.random() * 0.20000000298023224D - 0.10000000149011612D));
        this.setNoGravity(true);
        super.setInfinitePickupDelay();
    }


    @UsedReflexively
    public EntityFloatingItem(World worldIn) {
        super(worldIn);
        this.setNoGravity(true);
        super.setInfinitePickupDelay();
    }

    // Provides Veinminer compatibility before their code gets fixed
    // I think it's safe to spawn the extra item.
    //  If drops are disabled, the original item won't be added to the drops and we won't create an EntityFloatingItem?
    //  Also, if this event gets cancelled for some reason other than veinminer, the event for the extra item that is
    //      dropped normally would also likely get cancelled
    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
        Entity entity;
        World world;
        // Event cancelled, entity is EntityFloatingItem, event is server side
        if (event.isCanceled()
                && (entity = event.getEntity()) != null
                && entity instanceof EntityFloatingItem
                && (world = entity.world) != null
                && !world.isRemote) {
            // Spawn the extra item that would be dropped normally
            EntityItem newItem = new EntityItem(world, entity.posX, entity.posY, entity.posZ, new ItemStack(StaticItems.GRAVITY_DUST, ConfigHandler.gravityDustAmountDropped));
            newItem.motionX *= 0.1;
            newItem.motionY *= 0.1;
            newItem.motionZ *= 0.1;
            newItem.setEntityInvulnerable(true);
            world.spawnEntity(newItem);
        }
    }

    @Override
    public void onUpdate() {
        if (this.firstUpdate) {
            if (this.motionY > 0.04) {
                this.motionY = 0.04;
            }
            if (this.world.isRemote) {
                BlockPos blockPosIn = new BlockPos(this);
                World world = this.world;
//                entityItem.worldObj.playEvent(2003, new BlockPos(entityItem), 0);
                double d0 = this.posX;
                double d1 = this.posY + 0.5;
                double d2 = this.posZ;


                for (double d11 = 0.0D; d11 < (Math.PI * 2D); d11 += 0.15707963267948966D) {
                    world.spawnParticle(EnumParticleTypes.SPELL_WITCH, d0, d1 - 0.4D, d2, Math.cos(d11) * -5.0D + this.motionX, 0.0D, Math.sin(d11) * -5.0D + this.motionZ);
                    world.spawnParticle(EnumParticleTypes.SPELL_WITCH, d0, d1 - 0.4D, d2, Math.cos(d11) * -7.0D + this.motionX, 0.0D, Math.sin(d11) * -7.0D + this.motionZ);
                }
            }
        }

        boolean remote = this.world.isRemote;

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

        if (this.actualPickupDelay > 0 && this.actualPickupDelay != 32767) {
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
            motionVec = motionVec.scale(MathHelper.sqrt(minMovementSquared / squareLength));
        }
        double maxSpeed = 0.1;
        this.motionX = motionVec.x;
        this.motionY = motionVec.y;
        this.motionZ = motionVec.z;
        this.motionX = MathHelper.clamp(this.motionX, -maxSpeed, maxSpeed);
        this.motionY = MathHelper.clamp(this.motionY, -maxSpeed, maxSpeed);
        this.motionZ = MathHelper.clamp(this.motionZ, -maxSpeed, maxSpeed);

        if (remote) {
            float yOffset = 0.6f;
            double yPos = this.posY - yOffset;
            // Smoke trail effect
//            this.worldObj.spawnParticle(EnumParticleTypes.PORTAL, this.posX, this.posY-0.3, this.posZ, f-this.motionX+f*this.rand.nextFloat(), f-this.motionY+f*this.rand.nextFloat(), f-this.motionZ+f*this.rand.nextFloat());

            this.world.spawnParticle(EnumParticleTypes.PORTAL, this.posX, yPos, this.posZ, -this.motionX + this.randParticleMotion(), 0.1 - this.motionY + this
                    .randParticleMotion(), -this.motionZ + this.randParticleMotion());
            this.world.spawnParticle(EnumParticleTypes.PORTAL, this.posX, yPos, this.posZ, -this.motionX + this.randParticleMotion(), 0.1 - this.motionY + this
                    .randParticleMotion(), -this.motionZ + this.randParticleMotion());
            this.world.spawnParticle(EnumParticleTypes.PORTAL, this.posX, yPos, this.posZ, -this.motionX + this.randParticleMotion(), 0.1 - this.motionY + this
                    .randParticleMotion(), -this.motionZ + this.randParticleMotion());
        }
    }

    private float randParticleMotion() {
        return (this.rand.nextFloat() - 0.5f) * 1f;
    }

    @Override
    public void onCollideWithPlayer(EntityPlayer entityIn) {
        if (!this.world.isRemote) {
            if (!this.cannotPickup()) {
                super.setNoPickupDelay();
                super.onCollideWithPlayer(entityIn);
                super.setInfinitePickupDelay();
            }
        }
    }

    @Override
    public boolean cannotPickup() {
        return this.actualPickupDelay > 0;
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

    // Won't replace with an item pickup listener, it's possible that
    // Using setDead to spawn the item instead of an item pickup listener so that machines and more can cause the dust item to spawn
    @Override
    public void setDead() {
        // Only spawn the new item if setDead wasn't called due to the entity despawning

        if (!this.world.isRemote
                && ReflectionLambdas.get_EntityItem$age.applyAsInt(this) < this.lifespan
                && ReflectionLambdas.get_EntityItem$health.applyAsInt(this) > 0
                && !this.getItem().isEmpty()
                /*&& stack.stackSize <= 0*/) {
            World world;
            if (this.getItem().getItem() != StaticItems.GRAVITY_DUST) {
                EntityItem newItem = new EntityItem(world = this.world, this.posX, this.posY, this.posZ, new ItemStack(StaticItems.GRAVITY_DUST,
                        ConfigHandler.gravityDustAmountDropped));
//                newItem.setNoPickupDelay();
//            newItem.lifespan = 20 * 10;
                newItem.motionX *= 0.1;
                newItem.motionY *= 0.1;
                newItem.motionZ *= 0.1;
                newItem.setEntityInvulnerable(true);
                world.spawnEntity(newItem);
//            this.worldObj.playEvent(2003, new BlockPos(this), 0);
            }
        }
        super.setDead();
    }

    @Override
    public void move(MoverType moverType, double x, double y, double z) {
        double xPosBefore = this.posX;
        double yPosBefore = this.posY;
        double zPosBefore = this.posZ;
        super.move(moverType, x, y, z);

        World world = this.world;

        if (!world.isRemote && this.isCollided/*(Math.abs(xToAdd) > 0.001 || Math.abs(yToAdd + 0.03999999910593033D) > 0.001 || Math.abs(zToAdd) > 0.001)*/) {

            double xDiff = this.posX - (xPosBefore + x);
            double yDiff = this.posY - (yPosBefore + y);
            double zDiff = this.posZ - (zPosBefore + z);

            BlockPos current = new BlockPos(this);

            if (xDiff > 0) {
                current = current.west();
                this.playBounceSound(current, world.getBlockState(current).getBlock());
            }
            else if (xDiff < 0) {
                current = current.east();
                this.playBounceSound(current, world.getBlockState(current).getBlock());
            }
            else if (yDiff > 0) {
                current = current.down();
                // super call so snow layer sounds play
                this.playBounceSoundSnowCheck(current, world.getBlockState(current).getBlock());
            }
            else if (yDiff < 0) {
                current = current.up();
                this.playBounceSound(current, world.getBlockState(current).getBlock());
            }
            else if (zDiff > 0) {
                current = current.north();
                this.playBounceSound(current, world.getBlockState(current).getBlock());
            }
            else if (zDiff < 0) {
                current = current.south();
                this.playBounceSound(current, world.getBlockState(current).getBlock());
            }
        }
    }

    // Similar to this.playStepSound
    private void playBounceSound(BlockPos pos, Block blockIn) {
        SoundType soundtype = blockIn.getSoundType(this.world.getBlockState(pos), this.world, pos, this);

        if (!blockIn.getDefaultState().getMaterial().isLiquid()) {
            this.playSound(soundtype.getHitSound(), soundtype.getVolume() * 0.15F, soundtype.getPitch());
        }
    }

    //Similar to this.playStepSound
    private void playBounceSoundSnowCheck(BlockPos pos, Block blockIn) {

        if (this.world.getBlockState(pos.up()).getBlock() == Blocks.SNOW_LAYER) {
            SoundType soundtype = Blocks.SNOW_LAYER.getSoundType(this.world.getBlockState(pos), this.world, pos, this);
            this.playSound(soundtype.getHitSound(), soundtype.getVolume() * 0.15F, soundtype.getPitch());
        }
        else {
            this.playBounceSound(pos, blockIn);
        }
    }

    /**
     * Created by Mysteryem on 2016-12-13.
     */
    public static class Wrapper implements IGravityModEntityClassWrapper<EntityFloatingItem> {

        @Override
        public Class<EntityFloatingItem> getEntityClass() {
            return EntityFloatingItem.class;
        }

        @Override
        public int getTrackingRange() {
            //extra range over EntityItem
            return 80;
        }

        @Override
        public int getUpdateFrequency() {
            return 5;
        }

        @Override
        public boolean sendsVelocityUpdates() {
            return true;
        }

        @Override
        public String getModObjectName() {
            return "itemfloating";
        }

    }
}

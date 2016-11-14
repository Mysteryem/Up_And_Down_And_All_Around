package uk.co.mysterymayhem.gravitymod.common.entities;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import uk.co.mysterymayhem.gravitymod.GravityMod;
import uk.co.mysterymayhem.gravitymod.client.renderers.RenderGravityEntityItem;
import uk.co.mysterymayhem.gravitymod.common.ModItems;
import uk.co.mysterymayhem.gravitymod.common.config.ConfigHandler;

/**
 * Created by Mysteryem on 2016-11-09.
 */
public class EntityFloatingItem extends EntityItem {

//    public static class PickupListener{
//        @SubscribeEvent(priority = EventPriority.LOWEST)
//        public void onPlayerPickUpItem(PlayerEvent.ItemPickupEvent event) {
//            EntityPlayer player = event.player;
//            if (player == null || player instanceof FakePlayer) {
//                return;
//            }
//            EntityItem entityItem = event.pickedUp;
//            if (entityItem instanceof EntityFloatingItem) {
//                World world;
//                EntityItem newItem = new EntityItem(world = player.worldObj, player.posX, player.posY, player.posZ, new ItemStack(ModItems.gravityDust));
////                newItem.setNoPickupDelay();
//                world.spawnEntityInWorld(newItem);
//            }
//        }
//    }

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


    public EntityFloatingItem(World worldIn, double x, double y, double z) {
        super(worldIn, x, y, z);
        this.setNoGravity(true);
    }

    public EntityFloatingItem(World worldIn, double x, double y, double z, ItemStack stack) {
        super(worldIn, x, y, z, stack);
        this.motionY = (double)((float)(Math.random() * 0.20000000298023224D - 0.10000000149011612D));
        this.setNoGravity(true);
    }

    public EntityFloatingItem(World worldIn) {
        super(worldIn);
        this.setNoGravity(true);
    }

    private static final NBTTagCompound fireworkTag = new NBTTagCompound();
    static {
        NBTTagList nbtTagList = new NBTTagList();
        NBTTagCompound explosionTag = new NBTTagCompound();
        explosionTag.setBoolean("Flicker", true);
        explosionTag.setBoolean("Trail", true);
        explosionTag.setByte("Type", (byte)0);
        explosionTag.setIntArray("Colors", new int[]{3145970,16713728,6215936});
        explosionTag.setIntArray("FadeColors", new int[]{857279,16734553,7012185});
        nbtTagList.appendTag(explosionTag);
        fireworkTag.setTag("Explosions", nbtTagList);
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
//        if (this.ticksExisted != this.getAge() + 1) {
//            this.lifespan = this.getAge() + ENTITY_LIFETIME_TICKS;
//            this.ticksExisted = this.getAge() + 1;
//        }
        boolean remote = this.worldObj.isRemote;

////        this.setGlowing(true);
        if (this.onGround) {
//            this.isCollidedVertically = false;
//            this.isAirBorne = true;
            this.motionY += 0.01;
        }
        else if (this.motionY > -0.1) {
            this.motionY -= 0.00075;
        }
//        this.isAirBorne = true;
//        this.onGround = false;
//        if (this.isCollidedHorizontally) {
//            this.motionX *= -1;
//            this.motionZ *= -1;
//        }
//        if (this.isCollidedVertically) {
//            this.motionY *= -1;
//        }

        double xBefore = this.posX;
        double yBefore = this.posY;
        double zBefore = this.posZ;

        double xMotionBefore = this.motionX;
        double yMotionBefore = this.motionY;
        double zMotionBefore = this.motionZ;

        super.onUpdate();

        double xToAdd = (this.posX - xBefore) - xMotionBefore;
        double yToAdd = (this.posY - yBefore) - yMotionBefore;
        double zToAdd = (this.posZ - zBefore) - zMotionBefore;

        this.motionX += xToAdd;
        this.motionY += yToAdd;
        this.motionZ += zToAdd;

        // I don't know why I've got to add this slightly less than twice
//        this.motionY += 0.03999999910593033D;
//        this.motionY += 0.0380D;
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
                this.playStepSound(current, this.worldObj.getBlockState(current).getBlock());
            }
            else if (xDiff < 0) {
                current = current.east();
                this.playStepSound(current, this.worldObj.getBlockState(current).getBlock());
            }
            else if (yDiff > 0) {
                current = current.down();
                this.playStepSound(current, this.worldObj.getBlockState(current).getBlock());
            }
            else if (yDiff < 0) {
                current = current.up();
                this.playStepSound(current, this.worldObj.getBlockState(current).getBlock());
            }
            else if (zDiff > 0) {
                current = current.north();
                this.playStepSound(current, this.worldObj.getBlockState(current).getBlock());
            }
            else if (zDiff < 0) {
                current = current.south();
                this.playStepSound(current, this.worldObj.getBlockState(current).getBlock());
            }

//            this.worldObj.makeFireworks(this.posX, this.posY, this.posZ, this.motionX, this.motionY, this.motionZ, fireworkTag);
//            this.playSound(SoundEvents.BLOCK_NOTE_HARP, 0.05F, 0.3F + this.rand.nextFloat() * 0.4F);
        }
    }
}

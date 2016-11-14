package uk.co.mysterymayhem.gravitymod.common.entities;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import uk.co.mysterymayhem.gravitymod.GravityMod;
import uk.co.mysterymayhem.gravitymod.api.EnumGravityDirection;
import uk.co.mysterymayhem.gravitymod.client.renderers.RenderGravityEntityItem;

/**
 * Created by Mysteryem on 2016-11-05.
 */
public class EntityGravityItem extends EntityItem implements IEntityAdditionalSpawnData {
    // From EntityItem::onUpdate
//    public static final double GRAVITY_DOWNWARDS_MOTION = 0.03999999910593033D;

    public static final String NAME = "itemgravity";

    private EnumGravityDirection direction = EnumGravityDirection.DOWN;

//    private boolean actuallyOnGround = false;

    @SuppressWarnings("unused")
    public EntityGravityItem(World world) {
        super(world);
        this.setNoGravity(true);
    }

    //    public EntityGravityItem(EnumGravityDirection direction, World worldIn, double x, double y, double z, ItemStack stack) {
//        super(worldIn, x, y, z, stack);
////        this.direction = direction;
//    }

    public EntityGravityItem(EnumGravityDirection direction, EntityItem orig) {
        super(orig.worldObj);
        this.setNoGravity(true);
        this.direction = direction;
        NBTTagCompound tag = new NBTTagCompound();
        orig.writeToNBT(tag);
//        orig.writeEntityToNBT(tag);
        super.readFromNBT(tag);
//        super.readEntityFromNBT(tag);
//        this.direction = direction;
//        this.motionX = orig.motionX;
//        this.motionY = orig.motionY;
//        this.motionZ = orig.motionZ;
//        this.prevPosX = orig.prevPosX;
//        this.prevPosY = orig.prevPosY;
//        this.prevPosZ = orig.prevPosZ;
//        this.hoverStart = orig.hoverStart;
//        this.lifespan = orig.lifespan;
//        this.onGround = orig.onGround;
//        this.isCollided = orig.isCollided;
//        this.health = orig.health;
    }

    private static final String DIRECTION_NBT_STRING = "gravitydirection";

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        if (compound.hasKey(DIRECTION_NBT_STRING)) {
            this.direction = EnumGravityDirection.getSafeDirectionFromOrdinal(compound.getShort(DIRECTION_NBT_STRING));
        }
        super.readEntityFromNBT(compound);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        compound.setShort(DIRECTION_NBT_STRING, (short)this.direction.ordinal());
        super.writeEntityToNBT(compound);
    }

    public EnumGravityDirection getDirection() {
        return this.direction;
    }

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        buffer.writeShort(this.direction.ordinal());
    }

    @Override
    public void readSpawnData(ByteBuf additionalData) {
        this.direction = EnumGravityDirection.getSafeDirectionFromOrdinal(additionalData.readShort());
    }

    //    @Override
//    public void moveEntity(double xMovement, double yMovement, double zMovement) {
//        if (this.direction == EnumGravityDirection.DOWN) {
//            super.moveEntity(xMovement, yMovement, zMovement);
//            this.actuallyOnGround = this.onGround;
//            return;
//        }
//
//        double xBefore = this.posX;
//        double yBefore = this.posY;
//        double zBefore = this.posZ;
//
//        double expectedXMovement = xMovement;
//        double expectedYMovement = yMovement;
//        double expectedZMovement = zMovement;
//
//        if (this.isInWeb) {
//            expectedXMovement *= 0.25D;
//            expectedYMovement *= 0.05000000074505806D;
//            expectedZMovement *= 0.25D;
//        }
//
//        super.moveEntity(xMovement, yMovement, zMovement);
//
//        if (this.isCollided) {
//            double actualXMovement = this.posX - xBefore;
//            double actualYMovement = this.posY - yBefore;
//            double actualZMovement = this.posZ - zBefore;
//
//            switch (this.direction) {
//                case UP:
//                    this.actuallyOnGround = expectedYMovement > 0
//                            && this.isCollidedVertically
//                            && actualYMovement > expectedYMovement;
//                    break;
//                case DOWN:
//                    // impossible to reach
//                    this.actuallyOnGround = expectedYMovement < 0
//                            && this.isCollidedVertically
//                            && actualYMovement > expectedYMovement;
//                    break;
//                case NORTH:
//                    this.actuallyOnGround = expectedZMovement < 0
//                            && this.isCollidedHorizontally
//                            && actualZMovement > expectedZMovement;
//                    break;
//                case EAST:
//                    this.actuallyOnGround = expectedXMovement > 0
//                            && this.isCollidedHorizontally
//                            && actualXMovement < expectedXMovement;
//                    break;
//                case SOUTH:
//                    this.actuallyOnGround = expectedZMovement > 0
//                            && this.isCollidedHorizontally
//                            && actualZMovement < expectedZMovement;
//                    break;
//                case WEST:
//                    this.actuallyOnGround = expectedXMovement < 0
//                            && this.isCollidedHorizontally
//                            && actualXMovement > expectedXMovement;
//                    break;
//            }
//        }
//        else {
//            this.actuallyOnGround = false;
//        }
//
//    }
//
//    @Override
//    public void onUpdate() {
//        if (this.direction == EnumGravityDirection.DOWN) {
//            super.onUpdate();
//            return;
//        }
//
//        // Undo pushOutOfBlocks movement
//        double motionXStore = this.motionX;
//        double motionYStore = this.motionY;
//        double motionZStore = this.motionZ;
//        this.pushOutOfBlocks(this.posX, (this.getEntityBoundingBox().minY + this.getEntityBoundingBox().maxY) / 2.0D, this.posZ);
//        this.motionX = motionXStore - (this.motionX - motionXStore);
//        this.motionY = motionYStore - (this.motionY - motionYStore);
//        this.motionZ = motionZStore - (this.motionZ - motionZStore);
//
//        //See EntityItem::onUpdate
//        if (!this.hasNoGravity())
//        {
//            // Pre-emptively undo vanilla gravity
//            this.motionY += GRAVITY_DOWNWARDS_MOTION;
//
//            // Apply the correct change to motion
//            double[] d = this.direction.adjustXYZValues(0, GRAVITY_DOWNWARDS_MOTION, 0);
//            this.motionX -= d[0];
//            this.motionY -= d[1];
//            this.motionZ -= d[2];
//        }
//
//        super.onUpdate();
//
//        double motionXSave = this.motionX;
//        double motionYSave = this.motionY;
//        double motionZSave = this.motionZ;
//
//        this.handleWaterMovement();
//
//        double waterXChange = this.motionX - motionXSave;
//        double waterYChange = this.motionY - motionYSave;
//        double waterZChange = this.motionZ - motionZSave;
//
//        // Undo the effects of this.handleWaterMovement();
//        this.motionX = motionXSave - waterXChange;
//        this.motionY = motionYSave - waterYChange;
//        this.motionZ = motionZSave - waterZChange;
//
//        if (this.onGround)
//        {
//            // Undo vanilla
//            this.motionY /= -0.5D;
//        }
//
//        float f = 0.98F;
//
//        if (this.onGround)
//        {
//            f = this.worldObj.getBlockState(new BlockPos(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.getEntityBoundingBox().minY) - 1, MathHelper.floor_double(this.posZ))).getBlock().slipperiness * 0.98F;
//        }
//
//        // Undo vanilla
//        this.motionX /= (double)f;
//        this.motionY /= 0.9800000190734863D;
//        this.motionZ /= (double)f;
//
//        if (!this.actuallyOnGround) {
//            f = 0.98F;
//        }
//
//        // Apply Adjusted
//        double[] d = this.direction.adjustXYZValuesMaintainSigns(f, 0.9800000190734863D, f);
//        this.motionX *= d[0];
//        this.motionY *= d[1];
//        this.motionZ *= d[2];
//
//        // Apply adjusted
//        if (this.actuallyOnGround) {
//            // Apply Adjusted
//            d = this.direction.adjustXYZValuesMaintainSigns(1, -0.5, 1);
//            this.motionX *= d[0];
//            this.motionY *= d[1];
//            this.motionZ *= d[2];
//        }
//
//        // Re-apply water motion change
//        this.motionX += waterXChange;
//        this.motionY += waterYChange;
//        this.motionZ += waterZChange;
//
//        this.onGround = this.actuallyOnGround;
//        this.isAirBorne = !this.onGround;
//    }
}

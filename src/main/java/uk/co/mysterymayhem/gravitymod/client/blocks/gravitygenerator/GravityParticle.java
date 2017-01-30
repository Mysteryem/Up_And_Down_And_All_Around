package uk.co.mysterymayhem.gravitymod.client.blocks.gravitygenerator;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import uk.co.mysterymayhem.gravitymod.api.EnumGravityDirection;
import uk.co.mysterymayhem.gravitymod.api.EnumGravityTier;

/**
 * Created by Mysteryem on 2017-01-12.
 */
@SideOnly(Side.CLIENT)
public class GravityParticle extends Particle {

    private final AxisAlignedBB bounds;
    private final EnumGravityDirection direction;

    public GravityParticle(EnumGravityDirection direction, AxisAlignedBB boundingbox, EnumGravityTier tier, World worldIn) {
        super(worldIn, 0, 0, 0);
        this.direction = direction;
        final Vec3d pos = this.getRandomPositionInside(boundingbox);
        double[] d = direction.adjustXYZValues(0, 0.5, 0);
        double x = pos.xCoord + d[0];
        double y = pos.yCoord + d[1];
        double z = pos.zCoord + d[2];
        this.setPosition(x, y, z);
        this.prevPosX = x;
        this.prevPosY = y;
        this.prevPosZ = z;
        float f = this.rand.nextFloat();
        this.particleGravity = 0.04f + (f * 0.06f) - 0.03f;
        float[] colour = tier.getColour();
        this.particleRed = colour[0];
        this.particleGreen = colour[1];
        this.particleBlue = colour[2];
        this.particleAlpha = 0.5f;
        this.particleScale = (f * 0.5F + 0.5F) * 5.0F;
        this.particleMaxAge = 40;
        this.bounds = boundingbox;
        this.canCollide = false;
    }

    private Vec3d getRandomPositionInside(AxisAlignedBB bb) {
        double randomX = bb.minX + this.rand.nextDouble() * (bb.maxX - bb.minX);
        double randomY = bb.minY + this.rand.nextDouble() * (bb.maxY - bb.minY);
        double randomZ = bb.minZ + this.rand.nextDouble() * (bb.maxZ - bb.minZ);
        return new Vec3d(randomX, randomY, randomZ);
    }

    @Override
    public int getFXLayer() {
        return super.getFXLayer();
    }

    @Override
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        if (this.particleAge++ >= this.particleMaxAge) {
            this.setExpired();
        }

        double[] d = this.direction.adjustXYZValues(0, -this.particleGravity, 0);

        this.motionX = d[0];
        this.motionY = d[1];
        this.motionZ = d[2];

//        this.motionY -= 0.04D * (double)this.particleGravity;
        this.moveEntity(this.motionX, this.motionY, this.motionZ);
        this.motionX *= 0.9800000190734863D;
        this.motionY *= 0.9800000190734863D;
        this.motionZ *= 0.9800000190734863D;

        if (this.isCollided || this.isOutOfBounds()) {
            this.isExpired = true;
        }
    }

//    public GravityParticle(EnumGravityDirection direction, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn) {
//        super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
//        this.direction = direction;
//    }

    private boolean isOutOfBounds() {
        switch (this.direction) {
            case UP:
                return this.posY > this.bounds.maxY;
            case DOWN:
                return this.posY < this.bounds.minY;
            case NORTH:
                return this.posZ < this.bounds.minZ;
            case EAST:
                return this.posX > this.bounds.maxX;
            case SOUTH:
                return this.posZ > this.bounds.maxZ;
            default://case WEST:
                return this.posX < this.bounds.minX;
        }
    }

    @Override
    public void renderParticle(VertexBuffer worldRendererIn, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        super.renderParticle(worldRendererIn, entityIn, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
    }

    /**
     * Can't use as the top could be
     *
     * @param bb
     * @return
     */
    private Vec3d getRandomPositionAtTopOf(AxisAlignedBB bb) {
        final double randomX;
        final double randomY;
        final double randomZ;
        switch (this.direction) {
            case UP:
                randomX = bb.minX + this.rand.nextDouble() * (bb.maxX - bb.minX);
                randomY = bb.minY;
                randomZ = bb.minZ + this.rand.nextDouble() * (bb.maxZ - bb.minZ);
                break;
            case DOWN:
                randomX = bb.minX + this.rand.nextDouble() * (bb.maxX - bb.minX);
                randomY = bb.maxY;
                randomZ = bb.minZ + this.rand.nextDouble() * (bb.maxZ - bb.minZ);
                break;
            case NORTH:
                randomX = bb.minX + this.rand.nextDouble() * (bb.maxX - bb.minX);
                randomY = bb.minY + this.rand.nextDouble() * (bb.maxY - bb.minY);
                randomZ = bb.maxZ;
                break;
            case EAST:
                randomX = bb.minX;
                randomY = bb.minY + this.rand.nextDouble() * (bb.maxY - bb.minY);
                randomZ = bb.minZ + this.rand.nextDouble() * (bb.maxZ - bb.minZ);
                break;
            case SOUTH:
                randomX = bb.minX + this.rand.nextDouble() * (bb.maxX - bb.minX);
                randomY = bb.minY + this.rand.nextDouble() * (bb.maxY - bb.minY);
                randomZ = bb.minZ;
                break;
            default://case WEST:
                randomX = bb.maxX;
                randomY = bb.minY + this.rand.nextDouble() * (bb.maxY - bb.minY);
                randomZ = bb.minZ + this.rand.nextDouble() * (bb.maxZ - bb.minZ);
                break;
        }
        return new Vec3d(randomX, randomY, randomZ);
    }
}

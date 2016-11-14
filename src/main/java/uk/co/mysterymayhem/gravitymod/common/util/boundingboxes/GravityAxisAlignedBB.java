package uk.co.mysterymayhem.gravitymod.common.util.boundingboxes;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import uk.co.mysterymayhem.gravitymod.api.EnumGravityDirection;
import uk.co.mysterymayhem.gravitymod.common.capabilities.gravitydirection.IGravityDirectionCapability;

/**
 * Created by Mysteryem on 2016-09-04.
 */
public class GravityAxisAlignedBB extends AxisAlignedBB {
    private final IGravityDirectionCapability gravityCapability;

    public GravityAxisAlignedBB(IGravityDirectionCapability gravityCapability, double x1, double y1, double z1, double x2, double y2, double z2) {
        super(x1, y1, z1, x2, y2, z2);
        this.gravityCapability = gravityCapability;
    }

    public GravityAxisAlignedBB(GravityAxisAlignedBB other, double x1, double y1, double z1, double x2, double y2, double z2) {
        super(x1, y1, z1, x2, y2, z2);
        this.gravityCapability = other.gravityCapability;
    }

    public GravityAxisAlignedBB(IGravityDirectionCapability gravityCapability, BlockPos pos) {
        super(pos);
        this.gravityCapability = gravityCapability;
    }

    public GravityAxisAlignedBB(GravityAxisAlignedBB other, BlockPos pos) {
        super(pos);
        this.gravityCapability = other.gravityCapability;
    }

    public GravityAxisAlignedBB(IGravityDirectionCapability gravityCapability, BlockPos pos1, BlockPos pos2) {
        super(pos1, pos2);
        this.gravityCapability = gravityCapability;
    }

    public GravityAxisAlignedBB(GravityAxisAlignedBB other, BlockPos pos1, BlockPos pos2) {
        super(pos1, pos2);
        this.gravityCapability = other.gravityCapability;
    }

    public GravityAxisAlignedBB(IGravityDirectionCapability gravityCapability, Vec3d p_i47144_1_, Vec3d p_i47144_2_) {
        super(p_i47144_1_, p_i47144_2_);
        this.gravityCapability = gravityCapability;
    }

    public GravityAxisAlignedBB(GravityAxisAlignedBB other, Vec3d p_i47144_1_, Vec3d p_i47144_2_) {
        super(p_i47144_1_, p_i47144_2_);
        this.gravityCapability = other.gravityCapability;
    }

    public GravityAxisAlignedBB(IGravityDirectionCapability gravityCapability, AxisAlignedBB axisAlignedBB) {
        this(gravityCapability, axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ, axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ);
    }

    public GravityAxisAlignedBB(GravityAxisAlignedBB other, AxisAlignedBB axisAlignedBB) {
        this(other.gravityCapability, axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ, axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ);
    }

    @Override
    public GravityAxisAlignedBB addCoord(double x, double y, double z) {
        double[] d = this.gravityCapability.getDirection().adjustXYZValues(x, y, z);
        return new GravityAxisAlignedBB(this, super.addCoord(d[0], d[1], d[2]));
    }

    @Override
    public GravityAxisAlignedBB expand(double x, double y, double z) {
        // Sign of the arguments is important, we only want to get the new directions the values correspond to
        double[] d = this.getDirection().adjustXYZValuesMaintainSigns(x, y, z);
        return new GravityAxisAlignedBB(this, super.expand(d[0], d[1], d[2]));
    }

    @Override
    public GravityAxisAlignedBB offset(BlockPos pos) {
        return new GravityAxisAlignedBB(this, this.offset(pos.getX(), pos.getY(), pos.getZ()));
    }

    @Override
    public GravityAxisAlignedBB offset(double x, double y, double z) {
        double[] d = this.gravityCapability.getDirection().adjustXYZValues(x, y, z);
        return new GravityAxisAlignedBB(this, super.offset(d[0], d[1], d[2]));
    }

    public GravityAxisAlignedBB offsetSuper(double x, double y, double z) {
        return new GravityAxisAlignedBB(this, super.offset(x, y, z));
    }

    // Do I need to gravity adjust these?
    @Override
    public double calculateXOffset(AxisAlignedBB other, double offsetX) {
        return offsetFromArray(this.getDirection().adjustXYZValues(1, 0, 0), other, offsetX);
    }

    // Do I need to gravity adjust these?
    @Override
    public double calculateYOffset(AxisAlignedBB other, double offsetY) {
        return offsetFromArray(this.getDirection().adjustXYZValues(0, 1, 0), other, offsetY);
    }

    // Do I need to gravity adjust these?
    @Override
    public double calculateZOffset(AxisAlignedBB other, double offsetZ) {
        return offsetFromArray(this.getDirection().adjustXYZValues(0, 0, 1), other, offsetZ);
    }

    public double superCalculateXOffset(AxisAlignedBB other, double offsetX) {
        return super.calculateXOffset(other, offsetX);
    }

    public double superCalculateYOffset(AxisAlignedBB other, double offsetY) {
        return super.calculateYOffset(other, offsetY);
    }

    public double superCalculateZOffset(AxisAlignedBB other, double offsetZ) {
        return super.calculateZOffset(other, offsetZ);
    }

    public static double getRelativeBottom(AxisAlignedBB bb) {
        if (bb instanceof GravityAxisAlignedBB) {
            return ((GravityAxisAlignedBB) bb).getRelativeBottom();
        }
        return bb.minY;
    }

    @SuppressWarnings("SuspiciousNameCombination")
    public double getRelativeBottom() {
        switch(this.getDirection()){
            case UP:
                return -this.maxY;
            case DOWN:
                return this.minY;
            case SOUTH:
                return -this.maxZ;
            case WEST:
                return this.minX;
            case NORTH:
                return this.minZ;
//                case EAST:
            default:
                return -this.maxX;
        }
    }

    private double offsetFromArray(double[] adjusted, AxisAlignedBB other, double offset) {
        switch ((int) adjusted[0]) {
            case -1:
                return -super.calculateXOffset(other, -offset);
            case 0:
                switch ((int) adjusted[1]) {
                    case -1:
                        return -super.calculateYOffset(other, -offset);
                    case 0:
                        switch ((int) adjusted[2]) {
                            case -1:
                                return -super.calculateZOffset(other, -offset);
                            default://case 0:
                                return super.calculateZOffset(other, offset);
                        }
                    default:
                        return super.calculateYOffset(other, offset);
                }
            default:
                return super.calculateXOffset(other, offset);
        }
    }

    public Vec3d getOrigin() {
        switch (this.gravityCapability.getDirection()) {
            case UP:
                return new Vec3d(this.getCentreX(), this.maxY, this.getCentreZ());
            case SOUTH:
                return new Vec3d(this.getCentreX(), this.getCentreY(), this.maxZ);
            case WEST:
                return new Vec3d(this.minX, this.getCentreY(), this.getCentreZ());
            case NORTH:
                return new Vec3d(this.getCentreX(), this.getCentreY(), this.minZ);
            case EAST:
                return new Vec3d(this.maxX, this.getCentreY(), this.getCentreZ());
            default://case DOWN:
                return new Vec3d(this.getCentreX(), this.minY, this.getCentreZ());
        }
    }

    private double getCentreX() {
        return (this.minX + this.maxX) / 2d;
    }

    private double getCentreY() {
        return (this.minY + this.maxY) / 2d;
    }

    private double getCentreZ() {
        return (this.minZ + this.maxZ) / 2d;
    }

    public GravityAxisAlignedBB expandUp(double y) {
        switch (this.gravityCapability.getDirection()) {
            case UP:
                return new GravityAxisAlignedBB(this, this.minX, this.minY - y, this.minZ, this.maxX, this.maxY, this.maxZ);
            case SOUTH:
                return new GravityAxisAlignedBB(this, this.minX, this.minY, this.minZ - y, this.maxX, this.maxY, this.maxZ);
            case WEST:
                return new GravityAxisAlignedBB(this, this.minX, this.minY, this.minZ, this.maxX + y, this.maxY, this.maxZ);
            case NORTH:
                return new GravityAxisAlignedBB(this, this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ + y);
            case EAST:
                return new GravityAxisAlignedBB(this, this.minX - y, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
            default://case DOWN:
                return new GravityAxisAlignedBB(this, this.minX, this.minY, this.minZ, this.maxX, this.maxY + y, this.maxZ);
        }
    }

    public EnumGravityDirection getDirection() {
        return this.gravityCapability.getDirection();
    }

    public IGravityDirectionCapability getCapability() {
        return this.gravityCapability;
    }

    public AxisAlignedBB toVanilla() {
        return new AxisAlignedBB(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
    }

}

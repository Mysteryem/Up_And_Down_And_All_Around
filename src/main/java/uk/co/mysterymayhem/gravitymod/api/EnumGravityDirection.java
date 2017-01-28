package uk.co.mysterymayhem.gravitymod.api;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import uk.co.mysterymayhem.gravitymod.GravityMod;
import uk.co.mysterymayhem.gravitymod.common.util.boundingboxes.GravityAxisAlignedBB;

//TODO: Replace anonymous inner BlockPos classes with non-inner classes (faster, no reference to 'EnumGravityDirection.this' passed around)
/**
 * Enum of the gravity direction objects.
 * Created by Mysteryem on 2016-08-14.
 */
public enum EnumGravityDirection implements IStringSerializable {
    UP(new Vec3i(0, 0, 180), "up", EnumFacing.UP) {
        @Override public double[] adjustXYZValues(double x, double y, double z) {return new double[]{-x, -y, z};}
        // Doing nothing is correct because the rotation of the player occurs about their position, which lines up with
        // how the UP gravity hitboxes are set up
        @Override public void applyOtherPlayerRenderTransformations(EntityPlayer player) {/*do nothing*/}
        @Override public GravityAxisAlignedBB getGravityAdjustedAABB(EntityPlayer player, float width, float height) {
            double widthOver2 = width/2f;
            return new GravityAxisAlignedBB(
                    API.getGravityDirectionCapability(player),
                    player.posX - widthOver2, player.posY - height, player.posZ - widthOver2,
                    player.posX + widthOver2, player.posY,          player.posZ + widthOver2
            );
        }
        @Override public void returnCentreOfGravityToPlayerPos(EntityPlayer player) {player.posY -= player.height/2;}
        @Override public void offsetCentreOfGravityFromPlayerPos(EntityPlayer player) {player.posY += player.height/2;}
        @Override public EnumGravityDirection getInverseAdjustmentFromDOWNDirection() {return this;}
        @Override public EnumGravityDirection getOpposite() {return DOWN;}
        @Override public float getEntityEyeHeight(EntityLivingBase entityLivingBase) {return -API.getStandardEyeHeight(entityLivingBase);}
        @Override public void resetPositionToBB(EntityLivingBase entityLivingBase, AxisAlignedBB bb) {
            entityLivingBase.posX = (bb.minX + bb.maxX) / 2.0D;
            entityLivingBase.posY = bb.maxY;
            entityLivingBase.posZ = (bb.minZ + bb.maxZ) / 2.0D;
        }
        @Override public BlockPos makeRelativeBlockPos(BlockPos blockPos) {
            return new BlockPos(blockPos) {
                @Override
                public BlockPos down(int n) {
                    return super.up(n);
                }

                @Override
                public BlockPos up(int n) {
                    return super.down(n);
                }

                @Override
                public BlockPos east(int n) {
                    return super.west(n);
                }

                @Override
                public BlockPos west(int n) {
                    return super.east(n);
                }
            };
        }
        @Override public EnumGravityDirection getRelativePositiveX() {return WEST;}
        @Override public EnumGravityDirection getRelativePositiveZ() {return SOUTH;}
    },
    DOWN(new Vec3i(0, 0, 0), "down", EnumFacing.DOWN) {
        @Override public double[] adjustXYZValues(double x, double y, double z) {return new double[]{x, y, z};}
        @Override public void applyOtherPlayerRenderTransformations(EntityPlayer player) {/*do nothing*/}
        @Override public GravityAxisAlignedBB getGravityAdjustedAABB(EntityPlayer player, float width, float height) {
            double widthOver2 = width/2f;
            return new GravityAxisAlignedBB(
                    API.getGravityDirectionCapability(player),
                    player.posX - widthOver2, player.posY,          player.posZ - widthOver2,
                    player.posX + widthOver2, player.posY + height, player.posZ + widthOver2
            );
        }
        @Override public void returnCentreOfGravityToPlayerPos(EntityPlayer player) {player.posY += player.height/2;}
        @Override public void offsetCentreOfGravityFromPlayerPos(EntityPlayer player) {player.posY -= player.height/2;}
        @Override public EnumGravityDirection getInverseAdjustmentFromDOWNDirection() {return this;}
        @Override public EnumGravityDirection getOpposite() {return UP;}
        @Override public float getEntityEyeHeight(EntityLivingBase entityLivingBase) {return API.getStandardEyeHeight(entityLivingBase);}
        @Override public void resetPositionToBB(EntityLivingBase entityLivingBase, AxisAlignedBB bb) {
            entityLivingBase.posX = (bb.minX + bb.maxX) / 2.0D;
            entityLivingBase.posY = bb.minY;
            entityLivingBase.posZ = (bb.minZ + bb.maxZ) / 2.0D;
        }
        @Override public BlockPos makeRelativeBlockPos(BlockPos blockPos) {return blockPos;}
        @Override public EnumGravityDirection getRelativePositiveX() {return EAST;}
        @Override public EnumGravityDirection getRelativePositiveZ() {return SOUTH;}
    },
    NORTH(new Vec3i(90, 0, 0), "north", EnumFacing.NORTH) {
        @Override public double[] adjustXYZValues(double x, double y, double z) {return new double[]{x, -z, y};}
        @Override public void applyOtherPlayerRenderTransformations(EntityPlayer player) {GlStateManager.translate(0, 0, -API.getStandardEyeHeight(player));}
        @Override public GravityAxisAlignedBB getGravityAdjustedAABB(EntityPlayer player, float width, float height) {
            double widthOver2 = width/2f;
            float eyeHeight = API.getStandardEyeHeight(player);
            return new GravityAxisAlignedBB(
                    API.getGravityDirectionCapability(player),
                    player.posX - widthOver2, player.posY - widthOver2, player.posZ - eyeHeight,
                    player.posX + widthOver2, player.posY + widthOver2, player.posZ + (height - eyeHeight)
            );
        }
        @Override public void returnCentreOfGravityToPlayerPos(EntityPlayer player) {player.posZ -= (API.getStandardEyeHeight(player) - (player.height / 2d));}
        @Override public void offsetCentreOfGravityFromPlayerPos(EntityPlayer player) {player.posZ += (API.getStandardEyeHeight(player) - (player.height / 2d));}
        @Override public EnumGravityDirection getInverseAdjustmentFromDOWNDirection() {return SOUTH;}
        @Override public EnumGravityDirection getOpposite() {return SOUTH;}
        @Override public void resetPositionToBB(EntityLivingBase entityLivingBase, AxisAlignedBB bb) {
            entityLivingBase.posX = (bb.minX + bb.maxX) / 2.0D;
            entityLivingBase.posY = (bb.minY + bb.maxY) / 2.0D;
            entityLivingBase.posZ = bb.minZ + API.getStandardEyeHeight(entityLivingBase);
        }
        @Override public BlockPos makeRelativeBlockPos(BlockPos blockPos) {
            return new BlockPos(blockPos) {
                @Override
                public BlockPos down(int n) {
                    return super.north(n);
                }

                @Override
                public BlockPos up(int n) {
                    return super.south(n);
                }

                @Override
                public BlockPos north(int n) {
                    return super.up(n);
                }

                @Override
                public BlockPos south(int n) {
                    return super.down(n);
                }
            };
        }
        @Override public EnumGravityDirection getRelativePositiveX() {return EAST;}
        @Override public EnumGravityDirection getRelativePositiveZ() {return DOWN;}
    },
    EAST(new Vec3i(0, 0, 90), "east", EnumFacing.EAST) {
        @Override public double[] adjustXYZValues(double x, double y, double z) {return new double[]{-y, x, z};}
        @Override public void applyOtherPlayerRenderTransformations(EntityPlayer player) {GlStateManager.translate(API.getStandardEyeHeight(player), 0, 0);}
        @Override public GravityAxisAlignedBB getGravityAdjustedAABB(EntityPlayer player, float width, float height) {
            double widthOver2 = width/2f;
            float eyeHeight = API.getStandardEyeHeight(player);
            return new GravityAxisAlignedBB(
                    API.getGravityDirectionCapability(player),
                    player.posX - (height - eyeHeight), player.posY - widthOver2, player.posZ - widthOver2,
                    player.posX + eyeHeight,            player.posY + widthOver2, player.posZ + widthOver2
            );
        }
        @Override public void returnCentreOfGravityToPlayerPos(EntityPlayer player) {player.posX += (API.getStandardEyeHeight(player) - (player.height / 2d));}
        @Override public void offsetCentreOfGravityFromPlayerPos(EntityPlayer player) {player.posX -= (API.getStandardEyeHeight(player) - (player.height / 2d));}
        @Override public EnumGravityDirection getInverseAdjustmentFromDOWNDirection() {return WEST;}
        @Override public void resetPositionToBB(EntityLivingBase entityLivingBase, AxisAlignedBB bb) {
            entityLivingBase.posX = bb.maxX - API.getStandardEyeHeight(entityLivingBase);
            entityLivingBase.posY = (bb.minY + bb.maxY) / 2.0D;
            entityLivingBase.posZ = (bb.minZ + bb.maxZ) / 2.0D;
        }
        @Override public BlockPos makeRelativeBlockPos(BlockPos blockPos) {
            return new BlockPos(blockPos) {
                @Override
                public BlockPos down(int n) {
                    return super.east(n);
                }

                @Override
                public BlockPos up(int n) {
                    return super.west(n);
                }

                @Override
                public BlockPos west(int n) {
                    return super.down(n);
                }

                @Override
                public BlockPos east(int n) {
                    return super.up(n);
                }
            };
        }
        @Override public EnumGravityDirection getRelativePositiveX() {return UP;}
        @Override public EnumGravityDirection getRelativePositiveZ() {return SOUTH;}
    },
    SOUTH(new Vec3i(-90, 0, 0), "south", EnumFacing.SOUTH) {
        @Override public double[] adjustXYZValues(double x, double y, double z) {return new double[]{x, z, -y};}
        @Override public void applyOtherPlayerRenderTransformations(EntityPlayer player) {GlStateManager.translate(0, 0, API.getStandardEyeHeight(player));}
        @Override public GravityAxisAlignedBB getGravityAdjustedAABB(EntityPlayer player, float width, float height) {
            double widthOver2 = width/2f;
            float eyeHeight = API.getStandardEyeHeight(player);
            return new GravityAxisAlignedBB(
                    API.getGravityDirectionCapability(player),
                    player.posX - widthOver2, player.posY - widthOver2, player.posZ - (height - eyeHeight),
                    player.posX + widthOver2, player.posY + widthOver2, player.posZ + eyeHeight
            );
        }
        @Override public void returnCentreOfGravityToPlayerPos(EntityPlayer player) {player.posZ += (API.getStandardEyeHeight(player) - (player.height / 2d));}
        @Override public void offsetCentreOfGravityFromPlayerPos(EntityPlayer player) {player.posZ -= (API.getStandardEyeHeight(player) - (player.height / 2d));}
        @Override public EnumGravityDirection getInverseAdjustmentFromDOWNDirection() {return NORTH;}
        @Override public void resetPositionToBB(EntityLivingBase entityLivingBase, AxisAlignedBB bb) {
            entityLivingBase.posX = (bb.minX + bb.maxX) / 2.0D;
            entityLivingBase.posY = (bb.minY + bb.maxY) / 2.0D;
            entityLivingBase.posZ = bb.maxZ - API.getStandardEyeHeight(entityLivingBase);
        }
        @Override public BlockPos makeRelativeBlockPos(BlockPos blockPos) {
            return new BlockPos(blockPos) {
                @Override
                public BlockPos down(int n) {
                    return super.south(n);
                }

                @Override
                public BlockPos up(int n) {
                    return super.north(n);
                }

                @Override
                public BlockPos north(int n) {
                    return super.down(n);
                }

                @Override
                public BlockPos south(int n) {
                    return super.up(n);
                }
            };
        }
        @Override public EnumGravityDirection getRelativePositiveX() {return EAST;}
        @Override public EnumGravityDirection getRelativePositiveZ() {return UP;}
    },
    WEST(new Vec3i(0, 0, -90), "west", EnumFacing.WEST) {
        @Override public double[] adjustXYZValues(double x, double y, double z) {return new double[]{y, -x, z};}
        @Override public void applyOtherPlayerRenderTransformations(EntityPlayer player) {GlStateManager.translate(-API.getStandardEyeHeight(player), 0, 0);}
        @Override public GravityAxisAlignedBB getGravityAdjustedAABB(EntityPlayer player, float width, float height) {
            double widthOver2 = width/2f;
            float eyeHeight = API.getStandardEyeHeight(player);
            return new GravityAxisAlignedBB(
                    API.getGravityDirectionCapability(player),
                    player.posX - eyeHeight,            player.posY - widthOver2, player.posZ - widthOver2,
                    player.posX + (height - eyeHeight), player.posY + widthOver2, player.posZ + widthOver2
            );
        }
        @Override public void returnCentreOfGravityToPlayerPos(EntityPlayer player) {player.posX -= (API.getStandardEyeHeight(player) - (player.height / 2d));}
        @Override public void offsetCentreOfGravityFromPlayerPos(EntityPlayer player) {player.posX += (API.getStandardEyeHeight(player) - (player.height / 2d));}
        @Override public EnumGravityDirection getInverseAdjustmentFromDOWNDirection() {return EAST;}
        @Override public void resetPositionToBB(EntityLivingBase entityLivingBase, AxisAlignedBB bb) {
            entityLivingBase.posX = bb.minX + API.getStandardEyeHeight(entityLivingBase);
            entityLivingBase.posY = (bb.minY + bb.maxY) / 2.0D;
            entityLivingBase.posZ = (bb.minZ + bb.maxZ) / 2.0D;
        }
        @Override public BlockPos makeRelativeBlockPos(BlockPos blockPos) {
            return new BlockPos(blockPos) {
                @Override
                public BlockPos down(int n) {
                    return super.west(n);
                }

                @Override
                public BlockPos up(int n) {
                    return super.east(n);
                }

                @Override
                public BlockPos east(int n) {
                    return super.down(n);
                }

                @Override
                public BlockPos west(int n) {
                    return super.up(n);
                }
            };
        }
        @Override public EnumGravityDirection getRelativePositiveX() {return DOWN;}
        @Override public EnumGravityDirection getRelativePositiveZ() {return SOUTH;}
    };

    public abstract double[] adjustXYZValues(double x, double y, double z);
    public abstract void applyOtherPlayerRenderTransformations(EntityPlayer player);
    public abstract GravityAxisAlignedBB getGravityAdjustedAABB(EntityPlayer player, float width, float height);
    public abstract void returnCentreOfGravityToPlayerPos(EntityPlayer player);
    public abstract void offsetCentreOfGravityFromPlayerPos(EntityPlayer player);
    public abstract EnumGravityDirection getInverseAdjustmentFromDOWNDirection();
    public abstract BlockPos makeRelativeBlockPos(BlockPos blockPos);
    public abstract void resetPositionToBB(EntityLivingBase entityLivingBase, AxisAlignedBB bb);

    private final Vec3i cameraTransformVars;
    private final String name;
    private final EnumFacing facingEquivalent;

    EnumGravityDirection(Vec3i cameraTransformVars, String name, EnumFacing facingEquivalent) {
        this.cameraTransformVars = cameraTransformVars;
        this.name = name;
        this.facingEquivalent = facingEquivalent;
    }

    public EnumFacing getFacingEquivalent() {
        return this.facingEquivalent;
    }

    public boolean isValidLadderDirection(EnumFacing ladderFacing) {
        return ladderFacing != this.facingEquivalent && ladderFacing.getOpposite() != this.facingEquivalent;
    }

    public Vec3i getCameraTransformVars() {
        return this.cameraTransformVars;
    }

    // Overridden in UP and DOWN
    public float getEntityEyeHeight(EntityLivingBase entityLivingBase) {
        return 0f;
    }

    // I'm lazy and don't want to put unnecessary code in the overridden methods of the enum constants
    public void resetPositionToBB(EntityLivingBase entityLivingBase) {
        this.resetPositionToBB(entityLivingBase, entityLivingBase.getEntityBoundingBox());
    }

    //TODO: Is it worth removing this method?
    public void preModifyPlayerOnGravityChange(EntityPlayer player, EnumGravityDirection newDirection) {
        //Does nothing currently
    }

    public Vec3d adjustLookVec(Vec3d input) {
        double[] d = this.adjustXYZValues(input.xCoord, input.yCoord, input.zCoord);
        return new Vec3d(d[0], d[1], d[2]);
    }

    public double[] adjustXYZValuesMaintainSigns(double x, double y, double z) {
        double[] values = this.adjustXYZValues(x, y, z);
        double[] signs = this.adjustXYZValues(1, 1, 1);
        return new double[]{values[0] * signs[0], values[1] * signs[1], values[2] * signs[2]};
    }

    private void setBoundingBoxAndPositionOnGravityChange(EntityPlayer player, EnumGravityDirection oldDirection, Vec3d oldEyePos) {
        AxisAlignedBB axisAlignedBB = this.getGravityAdjustedAABB(player);
        player.resetPositionToBB();

        if (player.worldObj.collidesWithAnyBlock(axisAlignedBB)) {
            // After rotating about the player's centre of gravity, the player is now partially inside of a block

            // TODO: Test being a 'spider' player and trying to change gravity direction in tight places
            // TODO: Re-add choosing the correct gravity direction and using that to inverseAdjust the movement values, so that 'spider' players don't get stuck in walls
            // Instead of trying to move the player in all 6 directions to see which would, we can eliminate all but 2
            EnumGravityDirection directionToTry;
            double distanceToMove;

            // The player has a relatively normal hitbox
            if (player.height > player.width) {
                // Collision will be happening either above or below the player's centre of gravity from the
                // NEW gravity direction's perspective
                distanceToMove = (player.height-player.width)/2;
                directionToTry = this;
            }
            // The player must be some sort of pancake, e.g. a spider
            else if (player.height < player.width){
                // Collision will be happening either above or below the player's centre of gravity from the
                // OLD gravity direction's perspective
                distanceToMove = (player.width-player.height)/2;
                directionToTry = oldDirection;
            }
            // The player is a cube, meaning that their collision/bounding box won't have actually changed shape after being rotated/moved
            else {
                // As rotation of the player occurs about their centre of gravity
                // This scenario means that the player was already inside a block when they rotated as this should be impossible otherwise

                // Not going to do anything in this case
                player.setEntityBoundingBox(axisAlignedBB);
                return;
            }

            // Get the movement that is considered 'up' by distanceToMove
            double[] adjustedMovement = directionToTry.adjustXYZValues(0, distanceToMove, 0);

            // Inverse to undo the adjustment caused by GravityAxisAlignedBB.offset(...)
            adjustedMovement = this.getInverseAdjustmentFromDOWNDirection().adjustXYZValues(adjustedMovement[0], adjustedMovement[1], adjustedMovement[2]);

            // Moving 'up' from the rotated player's perspective
            AxisAlignedBB secondTry = axisAlignedBB.offset(adjustedMovement[0], adjustedMovement[1], adjustedMovement[2]);


            // We try 'up' first because even if we move the player too far, their gravity will move them back 'down'
            if (player.worldObj.collidesWithAnyBlock(secondTry)) {

                // Moving 'down' from the rotated player's perspective
                AxisAlignedBB thirdTry = axisAlignedBB.offset(-adjustedMovement[0], -adjustedMovement[1], -adjustedMovement[2]);

                if (player.worldObj.collidesWithAnyBlock(thirdTry)) {
                    // Uh oh, looks like the player decided to rotate in a too small place
                    // Imagine a 2 block tall, 1 block wide player standing in a 2 block tall, one block wide space
                    // and then changing from UP/DOWN gravity to NORTH/EAST/SOUTH/WEST gravity
                    // they cannot possibly fit, we'll settle for putting the bottom of their bb at an integer value
                    // as well as trying one block up (relative)

                    // Move the player such that their old eye position is the same as the new one, this should limit suffocation
                    player.setEntityBoundingBox(axisAlignedBB);
                    player.resetPositionToBB();

                    Vec3d newEyePos = player.getPositionVector().addVector(0, player.getEyeHeight(), 0);
                    Vec3d eyesDifference = oldEyePos.subtract(newEyePos);
                    Vec3d adjustedDifference = this.getInverseAdjustmentFromDOWNDirection().adjustLookVec(eyesDifference);
                    AxisAlignedBB givenUp = axisAlignedBB.offset(adjustedDifference.xCoord, adjustedDifference.yCoord, adjustedDifference.zCoord);
                    //TODO: Set position at feet to closest int so we don't fall through blocks
                    double relativeBottomOfBB = GravityAxisAlignedBB.getRelativeBottom(givenUp);
                    long rounded = Math.round(relativeBottomOfBB);
                    double difference = rounded - relativeBottomOfBB;
                    //Try one block up (relative) from the found position to start with, to try and avoid falling through the block that is now at our feet
                    givenUp = givenUp.offset(0, difference + 1, 0);
                    //If one block up collided, then we have no choice but to choose the block below
                    if (player.worldObj.collidesWithAnyBlock(givenUp)) {
                        givenUp = givenUp.offset(0, -1, 0);
                    }
                    axisAlignedBB = givenUp;

                }
                else {
                    // Moving 'down' did not collide with the world
                    axisAlignedBB = thirdTry;

                }
            }
            else {
                // Moving 'up' did not collide with the world
                axisAlignedBB = secondTry;
            }
        }
        else if (GravityMod.GENERAL_DEBUG) {
            GravityMod.logInfo("Player's new hitbox fit in the world without moving the player");
        }
        player.setEntityBoundingBox(axisAlignedBB);
        player.resetPositionToBB();
    }

    public void runCameraTransformation() {
        Vec3i vars = this.getCameraTransformVars();
        int x = vars.getX();
        int y = vars.getY();
        int z = vars.getZ();
        if (x != 0) {
            GlStateManager.rotate(x, 1, 0, 0);
        }
        if (y != 0) {
            GlStateManager.rotate(y, 0, 1, 0);
        }
        if (z != 0) {
            GlStateManager.rotate(z, 0, 0, 1);
        }

    }

    public void postModifyPlayerOnGravityChange(EntityPlayer player, EnumGravityDirection oldDirection, Vec3d inputEyePos) {

        // Moves the player's position to their centre of gravity
        oldDirection.returnCentreOfGravityToPlayerPos(player);
        // Moves the player's position to the new place for this gravity direction, such that the player's centre of
        // gravity is in the same place as when postModifyPlayerOnGravityChange was called
        this.offsetCentreOfGravityFromPlayerPos(player);

        // Move the player to try and get them out of a wall. Being inside of a block for even a single tick causes
        // suffocation damage
        this.setBoundingBoxAndPositionOnGravityChange(player, oldDirection, inputEyePos);
    }

    public AxisAlignedBB getGravityAdjustedAABB(EntityPlayer player) {
        return this.getGravityAdjustedAABB(player, player.width, player.height);
    }

    /**
     * Get the opposite gravity direction to this. For all but UP and DOWN, it is the same as this.getInverseAdjustmentFromDOWNDirection()
     * @return The opposite direction to this.
     */
    public EnumGravityDirection getOpposite() {
        return this.getInverseAdjustmentFromDOWNDirection();
    }

    public EnumGravityDirection getRelativePositiveY() {
        return this.getOpposite();
    }

    public EnumGravityDirection getRelativeNegativeY() {
        return this;
    }

    public abstract EnumGravityDirection getRelativePositiveX();

    public EnumGravityDirection getRelativeNegativeX() {
        return this.getRelativePositiveX().getOpposite();
    }

    public abstract EnumGravityDirection getRelativePositiveZ();

    public EnumGravityDirection getRelativeNegativeZ() {
        return this.getRelativePositiveZ().getOpposite();
    }

    public EnumGravityDirection getRelativeEast() {
        return this.getRelativePositiveX();
    }

    public EnumGravityDirection getRelativeWest() {
        return this.getRelativeNegativeX();
    }

    public EnumGravityDirection getRelativeUp() {
        return this.getRelativePositiveY();
    }

    public EnumGravityDirection getRelativeDown() {
        return this.getRelativeNegativeY();
    }

    public EnumGravityDirection getRelativeNorth() {
        return this.getRelativeNegativeZ();
    }

    public EnumGravityDirection getRelativeSouth() {
        return this.getRelativePositiveZ();
    }

    @Override
    public String getName() {
        return this.name;
    }

    /**
     * Will return DOWN if the int argument is out of bounds
     * @param ordinal
     * @return
     */
    public static EnumGravityDirection getSafeDirectionFromOrdinal(int ordinal) {
        if (ordinal >= 0 && ordinal < EnumGravityDirection.values().length) {
            return EnumGravityDirection.values()[ordinal];
        }
        else {
            return EnumGravityDirection.DOWN;
        }
    }

    public static EnumGravityDirection fromEnumFacing(EnumFacing enumFacing) {
        switch (enumFacing) {
            case DOWN:
                return DOWN;
            case UP:
                return UP;
            case NORTH:
                return NORTH;
            case SOUTH:
                return SOUTH;
            case WEST:
                return WEST;
            default://case EAST:
                return EAST;
        }
    }
}

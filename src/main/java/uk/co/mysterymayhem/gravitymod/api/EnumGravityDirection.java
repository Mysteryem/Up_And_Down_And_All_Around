package uk.co.mysterymayhem.gravitymod.api;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.FMLLog;
import uk.co.mysterymayhem.gravitymod.asm.EntityPlayerWithGravity;
import uk.co.mysterymayhem.gravitymod.asm.Hooks;
import uk.co.mysterymayhem.gravitymod.capabilities.GravityDirectionCapability;
import uk.co.mysterymayhem.gravitymod.util.GravityAxisAlignedBB;
import uk.co.mysterymayhem.gravitymod.util.TriDoubleFunction;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * I heard you like lambda expressions
 *
 * This class needs some serious cleanup
 *
 * Created by Mysteryem on 2016-08-14.
 */
public enum EnumGravityDirection {

    UP(
            () -> GlStateManager.rotate(180, 0, 0, 1),
            (x, y, z) -> new double[]{-x, -y, z},
            player -> GlStateManager.translate(0, 0, 0),
            (player, oldDirection) ->  {
//                double widthOver2 = player.width/2d;
//                player.setEntityBoundingBox(new AxisAlignedBB(
//                        player.posX-widthOver2,
//                        player.posY,
//                        player.posZ-widthOver2,
//                        player.posX+widthOver2,
//                        player.posY+player.height,
//                        player.posZ+widthOver2));
//                player.eyeHeight = -player.getDefaultEyeHeight();

                //DO NOT SET EYE HEIGHT, EYE HEIGHT IN PLAYER CLASS MAY AS WELL BE FINAL //Set eye height - Must be before setting the hitbox so that the htibox can be adjusted for the new eye position
                //Set player's position - Forceful at first
                //Set player's hitbox - This is forceful, things may break otherwise
                //Move player - Must be after setting the hitbox so that we can guarantee that the final position is valid for the entity
            },
            (width, height, player) -> {
                double widthOver2 = width/2;
                return new GravityAxisAlignedBB(
                        GravityDirectionCapability.getGravityCapability(player),
                        player.posX - widthOver2,
                        player.posY - height,
                        player.posZ - widthOver2,
                        player.posX + widthOver2,
                        player.posY,
                        player.posZ + widthOver2
                );
            },
            player -> player.posY -= player.height/2,
            player -> player.posY += player.height/2
    ),
    DOWN(
            () -> {},
            (x, y, z) -> new double[]{x, y, z},
            player -> {},
            (player, oldDirection) -> {
//                double widthOver2 = player.width/2d;
//                player.setEntityBoundingBox(new AxisAlignedBB(
//                        player.posX-widthOver2,
//                        player.posY,
//                        player.posZ-widthOver2,
//                        player.posX+widthOver2,
//                        player.posY+player.height,
//                        player.posZ+widthOver2));
            },
            (width, height, player) -> {
                double widthOver2 = width/2f;
                return new GravityAxisAlignedBB(
                        GravityDirectionCapability.getGravityCapability(player),
                        player.posX - widthOver2,
                        player.posY,
                        player.posZ - widthOver2,
                        player.posX + widthOver2,
                        player.posY + height,
                        player.posZ + widthOver2
                );
            },
            player -> player.posY += player.height/2,
            player -> player.posY -= player.height/2
    ),
    SOUTH(
            () -> GlStateManager.rotate(-90, 1, 0, 0),
            (x, y, z) -> new double[]{x, z, -y},
            player -> GlStateManager.translate(0, 0, API.getStandardEyeHeight(player)),
            (player, oldDirection) -> {
//                double widthOver2 = player.width/2d;
//                double offset = player.getDefaultEyeHeight() - (player.height/2d);
//                player.setEntityBoundingBox(new AxisAlignedBB(
//                        player.posX-widthOver2,
//                        player.posY-widthOver2,
//                        player.posZ-offset,
//                        player.posX+widthOver2,
//                        player.posY+widthOver2,
//                        player.posZ+player.height-offset));
//                player.eyeHeight = (float)widthOver2;
//                player.posY+=widthOver2;
//                player.posZ+=player.height/2d;
            },
            (width, height, player) -> {
                double widthOver2 = width/2f;
                float eyeHeight = API.getStandardEyeHeight(player);
                return new GravityAxisAlignedBB(
                        GravityDirectionCapability.getGravityCapability(player),
                        player.posX - widthOver2,
                        player.posY - widthOver2,
                        player.posZ - (height - eyeHeight),
                        player.posX + widthOver2,
                        player.posY + widthOver2,
                        player.posZ + eyeHeight
                );
            },
            player -> player.posZ += (API.getStandardEyeHeight(player) - (player.height / 2d)),
            player -> player.posZ -= (API.getStandardEyeHeight(player) - (player.height / 2d))
    ),
    WEST(
            () -> GlStateManager.rotate(-90, 0, 0, 1),
            (x, y, z) -> new double[]{y, -x, z},
            player -> GlStateManager.translate(-API.getStandardEyeHeight(player), 0, 0),
            (player, oldDirection) -> {

            },
            (width, height, player) -> {
                double widthOver2 = width/2f;
                float eyeHeight = API.getStandardEyeHeight(player);
                return new GravityAxisAlignedBB(
                        GravityDirectionCapability.getGravityCapability(player),
                        player.posX - eyeHeight,
                        player.posY - widthOver2,
                        player.posZ - widthOver2,
                        player.posX + (height - eyeHeight),
                        player.posY + widthOver2,
                        player.posZ + widthOver2
                );
            },
            player -> player.posX -= (API.getStandardEyeHeight(player) - (player.height / 2d)),
            player -> player.posX += (API.getStandardEyeHeight(player) - (player.height / 2d))
    ),
    NORTH(
            () -> GlStateManager.rotate(90, 1, 0, 0),
            (x, y, z) -> new double[]{x, -z, y},
            player -> GlStateManager.translate(0, 0, -API.getStandardEyeHeight(player)),
            (player, oldDirection) -> {

            },
            (width, height, player) -> {
                double widthOver2 = width/2f;
                float eyeHeight = API.getStandardEyeHeight(player);
                return new GravityAxisAlignedBB(
                        GravityDirectionCapability.getGravityCapability(player),
                        player.posX - widthOver2,
                        player.posY - widthOver2,
                        player.posZ - eyeHeight,
                        player.posX + widthOver2,
                        player.posY + widthOver2,
                        player.posZ + (height - eyeHeight)
                );
            },
            player -> player.posZ -= (API.getStandardEyeHeight(player) - (player.height / 2d)),
            player -> player.posZ += (API.getStandardEyeHeight(player) - (player.height / 2d))
    ),
    EAST(
            () -> GlStateManager.rotate(90, 0, 0, 1),
            (x, y, z) -> new double[]{-y, x, z},
            player -> GlStateManager.translate(API.getStandardEyeHeight(player), 0, 0),
            (player, oldDirection) -> {

            },
            (width, height, player) -> {
                double widthOver2 = width/2f;
                float eyeHeight = API.getStandardEyeHeight(player);
                return new GravityAxisAlignedBB(
                        GravityDirectionCapability.getGravityCapability(player),
                        player.posX - (height - eyeHeight),
                        player.posY - widthOver2,
                        player.posZ - widthOver2,
                        player.posX + eyeHeight,
                        player.posY + widthOver2,
                        player.posZ + widthOver2
                );
            },
            player -> player.posX += (API.getStandardEyeHeight(player) - (player.height / 2d)),
            player -> player.posX -= (API.getStandardEyeHeight(player) - (player.height / 2d))
    );

    static {
        for (EnumGravityDirection direction : EnumGravityDirection.values()) {
            switch(direction) {
                case UP:
                    direction.inverseAdjustMentFromDOWNDirection = EnumGravityDirection.UP;
                    break;
                case DOWN:
                    direction.inverseAdjustMentFromDOWNDirection = EnumGravityDirection.DOWN;
                    break;
                case NORTH:
                    direction.inverseAdjustMentFromDOWNDirection = EnumGravityDirection.SOUTH;
                    break;
                case EAST:
                    direction.inverseAdjustMentFromDOWNDirection = EnumGravityDirection.WEST;
                    break;
                case SOUTH:
                    direction.inverseAdjustMentFromDOWNDirection = EnumGravityDirection.NORTH;
                    break;
                case WEST:
                    direction.inverseAdjustMentFromDOWNDirection = EnumGravityDirection.EAST;
                    break;
            }
        }
    }

    //TODO: Remove playerModificationsOnGravityChange
    private final Runnable cameraTransformRunnable;
    private final TriDoubleFunction<double[]> axisAdjustment;
    private final Consumer<EntityPlayer> otherPlayerRenderTransformations;
    private final BiConsumer<EntityPlayer, EnumGravityDirection> playerModificationsOnGravityChange;
    private final BiDoubleAndObjectFunction<EntityPlayer, AxisAlignedBB> boundingBoxFunction;
    private final Consumer<EntityPlayer> returnCOGToPlayerPos;
    private final Consumer<EntityPlayer> offsetCOGFromPlayerPos;
    private EnumGravityDirection inverseAdjustMentFromDOWNDirection;

    EnumGravityDirection(Runnable cameraTransformRunnable,
                         TriDoubleFunction<double[]> axisAdjustment,
                         Consumer<EntityPlayer> otherPlayerRenderTransformations,
                         BiConsumer<EntityPlayer, EnumGravityDirection> playerModificationsOnGravityChange,
                         BiDoubleAndObjectFunction<EntityPlayer, AxisAlignedBB> boundingBoxFunction,
                         Consumer<EntityPlayer> returnCOGToPlayerPos,
                         Consumer<EntityPlayer> offsetCOGFromPlayerPos) {
        this.cameraTransformRunnable = cameraTransformRunnable;
        this.axisAdjustment = axisAdjustment;
        this.otherPlayerRenderTransformations = otherPlayerRenderTransformations;
        this.playerModificationsOnGravityChange = playerModificationsOnGravityChange;
        this.boundingBoxFunction = boundingBoxFunction;
        this.returnCOGToPlayerPos = returnCOGToPlayerPos;
        this.offsetCOGFromPlayerPos = offsetCOGFromPlayerPos;
    }

    public void runCameraTransformation() {
        this.cameraTransformRunnable.run();
    }

    public Vec3d adjustLookVec(Vec3d input) {
        double[] adjustedValues = this.axisAdjustment.apply(input.xCoord, input.yCoord, input.zCoord);
        return new Vec3d(adjustedValues[0], adjustedValues[1], adjustedValues[2]);
    }

    public double[] adjustXYZValues(double inX, double inY, double inZ) {
        return this.axisAdjustment.apply(inX, inY, inZ);
    }

    public void applyOtherPlayerRenderTransformations(EntityPlayer otherPlayerToBeRendered) {
        this.otherPlayerRenderTransformations.accept(otherPlayerToBeRendered);
//        this.runCameraTransformation();
        //this.otherPlayerRenderTransformations.accept(otherPlayerToBeRendered);
    }


    public void preModifyPlayerOnGravityChange(EntityPlayer player, EnumGravityDirection newDirection) {
        //TODO: Is this needed? Could this be what's causing players to sometimes get stuck in place when changing gravity direction?
        player.resetPositionToBB();
//        player.setEntityBoundingBox(player.getEntityBoundingBox().offset(0, -API.getStandardEyeHeight(player)/2d, 0));
    }

    public void postModifyPlayerOnGravityChange(EntityPlayer player, EnumGravityDirection oldDirection) {
        // Converts the player current motion
        this.maintainEffectiveMotion(player, oldDirection);
        oldDirection.returnCentreOfGravityToPlayerPos(player);
        this.offsetCentreOfGravityFromPlayerPos(player);
        // Move player to try and get them out of a wall
        this.setBoundingBoxAndPositionOnGravityChange(player, oldDirection);
        this.playerModificationsOnGravityChange.accept(player, oldDirection);
    }

    // Converts the old motion of the player to the new direction, such that if the player was moving (a, b, c) from the
    // perspective of DOWN, that their new motion under the new direction is now (a, b, c) from the perspective of
    // DOWN
    //

    /**
     * Converts the old motion of the player to the new direction, such that if the player was moving (a, b, c) from the
     * perspective of DOWN, that their new motion under the new direction is now also (a, b, c) from the perspective of
     * DOWN.<br><br>
     * You can consider it a bit like applying matrices:<br><br>
     * The effective motion is the player's motion multiplied by the gravity adjustment matrix<br>
     * EFFECTIVE_MOTION = OLD_MOTION x OLD_GRAV_ADJUST<br><br>
     * But we will multiply by NEW_GRAV_ADJUST to get EFFECTIVE_MOTION under the new gravity so:<br>
     * EFFECTIVE_MOTION = ? x NEW_GRAV_ADJUST<br><br>
     * To solve, we need to multiple by the inverse of NEW_GRAV_ADJUST<br>
     * EFFECTIVE_MOTION = EFFECTIVE_MOTION x NEW_GRAV_ADJUST^(-1) x NEW_GRAV_ADJUST<br><br>
     * This gives us a single line solution of:<br>
     * EFFECTIVE_MOTION = OLD_MOTION x OLD_GRAV_ADJUST x NEW_GRAV_ADJUST^(-1) x NEW_GRAV_ADJUST<br>
     * (NEW_GRAV_ADJUST is applied when the player moves)
     * @param player The player whose effective motion we wish to maintain.
     * @param oldDirection The old gravity direction.
     */
    private void maintainEffectiveMotion(EntityPlayer player, EnumGravityDirection oldDirection) {
        double[] doubles = oldDirection.adjustXYZValues(player.motionX, player.motionY, player.motionZ);
        doubles = this.getInverseAdjustMentFromDOWNDirection().adjustXYZValues(doubles[0], doubles[1], doubles[2]);
        player.motionX = doubles[0];
        player.motionY = doubles[1];
        player.motionZ = doubles[2];
    }

    private void setBoundingBoxAndPositionOnGravityChange(EntityPlayer player, EnumGravityDirection oldDirection) {
        AxisAlignedBB axisAlignedBB = this.getGravityAdjustedAABB(player);
        Vec3d prevPos = player.getPositionVector();
        player.resetPositionToBB();
        if (!prevPos.equals(player.getPositionVector())) {
            // If we've got thing right, no change in position should have occured
            FMLLog.warning("Expected to find player at %s, but found player at %s", prevPos.toString(), player.getPositionVector().toString());
        }

        if (player.worldObj.collidesWithAnyBlock(axisAlignedBB)) {
            // After rotating about the player's centre of gravity, the player is now partially inside of a block

            // Instead of trying to move the player in all 6 directions to see which would, we can eliminate all but 2
            EnumGravityDirection directionToTry;
            float distanceToMove;

            // The player has a relatively normal hitbox
            if (player.height > player.width) {
                // Collision will be happening either above or below the player's centre of gravity from the
                // NEW gravity direction's perspective
                distanceToMove = player.height-player.width;
                directionToTry = this;
            }
            // The player must be some sort of pancake, e.g. a spider
            else if (player.height < player.width){
                // Collision will be happening either above or below the player's centre of gravity from the
                // OLD gravity direction's perspective
                distanceToMove = player.width-player.height;
                directionToTry = oldDirection;
            }
            // The player is a cube, meaning that their collision/bounding box won't have actually changed shape after being rotated/moved
            else {
                // This scenario means that the player was already inside a block when they rotated as this should be impossible otherwise

                // Not going to do anything in this case
                player.setEntityBoundingBox(axisAlignedBB);
                return;
            }

            // Get the movement that is considered 'up' by distanceToMove
            double[] adjustedMovement = directionToTry.adjustXYZValues(0, distanceToMove, 0);

            // Inverse to undo the adjustment caused by GravityAxisAlignedBB.offset(...)
            adjustedMovement = this.getInverseAdjustMentFromDOWNDirection().adjustXYZValues(adjustedMovement[0], adjustedMovement[1], adjustedMovement[2]);

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
                    // they cannot possibly fit

                    //DEBUG
//                    FMLLog.info("No matter how the player was moved, they would not fit, turning them into a square");

                    // Make the player as tall as they are wide (whichever is smaller)
                    float min = Math.min(player.height, player.width);
                    axisAlignedBB = this.getGravityAdjustedAABB(player, min, min);
                    // No change to the player's position is needed as their new hitbox is guaranteed to fit inside their old hitbox
                } else {
                    // Moving 'down' did not collide with the world
                    //DEBUG
//                    FMLLog.info("Moving 'down' did not collide with the world");
                    axisAlignedBB = thirdTry;
                    player.posX -= adjustedMovement[0];
                    player.posY -= adjustedMovement[1];
                    player.posZ -= adjustedMovement[2];
                }
            }
            else {
                // Moving 'up' did not collide with the world
                //DEBUG
//                FMLLog.info("Moving 'up' did not collide with the world");
                axisAlignedBB = secondTry;
                player.posX += adjustedMovement[0];
                player.posY += adjustedMovement[1];
                player.posZ += adjustedMovement[2];
            }
        }
        else {
            //DEBUG
//            FMLLog.info("Player's new hitbox fit in the world without moving the player");
        }
        player.setEntityBoundingBox(axisAlignedBB);
    }

    public AxisAlignedBB getGravityAdjustedAABB(EntityPlayer player) {
        return this.getGravityAdjustedAABB(player, player.width, player.height);
    }

    public AxisAlignedBB getGravityAdjustedAABB(EntityPlayer player, float width, float height) {
        return this.boundingBoxFunction.accept(width, height, player);
    }

    public void returnCentreOfGravityToPlayerPos(EntityPlayer player) {
        this.returnCOGToPlayerPos.accept(player);
    }

    public void offsetCentreOfGravityFromPlayerPos(EntityPlayer player) {
        this.offsetCOGFromPlayerPos.accept(player);
    }

    public EnumGravityDirection getInverseAdjustMentFromDOWNDirection() {
        return inverseAdjustMentFromDOWNDirection;
    }

    @FunctionalInterface
    public interface BiDoubleAndObjectFunction<T, R> {
        R accept(double d1, double d2, T t);
    }


}

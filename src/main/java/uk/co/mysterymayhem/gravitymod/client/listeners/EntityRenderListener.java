package uk.co.mysterymayhem.gravitymod.client.listeners;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import uk.co.mysterymayhem.gravitymod.api.API;
import uk.co.mysterymayhem.gravitymod.api.EnumGravityDirection;
import uk.co.mysterymayhem.gravitymod.common.capabilities.gravitydirection.GravityDirectionCapability;
import uk.co.mysterymayhem.gravitymod.common.capabilities.gravitydirection.IGravityDirectionCapability;
import uk.co.mysterymayhem.gravitymod.common.config.ConfigHandler;
import uk.co.mysterymayhem.gravitymod.common.util.boundingboxes.GravityAxisAlignedBB;

/**
 * Created by Mysteryem on 2016-08-07.
 */
@SideOnly(Side.CLIENT)
public class EntityRenderListener {

    private static final double rotationSpeed = ConfigHandler.animationRotationSpeed;
    private static final double rotationLength = GravityDirectionCapability.DEFAULT_TIMEOUT / rotationSpeed;
    private static final double rotationEnd = GravityDirectionCapability.DEFAULT_TIMEOUT - rotationLength;
    private static EntityLivingBase entityBeingRendered = null;
    private static boolean nameplateNeedToPop = false;
    private static boolean playerRotationNeedToPop = false;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onNameplateRenderPost(RenderLivingEvent.Specials.Post<EntityLivingBase> event) {
        if (nameplateNeedToPop) {
            nameplateNeedToPop = false;
            GlStateManager.popMatrix();
        }
        entityBeingRendered = null;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onNameplateRenderPre(RenderLivingEvent.Specials.Pre<EntityLivingBase> event) {
        if (playerRotationNeedToPop) {
            playerRotationNeedToPop = false;
            GlStateManager.popMatrix();
        }
        entityBeingRendered = event.getEntity();

        GlStateManager.pushMatrix();
        nameplateNeedToPop = true;

        // move nameplate into correct position if the player has been rotated due to non-downwards gravity
        if (entityBeingRendered instanceof EntityPlayer) {
            EntityPlayer playerBeingRendered = (EntityPlayer)entityBeingRendered;
            EnumGravityDirection gravityDirection = API.getGravityDirection(playerBeingRendered);
            if (gravityDirection != EnumGravityDirection.DOWN) {
                //see net.minecraft.client.renderer.entity renderLivingLabel for why this value in particular
                double distanceToUndo = playerBeingRendered.height + 0.5 - (playerBeingRendered.isSneaking() ? 0.25 : 0);
                GlStateManager.translate(0, -distanceToUndo, 0);
                double distanceToMove;
                switch (gravityDirection) {
                    case UP:
                        distanceToMove = distanceToUndo;
                        break;
                    default:
                        distanceToMove = distanceToUndo - API.getStandardEyeHeight(playerBeingRendered);
                        break;
                }
                double[] d = gravityDirection.adjustXYZValues(0, distanceToMove, 0);
                GlStateManager.translate(d[0], d[1], d[2]);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRender(RenderPlayerEvent.Pre event) {
        EntityPlayer player = event.getEntityPlayer();
        AxisAlignedBB entityBoundingBox = player.getEntityBoundingBox();
        if (!(entityBoundingBox instanceof GravityAxisAlignedBB)) {
            //Not good
            FMLLog.bigWarning("Tried rendering player %s, but their bounding box was not a GravityAxisAlignedBB", player.getName());
            return;
        }
        GravityAxisAlignedBB gAABB = (GravityAxisAlignedBB)entityBoundingBox;

        IGravityDirectionCapability capability = gAABB.getCapability();
        EnumGravityDirection gravityDirection = capability.getDirection();

        int timeoutTicks = capability.getTimeoutTicks();
        double effectiveTimeoutTicks = timeoutTicks - (1 * event.getPartialRenderTick());

        GlStateManager.pushMatrix();
        GlStateManager.translate(event.getX(), event.getY(), event.getZ());

        //TODO: Change to rotate about centre of gravity
        // Adjusts the player's rendered position due to the fact that gravityDirection.runCameraTransformation();
        // at the end of this method is not rotating the player about their centre of gravity
        gravityDirection.applyOtherPlayerRenderTransformations(player);

        if (timeoutTicks != 0 && effectiveTimeoutTicks > rotationEnd) {
            double numerator = GravityDirectionCapability.DEFAULT_TIMEOUT - effectiveTimeoutTicks;
            double denominator = rotationLength;

            double multiplier = numerator / denominator;
            double oneMinusMultiplier = 1 - multiplier;

            //TODO: Rewrite so it doesn't modify the player's position fields (will probably have to add something to EnumGravityDirection)
            // Work out the translation required such that we can rotate the player about their centre of gravity
            gravityDirection.returnCentreOfGravityToPlayerPos(player);
            Vec3d positionVector = player.getPositionVector();
            gravityDirection.offsetCentreOfGravityFromPlayerPos(player);
            Vec3d origin = gAABB.getOrigin();
            Vec3d subtract = origin.subtract(positionVector);
//
            GlStateManager.translate(-subtract.xCoord, -subtract.yCoord, -subtract.zCoord);
//
            Vec3i cameraTransformVars = gravityDirection.getCameraTransformVars();
            Vec3i prevCameraTransformVars = capability.getPrevDirection().getCameraTransformVars();

            GlStateManager.rotate((float)(prevCameraTransformVars.getX() * oneMinusMultiplier), 1, 0, 0);
            GlStateManager.rotate((float)(prevCameraTransformVars.getY() * oneMinusMultiplier), 0, 1, 0);
            GlStateManager.rotate((float)(prevCameraTransformVars.getZ() * oneMinusMultiplier), 0, 0, 1);

            GlStateManager.rotate((float)(-cameraTransformVars.getX() * oneMinusMultiplier), 1, 0, 0);
            GlStateManager.rotate((float)(-cameraTransformVars.getY() * oneMinusMultiplier), 0, 1, 0);
            GlStateManager.rotate((float)(-cameraTransformVars.getZ() * oneMinusMultiplier), 0, 0, 1);
//
            GlStateManager.translate(subtract.xCoord, subtract.yCoord, subtract.zCoord);
        }
        gravityDirection.runCameraTransformation();

        GlStateManager.translate(-event.getX(), -event.getY(), -event.getZ());
        playerRotationNeedToPop = true;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onRender(RenderPlayerEvent.Post event) {
        if (playerRotationNeedToPop) {
            playerRotationNeedToPop = false;
            GlStateManager.popMatrix();
        }
    }
}

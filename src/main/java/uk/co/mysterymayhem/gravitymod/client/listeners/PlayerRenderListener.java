package uk.co.mysterymayhem.gravitymod.client.listeners;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
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
import uk.co.mysterymayhem.gravitymod.common.util.Vec3dHelper;
import uk.co.mysterymayhem.gravitymod.common.util.boundingboxes.GravityAxisAlignedBB;

/**
 * Created by Mysteryem on 2016-08-07.
 */
@SideOnly(Side.CLIENT)
public class PlayerRenderListener {

    //TODO: Un-hardcode and force >=1
    private static final double rotationSpeed = 1;
    private static final double rotationLength = GravityDirectionCapability.DEFAULT_TIMEOUT/rotationSpeed;
    private static final double rotationEnd = GravityDirectionCapability.DEFAULT_TIMEOUT - rotationLength;

    private boolean needToPop = false;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRender(RenderPlayerEvent.Pre event) {
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

            GlStateManager.rotate((float) (prevCameraTransformVars.getX() * oneMinusMultiplier), 1, 0, 0);
            GlStateManager.rotate((float) (prevCameraTransformVars.getY() * oneMinusMultiplier), 0, 1, 0);
            GlStateManager.rotate((float) (prevCameraTransformVars.getZ() * oneMinusMultiplier), 0, 0, 1);

            GlStateManager.rotate((float) (-cameraTransformVars.getX() * oneMinusMultiplier), 1, 0, 0);
            GlStateManager.rotate((float) (-cameraTransformVars.getY() * oneMinusMultiplier), 0, 1, 0);
            GlStateManager.rotate((float) (-cameraTransformVars.getZ() * oneMinusMultiplier), 0, 0, 1);
//
            GlStateManager.translate(subtract.xCoord, subtract.yCoord, subtract.zCoord);
        }
        gravityDirection.runCameraTransformation();

        GlStateManager.translate(-event.getX(), -event.getY(), -event.getZ());
        this.needToPop = true;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public void onRender(RenderPlayerEvent.Post event) {
        if (this.needToPop) {
            this.needToPop = false;
            GlStateManager.popMatrix();
        }
    }
}

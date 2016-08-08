package uk.co.mysterymayhem.gravitymod;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import uk.co.mysterymayhem.gravitymod.events.GravityTransitionEvent;

/**
 * Created by Mysteryem on 2016-08-07.
 */

public class MovementInterceptionListener {

//    @SideOnly(Side.CLIENT)
//    @SubscribeEvent
//    public void onGravityChange(GravityTransitionEvent event) {
//        if (event.side == Side.CLIENT) {
//            if (GravityMod.proxy.gravityManagerServer.isPlayerUpsideDown())
//            if (GravityManagerClient.isClientUpsideDown() != event.newGravityIsUpsideDown) {
//
//            }
//        }
//
//    }

    private boolean setOnGroundInEndPhase = false;

    @SubscribeEvent
    public void onPlayerUpdate(PlayerTickEvent event) {
        //if (event.side == Side.CLIENT) {
            switch (event.phase) {
                case START:
                    //System.out.println("Below: " + event.player.worldObj.getBlockState(new BlockPos(event.player)).getBlock().getUnlocalizedName() + " at " + new BlockPos(event.player));
                    //System.out.println("Above: " + event.player.worldObj.getBlockState(new BlockPos(event.player).add(0, event.player.height + 1, 0)).getBlock() + " at " + new BlockPos(event.player).add(0, event.player.height + 1, 0));
                    //if (GravityManagerClient.isClientUpsideDown()) {
                    if (GravityMod.proxy.gravityManagerServer.isPlayerUpsideDown(event.player)) {
                        BlockPos posAbovePlayersHead = new BlockPos(event.player).add(0, event.player.height + 1, 0);
                        IBlockState blockState = event.player.worldObj.getBlockState(posAbovePlayersHead);
                        AxisAlignedBB collisionBoundingBox = blockState.getCollisionBoundingBox(event.player.worldObj, posAbovePlayersHead);
                        AxisAlignedBB playerCollisionBoundingBox = event.player.getCollisionBoundingBox();

                        //boolean headIsOnCeiling = event.player.isCollidedVertically;
//                        if((collisionBoundingBox != null) && (playerCollisionBoundingBox != null) && collisionBoundingBox.intersectsWith(playerCollisionBoundingBox)) {
//                            System.out.println("Block above intersects with player");
//                            //headIsOnCeiling = true;
//                        }
                        event.player.onGround = event.player.isCollidedVertically;
                        //event.player.onGround = headIsOnCeiling;
                        event.player.isAirBorne = !event.player.onGround;

                        if(event.player.onGround) {
                            //System.out.println("On ground");
//                            if (event.player == Minecraft.getMinecraft().thePlayer) {
//                                if(Minecraft.getMinecraft().thePlayer.movementInput.jump) {
//                                    event.player.jump();
//                                }
//                            }
                            event.player.motionY = 0;
                            event.player.fallDistance = 0;

                        }

                        else if (!event.player.onGround && !event.player.capabilities.isFlying) {
                            if (event.player.motionY < 0) {

                            }
                            // Default gravity seems to be -0.08, so add 0.16 to get 0.08
                            if (event.player.isInWater()) {
                                //TODO: Reverse the swimming upwards motion
                                //event.player.addVelocity(0, 0.05, 0);
                            }
                            else {
                                event.player.addVelocity(0, 0.16, 0);
                            }

                            event.player.handleWaterMovement();
                        }
                    }
                    break;
                case END:
                    //TODO: See if adding a player.onGround = true here will fix sprinting
                    break;
            }
        //}
    }

    @SubscribeEvent
    public void onLivingJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)event.getEntity();
            if (GravityMod.proxy.gravityManagerServer.isPlayerUpsideDown(player)) {
                player.motionY *= -1;
            }
        }
    }
}

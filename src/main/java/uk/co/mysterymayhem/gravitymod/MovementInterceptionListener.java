package uk.co.mysterymayhem.gravitymod;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import uk.co.mysterymayhem.gravitymod.capabilities.GravityCapability;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

/**
 * Created by Mysteryem on 2016-08-07.
 */

public class MovementInterceptionListener {

//    @SideOnly(Side.CLIENT)
//    @SubscribeEvent
//    public void onGravityChange(GravityTransitionEvent event) {
//        if (event.side == Side.CLIENT) {
//            if (GravityMod.proxy.gravityManagerCommon.isPlayerUpsideDown())
//            if (GravityManagerClient.isClientUpsideDown() != event.newGravityIsUpsideDown) {
//
//            }
//        }
//
//    }
    private MethodHandle getterNetHandlerPlayServer$floatingTickCount = null;
    private MethodHandle setterNetHandlerPlayServer$floatingTickCount = null;

    public MovementInterceptionListener() {
        this.initMethodHandles();
    }

    public void initMethodHandles() {
        if (getterNetHandlerPlayServer$floatingTickCount == null && setterNetHandlerPlayServer$floatingTickCount == null) {
            try {
                Field floatingTickCount = ReflectionHelper.findField(NetHandlerPlayServer.class, "floatingTickCount", "field_147365_f");
                getterNetHandlerPlayServer$floatingTickCount = MethodHandles.lookup().unreflectGetter(floatingTickCount);
                setterNetHandlerPlayServer$floatingTickCount = MethodHandles.lookup().unreflectSetter(floatingTickCount);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    //@SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerUpdate2(PlayerTickEvent event) {

    }


    private boolean setOnGroundInEndPhase = false;

    //TODO: Going to need a rewrite to fix sprinting probably, also, no way that this method will be able to work for gravity directions that are not "UP"
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerUpdate(PlayerTickEvent event) {
        //if (event.side == Side.CLIENT) {
            switch (event.phase) {
                case START:
                    //System.out.println("Below: " + event.player.worldObj.getBlockState(new BlockPos(event.player)).getBlock().getUnlocalizedName() + " at " + new BlockPos(event.player));
                    //System.out.println("Above: " + event.player.worldObj.getBlockState(new BlockPos(event.player).add(0, event.player.height + 1, 0)).getBlock() + " at " + new BlockPos(event.player).add(0, event.player.height + 1, 0));
                    //if (GravityManagerClient.isClientUpsideDown()) {
                    if (GravityCapability.getGravityDirection(event.player) == EnumGravityDirection.UP) {
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
                            BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain(event.player.posX, event.player.getEntityBoundingBox().maxY + 1.0D, event.player.posZ);
                            Block block = event.player.worldObj.getBlockState(blockpos$pooledmutableblockpos).getBlock();
                            //block.slipperiness;
                            //System.out.println("On ground");
//                            if (event.player == Minecraft.getMinecraft().thePlayer) {
//                                if(Minecraft.getMinecraft().thePlayer.movementInput.jump) {
//                                    event.player.jump();
//                                }
//                            }
                            event.player.motionY = 0;
                            event.player.fallDistance = 0;
                            this.setOnGroundInEndPhase = true;
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
                                // Prevent players getting kicked for flying when falling upwards for a long time
                                if (event.player instanceof EntityPlayerMP) {
                                    EntityPlayerMP playerMP = (EntityPlayerMP)event.player;
                                    try {
                                        int floatingTickCount = (Integer)this.getterNetHandlerPlayServer$floatingTickCount.invoke(playerMP.connection);
                                        if (floatingTickCount > 0) {
                                            floatingTickCount--;
                                            this.setterNetHandlerPlayServer$floatingTickCount.invoke(playerMP.connection, floatingTickCount);
                                        }
                                    } catch (Throwable throwable) {
                                        throw new RuntimeException(throwable);
                                    }
                                }
                            }

                            event.player.handleWaterMovement();
                        }
                        if (event.player.posY > (255 + 64)) {
                            //event.player.kill();
                            event.player.attackEntityFrom(DamageSource.outOfWorld, 4.0F);
                        }
                    }
                    break;
                case END:
                    //TODO: See if adding a player.onGround = true here will fix sprinting
                    if(this.setOnGroundInEndPhase) {
                        event.player.onGround = true;
                        event.player.isAirBorne = false;
                        event.player.fallDistance = 0;
                        this.setOnGroundInEndPhase = false;
                    }
                    break;
            }
        //}
    }

    @SubscribeEvent
    public void onLivingJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)event.getEntity();
            if (GravityCapability.getGravityDirection(player) == EnumGravityDirection.UP) {
                player.motionY *= -1;
            }
        }
    }
}

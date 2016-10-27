package uk.co.mysterymayhem.gravitymod;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.server.FMLServerHandler;
import uk.co.mysterymayhem.gravitymod.api.API;
import uk.co.mysterymayhem.gravitymod.api.EnumGravityDirection;
import uk.co.mysterymayhem.gravitymod.asm.Hooks;

import java.util.Optional;
import java.util.UUID;

/**
 * Created by Mysteryem on 2016-08-07.
 */
public class DebugHelperListener {

//    @SubscribeEvent
//    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
//        StringBuilder builder = new StringBuilder();
//        if (event.side == Side.CLIENT) {
//            builder.append("Client: ");
//        }
//        else {
//            builder.append("Server: ");
//        }
//        if (event.phase == TickEvent.Phase.START) {
//            builder.append("start");
//        }
//        else {
//            builder.append("end");
//        }
////        FMLLog.info(builder.toString());
//    }

//    @SubscribeEvent
//    public void onItemUse(PlayerInteractEvent event) {
//        //TODO: Remove?
////        if (event.getSide() == Side.SERVER && event.getHand() == EnumHand.MAIN_HAND && (event instanceof PlayerInteractEvent.RightClickBlock || event instanceof PlayerInteractEvent.RightClickItem)) {
////            ItemStack itemStack = event.getItemStack();
////            if (itemStack != null) {
////                Item item = itemStack.getItem();
////                if (item.equals(Items.ARROW)) {
////                    GravityMod.proxy.getGravityManager().doGravityTransition(EnumGravityDirection.DOWN, (EntityPlayerMP) event.getEntityPlayer(), false);
////                } else if (item.equals(Items.BLAZE_ROD)) {
////                    GravityMod.proxy.getGravityManager().doGravityTransition(EnumGravityDirection.UP, (EntityPlayerMP) event.getEntityPlayer(), false);
////                } else if (item.equals(Items.BRICK)) {
////                    GravityMod.proxy.getGravityManager().doGravityTransition(EnumGravityDirection.NORTH, (EntityPlayerMP) event.getEntityPlayer(), false);
////                } else if (item.equals(Items.BOOK)) {
////                    GravityMod.proxy.getGravityManager().doGravityTransition(EnumGravityDirection.EAST, (EntityPlayerMP) event.getEntityPlayer(), false);
////                } else if (item.equals(Items.CLOCK)) {
////                    GravityMod.proxy.getGravityManager().doGravityTransition(EnumGravityDirection.SOUTH, (EntityPlayerMP) event.getEntityPlayer(), false);
////                } else if (item.equals(Items.DIAMOND)) {
////                    GravityMod.proxy.getGravityManager().doGravityTransition(EnumGravityDirection.WEST, (EntityPlayerMP) event.getEntityPlayer(), false);
////                } else if (item.equals(Items.STICK)) {
////
////                    EntityPlayer player = event.getEntityPlayer();
////
//////                    if (1 == 1) {
//////                        Vec3d vec3d = Hooks.inverseAdjustVec(player.getLookVec(), player);
//////
//////                        Vec3d lookpos = vec3d.addVector(player.posX, player.posY + player.getEyeHeight(), player.posZ);
//////                        player.worldObj.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, player.posX, player.posY + player.getEyeHeight(), player.posZ, 0, 0, 0);
//////                    }
//////                    FMLLog.info("" + player.getLookVec());
////////                    float f = MathHelper.cos(-yaw * 0.017453292F - (float)Math.PI);
////////                    float f1 = MathHelper.sin(-yaw * 0.017453292F - (float)Math.PI);
////////                    float f2 = -MathHelper.cos(-pitch * 0.017453292F);
////////                    float f3 = MathHelper.sin(-pitch * 0.017453292F);
////////                    return new Vec3d((double)(f1 * f2), (double)f3, (double)(f * f2));
//////
//////                    float f = 0.017453292F;
//////                    float f1 = MathHelper.cos(player.rotationYaw * 0.017453292F);
//////                    float f2 = MathHelper.sin(player.rotationYaw * 0.017453292F);
//////                    float f3 = -f2 * MathHelper.sin(player.rotationPitch * 0.017453292F);
//////                    float f4 = f1 * MathHelper.sin(player.rotationPitch * 0.017453292F);
//////                    float f5 = MathHelper.cos(player.rotationPitch * 0.017453292F);
//////
//////                    float rotationX = f1;
//////                    float rotationZ = f5;
//////                    float rotationYZ = f2;
//////                    float rotationXY = f3;
//////                    float rotationXZ = f4;
////
//////                    FMLLog.info(event.getEntityPlayer().getHorizontalFacing().getName() + "(" + event.getEntityPlayer().getHorizontalFacing().ordinal());
////                    FMLLog.info("" + Hooks.getRelativeYaw(event.getEntityPlayer()) + ", " + Hooks.getRelativePitch(event.getEntityPlayer()));
//////                    FMLLog.info("" + Hooks.getAdjustedYaw(event.getEntityPlayer()) + ", " + Hooks.getAdjustedPitch(event.getEntityPlayer()));
//////
//////                    double yaw = event.getEntityPlayer().rotationYaw;
//////                    double pitch = event.getEntityPlayer().rotationPitch;
//////
//////                    final double f = Math.cos(-yaw * (Math.PI / 180d) - Math.PI);
//////                    final double f1 = Math.sin(-yaw * (Math.PI / 180d) - Math.PI);
//////                    final double f2 = -Math.cos(-pitch * (Math.PI/180d));
//////                    final double f3 = Math.sin(-pitch * (Math.PI/180d));
//////
//////                    Vec3d lookVecWithDoubleAccuracy =  new Vec3d(f1 * f2, f3, f * f2);
//////
//////                    Vec3d adjustedVec = Hooks.adjustVec(lookVecWithDoubleAccuracy, event.getEntityPlayer());
////////        Vec3d adjustedVec = Hooks.inverseAdjustVec(lookVecWithDoubleAccuracy, entity);
//////                    double modifiedYaw = (float)(Math.atan2(-adjustedVec.xCoord, adjustedVec.zCoord) * (180D/Math.PI));
//////                    double modifiedPitch = (float)-(Math.asin(adjustedVec.yCoord) * (180D/Math.PI));
//////                    modifiedYaw=(modifiedYaw + 180d) % 180;
//////                    modifiedPitch=(modifiedPitch + 180d) % 180;
//////                    FMLLog.info("" + modifiedYaw + ", " + modifiedPitch);
////
////                }
////            }
////        }
//        if (event.getHand() == EnumHand.MAIN_HAND && (event instanceof PlayerInteractEvent.RightClickItem)) {
//            ItemStack itemStack = event.getItemStack();
//            if (itemStack != null && itemStack.getItem() == Items.BEEF) {
//                StringBuilder builder = new StringBuilder();
//                if (event.getSide() == Side.CLIENT) {
//                    builder.append("Client: ");
//                } else {
//                    builder.append("Server: ");
//                }
//                EntityPlayer player = event.getEntityPlayer();
//                builder.append("\nGravity: ").append(API.getGravityDirection(player).name());
//                builder.append("\nPos: ").append(new Vec3d(player.posX, player.posY, player.posZ)).append("\nBB: ").append(player.getEntityBoundingBox());
//                builder.append("\nOnGround: ").append(player.onGround).append("\n");;
//                FMLLog.info(builder.toString());
//            }
//        }
////        else if (event.getSide() == Side.SERVER && event.getHand() == EnumHand.MAIN_HAND && event instanceof PlayerInteractEvent.EntityInteractSpecific) {
////            ItemStack stack = event.getItemStack();
////            if (stack != null) {
////                Item item = stack.getItem();
////                if (item.equals(Items.STICK)) {
////                    EntityPlayer player = event.getEntityPlayer();
//////                    Vec3d playerEyePos = new Vec3d(player.posX, player.posY + player.getEyeHeight(), player.posZ);
////                    Vec3d lookVec = player.getLookVec();
////                    Entity target = ((PlayerInteractEvent.EntityInteractSpecific) event).getTarget();
////                    target.motionX += lookVec.xCoord;
////                    target.motionY += lookVec.xCoord;
////                    target.motionZ += lookVec.xCoord;
////                }
////            }
////        }
//    }
}

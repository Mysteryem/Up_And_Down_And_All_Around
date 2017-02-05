package uk.co.mysterymayhem.gravitymod.common.capabilities.gravitydirection;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import uk.co.mysterymayhem.gravitymod.GravityMod;
import uk.co.mysterymayhem.gravitymod.api.EnumGravityDirection;
import uk.co.mysterymayhem.gravitymod.common.config.ConfigHandler;
import uk.co.mysterymayhem.gravitymod.common.util.boundingboxes.GravityAxisAlignedBB;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by Mysteryem on 2016-08-14.
 */
public class GravityDirectionCapability {
    public static final EnumGravityDirection DEFAULT_GRAVITY = EnumGravityDirection.DOWN;
    public static final int DEFAULT_TIMEOUT = 20;
    public static final int MIN_PRIORITY = Integer.MIN_VALUE;
    public static final String RESOURCE_NAME = "IGravityCapability";
    public static final ResourceLocation CAPABILITY_RESOURCE_LOCATION = new ResourceLocation(GravityMod.MOD_ID, RESOURCE_NAME);
    private static final float oppositeDirectionFallDistanceMultiplier = ConfigHandler.oppositeDirectionFallDistanceMultiplier;
    private static final float otherDirectionsFallDistanceMultiplier = ConfigHandler.otherDirectionFallDistanceMultiplier;
    @CapabilityInject(IGravityDirectionCapability.class)
    public static Capability<IGravityDirectionCapability> GRAVITY_CAPABILITY_INSTANCE = null;

    public static EnumGravityDirection getGravityDirection(String playerName, World world) {
        return getGravityDirection(getGravityCapability(playerName, world));
    }

    public static EnumGravityDirection getGravityDirection(IGravityDirectionCapability capability) {
        return capability == null ? DEFAULT_GRAVITY : capability.getDirection();
    }

    @Nullable
    private static IGravityDirectionCapability getGravityCapability(@Nonnull String playerName, @Nonnull World world) {
        world.getPlayerEntityByName(playerName);
        EntityPlayer playerByUsername = world.getPlayerEntityByName(playerName);
        return playerByUsername == null ? null : getGravityCapability(playerByUsername);
    }

    @Nullable
    public static IGravityDirectionCapability getGravityCapability(@Nonnull EntityPlayer player) {
        return player.getCapability(GRAVITY_CAPABILITY_INSTANCE, null);
    }

    public static EnumGravityDirection getGravityDirection(EntityPlayer player) {
        return getGravityDirection(getGravityCapability(player));
    }

    public static AxisAlignedBB newGravityAxisAligned(EntityPlayer player, AxisAlignedBB old) {
        IGravityDirectionCapability gravityCapability = getGravityCapability(player);
        if (gravityCapability == null) {
            // Should no longer occur during Entity::<init>, but there's nothing stopping other mods from calling code
            // in a EntityConstructingEvent (which happens before Capabilities are added)
            // Occurs during construction of players (<init>)
            // Once the capability is added, we'll make sure the player's bounding box is a GravityAxisAlignedBB
            return old;
        }
        return new GravityAxisAlignedBB(gravityCapability, old);
    }

    public static void registerCapability() {
        CapabilityManager.INSTANCE.register(IGravityDirectionCapability.class, new GravityDirectionCapabilityStorage(), GravityDirectionCapabilityImpl::new);
        MinecraftForge.EVENT_BUS.register(new GravityDirectionCapabilityEventHandler());
    }

    public static void setGravityDirection(EntityPlayer player, EnumGravityDirection newDirection, boolean noTimeout) {
        final boolean clientSide = player.worldObj.isRemote;

        // Get the player's capability
        IGravityDirectionCapability capability = getGravityCapability(player);
        // Get the current direction
        EnumGravityDirection oldDirection = capability.getDirection();
        // Get the current eye position (used when there's no safe position to put the player)
        Vec3d oldEyePos = player.getPositionVector().addVector(0, player.getEyeHeight(), 0);
        // Apply any changes the old direction needs to make before the direction gets changed
        oldDirection.preModifyPlayerOnGravityChange(player, newDirection);
        // Set the new direction
        setGravityDirection(capability, newDirection, noTimeout);
        // Apply any changes the new direction needs to make now that the direction has been changed
        newDirection.postModifyPlayerOnGravityChange(player, oldDirection, oldEyePos);

        if (oldDirection != newDirection) {
            if (oldDirection.getOpposite() == newDirection) {
                player.fallDistance *= oppositeDirectionFallDistanceMultiplier;
            }
            else {
                player.fallDistance *= otherDirectionsFallDistanceMultiplier;
            }
        }
        // This CAN occur on the client (so I'm only logging it on the server side)
        // Client is connected to server,
        // Server tells client to create an EntityOtherPlayerMP,
        // New player's direction is DOWN by default
        // Client requests gravity direction of player from server
        // Client receives the other player's direction
        // If DOWN, we would get this message (or whatever the default direction happens to be)
        else if (!clientSide) {
            GravityMod.logInfo("Tried to set gravity direction of %s to %s, but it was already %s." +
                    " I'm pretty sure this shouldn't happen.", player.getName(), oldDirection.name(), newDirection.name());
        }

        // This information is used in rendering, there's no reason to do it if we're a server
        if (clientSide) {
            Vec3d newEyePos = player.getPositionVector().addVector(0, player.getEyeHeight(), 0);

            Vec3d eyesDiff = newEyePos.subtract(oldEyePos);

            capability.setEyePosChangeVector(eyesDiff);
        }
    }

    private static void setGravityDirection(IGravityDirectionCapability capability, EnumGravityDirection direction, boolean noTimeout) {
        if (capability != null) {
            if (noTimeout) {
                capability.setDirectionNoTimeout(direction);
            }
            else {
                capability.setDirection(direction);
            }
        }
    }

}

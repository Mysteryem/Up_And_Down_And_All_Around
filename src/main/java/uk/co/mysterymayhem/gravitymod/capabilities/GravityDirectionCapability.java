package uk.co.mysterymayhem.gravitymod.capabilities;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.FMLLog;
import uk.co.mysterymayhem.gravitymod.api.EnumGravityDirection;
import uk.co.mysterymayhem.gravitymod.GravityMod;
import uk.co.mysterymayhem.gravitymod.util.GravityAxisAlignedBB;

import java.util.HashSet;

/**
 * Created by Mysteryem on 2016-08-14.
 */
public class GravityDirectionCapability {
    public static final EnumGravityDirection DEFAULT_GRAVITY = EnumGravityDirection.DOWN;
    public static final int MIN_PRIORITY = Integer.MIN_VALUE;
    public static final int DEFAULT_TIMEOUT = 20;

    public static void registerCapability() {
        CapabilityManager.INSTANCE.register(IGravityDirectionCapability.class, new GravityDirectionCapabilityStorage(), new GravityDirectionCapabilityFactory());
        MinecraftForge.EVENT_BUS.register(new GravityDirectionCapabilityEventHandler());
    }

    @CapabilityInject(IGravityDirectionCapability.class)
    public static Capability<IGravityDirectionCapability> GRAVITY_CAPABILITY_INSTANCE = null;

    public static final String RESOURCE_NAME = "IGravityCapability";
    public static final ResourceLocation CAPABILITY_RESOURCE_LOCATION  = new ResourceLocation(GravityMod.MOD_ID, RESOURCE_NAME);

    private static IGravityDirectionCapability getGravityCapability(String playerName, World world) {
        world.getPlayerEntityByName(playerName);
        EntityPlayer playerByUsername = world.getPlayerEntityByName(playerName);
        return playerByUsername == null ? null : getGravityCapability(playerByUsername);
    }

    public static IGravityDirectionCapability getGravityCapability(EntityPlayer player) {
        return player.getCapability(GRAVITY_CAPABILITY_INSTANCE, null);
    }

    public static EnumGravityDirection getGravityDirection(String playerName, World world) {
        return getGravityDirection(getGravityCapability(playerName, world));
    }

    public static EnumGravityDirection getGravityDirection(EntityPlayer player) {
        return getGravityDirection(getGravityCapability(player));
    }

    public static EnumGravityDirection getGravityDirection(IGravityDirectionCapability capability) {
        return capability == null ? DEFAULT_GRAVITY : capability.getDirection();
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
        else {

        }
        return new GravityAxisAlignedBB(gravityCapability, old);
    }

    private static void setGravityDirection(String playerName, EnumGravityDirection direction, World world, boolean noTimeout) {
        world.getPlayerEntityByName(playerName);
        EntityPlayer playerByUsername = world.getPlayerEntityByName(playerName);
        if (playerByUsername != null) {
            setGravityDirection(playerByUsername, direction, noTimeout);
        }
    }

    private static final HashSet<SPacketPlayerPosLook.EnumFlags> allRelative = new HashSet<>();
    static {
        for (SPacketPlayerPosLook.EnumFlags flag : SPacketPlayerPosLook.EnumFlags.values()) {
            allRelative.add(flag);
        }
    }

    public static void setGravityDirection(EntityPlayer player, EnumGravityDirection direction, boolean noTimeout) {
        double x = player.posX;
        double y = player.posY;
        double z = player.posZ;
        IGravityDirectionCapability capability = getGravityCapability(player);
        EnumGravityDirection oldDirection = capability.getDirection();
        Vec3d oldEyePos = player.getPositionVector().addVector(0, player.getEyeHeight(), 0);
        oldDirection.preModifyPlayerOnGravityChange(player, direction);
        setGravityDirection(capability, direction, noTimeout);
        direction.postModifyPlayerOnGravityChange(player, oldDirection, oldEyePos);
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

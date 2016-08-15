package uk.co.mysterymayhem.gravitymod.capabilities;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import uk.co.mysterymayhem.gravitymod.EnumGravityDirection;
import uk.co.mysterymayhem.gravitymod.GravityMod;

import java.util.concurrent.Callable;

/**
 * Created by Mysteryem on 2016-08-14.
 */
public class GravityCapability {
    public static void registerCapability() {
        CapabilityManager.INSTANCE.register(IGravityCapability.class, new Storage(), new Factory());
        MinecraftForge.EVENT_BUS.register(new GravityCapabilityEventHandler());
    }

    @CapabilityInject(IGravityCapability.class)
    public static Capability<IGravityCapability> GRAVITY_CAPABILITY_INSTANCE = null;

    public static final String RESOURCE_NAME = "IGravityCapability";
    public static final ResourceLocation CAPABILITY_RESOURCE_LOCATION  = new ResourceLocation(GravityMod.MOD_ID, RESOURCE_NAME);

    public static IGravityCapability getGravityCapability(String playerName, World world) {
        world.getPlayerEntityByName(playerName);
        EntityPlayer playerByUsername = world.getPlayerEntityByName(playerName);
        return playerByUsername == null ? null : getGravityCapability(playerByUsername);
    }

    public static IGravityCapability getGravityCapability(EntityPlayer player) {
        return player.getCapability(GRAVITY_CAPABILITY_INSTANCE, null);
    }

    public static EnumGravityDirection getGravityDirection(String playerName, World world) {
        return getGravityDirection(getGravityCapability(playerName, world));
    }

    public static EnumGravityDirection getGravityDirection(EntityPlayer player) {
        return getGravityDirection(getGravityCapability(player));
    }

    public static EnumGravityDirection getGravityDirection(IGravityCapability capability) {
        return capability == null ? null : capability.getDirection();
    }

    public static void setGravityCapability(String playerName, EnumGravityDirection direction, World world) {
        world.getPlayerEntityByName(playerName);
        EntityPlayer playerByUsername = world.getPlayerEntityByName(playerName);
        if (playerByUsername != null) {
            setGravityDirection(playerByUsername, direction);
        }
//        //DEBUG:
//        else {
//            FMLLog.info("Could not set gravity for %s, player could not be found in %s", playerName, world.getWorldInfo().getWorldName());
//        }
    }

    public static void setGravityDirection(EntityPlayer player, EnumGravityDirection direction) {
        setGravityDirection(getGravityCapability(player), direction);
    }

    public static void setGravityDirection(IGravityCapability capability, EnumGravityDirection direction) {
        if (capability != null) {
            capability.setDirection(direction);
        }
    }

    public interface IGravityCapability {
        EnumGravityDirection getDirection();
        void setDirection(EnumGravityDirection direction);
    }

    public static class GravityCapabilityImpl implements IGravityCapability {
        private EnumGravityDirection direction;

        public GravityCapabilityImpl() {
            this.direction = EnumGravityDirection.DOWN;
        }

        public GravityCapabilityImpl(EnumGravityDirection direction) {
            this.direction = direction;
        }

        public EnumGravityDirection getDirection() {
            return direction;
        }

        public void setDirection(EnumGravityDirection direction) {
            this.direction = direction;
        }
    }

    private static class Storage implements Capability.IStorage<IGravityCapability> {

        @Override
        public NBTBase writeNBT(Capability<IGravityCapability> capability, IGravityCapability instance, EnumFacing side) {
            return new NBTTagInt(instance.getDirection().ordinal());
        }

        @Override
        public void readNBT(Capability<IGravityCapability> capability, IGravityCapability instance, EnumFacing side, NBTBase nbt) {
            instance.setDirection(EnumGravityDirection.values()[((NBTPrimitive) nbt).getInt()]);
        }
    }

    private static class Factory implements Callable<IGravityCapability> {

        @Override
        public IGravityCapability call() throws Exception {
            return new GravityCapabilityImpl();
        }
    }

    /**
     * Created by Mysteryem on 2016-08-14.
     */
    public static class GravityCapabilityEventHandler {

        @SubscribeEvent
        public void onEntityContruct(AttachCapabilitiesEvent.Entity event) {
            if (event.getEntity() instanceof EntityPlayer) {
                event.addCapability(CAPABILITY_RESOURCE_LOCATION, new ICapabilitySerializable<NBTPrimitive>() {

                    IGravityCapability instance = GRAVITY_CAPABILITY_INSTANCE.getDefaultInstance();

                    @Override
                    public NBTPrimitive serializeNBT() {
                        return (NBTPrimitive) GRAVITY_CAPABILITY_INSTANCE.getStorage().writeNBT(GRAVITY_CAPABILITY_INSTANCE, instance, null);
                    }

                    @Override
                    public void deserializeNBT(NBTPrimitive nbt) {
                        GRAVITY_CAPABILITY_INSTANCE.getStorage().readNBT(GRAVITY_CAPABILITY_INSTANCE, instance, null, nbt);
                    }

                    @Override
                    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
                        return GRAVITY_CAPABILITY_INSTANCE == capability;
                    }

                    @Override
                    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
                        return capability == GRAVITY_CAPABILITY_INSTANCE ? GRAVITY_CAPABILITY_INSTANCE.<T>cast(instance) : null;
                    }
                });
            }
        }
    }
}

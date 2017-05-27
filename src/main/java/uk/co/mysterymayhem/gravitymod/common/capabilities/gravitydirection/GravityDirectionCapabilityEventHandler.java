package uk.co.mysterymayhem.gravitymod.common.capabilities.gravitydirection;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import uk.co.mysterymayhem.gravitymod.common.util.boundingboxes.GravityAxisAlignedBB;

import javax.annotation.Nonnull;

/**
 * Listens to the construction of entities and adds the GravityDirectionCapability to them if they're a player.
 * Also sets the player's initial bounding box.
 * Created by Mysteryem on 2016-08-14.
 */
@SuppressWarnings("WeakerAccess")
public class GravityDirectionCapabilityEventHandler {

    @SubscribeEvent
    public static void onEntityContruct(AttachCapabilitiesEvent<Entity> event) {
        Object ent = event.getObject();
        if (ent instanceof EntityPlayer) {
            final EntityPlayer player = (EntityPlayer)ent;
            event.addCapability(GravityDirectionCapability.CAPABILITY_RESOURCE_LOCATION, new ICapabilitySerializable<NBTPrimitive>() {

                IGravityDirectionCapability instance = GravityDirectionCapability.GRAVITY_CAPABILITY_INSTANCE.getDefaultInstance();

                // Give the player a GravityAxisAlignedBB as early as possible (can't give them one during the EntityConstructingEvent
                // as the AttachCapabilitiesEvent is fired after it
                {
                    player.setEntityBoundingBox(new GravityAxisAlignedBB(this.instance, player.getEntityBoundingBox()));
                }

                @Override
                public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {
                    return GravityDirectionCapability.GRAVITY_CAPABILITY_INSTANCE == capability;
                }

                @Override
                public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
                    return capability == GravityDirectionCapability.GRAVITY_CAPABILITY_INSTANCE ? GravityDirectionCapability.GRAVITY_CAPABILITY_INSTANCE.<T>cast(this.instance) : null;
                }

                @Override
                public NBTPrimitive serializeNBT() {
                    return (NBTPrimitive)GravityDirectionCapability.GRAVITY_CAPABILITY_INSTANCE.getStorage().writeNBT(GravityDirectionCapability.GRAVITY_CAPABILITY_INSTANCE, this.instance, null);
                }

                @Override
                public void deserializeNBT(NBTPrimitive nbt) {
                    GravityDirectionCapability.GRAVITY_CAPABILITY_INSTANCE.getStorage().readNBT(GravityDirectionCapability.GRAVITY_CAPABILITY_INSTANCE, this.instance, null, nbt);
                }
            });
            //
        }
    }
}

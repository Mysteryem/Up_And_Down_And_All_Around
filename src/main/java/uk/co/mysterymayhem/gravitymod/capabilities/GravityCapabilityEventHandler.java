package uk.co.mysterymayhem.gravitymod.capabilities;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import uk.co.mysterymayhem.gravitymod.util.GravityAxisAlignedBB;

/**
 * Created by Mysteryem on 2016-08-14.
 */
public class GravityCapabilityEventHandler {

    @SubscribeEvent
    public void onEntityContruct(AttachCapabilitiesEvent<Entity> event) {
        Entity ent = event.getObject();
        if (ent instanceof EntityPlayer) {
            final EntityPlayer player = (EntityPlayer)ent;
            event.addCapability(GravityDirectionCapability.CAPABILITY_RESOURCE_LOCATION, new ICapabilitySerializable<NBTPrimitive>() {

                IGravityDirectionCapability instance = GravityDirectionCapability.GRAVITY_CAPABILITY_INSTANCE.getDefaultInstance();
                //TODO: This should work right?
                {
                    player.setEntityBoundingBox(new GravityAxisAlignedBB(instance, player.getEntityBoundingBox()));
                }

                @Override
                public NBTPrimitive serializeNBT() {
                    return (NBTPrimitive) GravityDirectionCapability.GRAVITY_CAPABILITY_INSTANCE.getStorage().writeNBT(GravityDirectionCapability.GRAVITY_CAPABILITY_INSTANCE, instance, null);
                }

                @Override
                public void deserializeNBT(NBTPrimitive nbt) {
                    GravityDirectionCapability.GRAVITY_CAPABILITY_INSTANCE.getStorage().readNBT(GravityDirectionCapability.GRAVITY_CAPABILITY_INSTANCE, instance, null, nbt);
                }

                @Override
                public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
                    return GravityDirectionCapability.GRAVITY_CAPABILITY_INSTANCE == capability;
                }

                @Override
                public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
                    return capability == GravityDirectionCapability.GRAVITY_CAPABILITY_INSTANCE ? GravityDirectionCapability.GRAVITY_CAPABILITY_INSTANCE.<T>cast(instance) : null;
                }
            });
            //
        }
    }
}

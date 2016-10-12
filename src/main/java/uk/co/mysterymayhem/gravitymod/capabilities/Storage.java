package uk.co.mysterymayhem.gravitymod.capabilities;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import uk.co.mysterymayhem.gravitymod.api.EnumGravityDirection;

/**
 * Created by Mysteryem on 2016-10-10.
 */
class Storage implements Capability.IStorage<IGravityDirectionCapability> {

    //TODO: Store any of the new fields?
    @Override
    public NBTBase writeNBT(Capability<IGravityDirectionCapability> capability, IGravityDirectionCapability instance, EnumFacing side) {
        return new NBTTagInt(instance.getDirection().ordinal());
    }

    @Override
    public void readNBT(Capability<IGravityDirectionCapability> capability, IGravityDirectionCapability instance, EnumFacing side, NBTBase nbt) {
        EnumGravityDirection direction = EnumGravityDirection.values()[((NBTPrimitive) nbt).getInt()];
        instance.setDirection(direction);
        instance.setDirectionNoTimeout(direction);
        instance.setPendingDirection(direction, Integer.MIN_VALUE + 1);
    }
}

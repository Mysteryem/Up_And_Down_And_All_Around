package uk.co.mysterymayhem.gravitymod.common.entities;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import uk.co.mysterymayhem.gravitymod.api.EnumGravityDirection;
import uk.co.mysterymayhem.gravitymod.client.renderers.RenderGravityEntityItem;
import uk.co.mysterymayhem.gravitymod.common.registries.IGravityModEntityClassWrapper;

/**
 * Class exists so we can have different Render class for these item entities
 * Created by Mysteryem on 2016-11-05.
 */
public class EntityGravityItem extends EntityItem implements IEntityAdditionalSpawnData {

    public static final String NAME = "itemgravity";

    private EnumGravityDirection direction = EnumGravityDirection.DOWN;

    @SuppressWarnings("unused")
    public EntityGravityItem(World world) {
        super(world);
        this.setNoGravity(true);
    }

    public EntityGravityItem(EnumGravityDirection direction, EntityItem orig) {
        super(orig.worldObj);
        this.direction = direction;
        NBTTagCompound tag = new NBTTagCompound();

        // Is there no better way to copy the relevant information?
        orig.writeToNBT(tag);
        super.readFromNBT(tag);
        this.setNoGravity(true);
    }

    private static final String DIRECTION_NBT_STRING = "gravitydirection";

    // Read direction information
    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        if (compound.hasKey(DIRECTION_NBT_STRING)) {
            this.direction = EnumGravityDirection.getSafeDirectionFromOrdinal(compound.getShort(DIRECTION_NBT_STRING));
        }
        super.readEntityFromNBT(compound);
    }

    // Write direction information
    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        compound.setShort(DIRECTION_NBT_STRING, (short)this.direction.ordinal());
        super.writeEntityToNBT(compound);
    }

    public EnumGravityDirection getDirection() {
        return this.direction;
    }

    // Send direction data to client
    @Override
    public void writeSpawnData(ByteBuf buffer) {
        buffer.writeShort(this.direction.ordinal());
    }

    // Read direction data from server
    @Override
    public void readSpawnData(ByteBuf additionalData) {
        this.direction = EnumGravityDirection.getSafeDirectionFromOrdinal(additionalData.readShort());
    }

    /**
     * Created by Mysteryem on 2016-12-14.
     */
    public static class Wrapper implements IGravityModEntityClassWrapper<EntityGravityItem> {
        @Override
        public Class<EntityGravityItem> getEntityClass() {
            return EntityGravityItem.class;
        }

        @Override
        public int getTrackingRange() {
            return 64;
        }

        @Override
        public int getUpdateFrequency() {
            return 20;
        }

        @Override
        public boolean sendsVelocityUpdates() {
            return true;
        }

        @SideOnly(Side.CLIENT)
        @Override
        public IRenderFactory<? super EntityGravityItem> getRenderFactory() {
            return RenderGravityEntityItem::new;
        }

        @Override
        public String getName() {
            return "itemgravity";
        }
    }
}

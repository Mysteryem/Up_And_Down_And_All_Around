package uk.co.mysterymayhem.gravitymod.common.items.materials;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import uk.co.mysterymayhem.gravitymod.client.renderers.RenderNothing;
import uk.co.mysterymayhem.gravitymod.common.config.ConfigHandler;
import uk.co.mysterymayhem.gravitymod.common.registries.IGravityModEntityClassWrapper;
import uk.co.mysterymayhem.gravitymod.common.registries.IGravityModItem;
import uk.co.mysterymayhem.gravitymod.common.registries.StaticItems;
import uk.co.mysterymayhem.mystlib.annotations.UsedReflexively;

import java.util.List;
import java.util.Random;

/**
 * Created by Mysteryem on 2017-01-26.
 */
public class ItemDestabilisedGravityDust extends Item implements IGravityModItem<ItemDestabilisedGravityDust> {

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
        if (ConfigHandler.destabilisedGravityDustDissipatesWhenDropped) {
            tooltip.add(I18n.format("mouseovertext.mysttmtgravitymod.destabilisedgravitydust.warning"));
        }
    }

    // We have a custom entity that dies instantly and spawns some particles
    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return true;
    }

    @Override
    public Entity createEntity(World world, Entity location, ItemStack itemstack) {
        if (ConfigHandler.destabilisedGravityDustDissipatesWhenDropped && location instanceof EntityItem) {
            DissipationEntity dissipationEntity = new DissipationEntity(world);
            dissipationEntity.motionX = location.motionX;
            dissipationEntity.motionY = location.motionY;
            dissipationEntity.motionZ = location.motionZ;
            dissipationEntity.posX = location.posX;
            dissipationEntity.posY = location.posY;
            dissipationEntity.posZ = location.posZ;
            dissipationEntity.prevPosX = location.prevPosX;
            dissipationEntity.prevPosY = location.prevPosY;
            dissipationEntity.prevPosZ = location.prevPosZ;
            dissipationEntity.originalStackSize = ((EntityItem)location).getEntityItem().getCount();

            return dissipationEntity;
        }
        else {
            return null;
        }
    }

    @Override
    public String getModObjectName() {
        return "destabilisedgravitydust";
    }

    @Override
    public void postInit() {
        GameRegistry.addSmelting(StaticItems.GRAVITY_DUST, new ItemStack(this), 0f);
    }

    public static class DissipationEntity extends Entity implements IEntityAdditionalSpawnData {

        public static final String SIZE_KEY = "size";
        private int originalStackSize = 1;

        @UsedReflexively
        public DissipationEntity(World worldIn) {
            super(worldIn);
        }

        @Override
        public void onUpdate() {
            World worldObj = this.world;
            if (worldObj.isRemote) {
                // Spawn some particles

                // Based on EntityEnderEye
                double posX = this.posX;
                double posY = this.posY;
                double posZ = this.posZ;
                double motionX = this.motionX;
                double motionY = this.motionY;
                double motionZ = this.motionZ;
                Random rand = this.rand;
                int particleSetting = Minecraft.getMinecraft().gameSettings.particleSetting;
                int particleCount;
                switch (particleSetting) {//0 = All, 1 = Decreased, 2 = Minimal
                    case 0:
                        particleCount = Math.min(5 * this.originalStackSize, 64);
                        break;
                    case 1:
                        particleCount = Math.min(5 * this.originalStackSize, 32);
                        break;
                    default://case 2:
                        particleCount = 0;
                        break;
                }
                int itemID = Item.getIdFromItem(StaticItems.DESTABILISED_GRAVITY_DUST);
                // All non-static final access is done outside of the loop
                for (int i = 0; i < particleCount; i++) {
                    worldObj.spawnParticle(
                            EnumParticleTypes.ITEM_CRACK,
                            posX, posY, posZ,
                            motionX + rand.nextGaussian() * 0.1D,
                            motionY + rand.nextDouble() * 0.15D,
                            motionZ + rand.nextGaussian() * 0.1D,
                            itemID);
                }
            }
            this.setDead();
        }

        public void setOriginalStackSize(int originalStackSize) {
            this.originalStackSize = originalStackSize;
        }

        @Override
        public void writeSpawnData(ByteBuf buffer) {
            buffer.writeInt(this.originalStackSize);
        }

        @Override
        public void readSpawnData(ByteBuf additionalData) {
            this.originalStackSize = additionalData.readInt();
        }

        @Override
        protected void entityInit() {/**/}

        @Override
        protected void readEntityFromNBT(NBTTagCompound compound) {
            if (compound.hasKey(SIZE_KEY, Constants.NBT.TAG_INT)) {
                this.originalStackSize = compound.getInteger(SIZE_KEY);
            }
        }

        @Override
        protected void writeEntityToNBT(NBTTagCompound compound) {
            compound.setInteger(SIZE_KEY, this.originalStackSize);
        }

        public static class Wrapper implements IGravityModEntityClassWrapper<DissipationEntity> {

            @Override
            public Class<DissipationEntity> getEntityClass() {
                return DissipationEntity.class;
            }

            @Override
            public String getModObjectName() {
                return "antimassdissipation";
            }

            @Override
            public int getTrackingRange() {
                return 20;
            }

            @Override
            public int getUpdateFrequency() {
                return 20;
            }

            @Override
            public boolean sendsVelocityUpdates() {
                // Needs to be true so that motion field values are sent to clients
                return true;
            }

            @SideOnly(Side.CLIENT)
            @Override
            public IRenderFactory<? super DissipationEntity> getRenderFactory() {
                return RenderNothing::new;
            }


        }
    }
}

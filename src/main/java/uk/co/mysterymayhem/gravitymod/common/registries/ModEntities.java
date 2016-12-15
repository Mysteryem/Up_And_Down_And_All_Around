package uk.co.mysterymayhem.gravitymod.common.registries;

import net.minecraft.entity.Entity;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import uk.co.mysterymayhem.gravitymod.GravityMod;
import uk.co.mysterymayhem.gravitymod.common.entities.EntityFloatingItem;
import uk.co.mysterymayhem.gravitymod.common.entities.EntityGravityItem;
import uk.co.mysterymayhem.gravitymod.common.registries.ModEntities.ModEntityClassWrapper;

import java.util.ArrayList;

/**
 * Created by Mysteryem on 2016-12-07.
 */
public class ModEntities extends ModObjectRegistry<ModEntityClassWrapper<?>, ArrayList<ModEntityClassWrapper<?>>> {
    static EntityGravityItem.Wrapper wrapperEntityGravityItem;
    static EntityFloatingItem.Wrapper wrapperEntityFloatingItem;

//    public static final ArrayList<ModEntitySingletonWrapper<?>> allEntityWrappers = new ArrayList<>();

    public ModEntities() {
        super(new ArrayList<>());
    }

    @Override
    public void preInit() {
        registerWrappers();
        super.preInit();
    }

    private void registerWrappers() {
        ArrayList<ModEntityClassWrapper<?>> modObjects = this.getModObjects();
        modObjects.add(wrapperEntityFloatingItem = new EntityFloatingItem.Wrapper());
        modObjects.add(wrapperEntityGravityItem = new EntityGravityItem.Wrapper());
    }

    /**
     * Doesn't need to be an interface, unlike IModItem/IModBlock, since
     * Created by Mysteryem on 2016-12-08.
     */
    public abstract static class ModEntityClassWrapper<T extends Entity> implements IModObject<Class<T>> {
        public abstract Class<T> getEntityClass();

        /**
         * See {@link net.minecraft.entity.EntityTracker#trackEntity(Entity)} for vanilla entities
         *
         * Default for living mobs: 80.
         *
         * @return
         */
        public abstract int getTrackingRange();

        /**
         * See {@link net.minecraft.entity.EntityTracker#trackEntity(Entity)} for vanilla entities
         *
         * Default for living mobs: 3.
         *
         * @return
         */
        public abstract int getUpdateFrequency();

        /**
         * See {@link net.minecraft.entity.EntityTracker#trackEntity(Entity)} for vanilla entities
         *
         * Default for living mobs: true.
         *
         * @return
         */
        public abstract boolean sendsVelocityUpdates();


        @Override
        public void preInit() {
            EntityRegistry.registerModEntity(
                    this.getEntityClass(),
                    this.getName(),
                    GravityMod.getNextEntityID(),
                    GravityMod.INSTANCE,
                    this.getTrackingRange(),
                    this.getUpdateFrequency(),
                    this.sendsVelocityUpdates());
        }

        @Override
        @SideOnly(Side.CLIENT)
        public void preInitClient() {
            IRenderFactory<? super T> renderFactory = this.getRenderFactory();
            if (renderFactory != null) {
                RenderingRegistry.registerEntityRenderingHandler(this.getEntityClass(), renderFactory);
            }
        }

        @SideOnly(Side.CLIENT)
        public IRenderFactory<? super T> getRenderFactory() {
            return null;
        }

        @Override
        public Class<T> getCast() {
            return this.getEntityClass();
        }
    }
}

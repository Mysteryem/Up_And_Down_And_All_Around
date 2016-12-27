package uk.co.mysterymayhem.mystlib.setup.singletons;

import net.minecraft.entity.Entity;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Doesn't need to be an interface, unlike IModItem/IModBlock, since
 * Created by Mysteryem on 2016-12-08.
 */
public interface IModEntityClassWrapper<T extends Entity> extends IModObject<Class<T>> {
    Class<T> getEntityClass();

    /**
     * See {@link net.minecraft.entity.EntityTracker#trackEntity(Entity)} for vanilla entities
     *
     * Default for living mobs: 80.
     *
     * @return
     */
    int getTrackingRange();

    /**
     * See {@link net.minecraft.entity.EntityTracker#trackEntity(Entity)} for vanilla entities
     *
     * Default for living mobs: 3.
     *
     * @return
     */
    int getUpdateFrequency();

    /**
     * See {@link net.minecraft.entity.EntityTracker#trackEntity(Entity)} for vanilla entities
     *
     * Default for living mobs: true.
     *
     * @return
     */
    boolean sendsVelocityUpdates();

    Object getModInstance();
    int getUniqueID();

    @Override
    default void preInit() {
        EntityRegistry.registerModEntity(
                this.getEntityClass(),
                this.getName(),
                this.getUniqueID(),
                this.getModInstance(),
                this.getTrackingRange(),
                this.getUpdateFrequency(),
                this.sendsVelocityUpdates());
    }

    @Override
    @SideOnly(Side.CLIENT)
    default void preInitClient() {
        IRenderFactory<? super T> renderFactory = this.getRenderFactory();
        if (renderFactory != null) {
            RenderingRegistry.registerEntityRenderingHandler(this.getEntityClass(), renderFactory);
        }
    }

    @SideOnly(Side.CLIENT)
    default IRenderFactory<? super T> getRenderFactory() {
        return null;
    }

    @Override
    default Class<T> getCast() {
        return this.getEntityClass();
    }
}

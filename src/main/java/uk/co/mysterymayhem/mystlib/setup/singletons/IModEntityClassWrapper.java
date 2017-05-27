package uk.co.mysterymayhem.mystlib.setup.singletons;

import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Doesn't need to be an interface, unlike IModItem/IModBlock, since
 * Created by Mysteryem on 2016-12-08.
 */
public interface IModEntityClassWrapper<T extends Entity> extends IModObject {
    @Override
    default void preInit() {
        EntityRegistry.registerModEntity(
                new ResourceLocation(this.getModID(), this.getModObjectName()),
                this.getEntityClass(),
                this.getModObjectName(),
                this.getUniqueID(),
                this.getModInstance(),
                this.getTrackingRange(),
                this.getUpdateFrequency(),
                this.sendsVelocityUpdates());
        IModObject.super.preInit();
    }

    Class<T> getEntityClass();

    int getUniqueID();

    /**
     * See {@link net.minecraft.entity.EntityTracker#track(Entity)} for vanilla entities
     * <p>
     * Default for living mobs: 80.
     *
     * @return
     */
    int getTrackingRange();

    /**
     * See {@link net.minecraft.entity.EntityTracker#track(Entity)} for vanilla entities
     * <p>
     * Default for living mobs: 3.
     *
     * @return
     */
    int getUpdateFrequency();

    /**
     * See {@link net.minecraft.entity.EntityTracker#track(Entity)} for vanilla entities
     * <p>
     * Default for living mobs: true.
     *
     * @return
     */
    boolean sendsVelocityUpdates();

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
}

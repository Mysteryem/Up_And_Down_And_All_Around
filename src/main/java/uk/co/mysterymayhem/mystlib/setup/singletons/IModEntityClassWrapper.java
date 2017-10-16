package uk.co.mysterymayhem.mystlib.setup.singletons;

import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

/**
 * Doesn't need to be an interface, unlike IModItem/IModBlock, since
 * Created by Mysteryem on 2016-12-08.
 */
public interface IModEntityClassWrapper<T extends Entity> extends IModObject, IModRegistryEntry<EntityEntry> {

    @Override
    default void register(IForgeRegistry<EntityEntry> registry) {
        // Technically EntityRegistry.registerModEntity handles the actual registration if it's not already registered, but maybe that will change in the future
        String name = this.getModObjectName();
        ResourceLocation resourceLocation = new ResourceLocation(this.getModID(), name);
        EntityEntry entityEntry = new EntityEntry(this.getEntityClass(), name).setRegistryName(resourceLocation);
        registry.register(entityEntry);

        EntityRegistry.registerModEntity(
                resourceLocation,
                this.getEntityClass(),
                name,
                this.getUniqueID(),
                this.getModInstance(),
                this.getTrackingRange(),
                this.getUpdateFrequency(),
                this.sendsVelocityUpdates());
    }

    @SideOnly(Side.CLIENT)
    @Override
    default void registerClient(IForgeRegistry<EntityEntry> registry) {
        IRenderFactory<? super T> renderFactory = this.getRenderFactory();
        if (renderFactory != null) {
            RenderingRegistry.registerEntityRenderingHandler(this.getEntityClass(), renderFactory);
        }
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

    @SideOnly(Side.CLIENT)
    default IRenderFactory<? super T> getRenderFactory() {
        return null;
    }
}

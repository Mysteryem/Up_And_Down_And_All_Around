package uk.co.mysterymayhem.gravitymod.common.registries;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import uk.co.mysterymayhem.gravitymod.IFMLStaged;

import java.util.Collection;

/**
 * Created by Mysteryem on 2016-12-14.
 */
public abstract class ModObjectRegistry<T extends IModObject<?>, U extends Collection<T>> implements IFMLStaged {
    private final U modObjects;

    public ModObjectRegistry(U modObjects) {
        this.modObjects = modObjects;
    }

    public U getModObjects() {
        return this.modObjects;
    }

    @Override
    public void preInit() {
        this.modObjects.forEach(IModObject::preInit);
    }

    @Override
    public void init() {
        this.modObjects.forEach(IModObject::init);
    }

    @Override
    public void postInit() {
        this.modObjects.forEach(IModObject::postInit);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void initClient() {
        this.modObjects.forEach(IModObject::initClient);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void preInitClient() {
        this.modObjects.forEach(IModObject::preInitClient);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void postInitClient() {
        this.modObjects.forEach(IModObject::postInitClient);
    }
}

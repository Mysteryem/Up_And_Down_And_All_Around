package uk.co.mysterymayhem.gravitymod.common.blocks.gravitygenerator;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import uk.co.mysterymayhem.gravitymod.GravityMod;
import uk.co.mysterymayhem.gravitymod.client.blocks.gravitygenerator.GUIContainerGravityGenerator;
import uk.co.mysterymayhem.gravitymod.common.registries.IGravityModCommon;
import uk.co.mysterymayhem.gravitymod.common.registries.StaticTileEntities;
import uk.co.mysterymayhem.mystlib.setup.singletons.AbstractGuiProvider;

/**
 * Created by Mysteryem on 2017-01-08.
 */
public class GUIProviderGravityGenerator extends AbstractGuiProvider<ContainerGravityGenerator> implements IGravityModCommon {

    @Override
    public ContainerGravityGenerator getServerSide(EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));

        Class<TileGravityGenerator> tileEntityClass = StaticTileEntities.GRAVITY_GENERATOR.getTileEntityClass();
        if (tileEntityClass.isInstance(tileEntity)) {
            TileGravityGenerator cast = tileEntityClass.cast(tileEntity);
            return new ContainerGravityGenerator(player, cast);
        }

        GravityMod.logWarning("Expected to get a %s at (%s, %s, %s), but got a %s (%s)",
                tileEntityClass, x, y, z, tileEntity, tileEntity == null ? "null" : tileEntity.getClass());
        return null;
    }

    @Override
    public Object getClientSide(EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));

        Class<TileGravityGenerator> tileEntityClass = StaticTileEntities.GRAVITY_GENERATOR.getTileEntityClass();
        if (tileEntityClass.isInstance(tileEntity)) {
            TileGravityGenerator cast = tileEntityClass.cast(tileEntity);
            return new GUIContainerGravityGenerator(new ContainerGravityGenerator(player, cast));
        }
        GravityMod.logWarning("Expected to get a %s at (%s, %s, %s), but got a %s (%s)",
                tileEntityClass, x, y, z, tileEntity, tileEntity == null ? "null" : tileEntity.getClass());
        return null;
    }
}

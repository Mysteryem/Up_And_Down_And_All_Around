package uk.co.mysterymayhem.mystlib.setup.singletons;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Function that returns a Container on the server side or a GUIScreen on the client side
 * <p>
 * GUIScreen is client only so it can't be included in the interface' generics.
 * Created by Mysteryem on 2017-01-03.
 */
public interface IModGUIProvider<S extends Container> extends IModObject {
    S getServerSide(EntityPlayer player, World world, int x, int y, int z);

    /**
     * Note that this must return a GUIScreen (or null) otherwise a ClassCastException will occur.
     * <p>
     * As the method in IGUIHandler that this is based off has not been set to SideOnly.CLIENT, I have opted to do the same.
     *
     * @return
     */
    Object getClientSide(EntityPlayer player, World world, int x, int y, int z);

    @Override
    default String getName() {
        return "unused";
    }

    default void openGUI(EntityPlayer player, World world, BlockPos pos) {
        this.openGUI(player, world, pos.getX(), pos.getY(), pos.getZ());
    }

    default void openGUI(EntityPlayer player, World world, int x, int y, int z) {
        player.openGui(this.getModInstance(), this.getModGUIID(), world, x, y, z);
    }

    int getModGUIID();

    void setModGUIID(int id);
}

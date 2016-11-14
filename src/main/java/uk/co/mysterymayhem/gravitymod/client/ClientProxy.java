package uk.co.mysterymayhem.gravitymod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import uk.co.mysterymayhem.gravitymod.client.listeners.GravityManagerClient;
import uk.co.mysterymayhem.gravitymod.client.listeners.ItemTooltipListener;
import uk.co.mysterymayhem.gravitymod.client.listeners.PlayerCameraListener;
import uk.co.mysterymayhem.gravitymod.client.listeners.PlayerRenderListener;
import uk.co.mysterymayhem.gravitymod.client.renderers.RenderGravityEntityItem;
import uk.co.mysterymayhem.gravitymod.common.CommonProxy;
import uk.co.mysterymayhem.gravitymod.common.entities.EntityFloatingItem;
import uk.co.mysterymayhem.gravitymod.common.entities.EntityGravityItem;
import uk.co.mysterymayhem.gravitymod.common.items.shared.IModItem;

/**
 * Created by Mysteryem on 2016-08-04.
 */
@SuppressWarnings("unused")
@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    @Override
    public void preInit() {
        RenderingRegistry.registerEntityRenderingHandler(
                EntityFloatingItem.class, manager -> new RenderEntityItem(manager, Minecraft.getMinecraft().getRenderItem()));
        RenderingRegistry.registerEntityRenderingHandler(
                EntityGravityItem.class, manager -> new RenderGravityEntityItem(manager, Minecraft.getMinecraft().getRenderItem()));
        super.preInit();
    }

    @Override
    public void registerListeners() {
        super.registerListeners();
        MinecraftForge.EVENT_BUS.register(new PlayerCameraListener());
        MinecraftForge.EVENT_BUS.register(new PlayerRenderListener());
        MinecraftForge.EVENT_BUS.register(new ItemTooltipListener());
    }

    @Override
    public void registerGravityManager() {
        this.gravityManagerCommon = new GravityManagerClient();
    }

    @Override
    public <T extends Item & IModItem> T preInitItem(T item) {
        super.preInitItem(item);
        item.preInitModel();
        return item;
    }
}

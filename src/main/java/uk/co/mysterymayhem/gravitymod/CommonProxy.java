package uk.co.mysterymayhem.gravitymod;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import uk.co.mysterymayhem.gravitymod.asm.Hooks;
import uk.co.mysterymayhem.gravitymod.common.capabilities.gravitydirection.GravityDirectionCapability;
import uk.co.mysterymayhem.gravitymod.common.entities.EntityFloatingItem;
import uk.co.mysterymayhem.gravitymod.common.items.materials.ItemGravityDust;
import uk.co.mysterymayhem.gravitymod.common.listeners.GravityManagerCommon;
import uk.co.mysterymayhem.gravitymod.common.listeners.ItemStackUseListener;
import uk.co.mysterymayhem.gravitymod.common.packets.PacketHandler;
import uk.co.mysterymayhem.gravitymod.common.registries.ModBlocks;
import uk.co.mysterymayhem.gravitymod.common.registries.ModEntities;
import uk.co.mysterymayhem.gravitymod.common.registries.ModItems;
import uk.co.mysterymayhem.gravitymod.common.registries.ModTileEntities;
import uk.co.mysterymayhem.mystlib.setup.IFMLStaged;
import uk.co.mysterymayhem.mystlib.setup.registries.AbstractIFMLStagedRegistry;

import java.util.ArrayList;

/**
 * Created by Mysteryem on 2016-08-04.
 */
public class CommonProxy extends AbstractIFMLStagedRegistry<IFMLStaged, ArrayList<IFMLStaged>> {

    public GravityManagerCommon gravityManagerCommon;

    public CommonProxy() {
        super(new ArrayList<>());
    }

    @Override
    protected void addToCollection(ArrayList<IFMLStaged> modObjects) {
        modObjects.add(new ModItems());
        modObjects.add(new ModBlocks());
        modObjects.add(new ModEntities());
        modObjects.add(new ModTileEntities());
    }

    @Override
    public void preInit() {
        GravityDirectionCapability.registerCapability();
        this.registerGravityManager();
        PacketHandler.registerMessages();
        super.preInit();
    }

    @Override
    public void init() {
        super.init();
        this.registerListeners();
    }

    public void registerGravityManager() {
        this.gravityManagerCommon = new GravityManagerCommon();
    }

    public void registerListeners() {
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(this.getGravityManager());
        MinecraftForge.EVENT_BUS.register(new ItemStackUseListener());
        MinecraftForge.EVENT_BUS.register(new ItemGravityDust.BlockBreakListener());
        MinecraftForge.EVENT_BUS.register(EntityFloatingItem.class);
//        MinecraftForge.EVENT_BUS.register(new DebugHelperListener());
    }

    public GravityManagerCommon getGravityManager() {
        return this.gravityManagerCommon;
    }


    // Events that try to allow other mods using these events to modify the player's motion as if they have currently
    // have downwards gravity

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public void onRightClickBlockHighest(PlayerInteractEvent.RightClickBlock event) {
        Hooks.makeMotionAbsolute(event.getEntityPlayer());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public void onRightClickBlockLowest(PlayerInteractEvent.RightClickBlock event) {
        Hooks.popMotionStack(event.getEntityPlayer());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public void onRightClickItemHighest(PlayerInteractEvent.RightClickItem event) {
        Hooks.makeMotionAbsolute(event.getEntityPlayer());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public void onRightClickItemLowest(PlayerInteractEvent.RightClickItem event) {
        Hooks.popMotionStack(event.getEntityPlayer());
    }
}

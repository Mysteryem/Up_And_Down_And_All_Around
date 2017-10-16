package uk.co.mysterymayhem.gravitymod;

import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionType;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import uk.co.mysterymayhem.gravitymod.common.capabilities.gravitydirection.GravityDirectionCapability;
import uk.co.mysterymayhem.gravitymod.common.config.ConfigHandler;
import uk.co.mysterymayhem.gravitymod.common.entities.EntityFloatingItem;
import uk.co.mysterymayhem.gravitymod.common.items.materials.ItemGravityDust;
import uk.co.mysterymayhem.gravitymod.common.listeners.FallOutOfWorldUpwardsListenerCommon;
import uk.co.mysterymayhem.gravitymod.common.listeners.GravityManagerCommon;
import uk.co.mysterymayhem.gravitymod.common.listeners.ItemStackUseListener;
import uk.co.mysterymayhem.gravitymod.common.listeners.LootTableListener;
import uk.co.mysterymayhem.gravitymod.common.packets.PacketHandler;
import uk.co.mysterymayhem.gravitymod.common.registries.*;
import uk.co.mysterymayhem.gravitymod.common.world.generation.ore.Generator;
import uk.co.mysterymayhem.mystlib.setup.registries.AbstractIFMLStagedRegistry;
import uk.co.mysterymayhem.mystlib.setup.registries.AbstractModObjectRegistry;
import uk.co.mysterymayhem.mystlib.setup.registries.AbstractRegistrableModObjectRegistry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * Created by Mysteryem on 2016-08-04.
 */
public class CommonProxy extends AbstractIFMLStagedRegistry<AbstractModObjectRegistry<?,?>, ArrayList<AbstractModObjectRegistry<?,?>>> {

    public GravityManagerCommon gravityManagerCommon;

    public CommonProxy() {
        super(new ArrayList<>());
    }

    @Override
    public void preInit() {
        MinecraftForge.EVENT_BUS.register(this);
        GravityDirectionCapability.registerCapability();
        this.registerGravityManager();
        PacketHandler.registerMessages();
        super.preInit();
    }

    public void registerGravityManager() {
        this.gravityManagerCommon = new GravityManagerCommon();
    }

    @Override
    public void init() {
        super.init();
        this.registerListeners();
        GameRegistry.registerWorldGenerator(Generator.INSTANCE, 0);
    }

    public void registerListeners() {
        MinecraftForge.EVENT_BUS.register(this.getGravityManager());
        MinecraftForge.EVENT_BUS.register(ItemStackUseListener.class);
        MinecraftForge.EVENT_BUS.register(ItemGravityDust.BlockBreakListener.class);
        MinecraftForge.EVENT_BUS.register(EntityFloatingItem.class);
        MinecraftForge.EVENT_BUS.register(LootTableListener.class);
        MinecraftForge.EVENT_BUS.register(Generator.class);
        MinecraftForge.EVENT_BUS.register(ConfigHandler.class);
        this.createSidedEventListeners().forEach(MinecraftForge.EVENT_BUS::register);
//        MinecraftForge.EVENT_BUS.register(new DebugHelperListener());
    }

    public GravityManagerCommon getGravityManager() {
        return this.gravityManagerCommon;
    }

    public Collection<?> createSidedEventListeners() {
        return Lists.newArrayList(new FallOutOfWorldUpwardsListenerCommon());
    }

    @Override
    protected void addToCollection(ArrayList<AbstractModObjectRegistry<?,?>> modObjects) {
        modObjects.add(new ModItems());
        modObjects.add(new ModBlocks());
        modObjects.add(new ModEntities());
        modObjects.add(new ModTileEntities());
        modObjects.add(new ModGUIs());
        modObjects.add(new ModPotions());
    }

    @SubscribeEvent
    public void onRegisterBlocks(RegistryEvent.Register<Block> event) {
        this.doRegister(registry -> registry.onRegisterBlocks(event));
    }

    @SubscribeEvent
    public void onRegisterItems(RegistryEvent.Register<Item> event) {
        this.doRegister(registry -> registry.onRegisterItems(event));
    }

    @SubscribeEvent
    public void onRegisterPotions(RegistryEvent.Register<Potion> event) {
        this.doRegister(registry -> registry.onRegisterPotions(event));
    }

    @SubscribeEvent
    public void onRegisterBiomes(RegistryEvent.Register<Biome> event) {
        this.doRegister(registry -> registry.onRegisterBiomes(event));
    }

    @SubscribeEvent
    public void onRegisterSoundEvents(RegistryEvent.Register<SoundEvent> event) {
        this.doRegister(registry -> registry.onRegisterSoundEvents(event));
    }

    @SubscribeEvent
    public void onRegisterPotionTypes(RegistryEvent.Register<PotionType> event) {
        this.doRegister(registry -> registry.onRegisterPotionTypes(event));
    }

    @SubscribeEvent
    public void onRegisterEnchantments(RegistryEvent.Register<Enchantment> event) {
        this.doRegister(registry -> registry.onRegisterEnchantments(event));
    }

    @SubscribeEvent
    public void onRegisterIRecipes(RegistryEvent.Register<IRecipe> event) {
        this.doRegister(registry -> registry.onRegisterIRecipes(event));
    }

    @SubscribeEvent
    public void onRegisterVillagerProfessions(RegistryEvent.Register<VillagerRegistry.VillagerProfession> event) {
        this.doRegister(registry -> registry.onRegisterVillagerProfessions(event));
    }

    @SubscribeEvent
    public void onRegisterEntityEntries(RegistryEvent.Register<EntityEntry> event) {
        this.doRegister(registry -> registry.onRegisterEntityEntries(event));
    }

    protected void doRegister(Consumer<AbstractRegistrableModObjectRegistry<?,?>> consumer) {
        this.getCollection().forEach(registry -> {
            if (registry instanceof AbstractRegistrableModObjectRegistry) {
                consumer.accept((AbstractRegistrableModObjectRegistry<?,?>)registry);
            }
        });
    }
}

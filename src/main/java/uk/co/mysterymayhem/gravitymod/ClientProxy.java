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
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import uk.co.mysterymayhem.gravitymod.client.listeners.*;

import java.util.Collection;

/**
 * Created by Mysteryem on 2016-08-04.
 */
@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    @Override
    public void postInit() {
        super.postInit();
        this.postInitClient();
    }

    @Override
    public void preInit() {
        super.preInit();
        this.preInitClient();
    }

    @Override
    public void init() {
        super.init();
        this.initClient();
    }

    @Override
    public void registerGravityManager() {
        this.gravityManagerCommon = new GravityManagerClient();
    }

    @Override
    public void registerListeners() {
        super.registerListeners();
        MinecraftForge.EVENT_BUS.register(PlayerCameraListener.class);
        MinecraftForge.EVENT_BUS.register(EntityRenderListener.class);
        MinecraftForge.EVENT_BUS.register(ItemTooltipListener.class);
        MinecraftForge.EVENT_BUS.register(FallOutOfWorldUpwardsListenerClient.class);
    }

    @Override
    public Collection<?> createSidedEventListeners() {
        return Lists.newArrayList(new FallOutOfWorldUpwardsListenerClient());
    }

    @Override
    @SubscribeEvent
    public void onRegisterBlocks(RegistryEvent.Register<Block> event) {
        super.onRegisterBlocks(event);
        this.doRegister(registry -> registry.onRegisterBlocksClient(event));
    }

    @Override
    @SubscribeEvent
    public void onRegisterItems(RegistryEvent.Register<Item> event) {
        super.onRegisterItems(event);
        this.doRegister(col -> col.onRegisterItemsClient(event));
    }

    @Override
    @SubscribeEvent
    public void onRegisterPotions(RegistryEvent.Register<Potion> event) {
        super.onRegisterPotions(event);
        this.doRegister(registry -> registry.onRegisterPotionsClient(event));
    }

    @Override
    @SubscribeEvent
    public void onRegisterBiomes(RegistryEvent.Register<Biome> event) {
        super.onRegisterBiomes(event);
        this.doRegister(registry -> registry.onRegisterBiomesClient(event));
    }

    @Override
    @SubscribeEvent
    public void onRegisterSoundEvents(RegistryEvent.Register<SoundEvent> event) {
        super.onRegisterSoundEvents(event);
        this.doRegister(registry -> registry.onRegisterSoundEventsClient(event));
    }

    @Override
    @SubscribeEvent
    public void onRegisterPotionTypes(RegistryEvent.Register<PotionType> event) {
        super.onRegisterPotionTypes(event);
        this.doRegister(registry -> registry.onRegisterPotionTypesClient(event));
    }

    @Override
    @SubscribeEvent
    public void onRegisterEnchantments(RegistryEvent.Register<Enchantment> event) {
        super.onRegisterEnchantments(event);
        this.doRegister(registry -> registry.onRegisterEnchantmentsClient(event));
    }

    @Override
    @SubscribeEvent
    public void onRegisterIRecipes(RegistryEvent.Register<IRecipe> event) {
        super.onRegisterIRecipes(event);
        this.doRegister(registry -> registry.onRegisterIRecipesClient(event));
    }

    @Override
    @SubscribeEvent
    public void onRegisterVillagerProfessions(RegistryEvent.Register<VillagerRegistry.VillagerProfession> event) {
        super.onRegisterVillagerProfessions(event);
        this.doRegister(registry -> registry.onRegisterVillagerProfessionsClient(event));
    }

    @Override
    @SubscribeEvent
    public void onRegisterEntityEntries(RegistryEvent.Register<EntityEntry> event) {
        super.onRegisterEntityEntries(event);
        this.doRegister(registry -> registry.onRegisterEntityEntriesClient(event));
    }
}

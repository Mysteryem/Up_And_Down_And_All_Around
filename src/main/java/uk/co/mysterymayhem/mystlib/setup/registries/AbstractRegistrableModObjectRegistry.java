package uk.co.mysterymayhem.mystlib.setup.registries;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionType;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import uk.co.mysterymayhem.mystlib.setup.singletons.IModObject;
import uk.co.mysterymayhem.mystlib.setup.singletons.IModRegistryEntry;

import java.util.Collection;

/**
 * Created by Mysteryem on 15/10/2017.
 */
public abstract class AbstractRegistrableModObjectRegistry<SINGLETON extends IModObject & IModRegistryEntry<?>, COLLECTION extends Collection<SINGLETON>>
        extends AbstractModObjectRegistry<SINGLETON, COLLECTION> {

    public AbstractRegistrableModObjectRegistry(COLLECTION modObjects) {
        super(modObjects);
    }

    @Override
    public void preInit() {
        // Don't want to create the IModObjects in preInit as registration events are yet to occur
    }

    @SideOnly(Side.CLIENT)
    public void onRegisterBlocksClient(RegistryEvent.Register<Block> event) {
//        this.getCollection().forEach(obj -> obj.registerBlockClient(event.getRegistry()));
    }

    @SideOnly(Side.CLIENT)
    public void onRegisterItemsClient(RegistryEvent.Register<Item> event) {
//        this.getCollection().forEach(obj -> obj.registerItemClient(event.getRegistry()));
    }

    @SideOnly(Side.CLIENT)
    public void onRegisterPotionsClient(RegistryEvent.Register<Potion> event) {
//        this.getCollection().forEach(obj -> obj.registerPotionClient(event.getRegistry()));
    }

    @SideOnly(Side.CLIENT)
    public void onRegisterBiomesClient(RegistryEvent.Register<Biome> event) {
//        this.getCollection().forEach(obj -> obj.registerBiomeClient(event.getRegistry()));
    }

    @SideOnly(Side.CLIENT)
    public void onRegisterSoundEventsClient(RegistryEvent.Register<SoundEvent> event) {
//        this.getCollection().forEach(obj -> obj.registerSoundEventClient(event.getRegistry()));
    }

    @SideOnly(Side.CLIENT)
    public void onRegisterPotionTypesClient(RegistryEvent.Register<PotionType> event) {
//        this.getCollection().forEach(obj -> obj.registerPotionTypeClient(event.getRegistry()));
    }

    @SideOnly(Side.CLIENT)
    public void onRegisterEnchantmentsClient(RegistryEvent.Register<Enchantment> event) {
//        this.getCollection().forEach(obj -> obj.registerEnchantmentClient(event.getRegistry()));
    }

    @SideOnly(Side.CLIENT)
    public void onRegisterIRecipesClient(RegistryEvent.Register<IRecipe> event) {
//        this.getCollection().forEach(obj -> obj.registerIRecipeClient(event.getRegistry()));
    }

    @SideOnly(Side.CLIENT)
    public void onRegisterVillagerProfessionsClient(RegistryEvent.Register<VillagerRegistry.VillagerProfession> event) {
//        this.getCollection().forEach(obj -> obj.registerVillagerProfessionClient(event.getRegistry()));
    }

    @SideOnly(Side.CLIENT)
    public void onRegisterEntityEntriesClient(RegistryEvent.Register<EntityEntry> event) {
//        this.getCollection().forEach(obj -> obj.registerEntityEntryClient(event.getRegistry()));
    }

    public void onRegisterBlocks(RegistryEvent.Register<Block> event) {
//        this.getCollection().forEach(obj -> obj.registerBlock(event.getRegistry()));
    }

    public void onRegisterItems(RegistryEvent.Register<Item> event) {
//        this.getCollection().forEach(obj -> obj.registerItem(event.getRegistry()));
    }

    public void onRegisterPotions(RegistryEvent.Register<Potion> event) {
//        this.getCollection().forEach(obj -> obj.registerPotion(event.getRegistry()));
    }

    public void onRegisterBiomes(RegistryEvent.Register<Biome> event) {
//        this.getCollection().forEach(obj -> obj.registerBiome(event.getRegistry()));
    }

    public void onRegisterSoundEvents(RegistryEvent.Register<SoundEvent> event) {
//        this.getCollection().forEach(obj -> obj.registerSoundEvent(event.getRegistry()));
    }

    public void onRegisterPotionTypes(RegistryEvent.Register<PotionType> event) {
//        this.getCollection().forEach(obj -> obj.registerPotionType(event.getRegistry()));
    }

    public void onRegisterEnchantments(RegistryEvent.Register<Enchantment> event) {
//        this.getCollection().forEach(obj -> obj.registerEnchantment(event.getRegistry()));
    }

    public void onRegisterIRecipes(RegistryEvent.Register<IRecipe> event) {
//        this.getCollection().forEach(obj -> obj.registerIRecipe(event.getRegistry()));
    }

    public void onRegisterVillagerProfessions(RegistryEvent.Register<VillagerRegistry.VillagerProfession> event) {
//        this.getCollection().forEach(obj -> obj.registerVillagerProfession(event.getRegistry()));
    }

    public void onRegisterEntityEntries(RegistryEvent.Register<EntityEntry> event) {
//        this.getCollection().forEach(obj -> obj.registerEntityEntry(event.getRegistry()));
    }
}

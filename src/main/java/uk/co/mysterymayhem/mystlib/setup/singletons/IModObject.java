package uk.co.mysterymayhem.mystlib.setup.singletons;

import net.minecraft.creativetab.CreativeTabs;
import uk.co.mysterymayhem.mystlib.setup.IFMLStaged;

/**
 * Created by Mysteryem on 2016-12-07.
 */
public interface IModObject extends IFMLStaged {

    /**
     * Get a name of this IModObject, often you would ensure this is unique, but it depends on the circumstances
     *
     * @return
     */
    String getModObjectName();
//    default String getName() {
//        return this.getClass().getSimpleName().toLowerCase(Locale.ENGLISH);
//    }

    /**
     * Get the String ID of your mod
     *
     * @return
     */
    String getModID();

    /**
     * Get your mod's instance. Use @Mod.Instance(&lt;modID&gt;) on a field and return that field in this method.
     *
     * @return
     */
    Object getModInstance();

    /**
     * Get the creative tab for this item/block, not really useful for other types of IModObject,
     * but cleaner than having a separate Interface for IModBlock and IModItem to extend.
     *
     * @return
     */
    default CreativeTabs getModCreativeTab() {
        return null;
    }

//    default void registerBlock(IForgeRegistry<Block> registry) {}
//
//    @SideOnly(Side.CLIENT)
//    default void registerBlockClient(IForgeRegistry<Block> registry) {}
//
//    default void registerItem(IForgeRegistry<Item> registry) {}
//
//    @SideOnly(Side.CLIENT)
//    default void registerItemClient(IForgeRegistry<Item> registry) {}
//
//    default void registerPotion(IForgeRegistry<Potion> registry) {}
//
//    @SideOnly(Side.CLIENT)
//    default void registerPotionClient(IForgeRegistry<Potion> registry) {}
//
//    default void registerBiome(IForgeRegistry<Biome> registry) {}
//
//    @SideOnly(Side.CLIENT)
//    default void registerBiomeClient(IForgeRegistry<Biome> registry) {}
//
//    default void registerSoundEvent(IForgeRegistry<SoundEvent> registry) {}
//
//    @SideOnly(Side.CLIENT)
//    default void registerSoundEventClient(IForgeRegistry<SoundEvent> registry) {}
//
//    default void registerPotionType(IForgeRegistry<PotionType> registry) {}
//
//    @SideOnly(Side.CLIENT)
//    default void registerPotionTypeClient(IForgeRegistry<PotionType> registry) {}
//
//    default void registerEnchantment(IForgeRegistry<Enchantment> registry) {}
//
//    @SideOnly(Side.CLIENT)
//    default void registerEnchantmentClient(IForgeRegistry<Enchantment> registry) {}
//
//    default void registerIRecipe(IForgeRegistry<IRecipe> registry) {}
//
//    @SideOnly(Side.CLIENT)
//    default void registerIRecipeClient(IForgeRegistry<IRecipe> registry) {}
//
//    default void registerVillagerProfession(IForgeRegistry<VillagerRegistry.VillagerProfession> registry) {}
//
//    @SideOnly(Side.CLIENT)
//    default void registerVillagerProfessionClient(IForgeRegistry<VillagerRegistry.VillagerProfession> registry) {}
//
//    default void registerEntityEntry(IForgeRegistry<EntityEntry> registry) {}
//
//    @SideOnly(Side.CLIENT)
//    default void registerEntityEntryClient(IForgeRegistry<EntityEntry> registry) {}
}

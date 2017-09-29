package uk.co.mysterymayhem.mystlib;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public final class RecipeCreationWrapper {
	private RecipeCreationWrapper() {

	}

	@Deprecated
	public static void addShapelessRecipe(ResourceLocation name, ResourceLocation group, ItemStack output, Object... input) {
		ForgeRegistries.RECIPES.register(new ShapelessOreRecipe(group, output, input).setRegistryName(name));
	}

	@Deprecated
	public static void addShapedRecipe(ResourceLocation name, ResourceLocation group, ItemStack output, Object... input) {
		ForgeRegistries.RECIPES.register(new ShapedOreRecipe(group, output, input).setRegistryName(name));
	}
}

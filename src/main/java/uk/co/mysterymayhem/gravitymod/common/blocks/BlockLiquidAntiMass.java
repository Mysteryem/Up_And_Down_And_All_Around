package uk.co.mysterymayhem.gravitymod.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import net.minecraftforge.registries.IForgeRegistry;
import uk.co.mysterymayhem.gravitymod.GravityMod;
import uk.co.mysterymayhem.gravitymod.common.liquids.LiquidAntiMass;
import uk.co.mysterymayhem.gravitymod.common.registries.IGravityModCommon;
import uk.co.mysterymayhem.gravitymod.common.registries.StaticItems;
import uk.co.mysterymayhem.mystlib.setup.singletons.IModBlock;

import java.util.Random;

/**
 * Created by Mysteryem on 2017-01-19.
 */
public class BlockLiquidAntiMass extends BlockFluidClassic implements IModBlock<BlockLiquidAntiMass>, IGravityModCommon {

    public BlockLiquidAntiMass() {
        super(LiquidAntiMass.INSTANCE, Material.WATER);
        this.setTickRandomly(false);
    }

    @Override
    public String getModObjectName() {
        return "liquidantimass";
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos neighborPos) {/**/}

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {/**/}

    @Override
    public void postInit() {
        //TODO: Replace with recipe factory that produces recipes that do not return container items
        // Recipe whereby the input bucket (as a part of the lava bucket) becomes the output bucket
        ForgeRegistries.RECIPES.register(
                new ShapelessVoidOneBucket(
                        new ResourceLocation(GravityMod.MOD_ID, "special_shapeless"),
                        StaticItems.LIQUID_ANTI_MASS_BUCKET,
                        Ingredient.fromItem(Items.LAVA_BUCKET), Ingredient.fromItem(StaticItems.RESTABILISED_GRAVITY_DUST)
                ).setRegistryName(GravityMod.MOD_ID, "shapeless_void_one_bucket"));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerClient(IForgeRegistry<Block> registry) {
        ModelLoader.setCustomStateMapper(this, new StateMapperBase() {
            @Override
            protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
                ResourceLocation registryName = BlockLiquidAntiMass.this.getRegistryName();
                return new ModelResourceLocation(registryName.toString());
            }
        });
    }

    @Override
    public void randomTick(World worldIn, BlockPos pos, IBlockState state, Random random) {/**/}

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {/**/}

    private static class ShapelessVoidOneBucket extends ShapelessOreRecipe {

        public ShapelessVoidOneBucket(ResourceLocation resourceLocation, ItemStack output, Object... inputs) {
            super(resourceLocation, output, inputs);
        }

        @Override
        public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
            NonNullList<ItemStack> remainingItems = super.getRemainingItems(inv);
            for (int i = 0; i < remainingItems.size(); i++) {
                ItemStack remainingItem = remainingItems.get(i);
                if (!remainingItem.isEmpty() && remainingItem.getItem() == Items.BUCKET) {
                    remainingItems.set(i, ItemStack.EMPTY);
                    break;
                }
            }
            return remainingItems;
        }
    }
}

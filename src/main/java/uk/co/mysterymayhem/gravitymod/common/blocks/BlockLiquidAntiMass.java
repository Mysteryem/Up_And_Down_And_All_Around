package uk.co.mysterymayhem.gravitymod.common.blocks;

import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.RecipeSorter;
import uk.co.mysterymayhem.gravitymod.GravityMod;
import uk.co.mysterymayhem.gravitymod.common.liquids.LiquidAntiMass;
import uk.co.mysterymayhem.gravitymod.common.registries.IGravityModBlock;
import uk.co.mysterymayhem.gravitymod.common.registries.StaticItems;

import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Created by Mysteryem on 2017-01-19.
 */
public class BlockLiquidAntiMass extends BlockFluidClassic implements IGravityModBlock<BlockLiquidAntiMass> {

    public BlockLiquidAntiMass() {
        super(LiquidAntiMass.INSTANCE, Material.WATER);
        this.setTickRandomly(false);
    }

    @Override
    public String getName() {
        return "liquidantimass";
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighborBlock) {/**/}

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {/**/}

    @Override
    public void postInit() {
        // Recipe whereby the input bucket (as a part of the lava bucket) becomes the output bucket
        RecipeSorter.register(GravityMod.MOD_ID + ":" + ShapelessVoidOneBucket.class.getSimpleName().toLowerCase(Locale.ENGLISH), ShapelessVoidOneBucket.class, RecipeSorter.Category.SHAPELESS, "after:minecraft:shapeless");
        GameRegistry.addRecipe(new ShapelessVoidOneBucket(
                StaticItems.LIQUID_ANTI_MASS_BUCKET,
                Lists.newArrayList(new ItemStack(Items.LAVA_BUCKET), new ItemStack(Items.LAVA_BUCKET), new ItemStack(Items.LAVA_BUCKET),
                        new ItemStack(Items.LAVA_BUCKET), new ItemStack(StaticItems.RESTABILISED_GRAVITY_DUST))));
        // Recipe whereby an extra bucket is used in the input, which will become a returned bucket
        GameRegistry.addRecipe(new ShapelessRecipes(
                StaticItems.LIQUID_ANTI_MASS_BUCKET,
                Lists.newArrayList(new ItemStack(Items.LAVA_BUCKET), new ItemStack(Items.LAVA_BUCKET), new ItemStack(Items.LAVA_BUCKET),
                        new ItemStack(Items.LAVA_BUCKET), new ItemStack(Items.BUCKET), new ItemStack(StaticItems.RESTABILISED_GRAVITY_DUST))));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void preInitClient() {
        IGravityModBlock.super.preInitClient();
        ModelLoader.setCustomStateMapper(this, new StateMapperBase() {
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

    private static class ShapelessVoidOneBucket extends ShapelessRecipes {

        public ShapelessVoidOneBucket(ItemStack output, List<ItemStack> inputList) {
            super(output, inputList);
        }

        @Override
        public ItemStack[] getRemainingItems(InventoryCrafting inv) {
            ItemStack[] remainingItems = super.getRemainingItems(inv);
            for (int i = 0; i < remainingItems.length; i++) {
                ItemStack remainingItem = remainingItems[i];
                if (remainingItem != null && remainingItem.getItem() == Items.BUCKET) {
                    remainingItems[i] = null;
                    break;
                }
            }
            return remainingItems;
        }
    }
}

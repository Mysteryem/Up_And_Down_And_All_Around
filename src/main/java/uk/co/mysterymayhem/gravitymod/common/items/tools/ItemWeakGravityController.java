package uk.co.mysterymayhem.gravitymod.common.items.tools;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import uk.co.mysterymayhem.gravitymod.client.listeners.ItemTooltipListener;
import uk.co.mysterymayhem.gravitymod.common.listeners.GravityManagerCommon;
import uk.co.mysterymayhem.gravitymod.common.registries.GravityPriorityRegistry;
import uk.co.mysterymayhem.gravitymod.common.registries.StaticItems;

import java.util.List;

/**
 * Created by Mysteryem on 2016-11-11.
 */
public class ItemWeakGravityController extends ItemAbstractGravityController {
    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("mouseovertext.mysttmtgravitymod.weakgravitycontroller"));
        ItemTooltipListener.addWeakGravityTooltip(tooltip, playerIn);
        super.addInformation(stack, playerIn, tooltip, advanced);
    }

    @Override
    public boolean affectsPlayer(EntityPlayerMP player) {
        return GravityManagerCommon.playerIsAffectedByWeakGravity(player);
    }

    @Override
    public String getName() {
        return "weakgravitycontroller";
    }

    @Override
    public int getPriority(EntityPlayerMP target) {
        return GravityPriorityRegistry.WEAK_GRAVITY_CONTROLLER;
    }

    @Override
    public void postInit() {
        // Registers one recipe for 'off' visible direction, in-game, only the DOWN_OFF state is craftable, but this
        // will allow JEI to show recipes for each 'off' visible direction
        for (EnumControllerVisibleState visibleState : EnumControllerVisibleState.values()) {
            if (visibleState.isOffState()) {
                int outputMeta = getCombinedMetaFor(EnumControllerActiveDirection.NONE, visibleState);
                GameRegistry.addRecipe(new ShapedOreRecipe(
                        new ItemStack(this, 1, outputMeta),
                        "RLR",
                        "LAL",
                        "RLR",
                        'R', StaticItems.RESTABILISED_GRAVITY_DUST,
                        'L', Blocks.LEVER,
                        'A', new ItemStack(StaticItems.GRAVITY_ANCHOR, 1, OreDictionary.WILDCARD_VALUE)));
            }
        }
    }
}

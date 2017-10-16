package uk.co.mysterymayhem.gravitymod.common.items.tools;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import uk.co.mysterymayhem.gravitymod.GravityMod;
import uk.co.mysterymayhem.gravitymod.common.listeners.GravityManagerCommon;
import uk.co.mysterymayhem.gravitymod.common.registries.GravityPriorityRegistry;

import java.util.List;

/**
 * Created by Mysteryem on 2016-11-12.
 */
public class ItemUltimateGravityController extends ItemAbstractGravityController {
    private static final String NAME = "ultimategravitycontroller";

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag advanced) {
        tooltip.add(I18n.format("mouseovertext.mysttmtgravitymod.ultimategravitycontroller"));
        super.addInformation(stack, world, tooltip, advanced);
    }

    @Override
    public boolean affectsPlayer(EntityPlayerMP player) {
        return GravityManagerCommon.playerIsAffectedByStrongGravity(player);
    }

    @Override
    public String getModObjectName() {
        return NAME;
    }

    @Override
    public int getPriority(EntityPlayerMP target) {
        return GravityPriorityRegistry.ULTIMATE_GRAVITY_CONTROLLER;
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return stack.isItemEnchanted() ? EnumRarity.RARE : GravityMod.RARITY_STRONG;
    }

    // Enchantment glow
    @SideOnly(Side.CLIENT)
    @Override
    public boolean hasEffect(ItemStack stack) {
        return true;
    }
}

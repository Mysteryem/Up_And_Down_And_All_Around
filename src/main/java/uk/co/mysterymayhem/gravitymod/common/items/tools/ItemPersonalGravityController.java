package uk.co.mysterymayhem.gravitymod.common.items.tools;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import uk.co.mysterymayhem.gravitymod.GravityMod;
import uk.co.mysterymayhem.gravitymod.client.listeners.ItemTooltipListener;
import uk.co.mysterymayhem.gravitymod.common.listeners.GravityManagerCommon;
import uk.co.mysterymayhem.gravitymod.common.registries.GravityPriorityRegistry;

import java.util.List;

/**
 * Created by Mysteryem on 2016-11-12.
 */
public class ItemPersonalGravityController extends ItemAbstractGravityController {

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag advanced) {
        tooltip.add(I18n.format("mouseovertext.mysttmtgravitymod.personalgravitycontroller"));
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if (player != null) {
            ItemTooltipListener.addNormalGravityTooltip(tooltip, player);
        }
        super.addInformation(stack, world, tooltip, advanced);
    }

    @Override
    public boolean affectsPlayer(EntityPlayerMP player) {
        return GravityManagerCommon.playerIsAffectedByNormalGravity(player);
    }

    @Override
    public String getModObjectName() {
        return "personalgravitycontroller";
    }

    @Override
    public int getPriority(EntityPlayerMP target) {
        return GravityPriorityRegistry.PERSONAL_GRAVITY_CONTROLLER;
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return stack.isItemEnchanted() ? EnumRarity.RARE : GravityMod.RARITY_NORMAL;
    }
}

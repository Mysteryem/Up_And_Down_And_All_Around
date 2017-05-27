package uk.co.mysterymayhem.gravitymod.client.listeners;

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import uk.co.mysterymayhem.gravitymod.common.config.ConfigHandler;
import uk.co.mysterymayhem.gravitymod.common.listeners.FallOutOfWorldUpwardsListenerCommon;
import uk.co.mysterymayhem.gravitymod.common.registries.StaticPotions;

import java.util.WeakHashMap;

/**
 * Created by Mysteryem on 12/03/2017.
 */
@SideOnly(Side.CLIENT)
public class FallOutOfWorldUpwardsListenerClient extends FallOutOfWorldUpwardsListenerCommon {

    private static final WeakHashMap<EntityPlayer, Integer> clientMap = new WeakHashMap<>();
    private static final WeakHashMap<Entity, int[]> freezeTicks = new WeakHashMap<>();
    private static final ResourceLocation ICE_TEXTURE_LOCATION = new ResourceLocation("textures/blocks/ice.png");
    private static final ResourceLocation PACKED_ICE_TEXTURE_LOCATION = new ResourceLocation("textures/blocks/ice_packed.png");

    @Override
    protected void processSidedPlayer(EntityPlayer player) {
        if (player.world.isRemote) {
            this.processClientPlayer(player);
        }
        else {
            this.processServerPlayer(player);
        }
    }

    private void processClientPlayer(EntityPlayer player) {
        if (player.isPotionActive(StaticPotions.FREEZING)) {
            this.incrementFreezeCounter(player);
        }
        else {
            this.decrementFreezeCounter(player);
        }
    }

    @SubscribeEvent
    public void onRenderOverlayPre(RenderGameOverlayEvent.Post event) {
        Minecraft minecraft;
        Entity renderViewEntity;
        switch (event.getType()) {
            case AIR:
                minecraft = Minecraft.getMinecraft();
                renderViewEntity = minecraft.getRenderViewEntity();
                // Unlikely, though possible for a mod to post this event
                if (minecraft.playerController.shouldDrawHUD() && renderViewEntity instanceof EntityPlayer) {
                    EntityPlayer thePlayer = (EntityPlayer)renderViewEntity;
                    Integer specialAir = this.getSpecialAir(thePlayer);
                    if (specialAir != null) {
                        int air = Math.min(specialAir, thePlayer.getAir());
                        if (air == 300) {
                            return;
                        }

                        // Drawing would not normally occur, so we draw the air ourselves
                        if (!thePlayer.isInsideOfMaterial(Material.WATER)) {
                            GlStateManager.enableBlend();
                            ScaledResolution resolution = event.getResolution();
                            int left = resolution.getScaledWidth() / 2 + 91;
                            int top = resolution.getScaledHeight() - GuiIngameForge.right_height;

                            int full = MathHelper.ceil((double)(air - 2) * 10.0D / 300.0D);
                            int partial = MathHelper.ceil((double)air * 10.0D / 300.0D) - full;

                            for (int i = 0; i < full + partial; ++i) {
                                minecraft.ingameGUI.drawTexturedModalRect(left - i * 8 - 9, top, (i < full ? 16 : 25), 18, 9, 9);
                            }
                            GuiIngameForge.right_height += 10;

                            GlStateManager.disableBlend();
                        }
                    }
                }
                break;
            case HELMET:
                minecraft = Minecraft.getMinecraft();
                renderViewEntity = minecraft.getRenderViewEntity();
                if (renderViewEntity instanceof EntityLivingBase) {
                    EntityLivingBase entityLivingBase = (EntityLivingBase)renderViewEntity;
                    int[] ints = freezeTicks.get(entityLivingBase);
                    if (ints != null && ints[0] != 0) {
                        renderIceOverlay(minecraft, event.getResolution(), ints[0]);
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public Integer getSpecialAir(EntityPlayer player) {
        if (player.world.isRemote) {
            return clientMap.get(player);
        }
        else {
            return super.getSpecialAir(player);
        }
    }

    /**
     * @param minecraft
     * @param scaledRes
     * @see net.minecraft.client.gui.GuiIngame#renderPumpkinOverlay(ScaledResolution)
     */
    private static void renderIceOverlay(Minecraft minecraft, ScaledResolution scaledRes, int currentTicks) {
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        float transparency = currentTicks * (1f / ConfigHandler.ticksUntilFullyFrozen);
        GlStateManager.color(1.0F, 1.0F, 1.0F, transparency);
        GlStateManager.disableAlpha();
        minecraft.getTextureManager().bindTexture(ConfigHandler.usePackedIceTexture ? PACKED_ICE_TEXTURE_LOCATION : ICE_TEXTURE_LOCATION);
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer vertexbuffer = tessellator.getBuffer();
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        vertexbuffer.pos(0.0D, (double)scaledRes.getScaledHeight(), -90.0D).tex(0.0D, 1.0D).endVertex();
        vertexbuffer.pos((double)scaledRes.getScaledWidth(), (double)scaledRes.getScaledHeight(), -90.0D).tex(1.0D, 1.0D).endVertex();
        vertexbuffer.pos((double)scaledRes.getScaledWidth(), 0.0D, -90.0D).tex(1.0D, 0.0D).endVertex();
        vertexbuffer.pos(0.0D, 0.0D, -90.0D).tex(0.0D, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableAlpha();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public void setSpecialAir(EntityPlayer player, int air) {
        if (player.world.isRemote) {
            clientMap.put(player, air);
        }
        else {
            super.setSpecialAir(player, air);
        }
    }

    @Override
    protected void incrementFreezeCounter(EntityPlayer player) {
        int[] array = freezeTicks.get(player);
        if (array == null) {
            freezeTicks.put(player, new int[]{1});
        }
        else {
            int currentValue = array[0];
            if (currentValue < ConfigHandler.ticksUntilFullyFrozen) {
                array[0] = currentValue + 1;
            }
        }
    }

    @Override
    protected void decrementFreezeCounter(EntityPlayer player) {
        int[] array = freezeTicks.get(player);
        if (array == null) {
            freezeTicks.put(player, new int[]{0});
        }
        else {
            int currentValue = array[0];
            if (currentValue > 0) {
                array[0] = currentValue - 1;
            }
        }
    }
}

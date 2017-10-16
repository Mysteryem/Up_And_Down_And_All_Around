package uk.co.mysterymayhem.gravitymod.client.blocks.gravitygenerator;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import uk.co.mysterymayhem.gravitymod.api.IGravityFieldsVisible;
import uk.co.mysterymayhem.gravitymod.common.modsupport.ModSupport;

/**
 * Created by Mysteryem on 2017-01-25.
 */
@SideOnly(Side.CLIENT)
public class ClientTickListener {
    // When moving goggles into one of the client player's hands, there is a small delay before the effects activate
    // This delay is the same length as the attack cooldown of the item by default and is roughly the same amount of time
    // that it takes for a new item to appear in the player's hand
    private static final int DELAY_TICKS = 5;
    // Counter for use with held goggles
    private static int cumulativeTickCount = 0;
    // Accessor used to retrieve state
    private static boolean playerCanSeeGravityFields = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
//        final int heldItemTicks = 5;

        if (event.phase == TickEvent.Phase.END) {
            EntityPlayerSP thePlayer = Minecraft.getMinecraft().player;
//            final int heldItemTicks = DELAY_TICKS;//(int)thePlayer.getCooldownPeriod(); // probably always returns 5f for the goggles item
            if (thePlayer != null) {
                Iterable<ItemStack> armorInventoryList = thePlayer.getArmorInventoryList();
                for (ItemStack stack : armorInventoryList) {
                    if (IGravityFieldsVisible.stackMakesFieldsVisible(stack)) {
                        cumulativeTickCount = DELAY_TICKS + 1;
                        playerCanSeeGravityFields = true;
                        return;
                    }
                }
                if (ModSupport.isModLoaded(ModSupport.BAUBLES_MOD_ID)) {
                    IBaublesItemHandler baublesHandler = BaublesApi.getBaublesHandler(thePlayer);
                    int slots = baublesHandler.getSlots();
                    for (int i = 0; i < slots; i++) {
                        if (IGravityFieldsVisible.stackMakesFieldsVisible(baublesHandler.getStackInSlot(i))) {
                            cumulativeTickCount = DELAY_TICKS + 1;
                            playerCanSeeGravityFields = true;
                            return;
                        }
                    }
                }
                boolean foundInHand = false;
                for (EnumHand hand : EnumHand.values()) {
                    if (IGravityFieldsVisible.stackMakesFieldsVisible(thePlayer.getHeldItem(hand))) {
                        foundInHand = true;
                        break;
                    }
                }
                if (foundInHand) {
                    cumulativeTickCount++;
                    playerCanSeeGravityFields = cumulativeTickCount >= DELAY_TICKS;
                    return;
                }
                cumulativeTickCount = 0;
                playerCanSeeGravityFields = false;
            }
        }
    }

    /**
     * Based on GuiIngame::renderPumpkinOverlay for original code
     *
     * @param event
     */
    @SubscribeEvent
    public static void onGameOverlayRenderPre(RenderGameOverlayEvent.Pre event) {
        // Use of '>' rather than '>=' so that creation of gravity field generator range particle and overlay appear simultaneously
        if (event.getType() == RenderGameOverlayEvent.ElementType.HELMET && cumulativeTickCount > DELAY_TICKS && clientPlayerCanSeeGravityFields()) {
            ScaledResolution scaledRes = event.getResolution();
            GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
            GlStateManager.disableDepth();
            GlStateManager.depthMask(false);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                    GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            final float red = 116f / 255f;
            final float green = 94f / 255f;
            final float blue = 140f / 255f;
            final float alpha = 101f / 255f;
            GlStateManager.disableAlpha();
            GlStateManager.disableTexture2D();
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder vertexbuffer = tessellator.getBuffer();
            vertexbuffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
            vertexbuffer.pos(0.0D, (double)scaledRes.getScaledHeight(), -90.0D).color(red, green, blue, alpha).endVertex();
            vertexbuffer.pos((double)scaledRes.getScaledWidth(), (double)scaledRes.getScaledHeight(), -90.0D).color(red, green, blue, alpha).endVertex();
            vertexbuffer.pos((double)scaledRes.getScaledWidth(), 0.0D, -90.0D).color(red, green, blue, alpha).endVertex();
            vertexbuffer.pos(0.0D, 0.0D, -90.0D).color(red, green, blue, alpha).endVertex();
            tessellator.draw();
            GlStateManager.enableTexture2D();
            GlStateManager.depthMask(true);
            GlStateManager.enableDepth();
            GlStateManager.enableAlpha();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    public static boolean clientPlayerCanSeeGravityFields() {
        return playerCanSeeGravityFields;
    }
}

package uk.co.mysterymayhem.gravitymod.client.blocks.gravitygenerator;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import uk.co.mysterymayhem.gravitymod.common.blocks.gravitygenerator.TileGravityGenerator;

import static org.lwjgl.opengl.GL11.GL_ALWAYS;

/**
 * This class is modified from Ender IO's crazypants.enderio.machine.ranged.RangeParticle (CC0 at the time of writing)
 * Created by Mysteryem on 2017-01-11.
 */
@SideOnly(Side.CLIENT)
public class VolumeParticle extends Particle {

    private static final int AGE_LIMIT = 20 * 60 * 10; // 10 minutes
    private final float alphaPowered;
    private final float alphaUnpowered;
    private final float blue;
    private final float green;
    private final TileGravityGenerator owner;
    private final float red;
    private int age = 0;

    public VolumeParticle(TileGravityGenerator owner) {
        this(owner, 1, 1, 1, 0.3f, 0.15f);
    }

    public VolumeParticle(TileGravityGenerator owner, float red, float green, float blue, float alphaPowered, float alphaUnpowered) {
        super(owner.getWorld(), owner.getPos().getX(), owner.getPos().getY(), owner.getPos().getZ());
        this.owner = owner;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alphaPowered = alphaPowered;
        this.alphaUnpowered = alphaUnpowered;
    }

    @Override
    public int getFXLayer() {
        return 3;
    }

    @Override
    public boolean isAlive() {
        return this.age < AGE_LIMIT
                && this.owner.getVolumeParticle() == this
                && this.owner.hasWorldObj()
                && !this.owner.isInvalid()
                && this.worldObj.getTileEntity(this.owner.getPos()) == this.owner;
    }

    @Override
    public void onUpdate() {
        this.age++;
    }

    @Override
    public void renderParticle(VertexBuffer worldRendererIn, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
//        GlStateManager.translate(-interpPosX, -interpPosY, -interpPosZ);
//        GlStateManager.color(this.red, this.green, this.blue, this.alpha);
        EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
        if (thePlayer != null) {

            GlStateManager.pushMatrix();
            GlStateManager.pushAttrib();
            GlStateManager.enableLighting();
            GlStateManager.disableLighting();
            GlStateManager.disableCull();
            GlStateManager.enableBlend();
            GlStateManager.enableAlpha();
            GlStateManager.alphaFunc(GL_ALWAYS, 1);
//        GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.DestFactor.SRC_ALPHA);
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
//        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
            // EnderCore code:
            //RenderUtil.bindBlockTexture();
            // Same as:
//        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            GlStateManager.depthMask(false);
            GlStateManager.disableTexture2D();


            double d0 = thePlayer.prevPosX + (thePlayer.posX - thePlayer.prevPosX) * (double)partialTicks;
            double d1 = thePlayer.prevPosY + (thePlayer.posY - thePlayer.prevPosY) * (double)partialTicks;
            double d2 = thePlayer.prevPosZ + (thePlayer.posZ - thePlayer.prevPosZ) * (double)partialTicks;

//        RenderGlobal.renderFilledBox(Block.FULL_BLOCK_AABB, this.red, this.green, this.blue, this.alpha);

//        RenderGlobal.renderFilledBox(Block.FULL_BLOCK_AABB.expandXyz(0.01).offset(-interpPosX, -interpPosY, -interpPosZ).offset(this.posX, this.posY, this.posZ), this.red, this.green, this.blue, this.alpha);

            final float alpha;
            if (this.owner.isPowered()) {
//            int mod10 = this.age % 10;
                // by using thePlayer.ticksExisted instead of this.age, all particles will use the same timer
                float interp10 = (thePlayer.ticksExisted + partialTicks) % 40f; //0-9.9999 [0,10)
                float div10 = interp10 * 0.025f; //0-0.999 [0,1)
                float v = (float)(div10 * (Math.PI + Math.PI)); //[0,2PI)
                float sin = MathHelper.sin(v); //0,1,0,-1,0 [-1,1]
                alpha = this.alphaPowered + sin * 0.06f;
            }
            else {
                alpha = this.alphaUnpowered;
            }
            AxisAlignedBB bbToRender = this.owner.getSearchVolume().offset(-d0, -d1, -d2).expandXyz(-0.01);

            RenderGlobal.renderFilledBox(bbToRender, this.red, this.green, this.blue, alpha);
            RenderGlobal.drawSelectionBoundingBox(bbToRender, this.red, this.green, this.blue, alpha);

            GlStateManager.enableTexture2D();
            GlStateManager.depthMask(true);
            GlStateManager.disableAlpha();
            GlStateManager.disableBlend();
            GlStateManager.enableCull();
            GlStateManager.enableLighting();
            GlStateManager.popAttrib();
            GlStateManager.popMatrix();
        }
    }


}

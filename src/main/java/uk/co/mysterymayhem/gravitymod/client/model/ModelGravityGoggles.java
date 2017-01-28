package uk.co.mysterymayhem.gravitymod.client.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import uk.co.mysterymayhem.gravitymod.common.registries.StaticItems;

/**
 * Created by Mysteryem on 2017-01-25.
 */
public class ModelGravityGoggles extends ModelBiped {
    public static final ModelGravityGoggles INSTANCE = new ModelGravityGoggles();
    private static final ItemStack STACK = new ItemStack(StaticItems.GRAVITY_FIELD_GOGGLES);

    public ModelGravityGoggles() {
//        this.textureHeight = 16;
//        this.textureWidth = 16;
////        this.bipedHead.isHidden = true;
//        this.bipedLeftArm.isHidden = true;
//        this.bipedBody.isHidden = true;
//        this.bipedHeadwear.isHidden = true;
//        this.bipedLeftLeg.isHidden = true;
//        this.bipedRightArm.isHidden = true;
//        this.bipedRightLeg.isHidden = true;
//        this.bipedHead = new ModelRenderer(this, 0, 0);
//        this.bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
//        this.bipedHead.addBox(-4.0F, -8.0F, -4.5F, 8, 3, 9, 0.6f);
    }

    @Override
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        RenderItem itemRenderer = Minecraft.getMinecraft().getRenderItem();

        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();

        if (entityIn.isSneaking()) {
            GlStateManager.translate(0.0F, 0.25F, 0.0F);
        }

        GlStateManager.rotate(netHeadYaw, 0, 1, 0);
        GlStateManager.rotate(headPitch, 1, 0, 0);

//            GlStateManager.rotate(90, 0, 1, 0);
        GlStateManager.rotate(180, 1, 0, 0);

//            GlStateManager.scale(0.5F, 0.5F, 0.5F);
        GlStateManager.translate(0, 0.3, 0.25);

        if (itemRenderer.shouldRenderItemIn3D(STACK)/* || item instanceof ItemSkull*/) {
            GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
        }

        GlStateManager.pushAttrib();
        RenderHelper.enableStandardItemLighting();

        itemRenderer.renderItem(STACK, ItemCameraTransforms.TransformType.FIXED);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.popAttrib();

        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
//        this.bipedHead
//        super.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
    }
}

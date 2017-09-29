package uk.co.mysterymayhem.gravitymod.client.renderers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import uk.co.mysterymayhem.gravitymod.api.EnumGravityDirection;
import uk.co.mysterymayhem.gravitymod.common.entities.EntityGravityItem;

/**
 * Used to render gravity affected items, currently just ItemGravityAnchor, so that their bobbing and spinning are
 * rotated to take into account their gravity direction
 * Created by Mysteryem on 2016-11-07.
 */
@SideOnly(Side.CLIENT)
public class RenderGravityEntityItem extends RenderEntityItem {
    public RenderGravityEntityItem(RenderManager renderManagerIn) {
        super(renderManagerIn, Minecraft.getMinecraft().getRenderItem());
    }

    //See RenderEntityItem's implementation
    //Access transformer has been used to make this method public so it could be overridden
    @SuppressWarnings("deprecation")
    @Override
    public int transformModelCount(EntityItem itemIn, double xPos, double yPos, double zPos, float partialTicks, IBakedModel bakedModel) {
        if (itemIn instanceof EntityGravityItem) {
            ItemStack itemstack = itemIn.getItem();

            if (itemstack.isEmpty()) {
                return 0;
            }
            else {
                boolean is3d = bakedModel.isGui3d();
                int modelCount = this.getModelCount(itemstack);
                float yOffset = 0.25F;
                float upwardsBobTranslation = this.shouldBob() ? MathHelper.sin(((float)itemIn.getAge() + partialTicks) / 10.0F + itemIn.hoverStart) * 0.1F + 0.1F : 0;
                float scaleYCameraTransformation = bakedModel.getItemCameraTransforms().getTransform(ItemCameraTransforms.TransformType.GROUND).scale.y;

                EnumGravityDirection direction = ((EntityGravityItem)itemIn).getDirection();
                double[] d = direction.adjustXYZValues(0, upwardsBobTranslation, 0);

                // Replacement line
                GlStateManager.translate(xPos + d[0], yPos + d[1] + yOffset * scaleYCameraTransformation, zPos + d[2]);
                //orig line
//                GlStateManager.translate(xPos, yPos + upwardsBobTranslation + 0.25F * scaleYCameraTransformation, zPos);

                if (is3d || this.renderManager.options != null) {
                    float itemSpinYaw = (((float)itemIn.getAge() + partialTicks) / 20.0F + itemIn.hoverStart) * (180F / (float)Math.PI);

                    // Added line
                    direction.runCameraTransformation();
                    GlStateManager.rotate(itemSpinYaw, 0f, 1f, 0f);
                }

                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                return modelCount;
            }
        }
        else {
            return super.transformModelCount(itemIn, xPos, yPos, zPos, partialTicks, bakedModel);
        }
    }
}

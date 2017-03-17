package uk.co.mysterymayhem.gravitymod.common.items.baubles;

import baubles.api.BaubleType;
import baubles.api.BaublesApi;
import baubles.api.IBauble;
import baubles.api.cap.IBaublesItemHandler;
import baubles.api.render.IRenderBauble;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.ShapedOreRecipe;
import uk.co.mysterymayhem.gravitymod.GravityMod;
import uk.co.mysterymayhem.gravitymod.api.IGravityFieldsVisible;
import uk.co.mysterymayhem.gravitymod.client.model.ModelGravityGoggles;
import uk.co.mysterymayhem.gravitymod.common.modsupport.ModSupport;
import uk.co.mysterymayhem.gravitymod.common.registries.IGravityModItem;
import uk.co.mysterymayhem.gravitymod.common.registries.StaticItems;

import java.util.List;

/**
 * Created by Mysteryem on 2017-01-25.
 */
@Optional.InterfaceList({
        @Optional.Interface(iface = ModSupport.INTERFACE_IBAUBLE, modid = ModSupport.BAUBLES_MOD_ID),
        @Optional.Interface(iface = ModSupport.INTERFACE_IRENDERBAUBLE, modid = ModSupport.BAUBLES_MOD_ID)
})
public class ItemGravityFieldGoggles extends ItemArmor implements IBauble, IRenderBauble, IGravityFieldsVisible, IGravityModItem<ItemGravityFieldGoggles> {

    public static final ArmorMaterial material = EnumHelper.addArmorMaterial(
            GravityMod.MOD_ID + ":emptyarmourmaterial",
            "chainmail",//shouldn't actually ever be used
            0,
            new int[]{0, 0, 0, 0},
            Integer.MIN_VALUE,
            SoundEvents.ITEM_ARMOR_EQUIP_GENERIC,
            0);

    public ItemGravityFieldGoggles() {
        super(material, 1, EntityEquipmentSlot.HEAD);
//        this.setNoRepair();
//        this.setMaxDamage(0);
    }

    //    public static final ArmorMaterial material = EnumHelper.addArmorMaterial()

//    public ItemGravityFieldGoggles(ArmorMaterial materialIn, int renderIndexIn, EntityEquipmentSlot equipmentSlotIn) {
//        super(materialIn, renderIndexIn, equipmentSlotIn);
//    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("mouseovertext.mysttmtgravitymod.gravityfieldgoggles.line1"));
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return stack.isItemEnchanted() ? EnumRarity.RARE : GravityMod.RARITY_NORMAL;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, EntityEquipmentSlot armorSlot, ModelBiped _default) {
        return ModelGravityGoggles.INSTANCE;
    }

    @Override
    public String getModObjectName() {
        return "gravitygoggles";
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand) {
        if (ModSupport.isModLoaded(ModSupport.BAUBLES_MOD_ID)) {
            IBaublesItemHandler baublesHandler = BaublesApi.getBaublesHandler(playerIn);

            int[] validSlots = this.getBaubleType(itemStackIn).getValidSlots();
            for (int slot : validSlots) {
                ItemStack stackInSlot = baublesHandler.getStackInSlot(slot);
                if (stackInSlot == null) {
//                    baublesHandler.setStackInSlot();
                    baublesHandler.insertItem(slot, itemStackIn.copy(), false);
                    itemStackIn.stackSize = 0;
                    return ActionResult.newResult(EnumActionResult.SUCCESS, itemStackIn);
                }
            }
        }
        return super.onItemRightClick(itemStackIn, worldIn, playerIn, hand);
    }

    @Override
    @Optional.Method(modid = ModSupport.BAUBLES_MOD_ID)
    public BaubleType getBaubleType(ItemStack itemStack) {
        return BaubleType.HEAD;
    }

    @Override
    @SideOnly(Side.CLIENT)
    @Optional.Method(modid = ModSupport.BAUBLES_MOD_ID)
    public void onPlayerBaubleRender(ItemStack itemStack, EntityPlayer entityPlayer, RenderType renderType, float v) {
//        ItemRenderer itemRenderer = Minecraft.getMinecraft().getItemRenderer();
        RenderItem itemRenderer = Minecraft.getMinecraft().getRenderItem();
        if (renderType == RenderType.HEAD && itemStack.getItem() == StaticItems.GRAVITY_FIELD_GOGGLES) {

            GlStateManager.pushMatrix();
            GlStateManager.disableLighting();

//            GlStateManager.translate(0, -entityPlayer.getDefaultEyeHeight(), 0);
            /*
            Code taken from vazkii.botania.api.item.IBaubleRender.Helper#translateToHeadLevel(EntityPlayer player)t
             */
//            IRenderBauble.Helper.translateToHeadLevel(entityPlayer);
            if (entityPlayer.isSneaking()) {
                GlStateManager.translate(
                        0.25F * MathHelper.sin(entityPlayer.rotationPitch * (float)Math.PI / 180),
                        0.25F * MathHelper.cos(entityPlayer.rotationPitch * (float)Math.PI / 180),
                        0F);
            }

            ItemStack helmet = entityPlayer.inventory.armorItemInSlot(3);
            if (helmet != null && helmet.getItem() == this) {
                //We're already wearing a pair of goggles in our helmet slot, so render the bauble ones in a slightly
                //different position
//                GlStateManager.translate(0.2, -0.2, 0);
//                GlStateManager.translate(0.15, -0.12, 0);
                GlStateManager.rotate(26, 0, 1, 0);
                GlStateManager.translate(0.2, -0.28, 0);
                GlStateManager.rotate(-90, 0, 0, 1);
            }

            GlStateManager.rotate(90, 0, 1, 0);
            GlStateManager.rotate(180, 1, 0, 0);
//            GlStateManager.scale(0.5F, 0.5F, 0.5F);
            GlStateManager.translate(0, 0.3, -0.25);

            if (!itemRenderer.shouldRenderItemIn3D(itemStack)/* || item instanceof ItemSkull*/) {
                GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
            }

            GlStateManager.pushAttrib();
            RenderHelper.enableStandardItemLighting();

            itemRenderer.renderItem(itemStack, ItemCameraTransforms.TransformType.FIXED);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.popAttrib();

            GlStateManager.enableLighting();
            GlStateManager.popMatrix();
        }
    }

    @Override
    public void postInit() {
        GameRegistry.addRecipe(new ShapedOreRecipe(this,
                "IBI",
                "GIG",
                'I', "ingotIron",
                'B', StaticItems.LIQUID_ANTI_MASS_BUCKET,
                'G', "blockGlass"));
    }

    //    @SideOnly(Side.CLIENT)
//    public EntityEquipmentSlot getEquipmentSlot()
//    {
//        return EntityEquipmentSlot.HEAD;
//    }
}

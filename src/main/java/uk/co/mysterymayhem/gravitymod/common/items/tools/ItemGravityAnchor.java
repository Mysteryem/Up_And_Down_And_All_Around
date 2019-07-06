package uk.co.mysterymayhem.gravitymod.common.items.tools;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;
import uk.co.mysterymayhem.gravitymod.GravityMod;
import uk.co.mysterymayhem.gravitymod.api.API;
import uk.co.mysterymayhem.gravitymod.api.EnumGravityDirection;
import uk.co.mysterymayhem.gravitymod.api.ITickOnMouseCursor;
import uk.co.mysterymayhem.gravitymod.asm.Hooks;
import uk.co.mysterymayhem.gravitymod.common.entities.EntityGravityItem;
import uk.co.mysterymayhem.gravitymod.common.registries.GravityPriorityRegistry;
import uk.co.mysterymayhem.gravitymod.common.registries.IGravityModItem;
import uk.co.mysterymayhem.gravitymod.common.registries.ModItems;
import uk.co.mysterymayhem.gravitymod.common.util.boundingboxes.GravityAxisAlignedBB;
import uk.co.mysterymayhem.mystlib.util.KeyBindingUtil;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;

/**
 * Created by Mysteryem on 2016-11-03.
 */
public class ItemGravityAnchor extends Item implements ITickOnMouseCursor, IGravityModItem<ItemGravityAnchor> {

    // From EntityItem::onUpdate
    private static final double GRAVITY_DOWNWARDS_MOTION = 0.03999999910593033D;

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        KeyBinding keyBindSneak = Minecraft.getMinecraft().gameSettings.keyBindSneak;
        if (KeyBindingUtil.isKeyPressed(keyBindSneak)) {
//            tooltip.add("Affects gravity in inventory or on mouse cursor");
            tooltip.add(I18n.format("mouseovertext.mysttmtgravitymod.gravityanchor.sneak.line1"));
//            tooltip.add("Take care when crafting");
            tooltip.add(I18n.format("mouseovertext.mysttmtgravitymod.gravityanchor.sneak.line2"));
        }
        else {
            tooltip.add(I18n.format("mouseovertext.mysttmtgravitymod.gravityanchor.line1"));
            tooltip.add(keyBindSneak.getDisplayName() + I18n.format("mouseovertext.mysttmtgravitymod.presskeyfordetails"));
        }
    }

    @Override
    public Entity createEntity(World world, Entity location, ItemStack itemstack) {
        EnumGravityDirection direction = EnumGravityDirection.getSafeDirectionFromOrdinal(itemstack.getItemDamage());
        if (direction == EnumGravityDirection.DOWN) {
            return null;
        }
        if (location instanceof EntityItem) {
            return new EntityGravityItem(direction, (EntityItem)location);
        }
        else {
            //what
            FMLLog.bigWarning(
                    "Entity argument should always be an EntityItem, it was "
                            + (location == null ? "null" : "something else (not null)"));
            return null;
        }
    }

    @Override
    public String getModObjectName() {
        return "gravityanchor";
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return stack.isItemEnchanted() ? EnumRarity.RARE : GravityMod.RARITY_NORMAL;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (tab == ModItems.UP_AND_DOWN_CREATIVE_TAB) {
            for (int damage = 0; damage < EnumGravityDirection.values().length; damage++) {
                items.add(new ItemStack(this, 1, damage));
            }
        }
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        int i = stack.getItemDamage();
        return super.getUnlocalizedName() + "." + EnumGravityDirection.getSafeDirectionFromOrdinal(i).getName();
    }

    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        EnumGravityDirection direction = EnumGravityDirection.getSafeDirectionFromOrdinal(stack.getItemDamage());
        return direction != EnumGravityDirection.DOWN;
    }

    // Counteracts vanilla gravity and applies motion with the same strength in the gravity direction of the item
    @Override
    public boolean onEntityItemUpdate(EntityItem entityItem) {
        entityItem.hoverStart = 0;
        final EnumGravityDirection direction = EnumGravityDirection.getSafeDirectionFromOrdinal(entityItem.getItem().getItemDamage());
        if (direction == EnumGravityDirection.DOWN) {
            return false;
        }

        //TODO: Find the correct block and apply its slipperiness instead?
        // The item entities tend to slide all over the place otherwise
        if (entityItem.isCollided) {
            entityItem.motionX *= 0.8d;
            entityItem.motionY *= 0.8d;
            entityItem.motionZ *= 0.8d;
        }

        // Undo usual vanilla gravity
//            entityItem.motionY += GRAVITY_DOWNWARDS_MOTION;

        // Apply the correct change to motion
        double[] d = direction.adjustXYZValues(0, GRAVITY_DOWNWARDS_MOTION, 0);
        entityItem.motionX -= d[0];
        entityItem.motionY -= d[1];
        entityItem.motionZ -= d[2];

        // TODO: Determine how much of an effect (if any) this has
        switch (direction) {
            case UP:
                entityItem.onGround = entityItem.isCollidedVertically && entityItem.motionY > 0;
                break;
            case NORTH:
                entityItem.onGround = entityItem.isCollidedHorizontally && entityItem.motionZ < 0;
                break;
            case EAST:
                entityItem.onGround = entityItem.isCollidedHorizontally && entityItem.motionX > 0;
                break;
            case SOUTH:
                entityItem.onGround = entityItem.isCollidedHorizontally && entityItem.motionZ > 0;
                break;
            case WEST:
                entityItem.onGround = entityItem.isCollidedHorizontally && entityItem.motionX < 0;
                break;
        }

        // Not sure if it's needed
        entityItem.isAirBorne = !entityItem.onGround;
        return false;
    }

    @Override
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (entityIn instanceof EntityPlayerMP) {
            int meta = stack.getItemDamage();
            API.setPlayerGravity(EnumGravityDirection.getSafeDirectionFromOrdinal(meta), (EntityPlayerMP)entityIn, GravityPriorityRegistry.GRAVITY_ANCHOR);
        }
    }

    @Override
    public void register(IForgeRegistry<Item> registry) {
        this.setHasSubtypes(true);
        this.addPropertyOverride(new ResourceLocation("facing"), new FacingPropertyGetter());
        IGravityModItem.super.register(registry);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerClient(IForgeRegistry<Item> registry) {
        // ItemFacing used to produce the ModelResourceLocations
        for (ItemFacing direction : ItemFacing.values()) {
            ModelBakery.registerItemVariants(this, new ModelResourceLocation(this.getRegistryName() + "_" + direction.name().toLowerCase(Locale.ENGLISH), "inventory"));
        }

        // Ordinal of EnumGravityDirection provides the damage value/metadata of the itemstack
        for (EnumGravityDirection gravityDirection : EnumGravityDirection.values()) {
            ModelLoader.setCustomModelResourceLocation(this, gravityDirection.ordinal(), new ModelResourceLocation(this.getRegistryName(), "inventory"));
        }
    }

    // If these are changed, the item jsons will need to be changed too!
    private enum ItemFacing {
        FORWARDS,
        BACKWARDS,
        UP,
        DOWN,
        LEFT,
        RIGHT
    }

    // Most of the ItemGravityAnchor class is this ItemPropertyGetter :\, why did I do this?
    private static class FacingPropertyGetter implements IItemPropertyGetter {
        @Override
        public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn) {
            if (entityIn != null/* && stack != null*/) {
                AxisAlignedBB bb = entityIn.getEntityBoundingBox();
                EnumGravityDirection entityGravityDirection;
                if (bb instanceof GravityAxisAlignedBB) {
                    entityGravityDirection = ((GravityAxisAlignedBB)bb).getDirection();
                }
                else {
                    entityGravityDirection = EnumGravityDirection.DOWN;
                }

                final double entityPitch = entityIn.rotationPitch;
                final double entityYaw;
                if (entityIn instanceof EntityPlayer) {
                    entityYaw = MathHelper.wrapDegrees(entityIn.rotationYaw);
                }
                else {
                    entityYaw = MathHelper.wrapDegrees(entityIn.rotationYawHead);
                }
//
                double[] relativeYawAndPitch = Hooks.getRelativeYawAndPitch(entityYaw, entityPitch, entityIn);
                final double relativeYaw = relativeYawAndPitch[Hooks.YAW];
                final double relativePitch = relativeYawAndPitch[Hooks.PITCH];

                EnumGravityDirection itemDirection = EnumGravityDirection.getSafeDirectionFromOrdinal(stack.getItemDamage());

                ItemFacing itemFacing = null;

                if (itemDirection == entityGravityDirection) {
                    if (relativePitch >= 45) {
                        itemFacing = ItemFacing.FORWARDS;
                    }
                    else if (relativePitch <= -45) {
                        itemFacing = ItemFacing.BACKWARDS;
                    }
                    else {
                        itemFacing = ItemFacing.DOWN;
                    }
                }
                else if (itemDirection.getOpposite() == entityGravityDirection) {
                    if (relativePitch >= 45) {
                        itemFacing = ItemFacing.BACKWARDS;
                    }
                    else if (relativePitch <= -45) {
                        itemFacing = ItemFacing.FORWARDS;
                    }
                    else {
                        itemFacing = ItemFacing.UP;
                    }
                }
                else {
                    EnumFacing relativeHorizontalFacing = EnumFacing.getHorizontal(MathHelper.floor((relativeYaw * 4.0F / 360.0F) + 0.5D) & 3);
                    EnumFacing absoluteHorizontalFacing;

                    switch (entityGravityDirection.getInverseAdjustmentFromDOWNDirection()) {
                        case UP:
                            absoluteHorizontalFacing = relativeHorizontalFacing.rotateAround(EnumFacing.Axis.Z).rotateAround(EnumFacing.Axis.Z);
                            break;
                        case DOWN:
                            absoluteHorizontalFacing = relativeHorizontalFacing;
                            break;
                        case NORTH:
                            absoluteHorizontalFacing = relativeHorizontalFacing.rotateAround(EnumFacing.Axis.X);
                            break;
                        case EAST:
                            absoluteHorizontalFacing = relativeHorizontalFacing.rotateAround(EnumFacing.Axis.Z);
                            break;
                        case SOUTH:
                            absoluteHorizontalFacing = relativeHorizontalFacing.rotateAround(EnumFacing.Axis.X).rotateAround(EnumFacing.Axis.X);
                            break;
                        default://case WEST:
                            absoluteHorizontalFacing = relativeHorizontalFacing.rotateAround(EnumFacing.Axis.Z).rotateAround(EnumFacing.Axis.Z).rotateAround(EnumFacing.Axis.Z);
                            break;
                    }

                    EnumGravityDirection absoluteHorizontalFacingDirection = EnumGravityDirection.fromEnumFacing(absoluteHorizontalFacing);

                    if (absoluteHorizontalFacingDirection == itemDirection) {
                        if (relativePitch >= 45) {
                            itemFacing = ItemFacing.UP;
                        }
                        else if (relativePitch <= -45) {
                            itemFacing = ItemFacing.DOWN;
                        }
                        else {
                            itemFacing = ItemFacing.FORWARDS;
                        }
                    }
                    else if (absoluteHorizontalFacingDirection == itemDirection.getOpposite()) {
                        if (relativePitch >= 45) {
                            itemFacing = ItemFacing.DOWN;
                        }
                        else if (relativePitch <= -45) {
                            itemFacing = ItemFacing.UP;
                        }
                        else {
                            itemFacing = ItemFacing.BACKWARDS;
                        }
                    }
                    // I'm not sure of a better way to handle the rest of the cases
                    // This certainly is pretty horrible, not sure how it is for speed
                    else {
                        ItemFacing option1 = null;
                        ItemFacing option2 = null;
                        switch (itemDirection) {
                            case UP:
                                switch (entityGravityDirection) {
                                    case SOUTH:
                                        if (entityYaw > 0) {
                                            itemFacing = ItemFacing.LEFT;
                                        }
                                        else {
                                            itemFacing = ItemFacing.RIGHT;
                                        }
                                        break;
                                    case NORTH:
                                        if (entityYaw > 0) {
                                            itemFacing = ItemFacing.RIGHT;
                                        }
                                        else {
                                            itemFacing = ItemFacing.LEFT;
                                        }
                                        break;
                                    case WEST:
                                        if (relativeYaw < -90 || relativeYaw > 90) {
                                            itemFacing = ItemFacing.LEFT;
                                        }
                                        else {
                                            itemFacing = ItemFacing.RIGHT;
                                        }
                                        break;
                                    case EAST:
                                        if (relativeYaw < -90 || relativeYaw > 90) {
                                            itemFacing = ItemFacing.RIGHT;
                                        }
                                        else {
                                            itemFacing = ItemFacing.LEFT;
                                        }
                                        break;
                                }
                                break;
                            case DOWN:
                                switch (entityGravityDirection) {
                                    case SOUTH:
                                        if (entityYaw > 0) {
                                            itemFacing = ItemFacing.RIGHT;
                                        }
                                        else {
                                            itemFacing = ItemFacing.LEFT;
                                        }
                                        break;
                                    case NORTH:
                                        if (entityYaw > 0) {
                                            itemFacing = ItemFacing.LEFT;
                                        }
                                        else {
                                            itemFacing = ItemFacing.RIGHT;
                                        }
                                        break;
                                    case WEST:
                                        if (relativeYaw < -90 || relativeYaw > 90) {
                                            itemFacing = ItemFacing.RIGHT;
                                        }
                                        else {
                                            itemFacing = ItemFacing.LEFT;
                                        }
                                        break;
                                    case EAST:
                                        if (relativeYaw < -90 || relativeYaw > 90) {
                                            itemFacing = ItemFacing.LEFT;
                                        }
                                        else {
                                            itemFacing = ItemFacing.RIGHT;
                                        }
                                        break;
                                }
                                break;
                            case NORTH:
                                switch (entityGravityDirection) {
                                    case UP:
                                        if (entityYaw > 0) {
                                            itemFacing = ItemFacing.LEFT;
                                        }
                                        else {
                                            itemFacing = ItemFacing.RIGHT;
                                        }
                                        break;
                                    case DOWN:
                                        if (entityYaw > 0) {
                                            itemFacing = ItemFacing.RIGHT;
                                        }
                                        else {
                                            itemFacing = ItemFacing.LEFT;
                                        }
                                        break;
                                    case EAST:
                                        if (entityPitch > 0) {
                                            itemFacing = ItemFacing.RIGHT;
                                        }
                                        else {
                                            itemFacing = ItemFacing.LEFT;
                                        }
                                        break;
                                    case WEST:
                                        if (entityPitch > 0) {
                                            itemFacing = ItemFacing.LEFT;
                                        }
                                        else {
                                            itemFacing = ItemFacing.RIGHT;
                                        }
                                        break;
                                }
                                break;
                            case EAST:
                                switch (entityGravityDirection) {
                                    case DOWN:
                                    case NORTH:
                                    case SOUTH:
                                        option1 = ItemFacing.RIGHT;
                                        option2 = ItemFacing.LEFT;
                                        break;
                                    case UP:
                                        option1 = ItemFacing.LEFT;
                                        option2 = ItemFacing.RIGHT;
                                }
                                if (relativeYaw < -90 || relativeYaw > 90) {
                                    itemFacing = option1;
                                }
                                else {
                                    itemFacing = option2;
                                }
                                break;
                            case SOUTH:
                                switch (entityGravityDirection) {
                                    case UP:
                                        if (entityYaw > 0) {
                                            itemFacing = ItemFacing.RIGHT;
                                        }
                                        else {
                                            itemFacing = ItemFacing.LEFT;
                                        }
                                        break;
                                    case DOWN:
                                        if (entityYaw > 0) {
                                            itemFacing = ItemFacing.LEFT;
                                        }
                                        else {
                                            itemFacing = ItemFacing.RIGHT;
                                        }
                                        break;
                                    case EAST:
                                        if (entityPitch > 0) {
                                            itemFacing = ItemFacing.LEFT;
                                        }
                                        else {
                                            itemFacing = ItemFacing.RIGHT;
                                        }
                                        break;
                                    case WEST:
                                        if (entityPitch > 0) {
                                            itemFacing = ItemFacing.RIGHT;
                                        }
                                        else {
                                            itemFacing = ItemFacing.LEFT;
                                        }
                                        break;
                                }
                                break;
                            case WEST:
                                switch (entityGravityDirection) {
                                    case DOWN:
                                    case NORTH:
                                    case SOUTH:
                                        option1 = ItemFacing.LEFT;
                                        option2 = ItemFacing.RIGHT;
                                        break;
                                    case UP:
                                        option1 = ItemFacing.RIGHT;
                                        option2 = ItemFacing.LEFT;
                                }
                                if (relativeYaw < -90 || relativeYaw > 90) {
                                    itemFacing = option1;
                                }
                                else {
                                    itemFacing = option2;
                                }
                                break;
                        }
                        if (itemFacing == null) {
                            //???
                            // Will display the creative tab icon I think
                            return -1;
                        }
                    }
                }
                return itemFacing.ordinal();
            }
            // Item frames and other things
            return ItemFacing.DOWN.ordinal();
        }
    }
}

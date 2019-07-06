package uk.co.mysterymayhem.gravitymod.common.items.misc;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import uk.co.mysterymayhem.gravitymod.common.registries.IGravityModItem;
import uk.co.mysterymayhem.gravitymod.common.util.ReflectionLambdas;
import uk.co.mysterymayhem.mystlib.util.KeyBindingUtil;

import java.util.List;

/**
 * Shhh, nothing to see here
 * Created by Mysteryem on 2016-11-02.
 */
public class ItemCreativeTabIcon extends Item implements IGravityModItem<ItemCreativeTabIcon> {
    private static final int ENTITY_LIFETIME_SECONDS = 30;
    private static final int ENTITY_LIFETIME_TICKS = ENTITY_LIFETIME_SECONDS * 20;
    private static final String NAME = "creativetabicon";
    private static final NBTTagCompound fireworkTag = new NBTTagCompound();

    static {
        NBTTagList nbtTagList = new NBTTagList();
        NBTTagCompound explosionTag = new NBTTagCompound();
        explosionTag.setBoolean("Flicker", true);
        explosionTag.setBoolean("Trail", true);
        explosionTag.setByte("Type", (byte)0);
        explosionTag.setIntArray("Colors", new int[]{3145970, 16713728, 6215936});
        explosionTag.setIntArray("FadeColors", new int[]{857279, 16734553, 7012185});
        nbtTagList.appendTag(explosionTag);
        fireworkTag.setTag("Explosions", nbtTagList);
    }

    private Entity currentClientEntity = null;
    private Entity currentServerEntity = null;

    public ItemCreativeTabIcon() {
        this.setMaxStackSize(1);
        this.setMaxDamage(0);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
        KeyBinding keyBindSneak = Minecraft.getMinecraft().gameSettings.keyBindSneak;
        if (KeyBindingUtil.isKeyPressed(keyBindSneak)) {
            tooltip.add("Lawnmowers are overrated");
        }
        else {
            tooltip.add("Creative tab icon");
            tooltip.add("You're not supposed to have this");
        }
    }

    // So we can access the created entity
    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return true;
    }

    // Returns null so the original entity is used
    @Override
    public Entity createEntity(World world, Entity location, ItemStack itemstack) {
        if (location instanceof EntityItem) {
            EntityItem entityItem = (EntityItem)location;
            entityItem.lifespan = ENTITY_LIFETIME_TICKS;
            entityItem.setNoGravity(true);
            // Why Mojang? Why when you want an item to only live for a minute do you increase it's age rather than
            // decreasing it's maximum lifetime?
//            NBTTagCompound entityData = entityItem.getEntityData();
//            entityData.setBoolean("whymojangwhy_mysttmtgravitymod", true);

            // If I wanted the same as items dropped in creative (doesn't work all the time)(a minute), I could use this
//            entityItem.setAgeToCreativeDespawnTime();
        }
        return null;
    }

    @Override
    public boolean onEntityItemUpdate(EntityItem entityItem) {
        int age = ReflectionLambdas.get_EntityItem$age.applyAsInt(entityItem);
        if (entityItem.ticksExisted != age + 1) {
            entityItem.lifespan = age + ENTITY_LIFETIME_TICKS;
            entityItem.ticksExisted = age + 1;
        }
        boolean remote;
        if ((remote = entityItem.world.isRemote) && this.currentClientEntity == entityItem) {
            return false;
        }
        else if (this.currentServerEntity == entityItem) {
            return false;
        }

        if (remote) {
            this.currentClientEntity = entityItem;
        }
        else {
            this.currentServerEntity = entityItem;
        }

        if (entityItem.onGround) {
            entityItem.motionY += 0.1;
        }

        double xBefore = entityItem.posX;
        double yBefore = entityItem.posY;
        double zBefore = entityItem.posZ;

        double xMotionBefore = entityItem.motionX;
        double yMotionBefore = entityItem.motionY;
        double zMotionBefore = entityItem.motionZ;

        entityItem.onUpdate();

        double xToAdd = (entityItem.posX - xBefore) - xMotionBefore;
        double yToAdd = (entityItem.posY - yBefore) - yMotionBefore;
        double zToAdd = (entityItem.posZ - zBefore) - zMotionBefore;


        if (remote && entityItem.isCollided) {
            // Spawn a firework when the item entity hits a block
            entityItem.world.makeFireworks(entityItem.posX, entityItem.posY, entityItem.posZ, entityItem.motionX, entityItem.motionY, entityItem.motionZ, fireworkTag);
        }

        entityItem.motionX += xToAdd;
        entityItem.motionY += yToAdd;
        entityItem.motionZ += zToAdd;

        entityItem.motionX /= 0.9810000190734863D;
        entityItem.motionY /= 0.9810000190734863D;
        entityItem.motionZ /= 0.9810000190734863D;

        double minMovement = 0.5;
        double minMovementSquared = minMovement * minMovement;

        Vec3d motionVec = new Vec3d(entityItem.motionX, entityItem.motionY, entityItem.motionZ);
        double squareLength = motionVec.lengthSquared();
        if (squareLength == 0) {
            entityItem.motionY = minMovement;
            squareLength = minMovementSquared;
        }
        if (squareLength < minMovementSquared) {
            motionVec = motionVec.scale(MathHelper.sqrt(minMovementSquared / squareLength));
        }
        entityItem.motionX = motionVec.x;
        entityItem.motionY = motionVec.y;
        entityItem.motionZ = motionVec.z;

        if (remote) {
            // Smoke trail effect
            entityItem.world.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, entityItem.posX, entityItem.posY, entityItem.posZ, 0, 0, 0);
            this.currentClientEntity = null;
        }
        else {
            this.currentServerEntity = null;
        }

        return true;
    }

    // Don't set the creative tab
    @Override
    public CreativeTabs getModCreativeTab() {
        return null;
    }

    @Override
    public String getModObjectName() {
        return NAME;
    }
}

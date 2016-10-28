package uk.co.mysterymayhem.gravitymod.common.events;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Created by Mysteryem on 2016-10-23.
 */
public class ItemStackUseEvent extends LivingEvent {

    public final Side side;
    public final ItemStack stack;
    public final TickEvent.Phase phase;

    /**
     * Will throw an NPE if player is null
     * @param itemStack
     * @param entityLivingBase
     * @param phase
     */
    public ItemStackUseEvent (ItemStack itemStack, EntityLivingBase entityLivingBase, TickEvent.Phase phase, boolean isClientSide) {
        super(entityLivingBase);
        this.stack = itemStack;
        this.phase = phase;
        this.side = isClientSide ? Side.CLIENT : Side.SERVER;
    }

    /**
     * Fired around onItemUse
     */
    public static class OnUseOnBlock extends ItemStackUseEvent {
        public OnUseOnBlock(ItemStack itemStack, EntityLivingBase entityLivingBase, TickEvent.Phase phase, boolean isClientSide) {
            super(itemStack, entityLivingBase, phase, isClientSide);
        }

        public static class Pre extends OnUseOnBlock {
            public Pre(ItemStack itemStack, EntityLivingBase entityLivingBase, boolean isClientSide) {
                super(itemStack, entityLivingBase, TickEvent.Phase.START, isClientSide);
            }
        }

        public static class Post extends OnUseOnBlock {
            public Post(ItemStack itemStack, EntityLivingBase entityLivingBase, boolean isClientSide) {
                super(itemStack, entityLivingBase, TickEvent.Phase.END, isClientSide);
            }
        }
    }

    /**
     * Fired around onItemRightClick
     */
    public static class OnUseGeneral extends ItemStackUseEvent {
        public OnUseGeneral(ItemStack itemStack, EntityLivingBase entityLivingBase, TickEvent.Phase phase, boolean isClientSide) {
            super(itemStack, entityLivingBase, phase, isClientSide);
        }

        public static class Pre extends OnUseGeneral {
            public Pre(ItemStack itemStack, EntityLivingBase entityLivingBase, boolean isClientSide) {
                super(itemStack, entityLivingBase, TickEvent.Phase.START, isClientSide);
            }
        }

        public static class Post extends OnUseGeneral {
            public Post(ItemStack itemStack, EntityLivingBase entityLivingBase, boolean isClientSide) {
                super(itemStack, entityLivingBase, TickEvent.Phase.END, isClientSide);
            }
        }
    }

    /**
     * Fired around onPlayerStoppedUsing
     */
    public static class OnStoppedUsing extends ItemStackUseEvent {
        public OnStoppedUsing(ItemStack itemStack, EntityLivingBase entityLivingBase, TickEvent.Phase phase, boolean isClientSide) {
            super(itemStack, entityLivingBase, phase, isClientSide);
        }

        public static class Pre extends OnStoppedUsing {
            public Pre(ItemStack itemStack, EntityLivingBase entityLivingBase, boolean isClientSide) {
                super(itemStack, entityLivingBase, TickEvent.Phase.START, isClientSide);
            }
        }

        public static class Post extends OnStoppedUsing {
            public Post(ItemStack itemStack, EntityLivingBase entityLivingBase, boolean isClientSide) {
                super(itemStack, entityLivingBase, TickEvent.Phase.END, isClientSide);
            }
        }
    }
}

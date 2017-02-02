package uk.co.mysterymayhem.gravitymod.asm.patches;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import uk.co.mysterymayhem.gravitymod.asm.Transformer;
import uk.co.mysterymayhem.gravitymod.asm.util.patching.ClassPatcher;

import java.util.function.Predicate;

import static uk.co.mysterymayhem.gravitymod.asm.Ref.*;

/**
 * Upon using items, we generally need to ensure the player has absolute motion fields, while there are events that
 * are fired before items are used, there aren't any events fired immediately after the item is used. I need a way
 * to put the player's rotation fields back to normal after the item has finished processing, so I've added what is
 * effectively some events around the item use methods in the ItemStack class.
 * <p>
 * Created by Mysteryem on 2017-02-01.
 */
public class PatchItemStack extends ClassPatcher {
    private static final String ON_ITEM_USE_KEY = "ON_ITEM_USE_KEY";
    private static final String USE_ITEM_RIGHT_CLICK_KEY = "USE_ITEM_RIGHT_CLICK_KEY";
    private static final String ON_PLAYER_STOPPED_USING_KEY = "ON_PLAYER_STOPPED_USING_KEY";

    public PatchItemStack() {
        super("net.minecraft.item.ItemStack", 0, ClassWriter.COMPUTE_MAXS);

        Predicate<MethodNode> onItemUse = ItemStack$onItemUse_name::is;
        this.addMethodPatch(onItemUse, methodNode -> this.storeData(ON_ITEM_USE_KEY, Transformer.addLocalVar(methodNode, ItemStackAndBoolean)));
        this.addMethodPatch(onItemUse, (node, iterator) -> {
            if (Item$onItemUse.is(node)) {
                int insertedLocalIndex = (Integer)this.getData(ON_ITEM_USE_KEY);

                iterator.previous();
                iterator.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this
                iterator.add(new VarInsnNode(Opcodes.ALOAD, 1)); // this, EntityPlayer
                Hooks$onItemUsePre.addTo(iterator);
                iterator.add(new VarInsnNode(Opcodes.ASTORE, insertedLocalIndex));
                iterator.next(); // Item$onItemUse
                iterator.add(new VarInsnNode(Opcodes.ALOAD, insertedLocalIndex)); // origItemStackCopy
                iterator.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this
                iterator.add(new VarInsnNode(Opcodes.ALOAD, 1)); // this, EntityPlayer
                Hooks$onItemUsePost.addTo(iterator);
                return true;
            }
            return false;
        });

        Predicate<MethodNode> useItemRightClick = ItemStack$useItemRightClick_name::is;
        this.addMethodPatch(useItemRightClick, methodNode -> this.storeData(USE_ITEM_RIGHT_CLICK_KEY, Transformer.addLocalVar(methodNode, ItemStackAndBoolean)));
        this.addMethodPatch(useItemRightClick, (node, iterator) -> {
            if (Item$onItemRightClick.is(node)) {
                int insertedLocalIndex = (Integer)this.getData(USE_ITEM_RIGHT_CLICK_KEY);

                iterator.previous();
                iterator.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this
                iterator.add(new VarInsnNode(Opcodes.ALOAD, 2)); // this, EntityPlayer
                Hooks$onItemRightClickPre.addTo(iterator);
                iterator.add(new VarInsnNode(Opcodes.ASTORE, insertedLocalIndex));
                iterator.next(); // Item$onItemRightClick
                iterator.add(new VarInsnNode(Opcodes.ALOAD, insertedLocalIndex)); // origItemStackCopy
                iterator.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this
                iterator.add(new VarInsnNode(Opcodes.ALOAD, 2)); // this, EntityPlayer
                Hooks$onItemRightClickPost.addTo(iterator);
                return true;
            }
            return false;
        });

        Predicate<MethodNode> onPlayerStoppedUsing = ItemStack$onPlayerStoppedUsing_name::is;
        this.addMethodPatch(onPlayerStoppedUsing, methodNode -> this.storeData(ON_PLAYER_STOPPED_USING_KEY, Transformer.addLocalVar(methodNode, ItemStackAndBoolean)));
        this.addMethodPatch(onPlayerStoppedUsing, (node, iterator) -> {
            if (Item$onPlayerStoppedUsing.is(node)) {
                int insertedLocalIndex = (Integer)this.getData(ON_PLAYER_STOPPED_USING_KEY);

                iterator.previous();
                iterator.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this
                iterator.add(new VarInsnNode(Opcodes.ALOAD, 2)); // this, EntityLivingBase
                Hooks$onPlayerStoppedUsingPre.addTo(iterator);
                iterator.add(new VarInsnNode(Opcodes.ASTORE, insertedLocalIndex));
                iterator.next(); // Item$onPlayerStoppedUsing
                iterator.add(new VarInsnNode(Opcodes.ALOAD, insertedLocalIndex)); // origItemStackCopy
                iterator.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this
                iterator.add(new VarInsnNode(Opcodes.ALOAD, 2)); // this, EntityLivingBase
                Hooks$onPlayerStoppedUsingPost.addTo(iterator);
                return true;
            }
            return false;
        });
    }
}

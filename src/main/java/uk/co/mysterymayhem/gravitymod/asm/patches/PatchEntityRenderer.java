package uk.co.mysterymayhem.gravitymod.asm.patches;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.VarInsnNode;
import uk.co.mysterymayhem.gravitymod.asm.Ref;
import uk.co.mysterymayhem.gravitymod.asm.Transformer;
import uk.co.mysterymayhem.gravitymod.asm.util.patching.ClassPatcher;

/**
 * Created by Mysteryem on 2017-02-01.
 */
public class PatchEntityRenderer extends ClassPatcher {
    public PatchEntityRenderer() {
        super("net.minecraft.client.renderer.EntityRenderer");

        // Minecraft checks if the player is looking at an entity by expanding their bounding box and then effectively raytracing those found entities (I
        // think it just does some angle and distance calculations).
        //
        // The issue is that the player's bounding box is a GravityAxisAlignedBB.
        //
        // The vanilla method gets the player's look vector and then adds it's x, y and z coordinates (multiplied by a scalar) to the player's bounding box.
        // The player's look is absolute, but the player's bounding box moves in a relative fashion.
        //
        // Either we have to get the relative look of the player or we have to get a bounding box that doesn't move in a relative fashion.
        //
        // I have chosen to insert a hook that calls the instance method GravityAxisAlignedBB::toVanilla if the bounding box in question is a
        // GravityAxisAlignedBB. The hook returns a vanilla AxisAlignedBB with the same shape/position as the existing GravityAxisAlignedBB.
        //
        // This vanilla AxisAlignedBB will then move in an absolute fashion.
        this.addMethodPatch(Ref.EntityRenderer$getMouseOver_name::is, (node, iterator) -> {
            // This is a pretty unique method call, so we'll use it as the basis for finding the bytecode to patch
            if (Ref.WorldClient$getEntitiesInAABBexcluding.is(node)) {
                // Not strictly necessary, but the first previous() call will return the getEntitiesInAABBexcluding method we just found
                iterator.previous();
                while (iterator.hasPrevious()) {
                    node = iterator.previous();
                    if (Ref.Entity$getEntityBoundingBox.is(node)) {
                        Ref.Hooks$getVanillaEntityBoundingBox.replace(iterator);
                        Transformer.logPatchComplete(Ref.EntityRenderer$getMouseOver_name);
                        return true;
                    }
                }
                Transformer.die("Failed to find " + Ref.WorldClient$getEntitiesInAABBexcluding + " in " + Ref.EntityRenderer$getMouseOver_name);
            }
            return false;
        });

        // This patch makes it so that entity nameplates render such that their text is always up the right way from the client player's perspective
        this.addMethodPatch(Ref.EntityRenderer$drawNameplate_name::is, (node, iterator) -> {
            if (Ref.GlStateManager$disableLighting_name.is(node)) {
                iterator.previous();
                // boolean isThirdPersonFrontal argument, '8' should be consistent unless the method's description changes
                iterator.add(new VarInsnNode(Opcodes.ILOAD, 8));
                Ref.Hooks$runNameplateCorrection.addTo(iterator);
                Transformer.logPatchComplete(Ref.EntityRenderer$drawNameplate_name);
                return true;
            }
            return false;
        });
    }
}

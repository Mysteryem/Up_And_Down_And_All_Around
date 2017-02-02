package uk.co.mysterymayhem.gravitymod.asm.patches;

import uk.co.mysterymayhem.gravitymod.asm.Ref;
import uk.co.mysterymayhem.gravitymod.asm.util.patching.ClassPatcher;
import uk.co.mysterymayhem.gravitymod.asm.util.patching.MethodPatcher;

/**
 * Created by Mysteryem on 2017-02-01.
 */
public class PatchNetHandlerPlayClient extends ClassPatcher {
    public PatchNetHandlerPlayClient() {
        super(
                "net.minecraft.client.network.NetHandlerPlayClient",
                MethodPatcher.create(
                        Ref.INetHandlerPlayClient$handlePlayerPosLook_name::is,
                        (node, iterator) -> {
                            // Mojang seems to assume that the bottom of a player's bounding box is equivalent to a player's posY a lot
                            // of the time, which is pretty wrong in my opinion (and breaks things when players have non-standard bounding
                            // boxes). So I replaced the access of minY of the player's bounding box with
                            // their Y position
                            if (Ref.AxisAlignedBB$minY_GET.is(node)) {
                                Ref.EntityPlayer$posY_GET.replace(iterator);
                                iterator.previous(); // the instruction we just replaced the old one with
                                iterator.previous(); // getEntityBoundingBox()
                                iterator.remove();
                                return true;
                            }
                            return false;
                        }));
    }
}

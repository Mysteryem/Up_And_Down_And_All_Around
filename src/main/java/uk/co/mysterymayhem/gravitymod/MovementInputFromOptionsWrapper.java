package uk.co.mysterymayhem.gravitymod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.MovementInput;
import net.minecraft.util.MovementInputFromOptions;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * UNUSED?
 * Created by Mysteryem on 2016-08-07.
 */
@SideOnly(Side.CLIENT)
public class MovementInputFromOptionsWrapper extends MovementInput {

    private final MovementInputFromOptions fromOptions;

    public MovementInputFromOptionsWrapper(MovementInputFromOptions wrapped) {
        this.fromOptions = wrapped;
    }

    @Override
    public void updatePlayerMoveState() {
        fromOptions.updatePlayerMoveState();
        EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
        thePlayer.movementInput = fromOptions;
        if (GravityMod.proxy.gravityManagerServer.isPlayerUpsideDown(thePlayer)) {
            fromOptions.moveStrafe *= -1;
        }
    }
}

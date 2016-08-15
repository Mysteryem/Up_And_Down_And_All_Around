package uk.co.mysterymayhem.gravitymod;

import net.minecraft.client.renderer.GlStateManager;

/**
 * Created by Mysteryem on 2016-08-14.
 */
public enum EnumGravityDirection {
    UP(() -> GlStateManager.rotate(-180, 0, 0, 1)),
    DOWN(() -> {}),
    NORTH(() -> GlStateManager.rotate(90, 1, 0, 0)),
    EAST(() -> GlStateManager.rotate(90, 0, 0, 1)),
    SOUTH(() -> GlStateManager.rotate(-90, 1, 0, 0)),
    WEST(() -> GlStateManager.rotate(-90, 0, 0, 1));

    private final Runnable cameraTransformRunnable;

    private EnumGravityDirection(Runnable cameraTransformRunnable) {
        this.cameraTransformRunnable = cameraTransformRunnable;
    }
}

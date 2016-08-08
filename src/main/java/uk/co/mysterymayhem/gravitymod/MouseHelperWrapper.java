package uk.co.mysterymayhem.gravitymod;

import net.minecraft.client.Minecraft;
import net.minecraft.util.MouseHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by Mysteryem on 2016-08-07.
 */
@SideOnly(Side.CLIENT)
public class MouseHelperWrapper extends MouseHelper {

    private final MouseHelper mouseHelper;

    public MouseHelperWrapper(MouseHelper mouseHelper) {
        this.mouseHelper = mouseHelper;
    }

    @Override
    public void ungrabMouseCursor() {
        super.ungrabMouseCursor();
    }

    @Override
    public void mouseXYChange() {
        super.mouseXYChange();
        this.deltaX *= -1;
        this.deltaY *= -1;
    }

    @Override
    public void grabMouseCursor() {
        super.grabMouseCursor();
    }

    public MouseHelper getMouseHelper() {
        return this.mouseHelper;
    }


}

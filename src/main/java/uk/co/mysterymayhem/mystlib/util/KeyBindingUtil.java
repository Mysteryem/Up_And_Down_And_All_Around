package uk.co.mysterymayhem.mystlib.util;

import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

/**
 * Created by Mysteryem on 06/07/2019.
 */
public class KeyBindingUtil {
    public static boolean isKeyPressed(KeyBinding keyBinding) {
        // We don't care about key conflicts so we pass null
        if (keyBinding.getKeyModifier().isActive(null)) {
            int keyCode = keyBinding.getKeyCode();
            if (keyCode < 0) {
                return Mouse.isButtonDown(keyCode + 100);
            }
            else {
                return Keyboard.isKeyDown(keyCode);
            }
        }
        return false;
    }
}

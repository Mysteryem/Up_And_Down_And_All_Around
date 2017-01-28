package uk.co.mysterymayhem.gravitymod.api;

import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

/**
 * Implemented by all items which, when worn as armour or in bauble slots, allow the player to see a visible
 * representation of gravity fields
 *
 * Make your items implement this interface if they should enable a player to see gravity fields
 * (use the @Optional annotation so your mod doesn't require UpAndDown to be loaded)
 * Created by Mysteryem on 2017-01-25.
 */
public interface IGravityFieldsVisible {
    static boolean stackMakesFieldsVisible(@Nullable ItemStack stack) {
        if (stack != null) {
            if (stack.getItem() instanceof IGravityFieldsVisible) {
                return true;
            }
        }
        return false;
    }
}

package uk.co.mysterymayhem.gravitymod.api;

/**
 * Implement on an Item subclass if it should tick (Item::onUpdate) while held on the mouse cursor.
 * Note that this will not occur if the player in question is in creative and in the creative inventory, due to a
 * bug/feature in vanilla Minecraft.
 * Created by Mysteryem on 2016-10-11.
 */
public interface ITickOnMouseCursor {
}

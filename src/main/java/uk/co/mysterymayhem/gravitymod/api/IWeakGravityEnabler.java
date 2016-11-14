package uk.co.mysterymayhem.gravitymod.api;

/**
 * Implemented by all items which, when worn as armour or in bauble slots, cause the player to be affected by weak
 * gravity fields (NYI). Config may require more than one item to be worn (also NYI).
 *
 * Make your items implement this interface if they should enable a player to be affected by weak gravity
 * (use the @Optional notation so your mod doesn't require UpAndDown to exist)
 * Created by Mysteryem on 2016-11-06.
 */
public interface IWeakGravityEnabler {
}

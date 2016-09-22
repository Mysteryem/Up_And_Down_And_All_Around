package uk.co.mysterymayhem.gravitymod.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import uk.co.mysterymayhem.gravitymod.GravityMod;
import uk.co.mysterymayhem.gravitymod.asm.EntityPlayerWithGravity;
import uk.co.mysterymayhem.gravitymod.capabilities.GravityDirectionCapability;

/**
 * Created by Mysteryem on 2016-08-26.
 */
public class API {

    public static final int X = 0;
    public static final int Y = 1;
    public static final int Z = 2;

    /**
     * Set a new gravity direction for a player, this wall fire (post) the relevant events, perform client sync etc.
     * @param newGravity New gravity direction for the player.
     * @param player Server player whose gravity you want to change.
     */
    public static void setPlayerGravity(EnumGravityDirection newGravity, EntityPlayerMP player) {
        GravityMod.proxy.getGravityManager().doGravityTransition(newGravity, player);
    }

    /**
     * Get the current gravity direction of a player.
     * @param player Player to look up, must not be NULL, can be either a client or server player.
     * @return The gravity direction of the player.
     * (will return DOWN if the player does not have the GravityCapability, e.g., while the EntityPlayer object is constructing)
     */
    public static EnumGravityDirection getGravityDirection(EntityPlayer player) {
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }
        return GravityDirectionCapability.getGravityDirection(player);
    }

    /**
     * Get the current gravity direction of a player.
     * @param player Player to look up, can be either a client or server player.
     * @return The gravity direction of the player.
     * (will return DOWN if the player does not have the GravityCapability, e.g., while the EntityPlayer object is
     * constructing or if the passed player object is NULL)
     */
    public static EnumGravityDirection getGravityDirectionUnsafe(EntityPlayer player) {
        if (player == null) {
            return GravityDirectionCapability.DEFAULT_GRAVITY;
        }
        return GravityDirectionCapability.getGravityDirection(player);
    }

    /**
     * Get the eye height the player would have if their gravity direction was currently DOWN.
     * This mod overrides getEyeHeight() so a ton of vanilla stuff works/mostly works. You can use this method to get
     * what getEyeHeight() would normally return if it hadn't been overwritten.
     * @param player Player whose 'standard' eye height you want
     * @return The player's eye height as if their current gravity direction is DOWN.
     */
    public static float getStandardEyeHeight(EntityPlayer player) {
        // Should pretty much always be the case
        if (player instanceof EntityPlayerWithGravity) {
            return ((EntityPlayerWithGravity)player).super_getEyeHeight();
        }
        else {
            // For FakePlayers and similar, maybe?
            return player.getEyeHeight();
        }

//        // Normal sneaking height is 1.65
//        // Normal sneaking eye height is 1.57
//        if (player.isSneaking()) {
//            return player.height - 0.08f;
//            //return 1.57f;
//        }
//        // Normal elytra flying height is 0.6
//        // Normal elytra flying eye height is 0.4
//        else if (player.isElytraFlying()) {
//            return player.height - 0.2f;
//            //return 0.4f
//        }
//        // Normal sleeping height is 0.2
//        // Normal sleeping eyeHeight is 0.2
//        else if (player.isPlayerSleeping()) {
//            return player.height;
//        }
//        // Normal height is 1.8
//        // Normal eye height is 1.62
//        else {
//            return player.height - 0.18f;
//        }
    }
}

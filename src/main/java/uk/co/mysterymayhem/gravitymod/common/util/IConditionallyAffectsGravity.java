package uk.co.mysterymayhem.gravitymod.common.util;

import net.minecraft.entity.player.EntityPlayerMP;

/**
 * Created by Mysteryem on 2016-11-11.
 */
public interface IConditionallyAffectsGravity extends IAffectsGravity {
    boolean affectsPlayer(EntityPlayerMP player);
}

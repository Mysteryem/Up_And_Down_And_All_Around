package uk.co.mysterymayhem.gravitymod.common.util;

import net.minecraft.entity.Entity;

/**
 * Created by Mysteryem on 2016-11-12.
 */
public interface IAffectsGravity {
    int getPriority(Entity target);
}

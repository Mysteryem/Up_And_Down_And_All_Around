package uk.co.mysterymayhem.gravitymod.common.util;

import net.minecraft.entity.Entity;

/**
 * Created by Mysteryem on 2016-11-11.
 */
public interface IConditionallyAffectsGravity extends IAffectsGravity {
    boolean affectsEntity(Entity entity);
}

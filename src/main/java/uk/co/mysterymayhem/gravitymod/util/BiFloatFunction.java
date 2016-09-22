package uk.co.mysterymayhem.gravitymod.util;

import java.util.function.Function;

/**
 * Created by Mysteryem on 2016-08-16.
 */
@FunctionalInterface
public interface BiFloatFunction<R>{
    R apply(float f1, float f2);
}

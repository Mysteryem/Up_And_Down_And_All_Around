package uk.co.mysterymayhem.gravitymod.util;

/**
 * Created by Mysteryem on 2016-08-16.
 */
@FunctionalInterface
public interface TriDoubleFunction<R> {
    R apply(double d1, double d2, double d3);
}

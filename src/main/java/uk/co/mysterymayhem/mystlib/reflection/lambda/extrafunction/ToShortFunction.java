package uk.co.mysterymayhem.mystlib.reflection.lambda.extrafunction;

/**
 * Created by Mysteryem on 2016-12-30.
 */
@FunctionalInterface
public interface ToShortFunction<T> {
    default ShortSupplier bind(T instance) {
        return () -> this.applyAsShort(instance);
    }

    short applyAsShort(T value);
}

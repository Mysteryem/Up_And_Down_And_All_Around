package uk.co.mysterymayhem.mystlib.reflection.lambda.extrafunction;

/**
 * Created by Mysteryem on 2016-12-30.
 */
@FunctionalInterface
public interface ToShortFunction<T> {
    short applyAsShort(T value);

    default ShortSupplier bind(T instance) {
        return () -> this.applyAsShort(instance);
    }
}

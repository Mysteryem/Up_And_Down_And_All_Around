package uk.co.mysterymayhem.mystlib.reflection.lambda.extrafunction;

/**
 * Created by Mysteryem on 2016-12-30.
 */
@FunctionalInterface
public interface ToCharFunction<T> {
    default CharSupplier bind(T instance) {
        return () -> this.applyAsChar(instance);
    }

    char applyAsChar(T value);
}

package uk.co.mysterymayhem.mystlib.reflection.lambda.extrafunction;

/**
 * Created by Mysteryem on 2016-12-30.
 */
@FunctionalInterface
public interface ToFloatFunction<T> {
    default FloatSupplier bind(T instance) {
        return () -> this.applyAsFloat(instance);
    }

    float applyAsFloat(T value);
}

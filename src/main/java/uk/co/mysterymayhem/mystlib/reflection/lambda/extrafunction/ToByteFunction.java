package uk.co.mysterymayhem.mystlib.reflection.lambda.extrafunction;

/**
 * Created by Mysteryem on 2016-12-30.
 */
@FunctionalInterface
public interface ToByteFunction<T> {
    byte applyAsByte(T value);

    default ByteSupplier bind(T instance) {
        return () -> this.applyAsByte(instance);
    }
}

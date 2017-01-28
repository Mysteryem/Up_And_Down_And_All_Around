package uk.co.mysterymayhem.mystlib.reflection.lambda.extrafunction;

import java.util.Objects;

/**
 * Created by Mysteryem on 2016-12-30.
 */
@FunctionalInterface
public interface ObjByteConsumer<T> {
    void accept(T t, byte value);

    default ObjByteConsumer<T> andThen(ObjByteConsumer<? super T> after) {
        Objects.requireNonNull(after);
        return (t, v) -> { this.accept(t, v); after.accept(t, v); };
    }

    default ByteConsumer bind(T instance) {
        return (b) -> this.accept(instance, b);
    }
}

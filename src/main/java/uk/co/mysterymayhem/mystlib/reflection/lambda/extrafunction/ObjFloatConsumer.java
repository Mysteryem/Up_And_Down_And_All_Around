package uk.co.mysterymayhem.mystlib.reflection.lambda.extrafunction;

import java.util.Objects;

/**
 * Created by Mysteryem on 2016-12-30.
 */
@FunctionalInterface
public interface ObjFloatConsumer<T> {
    default ObjFloatConsumer<T> andThen(ObjFloatConsumer<? super T> after) {
        Objects.requireNonNull(after);
        return (t, v) -> {
            this.accept(t, v);
            after.accept(t, v);
        };
    }

    void accept(T t, float value);

    default FloatConsumer bind(T instance) {
        return (f) -> this.accept(instance, f);
    }
}

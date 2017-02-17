package uk.co.mysterymayhem.mystlib.reflection.lambda.extrafunction;

import java.util.Objects;

/**
 * Created by Mysteryem on 2016-12-30.
 */
@FunctionalInterface
public interface ObjBooleanConsumer<T> {
    default ObjBooleanConsumer<T> andThen(ObjBooleanConsumer<? super T> after) {
        Objects.requireNonNull(after);
        return (t, v) -> {
            this.accept(t, v);
            after.accept(t, v);
        };
    }

    void accept(T t, boolean value);

    default BooleanConsumer bind(T instance) {
        return (bool) -> this.accept(instance, bool);
    }
}

package uk.co.mysterymayhem.mystlib.reflection.lambda.extrafunction;

import java.util.Objects;

/**
 * Created by Mysteryem on 2016-12-30.
 */
@FunctionalInterface
public interface ObjCharConsumer<T> {
    void accept(T t, char value);

    default ObjCharConsumer<T> andThen(ObjCharConsumer<? super T> after) {
        Objects.requireNonNull(after);
        return (t, v) -> { accept(t, v); after.accept(t, v); };
    }

    default CharConsumer bind(T instance) {
        return (c) -> this.accept(instance, c);
    }
}

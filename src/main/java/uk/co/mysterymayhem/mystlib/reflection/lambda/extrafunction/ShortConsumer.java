package uk.co.mysterymayhem.mystlib.reflection.lambda.extrafunction;

import java.util.Objects;

/**
 * Created by Mysteryem on 2016-12-30.
 */
@FunctionalInterface
public interface ShortConsumer {
    default ShortConsumer andThen(ShortConsumer after) {
        Objects.requireNonNull(after);
        return (t) -> {
            accept(t);
            after.accept(t);
        };
    }

    void accept(short value);
}

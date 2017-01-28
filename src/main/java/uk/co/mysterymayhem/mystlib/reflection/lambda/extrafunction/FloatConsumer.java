package uk.co.mysterymayhem.mystlib.reflection.lambda.extrafunction;

import java.util.Objects;

/**
 * Created by Mysteryem on 2016-12-30.
 */
@FunctionalInterface
public interface FloatConsumer {
    void accept (float value);

    default FloatConsumer andThen(FloatConsumer after) {
        Objects.requireNonNull(after);
        return (t) -> { accept(t); after.accept(t); };
    }
}

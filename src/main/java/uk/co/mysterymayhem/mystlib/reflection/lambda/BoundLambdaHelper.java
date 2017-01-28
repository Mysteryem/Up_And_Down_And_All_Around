package uk.co.mysterymayhem.mystlib.reflection.lambda;

import uk.co.mysterymayhem.mystlib.reflection.lambda.extrafunction.*;

import java.util.function.*;

/**
 * Note that these have not been benchmarked
 * Created by Mysteryem on 2017-01-23.
 */
public class BoundLambdaHelper {
    public static <T> BooleanConsumer bind(ObjBooleanConsumer<? super T> objBooleanConsumer, T instance) {
        return objBooleanConsumer.bind(instance);
    }

    public static <T> BooleanSupplier bind(Predicate<? super T> predicate, T instance) {
        return () -> predicate.test(instance);
    }

    public static <T> ByteConsumer bind(ObjByteConsumer<? super T> objByteConsumer, T instance) {
        return objByteConsumer.bind(instance);
    }

    public static <T> ByteSupplier bind(ToByteFunction<? super T> toByteFunction, T instance) {
        return toByteFunction.bind(instance);
    }

    public static <T> CharConsumer bind(ObjCharConsumer<? super T> objCharConsumer, T instance) {
        return objCharConsumer.bind(instance);
    }

    public static <T> CharSupplier bind(ToCharFunction<? super T> toCharFunction, T instance) {
        return toCharFunction.bind(instance);
    }

    public static <T> DoubleConsumer bind(ObjDoubleConsumer<? super T> objDoubleConsumer, T instance) {
        return (d) -> objDoubleConsumer.accept(instance, d);
    }

    public static <T> DoubleSupplier bind(ToDoubleFunction<? super T> toDoubleFunction, T instance) {
        return () -> toDoubleFunction.applyAsDouble(instance);
    }

    public static <T> FloatConsumer bind(ObjFloatConsumer<? super T> objFloatConsumer, T instance) {
        return objFloatConsumer.bind(instance);
    }

    public static <T> FloatSupplier bind(ToFloatFunction<? super T> toFloatFunction, T instance) {
        return toFloatFunction.bind(instance);
    }

    public static <T> IntConsumer bind(ObjIntConsumer<? super T> objIntConsumer, T instance) {
        return (i) -> objIntConsumer.accept(instance, i);
    }

    public static <T> IntSupplier bind(ToIntFunction<? super T> toIntFunction, T instance) {
        return () -> toIntFunction.applyAsInt(instance);
    }

    public static <T> LongConsumer bind(ObjLongConsumer<? super T> objLongConsumer, T instance) {
        return (l) -> objLongConsumer.accept(instance, l);
    }

    public static <T> LongSupplier bind(ToLongFunction<? super T> toLongFunction, T instance) {
        return () -> toLongFunction.applyAsLong(instance);
    }

    public static <T> ShortConsumer bind(ObjShortConsumer<? super T> objShortConsumer, T instance) {
        return objShortConsumer.bind(instance);
    }

    public static <T> ShortSupplier bind(ToShortFunction<? super T> toShortFunction, T instance) {
        return toShortFunction.bind(instance);
    }

    public static <T, U> Consumer<U> bind(BiConsumer<? super T, ? super U> biConsumer, T instance) {
        return (u -> biConsumer.accept(instance, u));
    }

    public static <T, R> Supplier<R> bind(Function<? super T, R> function, T instance) {
        return () -> function.apply(instance);
    }
//
//    public static <T, I> I bind(Object function, Class<I> boundFInterfaceClass, T instance, String methodName, MethodType methodType) {
//        LambdaBuilder.
//    }
}

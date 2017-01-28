package uk.co.mysterymayhem.mystlib.reflection.lambda;

import uk.co.mysterymayhem.mystlib.reflection.lambda.extrafunction.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.*;

/**
 * This is just a glorified tuple, store its getter and setter as static final fields somewhere for fast access (don't store the GetterSetterTuple instance, that's slow)
 * Created by Mysteryem on 2016-12-30.
 */
public class GetterSetterTuple<GETTER, SETTER> {

    enum Type {

        STATIC_OBJECT(Object.class, Supplier.class, Consumer.class, true),
        STATIC_BYTE(byte.class, ByteSupplier.class, ByteConsumer.class, true),
        STATIC_CHAR(char.class, CharSupplier.class, CharConsumer.class, true),
        STATIC_SHORT(short.class, ShortSupplier.class, ShortConsumer.class, true),
        STATIC_INT(int.class, IntSupplier.class, IntConsumer.class, true),
        STATIC_LONG(long.class, LongSupplier.class, LongConsumer.class, true),
        STATIC_FLOAT(float.class, FloatSupplier.class, FloatConsumer.class, true),
        STATIC_DOUBLE(double.class, DoubleSupplier.class, DoubleConsumer.class, true),
        INSTANCE_OBJECT(Object.class, Function.class, BiConsumer.class),
        INSTANCE_BYTE(byte.class, ToByteFunction.class, ObjByteConsumer.class),
        INSTANCE_CHAR(char.class, ToCharFunction.class, ObjCharConsumer.class),
        INSTANCE_SHORT(short.class, ToShortFunction.class, ObjShortConsumer.class),
        INSTANCE_INT(int.class, ToIntFunction.class, ObjIntConsumer.class),
        INSTANCE_LONG(long.class, ToLongFunction.class, ObjLongConsumer.class),
        INSTANCE_FLOAT(float.class, ToFloatFunction.class, ObjFloatConsumer.class),
        INSTANCE_DOUBLE(double.class, ToDoubleFunction.class, ObjDoubleConsumer.class),
        GENERATED(void.class, void.class, void.class);

        final Class<?> fieldType;
        final Class<?> getter;
        final Class<?> setter;
        final boolean isStatic;

        Type(Class<?> fieldType, Class<?> getter, Class<?> setter, boolean isStatic) {
            this.fieldType = fieldType;
            this.getter = getter;
            this.setter = setter;
            this.isStatic = isStatic;
        }

        Type(Class<?> fieldType, Class<?> getter, Class<?> setter) {
            this(fieldType, getter, setter, false);
        }

        static final Map<Class<?>, Type> GETTER_TO_TYPE = new HashMap<>();
        static {
            for (Type type : Type.values()) {
                GETTER_TO_TYPE.put(type.getter, type);
            }
        }

        static Type get(Object getter, Object setter) {
            Type toReturn = null;

            Class<?>[] getterInterfaces = getter.getClass().getInterfaces();
            outerfor:
            for (Class<?> getterInterface : getterInterfaces) {
                Type type = GETTER_TO_TYPE.get(getterInterface);
                if (type != null) {
                    Class<?>[] setterInterfaces = setter.getClass().getInterfaces();
                    for (Class<?> setterInterface : setterInterfaces) {
                        if (setterInterface == type.setter) {
                            toReturn = type;
                            break outerfor;
                        }
                    }
                }
            }

            return toReturn;
        }

        @SuppressWarnings("unchecked")
        static final IdentityHashMap<Class<?>, Type>[] INTERNAL_LOOKUP = new IdentityHashMap[]{new IdentityHashMap<>(), new IdentityHashMap<>()};

        static Type get(Field field) {
            return get(field.getType(), Modifier.isStatic(field.getModifiers()));
        }

        static Type get(Class<?> fieldType, boolean isStatic) {
            fieldType = fieldType.isPrimitive() ? Objects.requireNonNull(fieldType) : Object.class;
            int index = isStatic ? 0 : 1;
            return INTERNAL_LOOKUP[index].get(fieldType);
        }
    }

    private final Field field;
    private final GETTER getter;
    private final SETTER setter;
    private final Type type;

    public GetterSetterTuple(GETTER getter, SETTER setter, Field field) {
        this(getter, setter, field, false);
    }

    GetterSetterTuple(GETTER getter, SETTER setter, Field field, boolean generated) {
        this.field = field;
        this.getter = getter;
        this.setter = setter;
        Type type = Type.get(getter, setter);
        if (type == null) {
            if (generated) {
                type = Type.GENERATED;
            }
            else {
                throw new IllegalArgumentException("Getter and setter are not valid");
            }
        }
        this.type = type;
    }

    public static GetterSetterTuple<?, ?> fromField(Field field) {
        Type type = Type.get(field);
        return LambdaBuilder.buildGetterSetterTuple(type.getter, type.setter, field);
    }

    public GETTER getGetter() {
        return getter;
    }

    public SETTER getSetter() {
        return setter;
    }

    public Field getField() {
        return field;
    }

    public Type getType() {
        return type;
    }
}

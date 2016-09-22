package uk.co.mysterymayhem.gravitymod.util.reflection;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;

/**
 * Created by Mysteryem on 2016-09-05.
 */
public final class LookupThief {
    public static final LookupThief INSTANCE = new LookupThief();
    private final MethodHandle lookupConstructor;


    private LookupThief() {
        try {
            Constructor<MethodHandles.Lookup> declaredConstructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class);
            declaredConstructor.setAccessible(true);
            this.lookupConstructor = MethodHandles.lookup().unreflectConstructor(declaredConstructor);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public MethodHandles.Lookup lookup(Class<?> clazz) {
        try {
            return (MethodHandles.Lookup) this.lookupConstructor.invokeExact(clazz);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }
}

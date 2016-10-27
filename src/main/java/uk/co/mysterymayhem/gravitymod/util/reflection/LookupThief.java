package uk.co.mysterymayhem.gravitymod.util.reflection;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;

/**
 * Used during development or to access methods that could be overridden, without altering their access with an AT.
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

    /**
     * Get a Lookup object with the same access as the class argument (as if the Lookup object had been created inside
     * of the class in question).<br>
     * Note that some classes are not valid, such as java.lang.*<br>
     * This Lookup can create method handles for any fields/methods accessible from inside the class in question,
     * including calls to super methods, e.g. super.toString()
     * @param clazz Class whose access the Lookup object will inherit.
     * @return A Lookup object as if created by MethodHandles.lookup() from inside clazz.
     */
    public MethodHandles.Lookup lookup(Class<?> clazz) {
        try {
            return (MethodHandles.Lookup) this.lookupConstructor.invokeExact(clazz);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }
}

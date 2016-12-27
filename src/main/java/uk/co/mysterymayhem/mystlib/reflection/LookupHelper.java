package uk.co.mysterymayhem.mystlib.reflection;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

/**
 * Used during development or to access methods that could be overridden, without altering their access with an AT.
 * Created by Mysteryem on 2016-09-05.
 */
public final class LookupHelper {
    private static final LookupHelper INSTANCE = new LookupHelper();
    private static final MethodHandles.Lookup TRUSTED_LOOKUP;

    static {
        try {
            Field IMPL_LOOKUP = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            IMPL_LOOKUP.setAccessible(true);
            TRUSTED_LOOKUP = (MethodHandles.Lookup) IMPL_LOOKUP.get(null);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private final MethodHandle lookupConstructor;


    private LookupHelper() {
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
     * Note that some classes are not valid, such as java.*<br>
     * This Lookup can create method handles for any fields/methods accessible from inside the class in question,
     * including calls to super methods, e.g. super.toString()
     * @param clazz Class whose access the Lookup object will inherit.
     * @return A Lookup object as if created by MethodHandles.lookup() from inside clazz.
     */
    public static MethodHandles.Lookup createLookupInClass(Class<?> clazz) {
        try {
            return (MethodHandles.Lookup) INSTANCE.lookupConstructor.invokeExact(clazz);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    /**
     * Get a the Lookup object that is trusted and can access
     * @return
     */
    public static MethodHandles.Lookup getTrustedLookup() {
        return TRUSTED_LOOKUP;
    }
}

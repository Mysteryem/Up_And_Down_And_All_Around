package uk.co.mysterymayhem.gravitymod.asm.util.obfuscation;

/**
 * Created by Mysteryem on 2016-09-21.
 */
public interface IDeobfAware {
    default boolean is(String input) {
        return this.toString().equals(input);
    }

    default boolean isRuntimeDeobfEnabled() {
        return ObfuscationHelper.IS_DEV_ENVIRONMENT;
    }
}

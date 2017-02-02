package uk.co.mysterymayhem.gravitymod.asm.util.obfuscation;

/**
 * Created by Mysteryem on 2016-09-21.
 */
public interface IDeobfAware {

    default boolean isDeobfEnvironment() {
        return ObfuscationHelper.IS_DEV_ENVIRONMENT;
    }
}

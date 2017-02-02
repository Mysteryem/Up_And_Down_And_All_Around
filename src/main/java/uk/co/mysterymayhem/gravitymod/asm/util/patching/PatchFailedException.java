package uk.co.mysterymayhem.gravitymod.asm.util.patching;

/**
 * Created by Mysteryem on 2017-01-30.
 */
public class PatchFailedException extends RuntimeException {
    public PatchFailedException() {
        super();
    }

    public PatchFailedException(String message) {
        super(message);
    }

    public PatchFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public PatchFailedException(Throwable cause) {
        super(cause);
    }

    protected PatchFailedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

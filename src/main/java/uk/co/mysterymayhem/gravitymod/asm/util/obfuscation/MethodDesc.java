package uk.co.mysterymayhem.gravitymod.asm.util.obfuscation;

/**
 * Created by Mysteryem on 2017-01-30.
 */
public class MethodDesc extends DeobfAwareString {
    public MethodDesc(IClassProxy returnType, IClassProxy... paremeterTypes) {
        super(buildDesc(returnType, paremeterTypes));
    }

    private static String buildDesc(IClassProxy returnType, IClassProxy... paremeterTypes) {
        StringBuilder builder = new StringBuilder();
        builder.append('(');
        for (IClassProxy parameterType : paremeterTypes) {
            builder.append(parameterType.asDescriptor());
        }
        builder.append(')').append(returnType.asDescriptor());
        return builder.toString();
    }
}

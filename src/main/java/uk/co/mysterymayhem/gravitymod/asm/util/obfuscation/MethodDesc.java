package uk.co.mysterymayhem.gravitymod.asm.util.obfuscation;

import uk.co.mysterymayhem.gravitymod.asm.util.obfuscation.names.DeobfAwareString;

/**
 * Created by Mysteryem on 2017-01-30.
 */
public class MethodDesc extends DeobfAwareString {
    public MethodDesc(IClassName returnType, IClassName... paremeterTypes) {
        super(buildDesc(returnType, paremeterTypes));
    }

    private static String buildDesc(IClassName returnType, IClassName... paremeterTypes) {
        StringBuilder builder = new StringBuilder();
        builder.append('(');
        for (IClassName parameterType : paremeterTypes) {
            builder.append(parameterType.asDescriptor());
        }
        builder.append(')').append(returnType.asDescriptor());
        return builder.toString();
    }
}

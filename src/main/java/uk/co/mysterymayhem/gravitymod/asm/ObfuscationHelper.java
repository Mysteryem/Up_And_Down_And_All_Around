package uk.co.mysterymayhem.gravitymod.asm;

import net.minecraft.launchwrapper.Launch;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.ListIterator;
import java.util.Objects;

/**
 * Created by Mysteryem on 2016-09-21.
 */
public class ObfuscationHelper {
    public static final boolean IS_DEV_ENVIRONMENT = (Boolean)Launch.blackboard.get("fml.deobfuscatedEnvironment");

    public static final IDeobfAware INIT = new DeobfAwareString("<init>");
    public static final IClassProxy VOID = new PrimitiveClassName("V");
    public static final IClassProxy CHAR = new PrimitiveClassName("C");
    public static final IClassProxy DOUBLE = new PrimitiveClassName("D");
    public static final IClassProxy FLOAT = new PrimitiveClassName("F");
    public static final IClassProxy INT = new PrimitiveClassName("I");
    public static final IClassProxy LONG = new PrimitiveClassName("J");
    public static final IClassProxy SHORT = new PrimitiveClassName("S");
    public static final IClassProxy BOOLEAN = new PrimitiveClassName("Z");


    public static class FieldInstruction extends FieldInsnNode implements IDeobfAwareInstruction {

        public FieldInstruction(int opcode, UnqualifiedName ownerAndName, IClassProxy type) {
            super(opcode, ownerAndName.owner, ownerAndName.name, type.asDescriptor());
        }

        public FieldInstruction(int opcode, IClassProxy owner, IDeobfAware name, IClassProxy type) {
            super(opcode, owner.toString(), name.toString(), type.asDescriptor());
        }

        @Override
        public boolean is(AbstractInsnNode abstractInsnNode) {
            if (abstractInsnNode instanceof FieldInsnNode) {
                FieldInsnNode fieldInsnNode = (FieldInsnNode)abstractInsnNode;
                return this.is(fieldInsnNode);
            }
            return false;
        }

        public boolean is(FieldInsnNode fieldInsnNode) {
            return fieldInsnNode.getOpcode() == this.opcode
                    && fieldInsnNode.owner.equals(this.owner)
                    && fieldInsnNode.name.equals(this.name)
                    && fieldInsnNode.desc.equals(this.desc);
        }

        @Override
        public AbstractInsnNode asAbstract() {
            return this.clone(null);
        }

        @Override
        public String toString() {
            return "" + this.getOpcode() + " " + this.owner + "." + this.name + " " + this.desc;
        }

        public void replace(FieldInsnNode other) {
            other.setOpcode(this.getOpcode());
            other.owner = this.owner;
            other.name = this.name;
            other.desc = this.desc;
        }
    }

    public static class HooksMethodInstruction extends MethodInstruction {
        private static final ObjectClassName Hooks = new ObjectClassName("uk/co/mysterymayhem/gravitymod/asm/Hooks");

        public HooksMethodInstruction(IDeobfAware methodName, MethodDesc description) {
            super(Opcodes.INVOKESTATIC, Hooks, methodName, description);
        }

        public HooksMethodInstruction(String methodName, MethodDesc description) {
            super(Opcodes.INVOKESTATIC, Hooks, methodName, description);
        }
    }

    public static class MethodInstruction extends MethodInsnNode implements IDeobfAwareInstruction {

        public MethodInstruction(int opcode, UnqualifiedName ownerAndName, MethodDesc description, boolean ownerIsInterface) {
            super(opcode, ownerAndName.owner, ownerAndName.name, description.toString(), ownerIsInterface);
        }

        public MethodInstruction(int opcode, IClassProxy owner, IDeobfAware name, MethodDesc description, boolean ownerIsInterface) {
            super(opcode, owner.toString(), name.toString(), description.toString(), ownerIsInterface);
        }

        public MethodInstruction(int opcode, IClassProxy owner, IDeobfAware name, MethodDesc description) {
            this(opcode, owner, name, description, false);
        }

        public MethodInstruction(int opcode, IClassProxy owner, String name, MethodDesc description, boolean ownerIsInterface) {
            super(opcode, owner.toString(), name, description.toString(), ownerIsInterface);
        }

        public MethodInstruction(int opcode, IClassProxy owner, String name, MethodDesc description) {
            this(opcode, owner, name, description, false);
        }

        public MethodInstruction(int opcode, UnqualifiedName ownerAndName, MethodDesc description) {
            this(opcode, ownerAndName, description, false);
        }

        @Override
        public boolean is(AbstractInsnNode abstractInsnNode) {
            if (abstractInsnNode instanceof MethodInsnNode) {
                MethodInsnNode methodInsnNode = (MethodInsnNode)abstractInsnNode;
                return this.is(methodInsnNode);
            }
            return false;
        }

        public boolean is(MethodInsnNode methodInsnNode) {
            return methodInsnNode.getOpcode() == this.opcode
                    && methodInsnNode.owner.equals(this.owner)
                    && methodInsnNode.name.equals(this.name)
                    && methodInsnNode.desc.equals(this.desc)
                    && methodInsnNode.itf == this.itf;
        }

        @Override
        public AbstractInsnNode asAbstract() {
            return this.clone(null);
        }

        @Override
        public String toString() {
            return "" + this.getOpcode() + " " + this.owner + "." + this.name + " " + this.desc;
        }

        public void replace(MethodInsnNode other) {
            other.setOpcode(this.getOpcode());
            other.owner = this.owner;
            other.name = this.name;
            other.desc = this.desc;
            other.itf = this.itf;
        }
    }

    public interface IDeobfAwareInstruction extends IDeobfAware {
        boolean is(AbstractInsnNode abstractInsnNode);
        AbstractInsnNode asAbstract();
        default void replace(ListIterator<AbstractInsnNode> it) {
            it.remove();
            this.addTo(it);
        }
        default void addTo(ListIterator<AbstractInsnNode> it) {
            it.add(this.asAbstract());
        }
        default void addTo(InsnList insnList) {
            insnList.add(this.asAbstract());
        }
    }

    public static class UnqualifiedName extends DeobfAwareString {
        private final String owner;
        private final String name;

        public UnqualifiedName(IClassProxy owner, String name) {
            super(owner + "." + name);
            this.owner = owner.toString();
            this.name = name;
        }

        public UnqualifiedName(IClassProxy owner, IDeobfAware name) {
            this(owner, name.toString());
        }

        public boolean is(String owner, String name) {
            return this.owner.equals(owner) && this.name.equals(name);
        }
    }

    public static class MethodDesc extends DeobfAwareString {
        private static String buildDesc(IClassProxy returnType, IClassProxy... paremeterTypes) {
            StringBuilder builder = new StringBuilder();
            builder.append('(');
            for (IClassProxy parameterType : paremeterTypes) {
                builder.append(parameterType.asDescriptor());
            }
            builder.append(')').append(returnType.asDescriptor());
            return builder.toString();
        }

        public MethodDesc(IClassProxy returnType, IClassProxy... paremeterTypes) {
            super(buildDesc(returnType, paremeterTypes));
        }
    }

    public static class ArrayClassName extends DeobfAwareString implements IClassProxy {

        public ArrayClassName(IClassProxy componentClass) {
            super(componentClass.toString());
        }

        @Override
        public String asDescriptor() {
            return '[' + this.toString();
        }
    }

    public static class PrimitiveClassName extends DeobfAwareString implements IClassProxy {
        public PrimitiveClassName(String name) {
            super(name);
        }
    }

    public static class ObjectClassName extends DeobfAwareString implements IClassProxy {
        public ObjectClassName(String name) {
            super(name);
        }

        @Override
        public String asDescriptor() {
            return 'L' + this.toString() + ';';
        }
    }

    public static class FieldName extends DeobfAwareString {
        public FieldName(String deobf, String obf) {
            super(deobf, obf);
        }

        public FieldName(String name) {
            super(name);
        }
    }

    public static class MethodName extends DeobfAwareString {

        public MethodName(String deobf, String obf) {
            super(deobf, obf);
        }

        public MethodName(String name) {
            super(name);
        }

        public boolean is(MethodNode methodNode) {
            return this.is(methodNode.name);
        }
    }


    public static class DeobfAwareString implements IDeobfAware {

        private final String value;
        private final String deobf;

        public DeobfAwareString(String deobf, String obf) {
            this.value = ObfuscationHelper.IS_DEV_ENVIRONMENT ? Objects.requireNonNull(deobf) : Objects.requireNonNull(obf);
            this.deobf = Objects.requireNonNull(deobf);
        }

        public DeobfAwareString(String name) {
            this.value = Objects.requireNonNull(name);
            this.deobf = this.value;
        }

        @Override
        public String toString() {
            return this.value;
        }

//        @Override
        public String getDeobf() {
            return this.deobf;
        }
    }

    /**
     * Created by Mysteryem on 2016-09-21.
     */
    public interface IDeobfAware {
        default boolean isRuntimeDeobfEnabled() {
            return ObfuscationHelper.IS_DEV_ENVIRONMENT;
        }

        default boolean is(String input) {
            return this.toString().equals(input);
        }
    }

    public interface IClassProxy {
        default String asDescriptor() {
            return this.toString();
        }

        default ArrayClassName asArray() {
            return new ArrayClassName(this);
        }
    }
}

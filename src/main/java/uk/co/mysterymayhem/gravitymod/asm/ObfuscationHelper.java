package uk.co.mysterymayhem.gravitymod.asm;

import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Mysteryem on 2016-09-21.
 */
public class ObfuscationHelper {
    public static final IClassProxy BOOLEAN = new PrimitiveClassName("Z");
    public static final IClassProxy CHAR = new PrimitiveClassName("C");
    public static final IClassProxy DOUBLE = new PrimitiveClassName("D");
    public static final IClassProxy FLOAT = new PrimitiveClassName("F");
    public static final IDeobfAware INIT = new DeobfAwareString("<init>");
    public static final IClassProxy INT = new PrimitiveClassName("I");
    public static final boolean IS_DEV_ENVIRONMENT = (Boolean)Launch.blackboard.get("fml.deobfuscatedEnvironment");
    public static final IClassProxy LONG = new PrimitiveClassName("J");
    public static final IClassProxy SHORT = new PrimitiveClassName("S");
    public static final IClassProxy VOID = new PrimitiveClassName("V");
    public static final Map<String, String> deobfNameLookup = new HashMap<>();

    static {
        if (IS_DEV_ENVIRONMENT) {
            FMLDeobfuscatingRemapper instance = FMLDeobfuscatingRemapper.INSTANCE;
            try {
                Field rawFieldMapsField = FMLDeobfuscatingRemapper.class.getDeclaredField("rawFieldMaps");
                rawFieldMapsField.setAccessible(true);

                @SuppressWarnings("unchecked")
                Map<String, Map<String, String>> rawFieldMaps = (Map<String, Map<String, String>>)rawFieldMapsField.get(instance);

                Field rawMethodMapsField = FMLDeobfuscatingRemapper.class.getDeclaredField("rawMethodMaps");
                rawMethodMapsField.setAccessible(true);

                @SuppressWarnings("unchecked")
                Map<String, Map<String, String>> rawMethodMaps = (Map<String, Map<String, String>>)rawMethodMapsField.get(instance);

                for (Map<String, String> innerMap : rawFieldMaps.values()) {
                    for (Map.Entry<String, String> entry : innerMap.entrySet()) {
                        String key = entry.getKey();
                        String[] split = key.split(":");
                        if (split.length == 2) {
                            deobfNameLookup.put(split[0], entry.getValue());
                        }
                        else {
                            System.out.println("[UpAndDown] Unknown field mapping \"" + key + "\" -> \"" + entry.getValue() + "\"");
                        }
                    }
                }

                for (Map<String, String> innerMap : rawMethodMaps.values()) {
                    for (Map.Entry<String, String> entry : innerMap.entrySet()) {
                        String key = entry.getKey();
                        String[] split = key.split("\\(");
                        if (split.length == 2) {
                            deobfNameLookup.put(split[0], entry.getValue());
                        }
                        else {
                            System.out.println("[UpAndDown] Unknown method mapping \"" + key + "\" -> \"" + entry.getValue() + "\"");
                        }
                    }
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }

//            System.out.println("Map build complete");
        }
    }


    public interface IClassProxy {
        default ArrayClassName asArray() {
            return new ArrayClassName(this);
        }

        default String asDescriptor() {
            return this.toString();
        }
    }

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

    public interface IDeobfAwareInstruction extends IDeobfAware {
        boolean is(AbstractInsnNode abstractInsnNode);

        default void addTo(InsnList insnList) {
            insnList.add(this.asAbstract());
        }

        AbstractInsnNode asAbstract();

        default void replace(ListIterator<AbstractInsnNode> it) {
            it.remove();
            this.addTo(it);
        }

        default void addTo(ListIterator<AbstractInsnNode> it) {
            it.add(this.asAbstract());
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

    public static class DeobfAwareString implements IDeobfAware {

        private final String deobf;
        private final String value;

        public DeobfAwareString(String deobf, String obf) {
            this.value = ObfuscationHelper.IS_DEV_ENVIRONMENT ? this.getDeobfName(Objects.requireNonNull(obf)) : Objects.requireNonNull(obf);
            this.deobf = Objects.requireNonNull(deobf);
        }

        private String getDeobfName(String obf) {
            String toReturn = deobfNameLookup.get(Objects.requireNonNull(obf));
            if (toReturn == null) {
                System.out.println("[UpAndDown] [WARNING] Could not find mcp name for srg name \"" + obf + "\"");
                toReturn = obf;
            }
            return toReturn;
        }

        public DeobfAwareString(String name) {
            this.value = Objects.requireNonNull(name);
            this.deobf = this.value;
        }

        //        @Override
        public String getDeobf() {
            return this.deobf;
        }

        @Override
        public String toString() {
            return this.value;
        }


    }

    public static class FieldInsn extends FieldInsnNode implements IDeobfAwareInstruction {

        public FieldInsn(int opcode, UnqualifiedName ownerAndName, IClassProxy type) {
            super(opcode, ownerAndName.owner, ownerAndName.name, type.asDescriptor());
        }

        public FieldInsn(int opcode, IClassProxy owner, IDeobfAware name, IClassProxy type) {
            super(opcode, owner.toString(), name.toString(), type.asDescriptor());
        }

        @Override
        public AbstractInsnNode asAbstract() {
            return this.clone(null);
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

        public void replace(FieldInsnNode other) {
            other.setOpcode(this.getOpcode());
            other.owner = this.owner;
            other.name = this.name;
            other.desc = this.desc;
        }

        @Override
        public String toString() {
            return "" + this.getOpcode() + " " + this.owner + "." + this.name + " " + this.desc;
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

    public static class HooksInsn extends MethodInsn {
        private static final ObjectCName Hooks = new ObjectCName("uk/co/mysterymayhem/gravitymod/asm/Hooks");

        public HooksInsn(IDeobfAware methodName, MethodDesc description) {
            super(Opcodes.INVOKESTATIC, Hooks, methodName, description);
        }

        public HooksInsn(String methodName, MethodDesc description) {
            super(Opcodes.INVOKESTATIC, Hooks, methodName, description);
        }
    }

    public static class MethodDesc extends DeobfAwareString {
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

    public static class MethodInsn extends MethodInsnNode implements IDeobfAwareInstruction {

        public MethodInsn(int opcode, IClassProxy owner, IDeobfAware name, MethodDesc description) {
            this(opcode, owner, name, description, false);
        }

        public MethodInsn(int opcode, IClassProxy owner, IDeobfAware name, MethodDesc description, boolean ownerIsInterface) {
            super(opcode, owner.toString(), name.toString(), description.toString(), ownerIsInterface);
        }

        public MethodInsn(int opcode, IClassProxy owner, String name, MethodDesc description) {
            this(opcode, owner, name, description, false);
        }

        public MethodInsn(int opcode, IClassProxy owner, String name, MethodDesc description, boolean ownerIsInterface) {
            super(opcode, owner.toString(), name, description.toString(), ownerIsInterface);
        }

        public MethodInsn(int opcode, UnqualifiedName ownerAndName, MethodDesc description) {
            this(opcode, ownerAndName, description, false);
        }

        public MethodInsn(int opcode, UnqualifiedName ownerAndName, MethodDesc description, boolean ownerIsInterface) {
            super(opcode, ownerAndName.owner, ownerAndName.name, description.toString(), ownerIsInterface);
        }

        @Override
        public AbstractInsnNode asAbstract() {
            return this.clone(null);
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

        public void replace(MethodInsnNode other) {
            other.setOpcode(this.getOpcode());
            other.owner = this.owner;
            other.name = this.name;
            other.desc = this.desc;
            other.itf = this.itf;
        }

        @Override
        public String toString() {
            return "" + this.getOpcode() + " " + this.owner + "." + this.name + " " + this.desc;
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

    public static class ObjectCName extends DeobfAwareString implements IClassProxy {
        public ObjectCName(String name) {
            super(name);
        }

        @Override
        public String asDescriptor() {
            return 'L' + this.toString() + ';';
        }
    }

    public static class PrimitiveClassName extends DeobfAwareString implements IClassProxy {
        public PrimitiveClassName(String name) {
            super(name);
        }
    }

    public static class UnqualifiedName extends DeobfAwareString {
        private final String name;
        private final String owner;

        public UnqualifiedName(IClassProxy owner, IDeobfAware name) {
            this(owner, name.toString());
        }

        public UnqualifiedName(IClassProxy owner, String name) {
            super(owner + "." + name);
            this.owner = owner.toString();
            this.name = name;
        }

        public boolean is(String owner, String name) {
            return this.owner.equals(owner) && this.name.equals(name);
        }
    }
}

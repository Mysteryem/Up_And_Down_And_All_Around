package uk.co.mysterymayhem.gravitymod.asm.util.obfuscation.instructions;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import uk.co.mysterymayhem.gravitymod.asm.util.obfuscation.*;
import uk.co.mysterymayhem.gravitymod.asm.util.obfuscation.names.UnqualifiedName;

/**
 * Created by Mysteryem on 2017-01-30.
 */
public class MethodInsn extends MethodInsnNode implements IDeobfAwareInstruction {

    public MethodInsn(int opcode, IClassName owner, IDeobfAware name, MethodDesc description) {
        this(opcode, owner, name, description, false);
    }

    public MethodInsn(int opcode, IClassName owner, IDeobfAware name, MethodDesc description, boolean ownerIsInterface) {
        super(opcode, owner.toString(), name.toString(), description.toString(), ownerIsInterface);
    }

    public MethodInsn(int opcode, IClassName owner, String name, MethodDesc description) {
        this(opcode, owner, name, description, false);
    }

    public MethodInsn(int opcode, IClassName owner, String name, MethodDesc description, boolean ownerIsInterface) {
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

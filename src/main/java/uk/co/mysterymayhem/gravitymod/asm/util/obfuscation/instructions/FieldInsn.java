package uk.co.mysterymayhem.gravitymod.asm.util.obfuscation.instructions;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import uk.co.mysterymayhem.gravitymod.asm.util.obfuscation.IClassName;
import uk.co.mysterymayhem.gravitymod.asm.util.obfuscation.IDeobfAware;
import uk.co.mysterymayhem.gravitymod.asm.util.obfuscation.names.UnqualifiedName;

/**
 * Created by Mysteryem on 2017-01-30.
 */
public class FieldInsn extends FieldInsnNode implements IDeobfAwareInstruction {

    public FieldInsn(int opcode, UnqualifiedName ownerAndName, IClassName type) {
        super(opcode, ownerAndName.owner, ownerAndName.name, type.asDescriptor());
    }

    public FieldInsn(int opcode, IClassName owner, IDeobfAware name, IClassName type) {
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

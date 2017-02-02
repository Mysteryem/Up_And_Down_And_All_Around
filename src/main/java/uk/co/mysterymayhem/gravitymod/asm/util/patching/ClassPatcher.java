package uk.co.mysterymayhem.gravitymod.asm.util.patching;

import com.google.common.collect.Lists;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import uk.co.mysterymayhem.gravitymod.asm.Transformer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Created by Mysteryem on 2017-01-30.
 */
public abstract class ClassPatcher implements Function<byte[], byte[]> {

    private final ArrayList<MethodPatcher> methodPatches;
    private final String className;
    // For passing around data between patches
    private final HashMap<String, Object> dataStore = new HashMap<>();
    private final int readerMode;
    private final int writerMode;
    private int numMethodPatches = 0;
    private int numMethodPatchesAttempted = 0;

    public ClassPatcher(String className, MethodPatcher... methodPatches) {
        this(className, 0, 0, methodPatches);
    }

    public ClassPatcher(String className, int readerMode, int writerMode, MethodPatcher... methodPatches) {
        this.methodPatches = Lists.newArrayList(methodPatches);
        this.numMethodPatches = this.methodPatches.size();
        this.className = className;
        this.readerMode = readerMode;
        this.writerMode = writerMode;
    }

    public String getClassName() {
        return this.className;
    }

    @Override
    public byte[] apply(byte[] bytes) {
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, this.readerMode);

        this.patchClass(classNode);

        if (this.numMethodPatchesAttempted != this.numMethodPatches) {
            Transformer.die("Failed to find all the methods to patch in " + classNode.name +
                    ". The Minecraft/Forge version you are using likely does not match what the mod requires.");
        }

        ClassWriter classWriter = new ClassWriter(this.writerMode);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }

    protected void patchClass(ClassNode classNode) {
        this.methodPatches.forEach(MethodPatcher::setup);

        outerloop:
        for (MethodNode methodNode : classNode.methods) {
            for (ListIterator<MethodPatcher> iterator = this.methodPatches.listIterator(); iterator.hasNext(); /**/) {
                MethodPatcher next = iterator.next();
                if (next.shouldPatchMethod(methodNode)) {
                    this.numMethodPatchesAttempted++;
                    iterator.remove();
                    try {
                        Transformer.logPatchStarting(methodNode.name);
                        next.patchMethod(methodNode);
                        Transformer.logPatchComplete(methodNode.name);
                    } catch (Exception t) {
                        Transformer.die("Exception thrown by '" + next + "' during patching of '" + this.className + "::" + methodNode.name + "'", t);
                    }
                    if (this.methodPatches.isEmpty()) {
                        // There are no method patches left, so we skip any remaining methods
                        break outerloop;
                    }
                }
            }
        }

        // Cleanup
        this.dataStore.clear();
    }

    protected Object storeData(String key, Object data) {
        return this.dataStore.put(key, data);
    }

    protected Object getData(String key) {
        return this.dataStore.get(key);
    }

    protected MethodPatcher addMethodPatch(Predicate<MethodNode> shouldPatch, BiPredicate<AbstractInsnNode, ListIterator<AbstractInsnNode>> insnPatch) {
        MethodPatcher methodPatcher = this.addMethodPatch(shouldPatch);
        methodPatcher.addInsnPatch(insnPatch);
        return methodPatcher;
    }

    protected MethodPatcher addMethodPatch(Predicate<MethodNode> shouldPatch) {
        return this.addMethodPatch(MethodPatcher.create(shouldPatch));
    }

    protected MethodPatcher addMethodPatch(MethodPatcher methodPatcher) {
        this.methodPatches.add(methodPatcher);
        this.numMethodPatches++;
        return methodPatcher;
    }

    protected MethodPatcher addMethodPatch(Predicate<MethodNode> shouldPatch, Consumer<MethodNode> entireMethodPatch) {
        return this.addMethodPatch(MethodPatcher.create(shouldPatch, entireMethodPatch));
    }
}

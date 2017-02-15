package uk.co.mysterymayhem.gravitymod.asm.util.patching;

import org.objectweb.asm.tree.AbstractInsnNode;

import java.util.HashSet;
import java.util.ListIterator;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

/**
 * Created by Mysteryem on 2017-01-31.
 */
public abstract class InsnPatcher implements BiPredicate<AbstractInsnNode, ListIterator<AbstractInsnNode>> {

    protected final MethodPatcher methodPatcher;
    // Patches that this patch is waiting on to complete before becoming active
    private final HashSet<InsnPatcher> parentPatches = new HashSet<>();
    // Patches that are waiting for this patch to complete
    private final HashSet<InsnPatcher> childPatches = new HashSet<>();

    private boolean setupComplete = false;
    // Set to true in tryPatch method once all patching is complete
    private boolean patchComplete = false;

    public InsnPatcher(MethodPatcher methodPatcher) {
        this.methodPatcher = methodPatcher;
    }

    public static InsnPatcher create(MethodPatcher owner, BiPredicate<AbstractInsnNode, ListIterator<AbstractInsnNode>> patchMethod) {
        return new InsnPatcherImpl(owner, patchMethod);
    }

    public abstract InsnPatcher copy();

    public void setup() {
        this.setupComplete = true;
    }

    public boolean isSetupComplete() {
        return this.setupComplete;
    }

    public boolean hasNoParents() {
        return this.parentPatches.isEmpty();
    }

    public void forEachChild(Consumer<? super InsnPatcher> action) {
        this.childPatches.forEach(action);
    }

    public void forEachParent(Consumer<? super InsnPatcher> action) {
        this.parentPatches.forEach(action);
    }

    public InsnPatcher addParentPatch(InsnPatcher... others) {
        if (this.setupComplete) {
            throw new RuntimeException();
        }
        for (InsnPatcher other : others) {
            boolean wasAdded = this.parentPatches.add(other);
            if (wasAdded) {
                other.addChildPatch(this);
            }
        }
        return this;
    }

    public InsnPatcher addChildPatch(InsnPatcher... others) {
        if (this.setupComplete) {
            throw new RuntimeException();
        }
        for (InsnPatcher other : others) {
            boolean wasAdded = this.childPatches.add(other);
            if (wasAdded) {
                other.addParentPatch(this);
            }
        }
        return this;
    }

    @Override
    public boolean test(AbstractInsnNode abstractInsnNode, ListIterator<AbstractInsnNode> abstractInsnNodeListIterator) {
        return this.tryPatch(abstractInsnNode, abstractInsnNodeListIterator);
    }

    /**
     * @param abstractInsnNode
     * @param abstractInsnNodeListIterator
     * @return True if patching was attempted and thus should be removed from the MethodPatcher
     */
    public abstract boolean tryPatch(AbstractInsnNode abstractInsnNode, ListIterator<AbstractInsnNode> abstractInsnNodeListIterator);

    void onPatchCompleted() {
        this.childPatches.forEach(child -> child.onParentCompleted(this));
    }

    void onParentCompleted(InsnPatcher parent) {
        if (!this.parentPatches.remove(parent)) {
            throw new RuntimeException("Tried to remove parent patcher that isn't a parent");
        }
        if (this.parentPatches.isEmpty()) {
            this.methodPatcher.onPatchActivate(this);
        }
    }

    public static void sequentialOrder(InsnPatcher... patchers) {
        if (patchers.length >= 2) {
            InsnPatcher previous = patchers[0];
            for (int i = 1; i < patchers.length; i++) {
                InsnPatcher nextPatcher = patchers[i];
                previous.addChildPatch(nextPatcher);
                previous = nextPatcher;
            }
        }
    }

    public static class Factory {
        private final MethodPatcher owner;

        public Factory(MethodPatcher owner) {
            this.owner = owner;
        }

        public InsnPatcher createPatcher(BiPredicate<AbstractInsnNode, ListIterator<AbstractInsnNode>> patchMethod) {
            return new InsnPatcherImpl(this.owner, patchMethod);
        }
    }

    private static class InsnPatcherImpl extends InsnPatcher {

        private final BiPredicate<AbstractInsnNode, ListIterator<AbstractInsnNode>> patchMethod;

        public InsnPatcherImpl(MethodPatcher methodPatcher, BiPredicate<AbstractInsnNode, ListIterator<AbstractInsnNode>> patchMethod) {
            super(methodPatcher);
            this.patchMethod = patchMethod;
        }

        @Override
        public boolean tryPatch(AbstractInsnNode abstractInsnNode, ListIterator<AbstractInsnNode> abstractInsnNodeListIterator) {
            return this.patchMethod.test(abstractInsnNode, abstractInsnNodeListIterator);
        }

        @Override
        public InsnPatcher copy() {
            if (this.isSetupComplete()) {
                throw new RuntimeException("Cannot copy an InsnPatcher that has already had its setup run");
            }
            InsnPatcherImpl copy = new InsnPatcherImpl(this.methodPatcher, this.patchMethod);
            this.forEachChild(copy::addChildPatch);
            this.forEachParent(copy::addParentPatch);
            return copy;
        }
    }
}

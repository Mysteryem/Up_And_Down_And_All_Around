package uk.co.mysterymayhem.gravitymod.asm.util.patching;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;
import uk.co.mysterymayhem.gravitymod.asm.Transformer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Created by Mysteryem on 2017-01-30.
 */
public abstract class MethodPatcher implements Predicate<MethodNode> {

    // Patches to iterate through
    protected final HashSet<InsnPatcher> activePatches = new HashSet<>();
    // Patches that are waiting on other patches to be completed before they become active
    protected final HashSet<InsnPatcher> waitingPatches = new HashSet<>();
    private boolean setupComplete = false;

    public MethodPatcher(InsnPatcher... patchers) {
        this.waitingPatches.addAll(Arrays.asList(patchers));
    }

    public static MethodPatcher create(Predicate<MethodNode> shouldPatch, Consumer<MethodNode> entireMethodPatcher) {
        return new MethodPatcherEntireMethodImpl(shouldPatch, entireMethodPatcher);
    }

    public static MethodPatcher create(Predicate<MethodNode> shouldPatch, BiPredicate<AbstractInsnNode, ListIterator<AbstractInsnNode>> patchMethod) {
        MethodPatcher methodPatcher = create(shouldPatch);
        methodPatcher.addInsnPatch(patchMethod);
        return methodPatcher;
    }

    public static MethodPatcher create(Predicate<MethodNode> shouldPatch) {
        return new MethodPatcherIterativeImpl(shouldPatch);
    }

    public InsnPatcher addInsnPatch(Predicate<AbstractInsnNode> patchMethod) {
        return this.addInsnPatch((node, iterator) -> patchMethod.test(node));
    }

    public InsnPatcher addInsnPatch(BiPredicate<AbstractInsnNode, ListIterator<AbstractInsnNode>> patchMethod) {
        InsnPatcher patcher = InsnPatcher.create(this, patchMethod);
        return this.addInsnPatch(patcher);
    }

    public InsnPatcher addInsnPatch(InsnPatcher patcher) {
        if (setupComplete) {
            Transformer.die("Attempted to add an InsnPatcher to a MethodPatcher that has already completed its setup");
            throw new RuntimeException();
        }
        this.waitingPatches.add(patcher);
        return patcher;
    }

    @Override
    public boolean test(MethodNode methodNode) {
        return this.shouldPatchMethod(methodNode);
    }

    protected abstract boolean shouldPatchMethod(MethodNode methodNode);

    public void setup() {
        this.ensureNoDependencyCyclesOrOrphans();
        this.setupComplete = true;
    }

    public void onPatchActivate(InsnPatcher patcher) {
        this.waitingPatches.remove(patcher);
        this.activePatches.add(patcher);
    }

//    // Move parentless patches into activePatches set
//    private void activateParentlessPatches() {
//        for(Iterator<InsnPatcher> iterator = waitingPatches.iterator(); iterator.hasNext(); /**/) {
//            InsnPatcher next = iterator.next();
//            if (next.hasNoParents()) {
//                iterator.remove();
//                activePatches.add(next);
//            }
//        }
//    }

    protected void patchMethod(MethodNode methodNode) {
        for (ListIterator<AbstractInsnNode> insnIterator = methodNode.instructions.iterator(); insnIterator.hasNext(); /**/) {
            AbstractInsnNode currentNode = insnIterator.next();

            for (Iterator<InsnPatcher> patchIterator = this.activePatches.iterator(); patchIterator.hasNext(); /**/) {
                InsnPatcher currentPatch = patchIterator.next();

                boolean patchCompleted = currentPatch.tryPatch(currentNode, insnIterator);

                if (patchCompleted) {
                    // Must be done first as active patches may get modified
                    patchIterator.remove();
                    currentPatch.onPatchCompleted();
                    // Without this, any child patch that needs to be applied to the very next instruction will not work, as we would end up skipping an
                    // instruction
                    insnIterator.previous();
                    // Active patches may be modified now, so we must end the loop
                    break;
                }
            }
        }
    }

    // Ensure there are no cycles in the dependencies
    // Ensure there are no unknown dependencies
    private void ensureNoDependencyCyclesOrOrphans() {
        HashSet<InsnPatcher> completelyDiscoveredNodes = new HashSet<>();
        HashSet<InsnPatcher> temporaryDiscoveredNodes = new HashSet<>();
        HashSet<InsnPatcher> rootNodes = new HashSet<>();
        HashSet<InsnPatcher> undiscoveredNodes = new HashSet<>(this.waitingPatches);

        while (!undiscoveredNodes.isEmpty()) {
            InsnPatcher undiscoveredNode = undiscoveredNodes.iterator().next();
            this.visitNode(undiscoveredNode, completelyDiscoveredNodes, temporaryDiscoveredNodes, rootNodes, undiscoveredNodes);
        }

        if (!completelyDiscoveredNodes.equals(this.waitingPatches)) {
            completelyDiscoveredNodes.removeAll(this.waitingPatches);
            Transformer.die("Unknown dependencies discovered in MethodPatcher: " + completelyDiscoveredNodes + ", these are referenced as children/parents of" +
                    " some known about InsnPatcher");
            throw new RuntimeException("Unknown dependencies discovered: " + completelyDiscoveredNodes);
        }

        this.activePatches.addAll(rootNodes);
//        TopologicalSort.DirectedGraph<InsnPatcher> graph = new TopologicalSort.DirectedGraph<>();
//        for (Iterator<InsnPatcher> iterator = this.waitingPatches.iterator(); iterator.hasNext(); /**/) {
//            InsnPatcher next = iterator.next();
//            graph.addNode(next);
//            if (next.hasNoParents()) {
//                iterator.remove();
//                this.activePatches.add(next);
//            }
//        }
//        graph.forEach(node -> {
//            node.forEachParent(parent -> graph.addEdge(parent, node));
//            node.forEachChild(child -> graph.addEdge(node, child));
//        });
//
//        try {
//            TopologicalSort.topologicalSort(graph);
//        } catch (ModSortingException ex) {
//            throw new RuntimeException(ex);
//        }

//        this.activePatches.forEach(parentlessPatch -> {
//            currentFoundChildren.clear();
//            this.discoverDependencies(discoveredParents, previouslyFoundChildren, currentFoundChildren, parentlessPatch);
//            previouslyFoundChildren.addAll(currentFoundChildren);
//            discoveredParents.remove(parentlessPatch);
//        });
//
////        // A child could have every parentlessPatch as its parents, in which case, it would discover some of the parentlessPatches
////        // But parentlessPatches are not in the waitingPatches and they're already accounted for
////        discoveredParents.removeAll(this.activePatches);
//
//        if (!this.waitingPatches.containsAll(discoveredParents)) {
//            throw new RuntimeException("Unknown parents discovered");
//        }
//
//        if (!this.waitingPatches.equals(previouslyFoundChildren)) {
//            throw new RuntimeException("Orphans detected");
//        }
    }

    private void visitNode(InsnPatcher node, HashSet<InsnPatcher> completelyDiscovered, HashSet<InsnPatcher> temporaryDiscovered,
            HashSet<InsnPatcher> rootNodes, HashSet<InsnPatcher> undiscoveredNodes) {
        if (temporaryDiscovered.contains(node)) {
            throw new RuntimeException("Cycle detected");
        }
        else if (!completelyDiscovered.contains(node)) {
            node.setup();
            if (node.hasNoParents()) {
                rootNodes.add(node);
            }
            temporaryDiscovered.add(node);
            node.forEachChild(child -> this.visitNode(child, completelyDiscovered, temporaryDiscovered, rootNodes, undiscoveredNodes));
            temporaryDiscovered.remove(node);
            completelyDiscovered.add(node);
            undiscoveredNodes.remove(node);
        }

    }

//    private void discoverGraph(HashSet<InsnPatcher> rootNodes, HashSet<InsnPatcher> discoveredNodes) {
//        InsnPatcher startNodeForDiscovery = this.waitingPatches.iterator().next();
//    }
//
//    private void discoverDependencies(HashSet<InsnPatcher> discoveredParents, HashSet<InsnPatcher> previouslyFoundChildren, HashSet<InsnPatcher>
//            currentFoundChildren, InsnPatcher parent) {
//        parent.forEachChild(child -> {
//            if (!previouslyFoundChildren.contains(child)) {
//                child.forEachParent(otherParent -> {
//                    if (otherParent != parent) {
//                        discoveredParents.add(otherParent);
//                    }
//                });
//
//                boolean added = currentFoundChildren.add(child);
//                if (!added) {
//                    throw new RuntimeException("Cycle detected");
//                }
//                // Recursive call
//                this.discoverDependencies(discoveredParents, previouslyFoundChildren, currentFoundChildren, child);
//            }
//            else {
//                // We've already found this as a child of a different parent and know it's fine
//            }
//        });
//    }

    private static class MethodPatcherIterativeImpl extends MethodPatcher {

        private final Predicate<MethodNode> shouldPatch;

        public MethodPatcherIterativeImpl(Predicate<MethodNode> shouldPatch) {
            this.shouldPatch = shouldPatch;
        }

        @Override
        protected boolean shouldPatchMethod(MethodNode methodNode) {
            return this.shouldPatch.test(methodNode);
        }
    }

    private static class MethodPatcherEntireMethodImpl extends MethodPatcherIterativeImpl {

        private final Consumer<MethodNode> entireMethodPatch;

        public MethodPatcherEntireMethodImpl(Predicate<MethodNode> shouldPatch, Consumer<MethodNode> entireMethodPatch) {
            super(shouldPatch);
            this.entireMethodPatch = entireMethodPatch;
        }

        @Override
        public InsnPatcher addInsnPatch(BiPredicate<AbstractInsnNode, ListIterator<AbstractInsnNode>> patchMethod) {
            throw new UnsupportedOperationException("Can't add patches to this MethodPatcher");
        }

        @Override
        public InsnPatcher addInsnPatch(InsnPatcher patcher) {
            throw new UnsupportedOperationException("Can't add patches to this MethodPatcher");
        }

        @Override
        protected void patchMethod(MethodNode methodNode) {
            this.entireMethodPatch.accept(methodNode);
        }
    }

}

package uk.co.mysterymayhem.gravitymod.util.prepostmodifier;

import gnu.trove.map.hash.TIntObjectHashMap;
import uk.co.mysterymayhem.gravitymod.asm.EntityPlayerWithGravity;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Objects;

/**
 * Created by Mysteryem on 2016-10-25.
 */
public class CombinedPrePostModifier implements IPrePostModifier<EntityPlayerWithGravity> {

    public static final ArrayList<CombinedPrePostModifier> COMBINED_REGISTRY = new ArrayList<>();

    public static void reset() {
        COMBINED_REGISTRY.clear();
        ID = EnumPrePostModifier.values().length;
        combinedModifierMap.clear();
    }

    public enum ProcessingOrder{
        PRE_FIRST_SECOND_POST_FIRST_SECOND,
        PRE_FIRST_SECOND_POST_SECOND_FIRST;

        public static final ProcessingOrder DEFAULT = PRE_FIRST_SECOND_POST_SECOND_FIRST;
    }

    private static int ID = EnumPrePostModifier.values().length;
    private static EnumMap<ProcessingOrder, TIntObjectHashMap<TIntObjectHashMap<CombinedPrePostModifier>>> combinedModifierMap = new EnumMap<>(ProcessingOrder.class);

    public static CombinedPrePostModifier getModifierFor(IPrePostModifier<EntityPlayerWithGravity> first, IPrePostModifier<EntityPlayerWithGravity> second) {
        return getModifierFor(first, second, ProcessingOrder.DEFAULT);
    }

    public static CombinedPrePostModifier getModifierFor(final IPrePostModifier<EntityPlayerWithGravity> first, final IPrePostModifier<EntityPlayerWithGravity> second, final ProcessingOrder processingOrder) {
        Objects.requireNonNull(first);
        Objects.requireNonNull(second);
        Objects.requireNonNull(processingOrder);
        TIntObjectHashMap<TIntObjectHashMap<CombinedPrePostModifier>> firstIDMap = combinedModifierMap.get(processingOrder);
        if (firstIDMap == null) {
            firstIDMap = new TIntObjectHashMap<>();
            combinedModifierMap.put(processingOrder, firstIDMap);
        }

        TIntObjectHashMap<CombinedPrePostModifier> secondIDMap = firstIDMap.get(first.getUniqueID());
        if (secondIDMap == null) {
            secondIDMap = new TIntObjectHashMap<>();
            firstIDMap.put(first.getUniqueID(), secondIDMap);
        }

        CombinedPrePostModifier combinedPrePostModifier = secondIDMap.get(second.getUniqueID());
        if (combinedPrePostModifier != null) {
            return combinedPrePostModifier;
        }
        else {
            combinedPrePostModifier = new CombinedPrePostModifier(first, second, processingOrder);
            secondIDMap.put(second.getUniqueID(), combinedPrePostModifier);
            COMBINED_REGISTRY.add(combinedPrePostModifier);
            return combinedPrePostModifier;
        }
    }

    private final int id = ID++;
    private final ProcessingOrder processingOrder;
    private final IPrePostModifier<EntityPlayerWithGravity> first;
    private final IPrePostModifier<EntityPlayerWithGravity> second;

    private CombinedPrePostModifier(IPrePostModifier<EntityPlayerWithGravity> first, IPrePostModifier<EntityPlayerWithGravity> second, ProcessingOrder processingOrder) {
        this.first = first;
        this.second = second;
        this.processingOrder = processingOrder;
    }

    @Override
    public void preModify(EntityPlayerWithGravity thing) {
        first.preModify(thing);
        second.preModify(thing);
    }

    @Override
    public void postModify(EntityPlayerWithGravity thing) {
        switch (this.processingOrder){
            case PRE_FIRST_SECOND_POST_FIRST_SECOND:
                first.postModify(thing);
                second.postModify(thing);
                break;
            case PRE_FIRST_SECOND_POST_SECOND_FIRST:
                second.postModify(thing);
                first.postModify(thing);
                break;
        }
    }

    @Override
    public int getUniqueID() {
        return id;
    }

    public ProcessingOrder getProcessingOrder() {
        return processingOrder;
    }

    public IPrePostModifier<EntityPlayerWithGravity> getFirst() {
        return first;
    }

    public IPrePostModifier<EntityPlayerWithGravity> getSecond() {
        return second;
    }
}

package uk.co.mysterymayhem.gravitymod.common.modsupport.prepostmodifier;

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

    private static int ID = EnumPrePostModifier.values().length;
    private static EnumMap<ProcessingOrder, TIntObjectHashMap<TIntObjectHashMap<CombinedPrePostModifier>>> combinedModifierMap = new EnumMap<>(ProcessingOrder.class);

    private final IPrePostModifier<EntityPlayerWithGravity> first;
    private final int id = ID++;
    private final ProcessingOrder processingOrder;
    private final IPrePostModifier<EntityPlayerWithGravity> second;

    private CombinedPrePostModifier(IPrePostModifier<EntityPlayerWithGravity> first, IPrePostModifier<EntityPlayerWithGravity> second, ProcessingOrder processingOrder) {
        this.first = first;
        this.second = second;
        this.processingOrder = processingOrder;
    }

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

    public static void reset() {
        COMBINED_REGISTRY.clear();
        ID = EnumPrePostModifier.values().length;
        combinedModifierMap.clear();
    }

    public IPrePostModifier<EntityPlayerWithGravity> getFirst() {
        return this.first;
    }

    public ProcessingOrder getProcessingOrder() {
        return this.processingOrder;
    }

    public IPrePostModifier<EntityPlayerWithGravity> getSecond() {
        return this.second;
    }

    @Override
    public int getUniqueID() {
        return this.id;
    }

    @Override
    public void postModify(EntityPlayerWithGravity thing) {
        switch (this.processingOrder) {
            case PRE_FIRST_SECOND_POST_FIRST_SECOND:
                this.first.postModify(thing);
                this.second.postModify(thing);
                break;
            case PRE_FIRST_SECOND_POST_SECOND_FIRST:
                this.second.postModify(thing);
                this.first.postModify(thing);
                break;
        }
    }

    @Override
    public void preModify(EntityPlayerWithGravity thing) {
        this.first.preModify(thing);
        this.second.preModify(thing);
    }

    public enum ProcessingOrder {
        PRE_FIRST_SECOND_POST_FIRST_SECOND,
        PRE_FIRST_SECOND_POST_SECOND_FIRST;

        public static final ProcessingOrder DEFAULT = PRE_FIRST_SECOND_POST_SECOND_FIRST;
    }
}

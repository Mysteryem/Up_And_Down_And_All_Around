package uk.co.mysterymayhem.gravitymod.common.modsupport.prepostmodifier;

import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * Created by Mysteryem on 2016-10-23.
 */
public interface IPrePostModifier<T> {
    void preModify(T thing);
    void postModify(T thing);
    /**
     * Must return the same value on both client and server, used to check if client and server have matching IPrePostModifiers
     * @return
     */
    int getUniqueID();

    /**
     *
     * @param phase Start or End, cannot be null
     * @param thing
     */
    default void modify(TickEvent.Phase phase, T thing) {
        switch(phase) {
            case START:
                this.preModify(thing);
                break;
            case END:
                this.postModify(thing);
                break;
        }
    }
}

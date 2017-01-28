package uk.co.mysterymayhem.gravitymod.common.registries;

import uk.co.mysterymayhem.gravitymod.common.entities.EntityFloatingItem;
import uk.co.mysterymayhem.gravitymod.common.entities.EntityGravityItem;
import uk.co.mysterymayhem.gravitymod.common.items.materials.ItemDestabilisedGravityDust;
import uk.co.mysterymayhem.mystlib.setup.registries.AbstractEntityRegistry;
import uk.co.mysterymayhem.mystlib.setup.singletons.IModEntityClassWrapper;

import java.util.ArrayList;

/**
 * Created by Mysteryem on 2016-12-07.
 */
public class ModEntities extends AbstractEntityRegistry<IModEntityClassWrapper<?>, ArrayList<IModEntityClassWrapper<?>>> {
    static boolean REGISTRY_SETUP_ALLOWED = false;

    static EntityGravityItem.Wrapper wrapperEntityGravityItem;
    static EntityFloatingItem.Wrapper wrapperEntityFloatingItem;
    static ItemDestabilisedGravityDust.DissipationEntity.Wrapper wrapperDissipationEntity;

    public ModEntities() {
        super(new ArrayList<>());
    }

    protected void addToCollection(ArrayList<IModEntityClassWrapper<?>> modObjects) {
        modObjects.add(wrapperEntityFloatingItem = new EntityFloatingItem.Wrapper());
        modObjects.add(wrapperEntityGravityItem = new EntityGravityItem.Wrapper());
        modObjects.add(wrapperDissipationEntity = new ItemDestabilisedGravityDust.DissipationEntity.Wrapper());
        REGISTRY_SETUP_ALLOWED = true;
    }

}

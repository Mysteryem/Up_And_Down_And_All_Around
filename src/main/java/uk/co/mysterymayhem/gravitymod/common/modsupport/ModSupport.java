package uk.co.mysterymayhem.gravitymod.common.modsupport;

import gnu.trove.impl.Constants;
import gnu.trove.map.hash.TObjectByteHashMap;
import net.minecraftforge.fml.common.Loader;

import java.util.HashMap;
import java.util.HashSet;

/**
 * This class should be referenced and thus &lt;clinit&gt; during post initialisation
 * Created by Mysteryem on 2016-11-05.
 */
public class ModSupport {
    private static final byte TRUE = 1;
    private static final byte FALSE = 0;
    private static final byte NOVALUE = 2;

    private static final HashMap<String, Boolean> LOADED_MODS_CACHE = new HashMap<>();
    private static final TObjectByteHashMap<String> LOADED_MODS_LOOKUP = new TObjectByteHashMap<>(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, NOVALUE);

    public static boolean isModLoaded(String modID) {
        byte b = LOADED_MODS_LOOKUP.get(modID);
        switch (b) {
            case FALSE:
                return false;
            case TRUE:
                return true;
            default://case NOVALUE:
                boolean isLoaded = Loader.isModLoaded(modID);
                LOADED_MODS_LOOKUP.put(modID, isLoaded ? TRUE : FALSE);
                return isLoaded;
        }
    }

    public static final String BAUBLES_MOD_ID = "Baubles";
    public static final String INTERFACE_IBAUBLE = "baubles.api.IBauble";
}

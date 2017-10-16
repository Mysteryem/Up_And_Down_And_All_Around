package uk.co.mysterymayhem.gravitymod.asm;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import java.util.Map;

/**
 * Created by Mysteryem on 2016-08-16.
 */
@IFMLLoadingPlugin.TransformerExclusions({"uk.co.mysterymayhem.gravitymod.asm.FMLLoadingPlugin"})
@IFMLLoadingPlugin.Name("mysttmtgravitymod")
//@IFMLLoadingPlugin.MCVersion("1.12.2")
// Extra late so we patch after most other mods so we can more easily tell if a patch has failed (I don't trust other mods to detect when they fail)
@IFMLLoadingPlugin.SortingIndex(value = 9001)
public class FMLLoadingPlugin implements IFMLLoadingPlugin {

    @Override
    public String[] getASMTransformerClass() {
        return new String[]{Transformer.class.getName()};
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

    @Override
    public String getModContainerClass() {
        return null;
//        return GravityMod.class.getName();
    }

    @Override
    public String getSetupClass() {
        return null;//GravityMod.class.getName();
    }

    @Override
    public void injectData(Map<String, Object> data) {
        // nothing to do here
    }
}

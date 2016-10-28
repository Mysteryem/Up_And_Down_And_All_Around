package uk.co.mysterymayhem.gravitymod.asm;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import java.util.Map;

/**
 * Created by Mysteryem on 2016-08-16.
 */
@IFMLLoadingPlugin.TransformerExclusions({"uk.co.mysterymayhem.gravitymod.asm.FMLLoadingPlugin"})
@IFMLLoadingPlugin.Name("mysttmtgravitymod")
@IFMLLoadingPlugin.MCVersion("1.10.2")
@IFMLLoadingPlugin.SortingIndex(value = 1001)
public class FMLLoadingPlugin implements IFMLLoadingPlugin {

    @Override
    public String[] getASMTransformerClass() {
        return new String[]{Transformer.class.getName()};
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

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}

package uk.co.mysterymayhem.gravitymod.asm;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import uk.co.mysterymayhem.gravitymod.GravityMod;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * Created by Mysteryem on 2016-08-16.
 */
@IFMLLoadingPlugin.TransformerExclusions({"uk.co.mysterymayhem.gravitymod.asm.FMLLoadingPlugin"})
@IFMLLoadingPlugin.Name(value = "mystgravitymodcore")
@IFMLLoadingPlugin.SortingIndex(value = 1001)
public class FMLLoadingPlugin implements IFMLLoadingPlugin {

    public static boolean DEBUG = true;

    static boolean runtimeDeobfEnabled = false;

    public static File source;

    @Override
    public String[] getASMTransformerClass() {
        return new String[]{Transformer.class.getName()};
    }

    @Override
    public String getModContainerClass() {
        return null;// GravityMod.class.getName();
    }

    @Override
    public String getSetupClass() {
        return null;//GravityMod.class.getName();
    }

    @Override
    public void injectData(Map<String, Object> data) {
        runtimeDeobfEnabled = !(Boolean)data.get("runtimeDeobfuscationEnabled");

//        source = (File) data.get("coremodLocation");
//        if( source == null ) {          // this is usually in a dev env
//            try {
//                source = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
//
//                if( !(new File(source, "assets")).exists() ) {          // fix for IntelliJ
//                    source = new File(source.getParentFile().getParentFile(), "resources/main");
//                }
//            } catch( URISyntaxException e ) {
//                throw new RuntimeException("Failed to acquire source location for SAPManPack!", e);
//            }
//        }
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}

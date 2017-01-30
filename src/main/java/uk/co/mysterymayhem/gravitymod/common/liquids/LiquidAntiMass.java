package uk.co.mysterymayhem.gravitymod.common.liquids;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import uk.co.mysterymayhem.gravitymod.GravityMod;
import uk.co.mysterymayhem.gravitymod.common.registries.IGravityModCommon;

/**
 * Created by Mysteryem on 2017-01-19.
 */
public class LiquidAntiMass extends Fluid implements IGravityModCommon {
    public static final ResourceLocation FLOWING_RESOURCE = new ResourceLocation(GravityMod.MOD_ID, "blocks/liquid_flow");
    public static final LiquidAntiMass INSTANCE;
    public static final ResourceLocation STILL_RESOURCE = new ResourceLocation(GravityMod.MOD_ID, "blocks/liquid_still");
    private static final String FLUID_NAME = "liquidantimass";

    static {
        INSTANCE = new LiquidAntiMass();
    }

    private LiquidAntiMass() {
        super(FLUID_NAME, STILL_RESOURCE, FLOWING_RESOURCE);
        this.density = -1;
//        this.viscosity = 10000;
//        this.density = Integer.MAX_VALUE;
//        this.viscosity = Integer.MAX_VALUE;
        this.isGaseous = true;
        FluidRegistry.registerFluid(this);
        FluidRegistry.addBucketForFluid(this);
    }

    @Override
    public int getColor() {
        return 0xFF4E006f;
    }
}

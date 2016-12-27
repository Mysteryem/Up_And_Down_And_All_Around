package uk.co.mysterymayhem.gravitymod.common.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.math.MathHelper;
import uk.co.mysterymayhem.mystlib.reflection.lambda.LambdaBuilder;

import java.util.function.DoubleSupplier;
import java.util.function.ObjIntConsumer;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

import static uk.co.mysterymayhem.mystlib.reflection.lambda.LambdaBuilder.buildInstanceFieldGetterLambda;
import static uk.co.mysterymayhem.mystlib.reflection.lambda.LambdaBuilder.buildInstanceFieldSetterLambda;

/**
 * Created by Mysteryem on 2016-12-26.
 */
@SuppressWarnings("unchecked")
public class ReflectionLambdas {
    //    public net.minecraft.entity.Entity field_70151_c #fire
//    public net.minecraft.entity.Entity field_70150_b #nextStepDistance
//    public net.minecraft.network.NetHandlerPlayServer field_147365_f #floatingTickCount
//    public net.minecraft.util.math.MathHelper field_181163_d #FRAC_BIAS
//    public net.minecraft.util.math.MathHelper field_181164_e #ASINE_TAB
//    public net.minecraft.entity.item.EntityItem field_70291_e #health
//    public net.minecraft.entity.item.EntityItem field_70292_b #age

    public static final ToIntFunction<Entity> get_Entity$nextStepDistance
            = buildInstanceFieldGetterLambda(ToIntFunction.class, Entity.class, int.class, "field_70150_b", "nextStepDistance");
    public static final ObjIntConsumer<Entity> set_Entity$nextStepDistance
            = buildInstanceFieldSetterLambda(ObjIntConsumer.class, Entity.class, int.class, "field_70150_b", "nextStepDistance");

    public static final DoubleSupplier get_MathHelper$FRAC_BIAS
            = LambdaBuilder.buildStaticFieldGetterLambda(DoubleSupplier.class, MathHelper.class, double.class, "field_181163_d", "FRAC_BIAS");

    public static final Supplier<double[]> get_MathHelper$ASINE_TAB
            = LambdaBuilder.buildStaticFieldGetterLambda(Supplier.class, MathHelper.class, double[].class, "field_181164_e", "ASINE_TAB");

    public static final ToIntFunction<EntityItem> get_EntityItem$health
            = LambdaBuilder.buildInstanceFieldGetterLambda(ToIntFunction.class, EntityItem.class, int.class, "field_70291_e", "health");

    public static final ToIntFunction<EntityItem> get_EntityItem$age
            = buildInstanceFieldGetterLambda(ToIntFunction.class, EntityItem.class, int.class, "field_70292_b", "age");
}

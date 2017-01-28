package uk.co.mysterymayhem.mystlib.reflection.lambda;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

import java.lang.invoke.MethodType;
import java.util.function.BiConsumer;

/**
 * Created by Mysteryem on 2016-12-31.
 */
public final class InternalReflectionLambdas {
    public static final @SuppressWarnings("unchecked")
    BiConsumer<Block, Item> callStatic_Item$registerItemBlock
            = LambdaBuilder.buildStaticMethodLambda(BiConsumer.class, Item.class, MethodType.methodType(void.class, Block.class, Item.class), "func_179214_a", "registerItemBlock");

    // No instantiation
    private InternalReflectionLambdas(){}
}

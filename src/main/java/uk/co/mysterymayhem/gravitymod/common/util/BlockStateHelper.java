package uk.co.mysterymayhem.gravitymod.common.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Mysteryem on 2016-11-17.
 */
public class BlockStateHelper {

    @SuppressWarnings("unchecked")
    public static <E extends Enum<E> & IStringSerializable> E getEnumOfBlockState(Class<E> enumClass, IBlockState blockState) {
        Collection<IProperty<?>> propertyNames = blockState.getPropertyKeys();
        for (IProperty<?> property : propertyNames) {
            if (property instanceof PropertyEnum<?>) {
                PropertyEnum<?> propertyEnum = (PropertyEnum<?>)property;
                if (propertyEnum.getValueClass() == enumClass) {
                    PropertyEnum<E> facingPropertyEnum = (PropertyEnum<E>)propertyEnum;
                    return blockState.getValue(facingPropertyEnum);
                }
            }
        }
        return null;
    }

    public static EnumFacing getFacingOfBlockState(IBlockState blockState) {

        Collection<IProperty<?>> propertyNames = blockState.getPropertyKeys();
        if (propertyNames.contains(BlockDirectional.FACING)) {
            return blockState.getValue(BlockDirectional.FACING);
        }
        else if (propertyNames.contains(BlockHorizontal.FACING)) {
            return blockState.getValue(BlockHorizontal.FACING);
        }

        Map<String, PropertyEnum<EnumFacing>> propertyMap = BlockStateHelper.getEnumsOfBlockState(EnumFacing.class, blockState);

        PropertyEnum<EnumFacing> bestGuessProperty;

        int mapSize = propertyMap.size();

        if (mapSize == 0) {
            bestGuessProperty = null;
        }
        else if (mapSize == 1) {
            Collection<PropertyEnum<EnumFacing>> values = propertyMap.values();
            bestGuessProperty = values.iterator().next();
        }
        else {
            PropertyEnum<EnumFacing> facing = propertyMap.get("facing");
            if (facing == null) {
                // Have to make a complete guess at this point
                Collection<PropertyEnum<EnumFacing>> values = propertyMap.values();
                int mostValues = 0;
                PropertyEnum<EnumFacing> propertyWithMostValues = null;
                for (PropertyEnum<EnumFacing> propertyEnum : values) {
                    int numAllowedValues = propertyEnum.getAllowedValues().size();
                    if (numAllowedValues > mostValues) {
                        mostValues = numAllowedValues;
                        propertyWithMostValues = propertyEnum;
                    }
                }
                bestGuessProperty = propertyWithMostValues;
            }
            else {
                bestGuessProperty = facing;
            }
        }

        EnumFacing facingOfBlockState;

        if (bestGuessProperty == null) {
            facingOfBlockState = null;
        }
        else {
            facingOfBlockState = blockState.getValue(bestGuessProperty);
        }

        return facingOfBlockState;
    }

    @SuppressWarnings("unchecked")
    public static <E extends Enum<E> & IStringSerializable> Map<String, PropertyEnum<E>> getEnumsOfBlockState(Class<E> enumClass, IBlockState blockState) {
        Map<String, PropertyEnum<E>> propertyMap = new HashMap<>();

        Collection<IProperty<?>> propertyNames = blockState.getPropertyKeys();
        for (IProperty<?> property : propertyNames) {
            if (property instanceof PropertyEnum<?>) {
                PropertyEnum<?> propertyEnum = (PropertyEnum<?>)property;
                if (propertyEnum.getValueClass() == enumClass) {
                    PropertyEnum<E> facingPropertyEnum = (PropertyEnum<E>)propertyEnum;
                    propertyMap.put(facingPropertyEnum.getName(), facingPropertyEnum);
                }
            }
        }
        return propertyMap;
    }

    public static boolean propertyIsSavedToMeta(IBlockState blockState, IProperty<?> property) {
        Collection<IProperty<?>> propertyNames = blockState.getPropertyKeys();
        if (!propertyNames.contains(property)) {
            // You should have checked beforehand!
            return false;
        }

        Block block = blockState.getBlock();
        IBlockState defaultState = block.getDefaultState();
        int defaultMeta = block.getMetaFromState(defaultState);
        int cycledMeta = block.getMetaFromState(defaultState.cycleProperty(property));
        return defaultMeta != cycledMeta;
    }
}

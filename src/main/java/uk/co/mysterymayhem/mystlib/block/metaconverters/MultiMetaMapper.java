package uk.co.mysterymayhem.mystlib.block.metaconverters;

import gnu.trove.impl.Constants;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.fml.common.FMLLog;

import java.util.*;

/**
 * Created by Mysteryem on 2016-12-15.
 */
@SuppressWarnings("WeakerAccess")
public class MultiMetaMapper<BLOCK extends Block> extends AbstractMetaMapper<BLOCK> {
    private static final int MAX_BITS = 4;

    // If the property doesn't exist when looking up its index, -1 will be returned which will throw an
    // IndexOutOfBoundsException wherever it's used
    private static final int MISSING_PROPERTY_VALUE = -1;

    // supply an IProperty to get its index (used in the other arrays)
    private final TObjectIntHashMap<IProperty<?>> propertyIndexLookup = new TObjectIntHashMap<>(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, MISSING_PROPERTY_VALUE);
    // get index from IProperty, use index to get number of bits used to store the property
    private final int[] bitsPerProperty; // will always be 1, 2 or 3
    // get index from IProperty, use index to get the bit shift needed when masking/unmasking the property
    private final int[] bitShiftNeeded; // will always be 1, 2 or 3
    // get index of IProperty, use index to get Comparable<?>[], use unmasked value to get Comparable<?>
    private final Comparable<?>[][] propertyIndexToValueArray;
    // get index of IProperty, use index to get map from (property value(Comparable)) -> unmasked value(int))
    private final TObjectIntHashMap<Comparable<?>>[] propertyIndexToValueIndexLookup;

    @SuppressWarnings("unchecked")
    public MultiMetaMapper(BLOCK block, IProperty<?>[] metaProperties, IProperty<?>[] allProperties) throws IllegalArgumentException {
        // Don't actually have to sort in advance as we hold onto the reference to the array, so sorting after would
        // sort the field in the AbstractConverter, but this is easier to understand.
        super(block, metaProperties, allProperties);

        if (new HashSet<>(Arrays.asList(metaProperties)).size() != metaProperties.length) {
            throw new IllegalArgumentException("Duplicate metaProperties found in " + propertyArrayToString(metaProperties));
        }

        if (metaProperties.length < 2) {
            throw new IllegalArgumentException("Only " + metaProperties.length + " metaProperties supplied. Must be at least 2, use a SingleMetaConverter for a single property");
        }
        if (metaProperties.length > MAX_BITS) {
            throw new IllegalArgumentException(metaProperties.length + " metaProperties supplied. 4 PropertyBool metaProperties (2 values each) use all 4 bits available," +
                    " using more than 4 metaProperties will never work");
        }

        // Sort so metaProperties are in a defined order instead of whatever order they are supplied to the method

        this.propertyIndexToValueArray = new Comparable<?>[metaProperties.length][];

        // Because generic array creation doesn't exist for some reason, we have to cast
        this.propertyIndexToValueIndexLookup = (TObjectIntHashMap<Comparable<?>>[])new TObjectIntHashMap[metaProperties.length];


        // Number of bits of data each property needs to store all its values
        int[] bitsPerProperty = new int[metaProperties.length];
        int[] shiftNeeded = new int[metaProperties.length];

        this.bitsPerProperty = bitsPerProperty;
        this.bitShiftNeeded = shiftNeeded;

        // Iterate through all metaProperties
        for (int i = 0, propertiesLength = metaProperties.length; i < propertiesLength; i++) {
            // Current property
            IProperty<?> property = metaProperties[i];
            property.getName();

            this.captureAndProcessProperty(property, i);

        }

        this.validateTotalBits();
    }

    private static String propertyArrayToString(IProperty<?>... properties) {

        int lengthMinusOne = properties.length - 1;
        StringBuilder builder = new StringBuilder();
        builder.append('{');

        for (int i = 0; i < properties.length; i++) {
            builder.append(properties[i].getName());
            if (i < lengthMinusOne) {
                builder.append(',');
            }
        }

        builder.append('}');

        return builder.toString();
    }

    private <VALUE extends Comparable<VALUE>> void captureAndProcessProperty(IProperty<VALUE> property, int i) {
        // Add (property -> index) mapping
        this.propertyIndexLookup.put(property, i);

        // Get all values for this property
        Collection<VALUE> allowedValues = property.getAllowedValues();
        int numAllowedValues = allowedValues.size();

        Comparable<?>[] comparables = new Comparable<?>[numAllowedValues];

        this.propertyIndexToValueArray[i] = comparables;

        // Validate number of values
        if (numAllowedValues < 2) {
            throw new IllegalArgumentException("Can't have a property with less than 2 values");
        }
        if (numAllowedValues > 8) {
            throw new IllegalArgumentException("Can't have a property with more than 8 values. A property with 8" +
                    " values requires 3 bits to store, leaving a single bit left for another property");
        }

        // Calculate number of bits needed to store the number of values
        int numberOfBitsNeededToStoreAllValues = Integer.SIZE - Integer.numberOfLeadingZeros(numAllowedValues - 1);
        // Store the calculated amount
        bitsPerProperty[i] = numberOfBitsNeededToStoreAllValues;

        // Calculate the rightshift needed for the current property
        // First property will always have 0 rightshift
        if (i != 0) {
            this.bitShiftNeeded[i] = this.bitShiftNeeded[i - 1] + bitsPerProperty[i - 1];
        }

        // Sort the values
        ArrayList<VALUE> sortedAllowedValues = new ArrayList<>(allowedValues);
        Collections.sort(sortedAllowedValues);

        // Add map from index to value
//        TIntObjectHashMap<Comparable<?>> indexToValueMap = new TIntObjectHashMap<>(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, -1);
//        this.propertySortedIndexToValueMap.put(property, indexToValueMap);

        TObjectIntHashMap<Comparable<?>> valueToIndexMap = new TObjectIntHashMap<>(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, -1);

        this.propertyIndexToValueIndexLookup[i] = valueToIndexMap;

        // Add mappings for each value and its index
        for (int j = 0, size = sortedAllowedValues.size(); j < size; j++) {
            Comparable<?> comparable = sortedAllowedValues.get(j);
//            indexToValueMap.put(j, comparable);
            comparables[j] = comparable;
            valueToIndexMap.put(comparable, j);
        }
    }

    private void validateTotalBits() throws IllegalArgumentException {
        int totalBits = 0;
        for (int bits : this.bitsPerProperty) {
            totalBits += bits;
        }
        if (totalBits > MAX_BITS) {
            throw new IllegalArgumentException("Can't store more than 4 bits in a blockstate");
        }
    }

    @Override
    public IBlockState apply(int value) {
        IBlockState blockState = this.block.getDefaultState();
        for (IProperty<?> property : this.metaProperties) {
            blockState = blockStateWithProperty(value, blockState, property);
        }
        return blockState;
    }

    @SuppressWarnings("unchecked")
    private <VALUE extends Comparable<VALUE>> IBlockState blockStateWithProperty(int meta, IBlockState iBlockState, IProperty<VALUE> property) {
        final int index = this.getIndex(property);
//        //TODO: Experiment with actualState and extendedState, see if those sorts of IBlockState ever get passed to this method
////        if (index == -1) {return iBlockState;}
//        int shift = this.getShift(index);
//        int bits = this.getBits(index);
//        int mask = getMask(bits);
//        int unmasked = (meta >> shift) & mask;
//
//        Comparable<?> comparable = this.propertyIndexToValueArray[index][unmasked];

//        return iBlockState.withProperty(property, (VALUE)comparable);

        // Compressed the above, now with safety
        Comparable<?>[] innerArray = this.propertyIndexToValueArray[index];
        int innerIndex = (meta >> this.getShift(index)) & getMask(this.getBits(index));
        if (innerIndex < 0 || innerIndex >= innerArray.length) {
            FMLLog.warning("MystLib: MultiMetaMapper: Invalid meta passed to 'getStateFromMeta' delegate (MultiMetaMapper::apply). Using default state for " +
                    "property %s", property);
            return iBlockState;
        }
        return iBlockState.withProperty(property, (VALUE)innerArray[innerIndex]);
    }

    private int getIndex(IProperty<?> property) {
        return this.propertyIndexLookup.get(property);
    }

    private int getShift(int index) {
        return this.bitShiftNeeded[index];
    }

    private static int getMask(int bits) {
        switch (bits) {
            case 1:
                return 0b1;
            case 2:
                return 0b11;
            case 3:
                return 0b111;
            default:
                throw new IllegalArgumentException("Unexpected " + bits + " bits argument (must be 1, 2 or 3)");
        }
//        return BITMASKS[bits];
    }

    private int getBits(int index) {
        return this.bitsPerProperty[index];
    }

    @Override
    public int applyAsInt(IBlockState iBlockState) {
        int meta = 0;
        for (Map.Entry<IProperty<?>, Comparable<?>> entry : iBlockState.getProperties().entrySet()) {
//            IProperty<?> key = entry.getKey();
//            Comparable<?> value = entry.getValue();
//            int index = this.getIndex(key);
//            int shift = this.getShift(index);
//
//            TObjectIntHashMap<Comparable<?>> comparableTObjectIntHashMap = propertyIndexToValueIndexLookup[index];
//
//            int unmasked = comparableTObjectIntHashMap.get(value);
//            int reMasked = unmasked << shift;
//            meta |= reMasked;

            // Compressed the above
            final int index = this.getIndex(entry.getKey());
            if (index == -1) {
                // If the properties used by the blockstate would use > 4 bits, all values of the blockstate get passed to this method
                // If the properties used take up <= 4 bits, then only those which get stored to metadata get passed to this method
                continue;
            }
            meta |= (propertyIndexToValueIndexLookup[index].get(entry.getValue()) << this.getShift(index));
        }
        return meta;
    }
}

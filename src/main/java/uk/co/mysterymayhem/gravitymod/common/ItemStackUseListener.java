package uk.co.mysterymayhem.gravitymod.common;

import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.oredict.OreDictionary;
import uk.co.mysterymayhem.gravitymod.asm.EntityPlayerWithGravity;
import uk.co.mysterymayhem.gravitymod.asm.Hooks;
import uk.co.mysterymayhem.gravitymod.util.prepostmodifier.CombinedPrePostModifier;
import uk.co.mysterymayhem.gravitymod.util.prepostmodifier.IPrePostModifier;
import uk.co.mysterymayhem.gravitymod.events.ItemStackUseEvent;

import java.util.*;

/**
 * Created by Mysteryem on 2016-10-23.
 */
public class ItemStackUseListener {

    public enum EnumItemStackUseCompat {
        BLOCK, GENERAL, STOPPED_USING
    }

    private static ByteBuf packetDataToSendOnClientConfigHashMismatch = new PacketBuffer(Unpooled.buffer());
    private static TIntObjectHashMap<String> intToStringMap = new TIntObjectHashMap<>();

    public static void buildPacketData() {

        // Gather all the unique Items
        HashSet<Item> allUniqueItems = new HashSet<>();
        allUniqueItems.addAll(onItemUse_itemToPrePostModifier.keySet());
        allUniqueItems.addAll(onItemRightClick_itemToPrePostModifier.keySet());
        allUniqueItems.addAll(onPlayerStoppedUsing_itemToPrePostModifier.keySet());

        // Gather all the unique modIds and itemNames of all the unique Items.
        HashSet<String> allUniqueStrings = new HashSet<>();
        for (Item item : allUniqueItems) {
            ResourceLocation resourceLocation = item.getRegistryName();
            allUniqueStrings.add(resourceLocation.getResourceDomain());
            allUniqueStrings.add(resourceLocation.getResourcePath());
        }

        // A map that assigns each unique string to an integer, so instead of sending lots of strings, we can send the
        // ints they're mapped to instead
        TObjectIntHashMap<String> stringToIndexMap = new TObjectIntHashMap<>();

        ByteBuf buf = packetDataToSendOnClientConfigHashMismatch;

        // Number of strings used in the items, so we know how many to read
        buf.writeInt(allUniqueStrings.size());

        int i = 0;
        for (String uniqueString : allUniqueStrings) {
            // write string to buffer
            ByteBufUtils.writeUTF8String(buf, uniqueString);
            // store the index we're using for this string (will be used to get the index when we want to refer to items later
            stringToIndexMap.put(uniqueString, i);
            i++;
        }

        // Now add information such that we can reconstruct all the combined modifiers
        ArrayList<CombinedPrePostModifier> combinedRegistry = CombinedPrePostModifier.COMBINED_REGISTRY;

        // So we know how many to read
        buf.writeInt(combinedRegistry.size());

        for (CombinedPrePostModifier combinedModifier : combinedRegistry) {
            // So we know what order the modifiers are used in
            buf.writeBoolean(combinedModifier.getProcessingOrder() == CombinedPrePostModifier.ProcessingOrder.PRE_FIRST_SECOND_POST_FIRST_SECOND);
            // ID of the first modifier
            buf.writeInt(combinedModifier.getFirst().getUniqueID());
            // ID of the second modifier
            buf.writeInt(combinedModifier.getSecond().getUniqueID());
        }

        // Write the data of the maps, substituting int values instead of Strings and IPrePostModifiers
        writeMapData(buf, stringToIndexMap, onItemUse_itemToPrePostModifier);
        writeMapData(buf, stringToIndexMap, onItemRightClick_itemToPrePostModifier);
        writeMapData(buf, stringToIndexMap, onPlayerStoppedUsing_itemToPrePostModifier);
    }

    public static ByteBuf getConfigPacket() {
        return packetDataToSendOnClientConfigHashMismatch;
    }

    private static void writeMapData(ByteBuf buf, TObjectIntHashMap<String> stringToIndexMap, TreeMap<Item, TIntObjectHashMap<IPrePostModifier<EntityPlayerWithGravity>>> map) {
        // Write the number of entries
        buf.writeInt(map.size());

        for (Map.Entry<Item, TIntObjectHashMap<IPrePostModifier<EntityPlayerWithGravity>>> entry : map.entrySet()) {
            Item item = entry.getKey();
            ResourceLocation resourceLocation = item.getRegistryName();
            // Write the ints that correspond to the registry name
            buf.writeInt(stringToIndexMap.get(resourceLocation.getResourceDomain()));
            buf.writeInt(stringToIndexMap.get(resourceLocation.getResourcePath()));

            TIntObjectHashMap<IPrePostModifier<EntityPlayerWithGravity>> value = entry.getValue();

            // Write how many damage values have a modifier
            buf.writeInt(value.size());

            TIntObjectIterator<IPrePostModifier<EntityPlayerWithGravity>> iterator = value.iterator();
            for (int i = 0; i < value.size(); i++) {
                iterator.advance();

                // write the damage value
                buf.writeInt(iterator.key());
                // write the ID of the modifier it's mapped to
                buf.writeInt(iterator.value().getUniqueID());
            }
        }
    }

    private static int hashCode;

    public static int getHashCode() {
        return hashCode;
    }

    public static void makeHash() {
        int hash = 1;
        hash = 31*hash + getHashForMap(onItemUse_itemToPrePostModifier);
        hash = 31*hash + getHashForMap(onItemRightClick_itemToPrePostModifier);
        hash = 31*hash + getHashForMap(onPlayerStoppedUsing_itemToPrePostModifier);

        hashCode = hash;
    }

    public static void reset() {
        onItemUse_itemToPrePostModifier.clear();
        onItemRightClick_itemToPrePostModifier.clear();
        onPlayerStoppedUsing_itemToPrePostModifier.clear();
    }

    private static int getHashForMap(TreeMap<Item, TIntObjectHashMap<IPrePostModifier<EntityPlayerWithGravity>>> map) {
        int hash = 1;
        for (Item itemKey : map.keySet()) {
            hash = 31*hash + (itemKey == null ? 0 : itemKey.getRegistryName().hashCode());
            TIntObjectHashMap<IPrePostModifier<EntityPlayerWithGravity>> prePostMap = map.get(itemKey);
            if (prePostMap == null) {
                hash = 31*hash;
            }
            else {
                int[] keysArray = prePostMap.keySet().toArray();
                Arrays.sort(keysArray);
                for (int nextKeyIndex = 0; nextKeyIndex < keysArray.length; nextKeyIndex++) {
                    int nextKey = keysArray[nextKeyIndex];
                    FMLLog.info("nextKey: " + nextKey);
                    hash = 31*hash + nextKey;
                    IPrePostModifier<EntityPlayerWithGravity> prePostModifier = prePostMap.get(nextKey);
                    if (prePostModifier == null) {
                        hash = 31*hash;
                    }
                    else {
                        hash = 31*hash + prePostModifier.getUniqueID();
                    }
                }
            }
        }
        return hash;
    }

    private static class ItemComparator implements Comparator<Item> {
        @Override
        public int compare(Item o1, Item o2) {
            return o1.getRegistryName().toString().compareTo(o2.getRegistryName().toString());
        }
    }

    public static final TreeMap<Item, TIntObjectHashMap<IPrePostModifier<EntityPlayerWithGravity>>> onItemUse_itemToPrePostModifier = new TreeMap<>(new ItemComparator());
    public static final TreeMap<Item, TIntObjectHashMap<IPrePostModifier<EntityPlayerWithGravity>>> onItemRightClick_itemToPrePostModifier = new TreeMap<>(new ItemComparator());
    public static final TreeMap<Item, TIntObjectHashMap<IPrePostModifier<EntityPlayerWithGravity>>> onPlayerStoppedUsing_itemToPrePostModifier = new TreeMap<>(new ItemComparator());
//    private static final HashMap<String, TIntObjectHashMap<IPrePostModifier<EntityPlayerWithGravity>>> CUSTOM_PRE_POST_MODIFIERS = new HashMap<>();
//
//    public static void addCustomOnItemUseOnBlock(Item item, String yourModID, int uniqueID, Consumer<EntityPlayer> preRunnable, Consumer<EntityPlayer> postRunnable) {
//
//    }

    public static void addPrePostModifier(String fullModAndItemName, IPrePostModifier<EntityPlayerWithGravity> prePostModifier, EnumItemStackUseCompat listener, int... damageValues) {
        addPrePostModifier(fullModAndItemName, prePostModifier, new EnumItemStackUseCompat[]{listener}, damageValues);
    }

    public static void addPrePostModifier(String fullModAndItemName, IPrePostModifier<EntityPlayerWithGravity> prePostModifier, EnumItemStackUseCompat[] listenersSet, int... damageValues) {
        String[] split = fullModAndItemName.split(":");
        if (split.length != 2) {
            ModContainer activeModContainer = Loader.instance().activeModContainer();
            if (activeModContainer != null) {
                FMLLog.warning("[UpAndDownAndAllAround] Failed to register PrePostModifier. Failed to parse item registry name %s. Mod responsible: %s(%s)",
                        fullModAndItemName, activeModContainer.getName(), activeModContainer.getModId());
            }
            else {
                FMLLog.warning("[UpAndDownAndAllAround] Failed to register PrePostModifier. Failed to parse item registry name %s.",
                        fullModAndItemName);
            }
        }
        else {
            addPrePostModifier(split[0], split [1], prePostModifier, listenersSet, damageValues);
        }
    }

    public static void addPrePostModifier(String modID, String itemName, IPrePostModifier<EntityPlayerWithGravity> prePostModifier, EnumItemStackUseCompat listener, int... damageValues) {
        addPrePostModifier(modID, itemName, prePostModifier, new EnumItemStackUseCompat[]{listener}, damageValues);
    }

    public static void addPrePostModifier(String modID, String itemName, IPrePostModifier<EntityPlayerWithGravity> prePostModifier, EnumItemStackUseCompat[] listenersSet, int... damageValues) {
        addPrePostModifier(
                new ResourceLocation(Objects.requireNonNull(modID, "modID cannot be null"), Objects.requireNonNull(itemName, "itemName cannot be null")),
                prePostModifier,
                listenersSet,
                damageValues);

    }

    public static void addPrePostModifier(ResourceLocation itemRegistryName, IPrePostModifier<EntityPlayerWithGravity> prePostModifier, EnumItemStackUseCompat listener, int... damageValues) {
        addPrePostModifier(itemRegistryName, prePostModifier, new EnumItemStackUseCompat[]{listener}, damageValues);
    }

    public static void addPrePostModifier(
            ResourceLocation itemRegistryName,
            IPrePostModifier<EntityPlayerWithGravity> prePostModifier,
            EnumItemStackUseCompat[] listenersSet,
            int... damageValues) {

        Objects.requireNonNull(listenersSet, String.format("[UpAndDownAndAllAround] Failed to register PrePostModifier for %s. ListenersSet cannot be null", prePostModifier));

        if (listenersSet.length == 0) {
            throw new IllegalArgumentException(String.format("[UpAndDownAndAllAround] Failed to register PrePostModifier for %s. ListenersSet cannot be empty", prePostModifier));
        }

        Item item = Item.REGISTRY.getObject(Objects.requireNonNull(itemRegistryName, "itemRegistryName cannot be null"));
        if (item == null) {
            FMLLog.warning("[UpAndDownAndAllAround] Failed to register PrePostModifier for %s. The item could not be found", itemRegistryName);
            return;
        }

        Objects.requireNonNull(prePostModifier, String.format("[UpAndDownAndAllAround] Failed to register PrePostModifier for %s. PrePostModifier cannot be null", prePostModifier));
        if (damageValues == null || damageValues.length == 0) {
            damageValues = new int[]{OreDictionary.WILDCARD_VALUE};
        }
        for (EnumItemStackUseCompat itemUseMethod : listenersSet) {
            Objects.requireNonNull(itemUseMethod, String.format("[UpAndDownAndAllAround] Failed to register PrePostModifier for %s. ListenersSet cannot be null", prePostModifier));
            TreeMap<Item, TIntObjectHashMap<IPrePostModifier<EntityPlayerWithGravity>>> map;
            String mapName;
            switch (itemUseMethod) {
                case BLOCK:
                    mapName = "onItemUse(BLOCK)";
                    map = onItemUse_itemToPrePostModifier;
                    break;
                case GENERAL:
                    mapName = "onItemRightClick(GENERAL)";
                    map = onItemRightClick_itemToPrePostModifier;
                    break;
                default://case STOPPED_USING:
                    mapName = "onPlayerStoppedUsing(STOPPED_USING)";
                    map = onPlayerStoppedUsing_itemToPrePostModifier;
                    break;
            }
            for (int damageValue : damageValues) {
                TIntObjectHashMap<IPrePostModifier<EntityPlayerWithGravity>> damageToPrePostMap = map.get(item);
                if (damageToPrePostMap == null) {
                    damageToPrePostMap = new TIntObjectHashMap<>();
                    map.put(item, damageToPrePostMap);
                }
                if (damageToPrePostMap.containsKey(damageValue)) {
                    FMLLog.warning("[UpAndDownAndAllAround] A mapping for %s with damage value %s already exists in %s. Proceeding to overwrite it.", itemRegistryName, damageValue, mapName);
                }
                damageToPrePostMap.put(damageValue, prePostModifier);
            }
        }
    }

    private static IPrePostModifier<EntityPlayerWithGravity> getPrePostModifier
            (TreeMap<Item, TIntObjectHashMap<IPrePostModifier<EntityPlayerWithGravity>>> map, ItemStackUseEvent event) {
        ItemStack stack = event.stack;
        Item item = stack.getItem();
        TIntObjectHashMap<IPrePostModifier<EntityPlayerWithGravity>> intMap = map.get(item);
        if (intMap != null) {
            IPrePostModifier<EntityPlayerWithGravity> prePostModifier = intMap.get(OreDictionary.WILDCARD_VALUE);
            if (prePostModifier == null) {
                prePostModifier = intMap.get(stack.getItemDamage());
            }
            return prePostModifier;
        }
        return null;
    }

    @SubscribeEvent
    public void onItemUseOnBlock(ItemStackUseEvent.OnUseOnBlock event) {
        EntityLivingBase entity;
        if ((entity = event.getEntityLiving()) instanceof EntityPlayerWithGravity) {
            EntityPlayerWithGravity player = (EntityPlayerWithGravity) entity;
            IPrePostModifier<EntityPlayerWithGravity> prePostModifier = getPrePostModifier(onItemUse_itemToPrePostModifier, event);
            if (prePostModifier != null) {
                prePostModifier.modify(event.phase, player);
            }
        }
    }

    @SubscribeEvent
    public void onItemUseGeneral(ItemStackUseEvent.OnUseGeneral event) {
        EntityLivingBase entity;
        if ((entity = event.getEntityLiving()) instanceof EntityPlayerWithGravity) {
            EntityPlayerWithGravity player = (EntityPlayerWithGravity) entity;
            IPrePostModifier<EntityPlayerWithGravity> prePostModifier = getPrePostModifier(onItemRightClick_itemToPrePostModifier, event);
            if (prePostModifier != null) {
                prePostModifier.modify(event.phase, player);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerStoppedUsingItem(ItemStackUseEvent.OnStoppedUsing event) {
        EntityLivingBase entity;
        if ((entity = event.getEntityLiving()) instanceof EntityPlayerWithGravity) {
            EntityPlayerWithGravity player = (EntityPlayerWithGravity) entity;
            IPrePostModifier<EntityPlayerWithGravity> prePostModifier = getPrePostModifier(onPlayerStoppedUsing_itemToPrePostModifier, event);
            if (prePostModifier != null) {
                prePostModifier.modify(event.phase, player);
            }
        }
    }

    /**
     * Along with onAnyItemStackUseEventPost, enforces a consistent state of absolute motion for other listening methods
     * This usually results in no change at all
     * @param event
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onAnyItemStackUseEventPre(ItemStackUseEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            Hooks.makeMotionAbsolute(event.getEntityLiving());
        }
    }

    /**
     * Along with onAnyItemStackUseEventPre, enforces a consistent state of absolute motion for other listening methods
     * This usually results in no change at all
     * @param event
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onAnyItemStackUseEventPost(ItemStackUseEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Hooks.popMotionStack(event.getEntityLiving());
        }
    }

}

package uk.co.mysterymayhem.gravitymod.common.listeners;

import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import uk.co.mysterymayhem.gravitymod.GravityMod;
import uk.co.mysterymayhem.gravitymod.asm.EntityPlayerWithGravity;
import uk.co.mysterymayhem.gravitymod.asm.Hooks;
import uk.co.mysterymayhem.gravitymod.common.events.ItemStackUseEvent;
import uk.co.mysterymayhem.gravitymod.common.modsupport.prepostmodifier.IPrePostModifier;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.TreeMap;

//TODO: Build two separate hashcodes and sets of maps, one that's used in SSP and one that's used in SMP.
//TODO: If a server's hashcode does not match the client's, it replaces the SMP set.

/**
 * Listens to use of item stacks and modifies motion/rotation as defined in the config so items from other mods can be
 * made to work with different gravity directions
 * Created by Mysteryem on 2016-10-23.
 */
public class ItemStackUseListener {

    public static final TreeMap<Item, TIntObjectHashMap<IPrePostModifier<EntityPlayerWithGravity>>> onItemRightClick_itemToPrePostModifier = new TreeMap<>(new ItemComparator());
    public static final TreeMap<Item, TIntObjectHashMap<IPrePostModifier<EntityPlayerWithGravity>>> onItemUse_itemToPrePostModifier = new TreeMap<>(new ItemComparator());
    public static final TreeMap<Item, TIntObjectHashMap<IPrePostModifier<EntityPlayerWithGravity>>> onPlayerStoppedUsing_itemToPrePostModifier = new TreeMap<>(new ItemComparator());

    private static int hashCode;

    public static void clearPrePostModifiers() {
        onItemRightClick_itemToPrePostModifier.clear();
        onItemUse_itemToPrePostModifier.clear();
        onPlayerStoppedUsing_itemToPrePostModifier.clear();
    }

    public static void addPrePostModifier(String fullModAndItemName, IPrePostModifier<EntityPlayerWithGravity> prePostModifier, EnumItemStackUseCompat listener, int... damageValues) {
        addPrePostModifier(fullModAndItemName, prePostModifier, new EnumItemStackUseCompat[]{listener}, damageValues);
    }

    public static void addPrePostModifier(String fullModAndItemName, IPrePostModifier<EntityPlayerWithGravity> prePostModifier, EnumItemStackUseCompat[] listenersSet, int... damageValues) {
        String[] split = fullModAndItemName.split(":");
        if (split.length != 2) {
            ModContainer activeModContainer = Loader.instance().activeModContainer();
            if (activeModContainer != null) {
                GravityMod.logWarning("Failed to register PrePostModifier. Failed to parse item registry name %s. Mod responsible: %s(%s)",
                        fullModAndItemName, activeModContainer.getName(), activeModContainer.getModId());
            }
            else {
                GravityMod.logWarning("Failed to register PrePostModifier. Failed to parse item registry name %s.",
                        fullModAndItemName);
            }
        }
        else {
            addPrePostModifier(split[0], split[1], prePostModifier, listenersSet, damageValues);
        }
    }

    public static void addPrePostModifier(String modID, String itemName, IPrePostModifier<EntityPlayerWithGravity> prePostModifier, EnumItemStackUseCompat[] listenersSet, int... damageValues) {
        addPrePostModifier(
                new ResourceLocation(Objects.requireNonNull(modID, "modID cannot be null"), Objects.requireNonNull(itemName, "itemName cannot be null")),
                prePostModifier,
                listenersSet,
                damageValues);

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

        Item item = ForgeRegistries.ITEMS.getValue(Objects.requireNonNull(itemRegistryName, "itemRegistryName cannot be null"));
        if (item == null) {
            GravityMod.logWarning("Failed to register PrePostModifier for %s. The item could not be found", itemRegistryName);
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
                    GravityMod.logWarning("A mapping for %s with damage value %s already exists in %s. Proceeding to overwrite it.",
                            itemRegistryName,
                            damageValue, mapName);
                }
                damageToPrePostMap.put(damageValue, prePostModifier);
            }
        }
    }

    public static void addPrePostModifier(String modID, String itemName, IPrePostModifier<EntityPlayerWithGravity> prePostModifier, EnumItemStackUseCompat listener, int... damageValues) {
        addPrePostModifier(modID, itemName, prePostModifier, new EnumItemStackUseCompat[]{listener}, damageValues);
    }

    public static void addPrePostModifier(ResourceLocation itemRegistryName, IPrePostModifier<EntityPlayerWithGravity> prePostModifier, EnumItemStackUseCompat listener, int... damageValues) {
        addPrePostModifier(itemRegistryName, prePostModifier, new EnumItemStackUseCompat[]{listener}, damageValues);
    }

    public static int getHashCode() {
        return hashCode;
    }

    public static void makeHash() {
        int hash = 1;
        hash = 31 * hash + getHashForMap(onItemUse_itemToPrePostModifier);
        hash = 31 * hash + getHashForMap(onItemRightClick_itemToPrePostModifier);
        hash = 31 * hash + getHashForMap(onPlayerStoppedUsing_itemToPrePostModifier);

        hashCode = hash;
    }

    private static int getHashForMap(TreeMap<Item, TIntObjectHashMap<IPrePostModifier<EntityPlayerWithGravity>>> map) {
        int hash = 1;
        for (Item itemKey : map.keySet()) {
            hash = 31 * hash + (itemKey == null ? 0 : itemKey.getRegistryName().hashCode());
            TIntObjectHashMap<IPrePostModifier<EntityPlayerWithGravity>> prePostMap = map.get(itemKey);
            if (prePostMap == null) {
                hash = 31 * hash;
            }
            else {
                int[] keysArray = prePostMap.keySet().toArray();
                Arrays.sort(keysArray);
                for (int nextKeyIndex = 0; nextKeyIndex < keysArray.length; nextKeyIndex++) {
                    int nextKey = keysArray[nextKeyIndex];
                    hash = 31 * hash + nextKey;
                    IPrePostModifier<EntityPlayerWithGravity> prePostModifier = prePostMap.get(nextKey);
                    if (prePostModifier == null) {
                        hash = 31 * hash;
                    }
                    else {
                        hash = 31 * hash + prePostModifier.getUniqueID();
                    }
                }
            }
        }
        return hash;
    }

    /**
     * Along with onAnyItemStackUseEventPre, enforces a consistent state of absolute motion for other listening methods
     * This usually results in no change at all
     *
     * @param event
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onAnyItemStackUseEventPost(ItemStackUseEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Hooks.popMotionStack(event.getEntityLiving());
        }
    }

    /**
     * Along with onAnyItemStackUseEventPost, enforces a consistent state of absolute motion for other listening methods
     * This usually results in no change at all
     *
     * @param event
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onAnyItemStackUseEventPre(ItemStackUseEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            Hooks.makeMotionAbsolute(event.getEntityLiving());
        }
    }

    @SubscribeEvent
    public static void onItemUseGeneral(ItemStackUseEvent.OnUseGeneral event) {
        EntityLivingBase entity;
        if ((entity = event.getEntityLiving()) instanceof EntityPlayerWithGravity) {
            EntityPlayerWithGravity player = (EntityPlayerWithGravity)entity;
            IPrePostModifier<EntityPlayerWithGravity> prePostModifier = getPrePostModifier(onItemRightClick_itemToPrePostModifier, event);
            if (prePostModifier != null) {
                prePostModifier.modify(event.phase, player);
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
    public static void onItemUseOnBlock(ItemStackUseEvent.OnUseOnBlock event) {
        EntityLivingBase entity;
        if ((entity = event.getEntityLiving()) instanceof EntityPlayerWithGravity) {
            EntityPlayerWithGravity player = (EntityPlayerWithGravity)entity;
            IPrePostModifier<EntityPlayerWithGravity> prePostModifier = getPrePostModifier(onItemUse_itemToPrePostModifier, event);
            if (prePostModifier != null) {
                prePostModifier.modify(event.phase, player);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerStoppedUsingItem(ItemStackUseEvent.OnStoppedUsing event) {
        EntityLivingBase entity;
        if ((entity = event.getEntityLiving()) instanceof EntityPlayerWithGravity) {
            EntityPlayerWithGravity player = (EntityPlayerWithGravity)entity;
            IPrePostModifier<EntityPlayerWithGravity> prePostModifier = getPrePostModifier(onPlayerStoppedUsing_itemToPrePostModifier, event);
            if (prePostModifier != null) {
                prePostModifier.modify(event.phase, player);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public static void onRightClickBlockHighest(PlayerInteractEvent.RightClickBlock event) {
        Hooks.makeMotionAbsolute(event.getEntityPlayer());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onRightClickBlockLowest(PlayerInteractEvent.RightClickBlock event) {
        Hooks.popMotionStack(event.getEntityPlayer());
    }


    // Events that try to allow other mods using these events to modify the player's motion as if they have currently
    // have downwards gravity

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public static void onRightClickItemHighest(PlayerInteractEvent.RightClickItem event) {
        Hooks.makeMotionAbsolute(event.getEntityPlayer());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onRightClickItemLowest(PlayerInteractEvent.RightClickItem event) {
        Hooks.popMotionStack(event.getEntityPlayer());
    }

    public enum EnumItemStackUseCompat {
        BLOCK("onUseOnBlock"), GENERAL("onUseGeneral"), STOPPED_USING("onStoppedUsing");

        public final String configName;

        EnumItemStackUseCompat(String configName) {
            this.configName = configName;
        }
    }

    private static class ItemComparator implements Comparator<Item> {
        @Override
        public int compare(Item o1, Item o2) {
            return o1.getRegistryName().toString().compareTo(o2.getRegistryName().toString());
        }
    }

}

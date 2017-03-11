package uk.co.mysterymayhem.gravitymod.common.listeners;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.*;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.conditions.RandomChance;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraft.world.storage.loot.functions.SetCount;
import net.minecraft.world.storage.loot.functions.SetMetadata;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import uk.co.mysterymayhem.gravitymod.GravityMod;
import uk.co.mysterymayhem.gravitymod.api.EnumGravityDirection;
import uk.co.mysterymayhem.gravitymod.common.config.ConfigHandler;
import uk.co.mysterymayhem.gravitymod.common.registries.StaticItems;

import java.util.function.Predicate;

/**
 * Created by Mysteryem on 17/02/2017.
 */
public class LootTableListener {
    private static final LootFunction[] NO_LOOT_FUNCTIONS = new LootFunction[0];
    private static final LootCondition[] NO_LOOT_CONDITIONS = new LootCondition[0];
    private static final LootEntry[] NO_LOOT_ENTRIES = new LootEntry[0];

    private static final RandomValueRange randomAnchorMeta = new RandomValueRange(0, EnumGravityDirection.values().length - 1);// [0,5]
    public static final Predicate<LootTable> FISHING_JUNK_LOOT = LootTableListener::addFishingJunkLoot;
    public static final Predicate<LootTable> ADD_ANCHOR_POOL = LootTableListener::addAnchorPoolToLootTable;
    public static final Predicate<LootTable> ADD_GRAVITYDUST_TO_MINESHAFT = LootTableListener::addGravityDustToMineshaftChest;

    @SubscribeEvent
    public static void onLootLoad(LootTableLoadEvent event) {
        ResourceLocation name = event.getName();
        Predicate<LootTable> lootTableConsumer = ConfigHandler.lootTableAdditions.get(name);
        if (lootTableConsumer != null) {
            if (!lootTableConsumer.test(event.getTable())) {
                GravityMod.logWarning("Failed to add loot to %s", name);
            }
            else {
                GravityMod.logInfo("Added loot to %s", name);
            }
        }
    }

    private static boolean addGravityDustToMineshaftChest(LootTable lootTable) {
        LootPool pool1 = lootTable.getPool("pool1");
        if (pool1 != null) {
            SetCount setCountFunction = new SetCount(NO_LOOT_CONDITIONS, new RandomValueRange(4f, 9f));
            LootFunction[] lootFunctions = {setCountFunction};

            LootEntryItem lootEntryItem = new LootEntryItem(
                    StaticItems.GRAVITY_DUST,//item
                    ConfigHandler.gravityDustMineshaftWeight,//weight
                    0,//quality
                    lootFunctions,//loot functions
                    NO_LOOT_CONDITIONS,//loot conditions
                    StaticItems.GRAVITY_DUST.getRegistryName().toString()
            );

            pool1.addEntry(lootEntryItem);
            return true;
        }
        return false;
    }

    private static boolean addAnchorPoolToLootTable(LootTable lootTable) {
        SetMetadata setMetadataFunction = new SetMetadata(NO_LOOT_CONDITIONS, randomAnchorMeta);
        LootFunction[] lootFunctions = {setMetadataFunction};

        LootEntryItem lootEntry = new LootEntryItem(
                StaticItems.GRAVITY_ANCHOR,//item
                1,//weight
                0,//quality
                lootFunctions,//loot functions
                NO_LOOT_CONDITIONS,//loot conditions
                StaticItems.GRAVITY_ANCHOR.getRegistryName().toString()//name
        );

        LootEntryItem[] lootEntries = {lootEntry};

        RandomChance lootCondition = new RandomChance(ConfigHandler.anchorChestLootChance);

        RandomChance[] lootConditions = {lootCondition};

        RandomValueRange rolls = new RandomValueRange(1);

        RandomValueRange bonusRolls = new RandomValueRange(0);

        lootTable.addPool(new LootPool(lootEntries, lootConditions, rolls, bonusRolls, GravityMod.MOD_ID + ":anchors"));
        return true;
    }

    private static boolean addFishingJunkLoot(LootTable lootTable) {
        LootPool main = lootTable.getPool("main");
        if (main != null) {
            SetMetadata setMetadataFunction = new SetMetadata(NO_LOOT_CONDITIONS, new RandomValueRange(EnumGravityDirection.DOWN.ordinal()));
            LootFunction[] lootFunctions = {setMetadataFunction};

            LootEntry anchorEntry = new LootEntryItem(
                    StaticItems.GRAVITY_ANCHOR,//item
                    ConfigHandler.downAnchorFishingJunkWeight,//Same weight as fishing rod (smallest default weight, 2/94 chance to be picked if nothing else has been added to the pool)
                    0,//quality? 0 is default
                    lootFunctions,//loot functions
                    NO_LOOT_CONDITIONS,//loot conditions
                    StaticItems.GRAVITY_ANCHOR.getRegistryName().toString());//name
            main.addEntry(anchorEntry);
            return true;
        }
        return false;
    }
}

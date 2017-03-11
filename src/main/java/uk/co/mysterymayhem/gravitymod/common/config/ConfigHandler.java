package uk.co.mysterymayhem.gravitymod.common.config;

import gnu.trove.set.hash.TIntHashSet;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import uk.co.mysterymayhem.gravitymod.GravityMod;
import uk.co.mysterymayhem.gravitymod.asm.EntityPlayerWithGravity;
import uk.co.mysterymayhem.gravitymod.common.capabilities.gravitydirection.GravityDirectionCapability;
import uk.co.mysterymayhem.gravitymod.common.items.materials.ItemGravityDust;
import uk.co.mysterymayhem.gravitymod.common.listeners.ItemStackUseListener;
import uk.co.mysterymayhem.gravitymod.common.listeners.ItemStackUseListener.EnumItemStackUseCompat;
import uk.co.mysterymayhem.gravitymod.common.listeners.LootTableListener;
import uk.co.mysterymayhem.gravitymod.common.modsupport.prepostmodifier.CombinedPrePostModifier;
import uk.co.mysterymayhem.gravitymod.common.modsupport.prepostmodifier.EnumPrePostModifier;
import uk.co.mysterymayhem.gravitymod.common.modsupport.prepostmodifier.IPrePostModifier;

import java.io.File;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Created by Mysteryem on 07/03/2017.
 */
public class ConfigHandler {

    public static final String CATEGORY_MOD_COMPAT = newCategory("modcompat");
    public static String[] modCompatUseOnBlock;
    public static String[] modCompatUseGeneral;
    public static String[] modCompatOnStoppedUsing;
    //<mod id>:<item name>[:damage][:damage][...],<compatibility modifier>:[compatibility modifier]
    public static final Pattern modCompatPattern = Pattern.compile("[^:]+:[^:]+(:[\\d]+)*,[a-zA-Z]+(:[a-zA-Z]+)?");

    public static final String CATEGORY_ANTIMASS_INDUCER = newCategory("antimassinducer");
    public static int gravityDustAmountDropped;
    public static float gravityDustDropChance;
    public static boolean gravityDustChanceOncePerBrokenBlock;
    public static final Pattern validDropBlockOrePattern = Pattern.compile("[^:]+:[^:]+(:[\\d]+)?");
    public static String[] gravityDustValidBlocksToDropFrom;
    public static String[] gravityDustValidDropsToSpawnAs;

    public static final String CATEGORY_DESTABILISED_ANTIMASS = newCategory("destabilisedantimass");
    public static boolean destabilisedGravityDustDissipatesWhenDropped;

    public static final String CATEGORY_WORLD_GEN = newCategory("worldgen");
    public static boolean oreGenDimensionListIsBlackList;
    public static TIntHashSet oreGenDimensionIDs;
    public static boolean oreGenRetroGen;

    public static final String CATEGORY_LOOT = newCategory("extraloot");
    //TODO
    public static boolean addDownAnchorToFishingJunk;
    //TODO
    public static int downAnchorFishingJunkWeight;
    //TODO
    public static boolean addGravityDustToMineshafts;
    //TODO
    public static int gravityDustMineshaftWeight;
    public static float anchorChestLootChance;
    private static final String[] defaultAnchorLootTables = {
        "minecraft:chests/abandoned_mineshaft", "minecraft:chests/stronghold_corridor", "minecraft:chests/stronghold_crossing",
        "minecraft:chests/desert_pyramid", "minecraft:chests/jungle_temple", "minecraft:chests/simple_dungeon"};
    //TODO
    public static final HashMap<ResourceLocation, Predicate<LootTable>> lootTableAdditions = new HashMap<>();

    public static final String CATEGORY_GRAVITY = newCategory("gravity");
    public static float oppositeDirectionFallDistanceMultiplier;
    public static float otherDirectionsFallDistanceMultiplier;
    public static int numWeakGravityEnablersRequiredForWeakGravity;
    public static int numNormalGravityEnablersRequiredForNormalGravity;
    public static int numNormalEnablersWeakEnablersCountsAs;

    public static final String CATEGORY_GRAVITON_PEARL = newCategory("gravitonpearl");
    public static double gravitonPearlRange;
    public static float baseGravitonPearlStrength;

    public static final String CATEGORY_GRAVITY_GENERATOR = newCategory("gravitygenerator");
    public static int gravityGeneratorMaxHeight; // Min value 1
    public static int gravityGeneratorMaxRadius; // Width = (x*2 + 1) // Min value 0
    public static int gravityGeneratorMaxVolume;
    public static double gravityGeneratorMaxDistance;

    public static final String CATEGORY_CLIENT = newCategory("client");
    public static double transitionAnimationRotationSpeed;
    public static double transitionAnimationRotationLength;
    public static double transitionAnimationRotationEnd;

    public static final String CATEGORY_SERVER = newCategory("server");
    public static boolean kickPlayersWithMismatchingModCompatHashes;

    public static Configuration config;
    private static List<String> propertyOrder;
    private static String category;
    private static Property prop;
    private static Map<String, Set<String>> configNameToPropertyKeySet;

    public static void initialConfigLoad(FMLPreInitializationEvent event) {
        File file = event.getModConfigurationDirectory().toPath().resolve("UpAndDownAndAllAround.cfg").toFile();
        config = new Configuration(file);
        syncConfig(true);
    }

    public static void syncConfig(boolean load) {
        propertyOrder = new ArrayList<>();
        configNameToPropertyKeySet = new HashMap<>();

        if (load) {
            GravityMod.logInfo("Loading config");
            config.load();
        }
        else {
            GravityMod.logInfo("Reloading config");
        }

        nextCategory(CATEGORY_MOD_COMPAT);

        prop = config.get(category, EnumItemStackUseCompat.BLOCK.configName, new String[0], "Adding an item to this list will apply the " +
                "compatibility " +
                "when the item is right clicked " +
                "on a block. Specifically when the item's \"onItemUse\" method is called from within ItemStack::onItemUse.", modCompatPattern);
        modCompatUseOnBlock = process().getStringList();

        prop = config.get(category, EnumItemStackUseCompat.GENERAL.configName, new String[]{"tconstruct:rapier,relativeMotionAll:relativeRotation"}, "Adding an " +
                "item to this list will " +
                "apply the compatibility when the item is right clicked on air, or on a block that doesn't open a chest/machine GUI or otherwise change " +
                "state, such as right clicking on a vanilla lever. Specifically when the item's \"onItemRightClick\" method is called from within " +
                "ItemStack::useItemRightClick.\n" +

                "The Tinkers' Construct Rapier makes the player jump upwards and backwards slightly when right clicked. We want the player to jump upwards " +
                "relative to their view of the world, so we will need at least use relative Y motion for this compatibility. We also want the player to jump " +
                "backwards relative to their view of the world. The rapier uses the player's pitch and yaw rotations to calculate the backwards direction and" +
                " modifies X and Z motion accordingly, so we need them to be relative too.", modCompatPattern);
        modCompatUseGeneral = process().getStringList();

        prop = config.get(category, EnumItemStackUseCompat.STOPPED_USING.configName, new String[]{"tconstruct:longsword,relativeMotionAll:relativeRotation"}, "Adding an " +
                        "item to this list " +
                        "will apply the compatibility when a player stops 'using' an item. For bows, this is when you release right click to fire an arrow.\n",
                modCompatPattern);
        modCompatOnStoppedUsing = process().getStringList();

        config.setCategoryComment(category,
                "Up And Down changes how player motion is used to move the player. Some items may move the player in unexpected ways when used. If that's the" +
                        " case, try adding them to one of the below lists. The format is (text in square brackets is optional) \"<mod id>:<item " +
                        "name>[:damage][:damage][...],<compatibility modifier>:[compatibility modifier]\"\n" +

                        "Where a compatibility modifier is case insensitive and one of: relativeRotation, relativeMotionAll, relativeMotionX, " +
                        "relativeMotionY, relativeMotionZ, absoluteMotionX, absoluteMotionY or absoluteMotionZ\n" +

                        "These modify the player's rotation and/or motion before the item is used (and then puts the rotation/motion back to normal, taking " +
                        "into account any changes to motion, but NOT taking into account any changes to rotation).\n" +

                        "You may use a max of one type of modifier per entry (up to 1 motion modifier and up to 1 rotation modifier).\n" +

                        "The default state when using items is that both rotation and motion are absolute (as if the player currently has downwards gravity)" +
                        ".\n" +

                        "Using relativeMotionX implies that Z and Y motion will be absolute.\n" +

                        "Likewise, using absoluteMotionY implies that X and Z motion will be relative.\n" +

                        "So if you wanted both X and Y motion to be relative, you would use absoluteMotionZ.\n" +

                        "Examples:\n" +
                        "'minecraft:stick:0:4,relativeMotionX' - Adds a relative X motion modifier to vanilla sticks with damage values 0 and 4.\n" +
                        "'tconstruct:longsword,relativeMotionAll:relativeRotation' - Adds a relative X, Y and Z motion modifier combined with a relative " +
                        "rotation modifier to all damage values of Tinkers' Construct Longswords.");

        nextCategory(CATEGORY_ANTIMASS_INDUCER);

        prop = config.get(category, "amountDropped", 5, "The amount of anti-mass items that drop from each anti-mass spawn when mining \n" +
                "This is also the base amount dropped by Anti-Mass ore.\n" +
                "The exact amount dropped by Anti-Mass ore is a random value between this value and double this value", 1, 64);
        gravityDustAmountDropped = process().getInt();

        prop = config.get(category, "dropChance", 0.05d, "The chance that anti-mass will spawn", 0d, 1d);
        gravityDustDropChance = (float)process().getDouble();

        prop = config.get(category, "oneChancePerBrokenBlock", false, "If true, only the first valid drop of a block will have a chance to spawn anti-mass." +
                "\nIf false, each valid drop will have a chance to spawn anti-mass");
        gravityDustChanceOncePerBrokenBlock = process().getBoolean();

        prop = config.get(category, "validBlocksToSpawnFrom",
                new String[]{"ore:oreRedstone", "ore:oreDiamond", "ore:oreLapis", "ore:oreCoal", "ore:oreQuartz", "ore:oreEmerald"},
                "Blocks that, when broken, have a chance for some of their drops to become special 'anti-mass' items which float around and drop both the " +
                        "original item and the 'anti-mass' item added by this mod." +
                        "\nThe normal item drops must be in the config as valid drops to spawn as for this to happen." +
                        "\nUse format \"<modid>:<block name>[:<meta>]\" (not providing a metadata value will allow for any value)" +
                        "\nOre registry names can be specified by using \"ore:<ore name>\"" +
                        "\nEnsure you don't add any blocks that drop themselves when broken (like iron ore), to both the blocks and drops lists, otherwise " +
                        "players can place the block back down and mine it again for another chance at getting the special drop." +
                        "\nRedstone ore has been hardcoded such that if a player breaks lit redstone ore, the mod acts as if the player broke non-lit " +
                        "redstone ore.",
                validDropBlockOrePattern);
        gravityDustValidBlocksToDropFrom = process().getStringList();

        prop = config.get(category, "validDropsToSpawnAs",
                new String[]{"ore:dustRedstone", "ore:gemDiamond", "ore:gemEmerald", "ore:gemQuartz", "ore:gemLapis", "minecraft:coal:0"},
                "Items/blocks that, when dropped from a broken block, have a chance to become a special 'anti-mass' item that floats around and drops both " +
                        "the original item and the 'anti-mass' item added by this mod." +
                        "\nThe broken block must be in the config as a valid block for this to happen." +
                        "\n\nUse format \"<modid>:<block/item name>[:<meta>]\"" +
                        "\nOre registry names can be specified by using \"ore:<ore name>\"" +
                        "\n\n",
                validDropBlockOrePattern);
        gravityDustValidDropsToSpawnAs = process().getStringList();

        nextCategory(CATEGORY_DESTABILISED_ANTIMASS);

        prop = config.get(category, "destroyedWhenDropped", true, "Destabilised Anti-Mass should dissipate/be destroyed when dropped out of an " +
                "inventory");
        destabilisedGravityDustDissipatesWhenDropped = process().getBoolean();

        nextCategory(CATEGORY_WORLD_GEN);

        prop = config.get(category, "oreDimensionListIsBlacklist",
                true, "Set to true if the dimension ID list for ore generation should be treated as a Blacklist.\n" +
                        "Set to false if the dimension ID list for ore generation should be treated as a Whitelist.");
        oreGenDimensionListIsBlackList = process().getBoolean();

        prop = config.get(category, "oreDimensionList", new int[0], "List of dimension IDs that UpAndDown's ore generation should either generate in, or not generate in.\n" +
                "Depending on whether the list is set to act as a Whitelist or Blacklist. See \"oreDimensionListIsBlacklist\"");
        oreGenDimensionIDs = new TIntHashSet(process().getIntList());

        prop = config.get(category, "oreRetroGenEnabled", true, "Set to true to enable retroactive generation of ores.\n" +
                "This will generate ore in existing chunks that were generated before this mod was added or were in previously blacklisted/non-whitelisted " +
                "dimensions.");
        oreGenRetroGen = process().getBoolean();

        nextCategory(CATEGORY_LOOT);

        prop = config.get(category, "addDownAnchorToFishingJunk", true, "True if downwards gravity anchors should be added to the fishing junk loot table");
        addDownAnchorToFishingJunk = process().getBoolean();

        prop = config.get(category, "downAnchorFishingJunkWeight", 2, "'Weight' of the downwards gravity anchor added to the fishing junk loot table (higher " +
                "weight = higher chance based on the weights of everything else in the loot table)", 1, 100);
        downAnchorFishingJunkWeight = process().getInt();

        prop = config.get(category, "addGravityDustToMineshafts", true, "True if anti-mass should be added to mineshaft chest loot");
        addGravityDustToMineshafts = process().getBoolean();

        prop = config.get(category, "gravityDustMineshaftWeight", 5, "'Weight' of anti-mass added to mineshaft chests (higher " +
                "weight = higher chance based on the weights of everything else in the loot table)", 1, 100);
        gravityDustMineshaftWeight = process().getInt();

        prop = config.get(category, "lootTablesToAddAnchorsTo", defaultAnchorLootTables, "Loot tables to add gravity anchors to (their direction is chosen at" +
                " random)");
        String[] lootTablesToAddAnchorsTo = process().getStringList();

        lootTableAdditions.clear();
        if (addDownAnchorToFishingJunk) {
            lootTableAdditions.put(new ResourceLocation("minecraft:gameplay/fishing/junk"), LootTableListener.FISHING_JUNK_LOOT);
        }
        if (addGravityDustToMineshafts) {
            lootTableAdditions.put(new ResourceLocation("minecraft:chests/abandoned_mineshaft"), LootTableListener.ADD_GRAVITYDUST_TO_MINESHAFT);
        }
        for (String lootTable : lootTablesToAddAnchorsTo) {
            ResourceLocation resourceLocation = new ResourceLocation(lootTable);
            Predicate<LootTable> lootTablePredicate = lootTableAdditions.get(resourceLocation);
            if (lootTablePredicate == null) {
                lootTableAdditions.put(resourceLocation, LootTableListener.ADD_ANCHOR_POOL);
            }
            else {
                lootTableAdditions.put(resourceLocation, lootTablePredicate.and(LootTableListener.ADD_ANCHOR_POOL));
            }
        }

        prop = config.get(category, "generalAnchorChance", 0.05d, "The chance that one of the above loot tables will generate a gravity anchor", 0d, 1d);
        anchorChestLootChance = (float)process().getDouble();

        nextCategory(CATEGORY_GRAVITY);

        prop = config.get(category, "oppositeDirectionFallDistanceMultiplier", 0d, "When a player's gravity direction changes to the opposite direction, " +
                "their accrued fall distance will be multiplied by this value.", 0d, 1d);
        oppositeDirectionFallDistanceMultiplier = (float)process().getDouble();

        prop = config.get(category, "otherDirectionFallDistanceMultiplier", 0.5d, "When a player's gravity direction changes to a direction other than the " +
                "opposite direction, their accrued fall distance will be multiplied by this value.", 0d, 1d);
        otherDirectionsFallDistanceMultiplier = (float)process().getDouble();

        prop = config.get(category, "numWeakGravityEnablersRequiredForWeakGravity", 1, "Number of weak gravity enablers (armor + baubles if installed) that " +
                "must be worn for a player to be affected by weak gravity", 0, Integer.MAX_VALUE);
        numWeakGravityEnablersRequiredForWeakGravity = process().getInt();

        prop = config.get(category, "numNormalGravityEnablersRequiredForNormalGravity", 4, "Number of normal gravity enablers (armor + baubles if installed) " +
                "that must be worn for a player to be affected by normal strength gravity", 0, Integer.MAX_VALUE);
        numNormalGravityEnablersRequiredForNormalGravity = process().getInt();

        prop = config.get(category, "numNormalEnablersWeakEnablersCountAs", 4, "Weak gravity enablers count as this many normal gravity enablers.\n" +
                "This makes more sense thematically to be greater than 1, but '1' or '0' will still work.");
        numNormalEnablersWeakEnablersCountsAs = process().getInt();

        nextCategory(CATEGORY_GRAVITON_PEARL);

        prop = config.get(category, "gravitonPearlRange", 7.5d, "Range of the Graviton Pearl's push and pull effect", 0d, 20d);
        gravitonPearlRange = process().getDouble();

        prop = config.get(category, "baseGravitonPearlStrength", 0.05d, "Set the strength of the Graviton Pearl's push and pull effect." +
                "\nPulled items and pushed projectiles are affected more than other entities", 0d, 1d);
        baseGravitonPearlStrength = (float)process().getDouble();

        nextCategory(CATEGORY_GRAVITY_GENERATOR);
        prop = config.get(category, "maxHeight", 11, "The maximum relative Y height of gravity fields created by gravity generators, in blocks", 1, 255);
        gravityGeneratorMaxHeight = process().getInt();

        prop = config.get(category, "maxRadius", 5, "The maximum relative X/Z radius of gravity fields created by gravity generators, in blocks.\nThe maximum" +
                " width is equal to double this value, plus one.", 0, 127);
        gravityGeneratorMaxRadius = process().getInt();
        gravityGeneratorMaxVolume = gravityGeneratorMaxHeight * (2 * gravityGeneratorMaxRadius + 1) * (2 * gravityGeneratorMaxRadius + 1);
        gravityGeneratorMaxDistance =
                (gravityGeneratorMaxRadius + 0.5) * (gravityGeneratorMaxRadius + 0.5)
                        + (gravityGeneratorMaxRadius + 0.5) * (gravityGeneratorMaxRadius + 0.5)
                        + gravityGeneratorMaxHeight * gravityGeneratorMaxHeight;

        nextCategory(CATEGORY_CLIENT, false);
        prop = config.get(category, "rotationAnimationSpeed", 1.5d, "Animation speed for gravity transition." +
                "Takes 1 second divided by this config value.", 1d, 1000d);
        transitionAnimationRotationSpeed = process().getDouble();
        transitionAnimationRotationLength = GravityDirectionCapability.DEFAULT_TIMEOUT / transitionAnimationRotationSpeed;
        transitionAnimationRotationEnd = GravityDirectionCapability.DEFAULT_TIMEOUT - transitionAnimationRotationLength;

        nextCategory(CATEGORY_SERVER);
        prop = config.get(category, "kickPlayersWithMismatchingModCompat", false, "True if the server should kick players that have different mod " +
                "compatibility settings.");
        kickPlayersWithMismatchingModCompatHashes = process().getBoolean();

        // Needed to set the ordering of the current category
        nextCategory(null);

        cleanup();

        if (config.hasChanged()) {
            config.save();
        }

        GravityMod.logInfo("Loaded config");
    }

    public static void processLateConfig() {
        ItemGravityDust.BlockBreakListener.clearAcceptableBlocksAndDrops();
        ItemGravityDust.BlockBreakListener.addBlocksFromConfig(gravityDustValidBlocksToDropFrom);
        ItemGravityDust.BlockBreakListener.addDropsFromConfig(gravityDustValidDropsToSpawnAs);
        ItemStackUseListener.clearPrePostModifiers();
        processModCompatConfig(modCompatUseOnBlock, EnumItemStackUseCompat.BLOCK);
        processModCompatConfig(modCompatUseGeneral, EnumItemStackUseCompat.GENERAL);
        processModCompatConfig(modCompatOnStoppedUsing, EnumItemStackUseCompat.STOPPED_USING);
        ItemStackUseListener.makeHash();
        if (GravityMod.GENERAL_DEBUG) {
            GravityMod.logInfo("HashCode: " + ItemStackUseListener.getHashCode());
        }
    }

    private static void cleanup() {
        category = null;
        prop = null;
        Set<String> categoryNames = config.getCategoryNames();
        for (String categoryName : categoryNames) {
            Set<String> knownPropertyKeys = configNameToPropertyKeySet.get(categoryName);
            if (knownPropertyKeys == null) {
                config.removeCategory(config.getCategory(categoryName));
            }
            else {
                ConfigCategory category = config.getCategory(categoryName);
                Set<String> categoryPropertyKeys = category.keySet();
                for (Iterator<String> iterator = categoryPropertyKeys.iterator(); iterator.hasNext(); /**/) {
                    if (!knownPropertyKeys.contains(iterator.next())) {
                        iterator.remove();
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(GravityMod.MOD_ID)) {
            syncConfig(false);
            processLateConfig();
        }
    }

    private static void nextCategory(String categoryName) {
        nextCategory(categoryName, true);
    }

    private static void nextCategory(String categoryName, boolean requiresMCRestart) {
        if (category != null) {
            setCategoryOrder();
        }
        category = categoryName;
        if (category != null) {

            config.setCategoryRequiresWorldRestart(category, requiresMCRestart);
        }
    }

    private static Property process(Property prop) {
        setLangKey(prop);
        order(prop);
        Set<String> propKeys = configNameToPropertyKeySet.get(category);
        if (propKeys == null) {
            propKeys = new HashSet<>();
            configNameToPropertyKeySet.put(category, propKeys);
        }
        propKeys.add(prop.getName());
        return prop;
    }

    private static void setCategoryOrder() {
        config.setCategoryPropertyOrder(category, propertyOrder);
        propertyOrder = new ArrayList<>();
    }

    private static void setLangKey(Property prop) {
        setLangKey(prop, category);
    }

    private static void order(Property prop) {
        propertyOrder.add(prop.getName());
    }

    private static void setLangKey(Property prop, String category) {
        StringBuilder builder = new StringBuilder();
        builder.append("config.").append(GravityMod.MOD_ID).append('.');
        // Removes "category_" from the front and then replaces '_' with '.'
        category = category.substring(category.indexOf('_') + 1).replace('_', '.');
        builder.append(category);

        String propName = prop.getName();
//        propName = propName.substring(propName.lastIndexOf('_') + 1);
        builder.append('.').append(propName);

        prop.setLanguageKey(builder.toString());
    }

    private static void processModCompatConfig(String[] stringList, EnumItemStackUseCompat compatType) {
        toNextEntry:
        for (String entry : stringList) {
            String[] sections = entry.split(",");
            if (sections.length != 2) {
                GravityMod.logWarning("Invalid config line in %s: %s. Each line should have a single comma " +
                        "separating item information and the compatibility information.", compatType.configName, entry);
                continue;
            }

            String[] itemResourceAndDamageValues = sections[0].split(":");
            if (itemResourceAndDamageValues.length < 2) {
                GravityMod.logWarning("Invalid config line in %s: %s. A colon is required between the item's mod ID and the item's name.", compatType.configName, entry);
                continue;
            }
            String modID = itemResourceAndDamageValues[0];
            String itemName = itemResourceAndDamageValues[1];
            int[] damageValues = new int[itemResourceAndDamageValues.length - 2];
            for (int i = 2; i < itemResourceAndDamageValues.length; i++) {
                try {
                    damageValues[i - 2] = Integer.parseInt(itemResourceAndDamageValues[i]);
                } catch (NumberFormatException numberFormatException) {
                    GravityMod.logWarning("Invalid config line in %s: %s. Could not parse item damage value \"%s\".",
                            compatType.configName, entry, itemResourceAndDamageValues[i]);
                    continue toNextEntry;
                }
            }

            IPrePostModifier<EntityPlayerWithGravity> modifier;

            String[] modifierData = sections[1].split(":");
            switch (modifierData.length) {
                case 1:
                    EnumPrePostModifier fromConfigString = EnumPrePostModifier.getFromConfigString(modifierData[0]);
                    if (fromConfigString == null) {
                        GravityMod.logWarning("Invalid config line in %s: %s. Invalid compatibility modifier \"%s\"",
                                compatType.configName, entry, modifierData[0]);
                        continue toNextEntry;
                    }
                    modifier = fromConfigString;
                    break;
                case 2:
                    EnumPrePostModifier first = EnumPrePostModifier.getFromConfigString(modifierData[0]);
                    if (first == null) {
                        GravityMod.logWarning("Invalid config line in %s: %s. Invalid compatibility modifier \"%s\"",
                                compatType.configName, entry, modifierData[0]);
                        continue toNextEntry;
                    }
                    EnumPrePostModifier second = EnumPrePostModifier.getFromConfigString(modifierData[1]);
                    if (second == null) {
                        GravityMod.logWarning("Invalid config line in %s: %s. Invalid compatibility modifier \"%s\"",
                                compatType.configName, entry, modifierData[1]);
                        continue toNextEntry;
                    }
                    if (first.isMotionModifier() == second.isMotionModifier()) {
                        GravityMod.logWarning("Invalid config line in %s: %s. Only up to 1 motion modifier and up to 1 rotation modifier may be used.",
                                compatType.configName, entry);
                        continue toNextEntry;
                    }
                    modifier = CombinedPrePostModifier.getModifierFor(first, second);
                    break;
                default:
                    GravityMod.logWarning("Invalid config line in %s: %s. Expecting 1 or 2 compatibility modifiers only.", compatType.configName, entry);
                    continue toNextEntry;
            }
            ItemStackUseListener.addPrePostModifier(modID, itemName, modifier, compatType, damageValues);
        }
    }

    private static String newCategory(String baseName) {
        return "category_" + baseName.replace(' ', '.').toLowerCase(Locale.ENGLISH);
    }

    private static Property process() {
        return process(prop);
    }
}

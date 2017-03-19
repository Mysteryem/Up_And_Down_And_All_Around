package uk.co.mysterymayhem.gravitymod.common.listeners;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import uk.co.mysterymayhem.gravitymod.common.config.ConfigHandler;
import uk.co.mysterymayhem.gravitymod.common.registries.StaticPotions;

import java.util.Random;
import java.util.WeakHashMap;

//TODO: The treating of remote/non-remote players needs to be cleaned up, see FallOutOfWorldUpwardsListenerClient
/**
 * Created by Mysteryem on 11/03/2017.
 */
public class FallOutOfWorldUpwardsListenerCommon {

    // Slow damage (same as drown damage (reduced by protection/resistance))
    private static final DamageSource[] SOURCES_ASPHYXIATION =
            createDamageSources("mysttmtgravitymod_asphyxiation", 2, true, false, false);
    // Fast damage
    private static final DamageSource[] SOURCES_BLOOD_BOIL =
            createDamageSources("mysttmtgravitymod_bloodboil", 2, true, true, true);
    // Instant death
    // Has all the cool death messages
    private static final DamageSource[] SOURCES_OUTERSPACE =
            createDamageSources("mysttmtgravitymod_outerspace", 10, true, true, true);
    private static final Random SHARED_RANDOM = new Random();
    private final WeakHashMap<EntityPlayer, Integer> serverMap = new WeakHashMap<>();

    public static DamageSource getBloodBoilDamageSource() {
        return randomFromArray(SOURCES_BLOOD_BOIL);
    }

    private static <T> T randomFromArray(T[] array) {
        return array[SHARED_RANDOM.nextInt(array.length)];
    }

    private static DamageSource[] createDamageSources(String baseName, int count, boolean bypassArmour, boolean absolute, boolean hurtCreative) {
        DamageSource[] sources = new DamageSource[count];
        for (int i = 0; i < sources.length; i++) {
            DamageSource damageSource = new DamageSource(baseName + i);
            if (bypassArmour) {
                damageSource.setDamageBypassesArmor();
            }
            if (absolute) {
                damageSource.setDamageIsAbsolute();
            }
            if (hurtCreative) {
                damageSource.setDamageAllowedInCreativeMode();
            }
            sources[i] = damageSource;
        }
        return sources;
    }

    /**
     *
     * @param player whose worldObj is NOT remote
     */
    protected void processServerPlayer(EntityPlayer player) {
        double posY = player.posY;
        //TODO: Find out if there's a better way to refresh the potion effect
        if (posY > ConfigHandler.yHeightNoAir) {
            player.addPotionEffect(new PotionEffect(StaticPotions.ASPHYXIATION, 32767, 0, false, false));
        }
        else {
            PotionEffect activePotionEffect = player.getActivePotionEffect(StaticPotions.ASPHYXIATION);
            if (activePotionEffect != null && activePotionEffect.getDuration() == 32766) {
                player.removePotionEffect(StaticPotions.ASPHYXIATION);
            }
        }

        if (posY > ConfigHandler.yHeightFreeze) {
            player.addPotionEffect(new PotionEffect(StaticPotions.FREEZING, 32767, 0, false, false));
        }
        else {
            PotionEffect activePotionEffect = player.getActivePotionEffect(StaticPotions.FREEZING);
            if (activePotionEffect != null && activePotionEffect.getDuration() == 32766) {
                player.removePotionEffect(StaticPotions.FREEZING);
            }
        }

        if (posY > ConfigHandler.yHeightBoil) {
            player.addPotionEffect(new PotionEffect(StaticPotions.BLOODBOIL, 32767, 0, false, false));
        }
        else {
            PotionEffect activePotionEffect = player.getActivePotionEffect(StaticPotions.BLOODBOIL);
            if (activePotionEffect != null && activePotionEffect.getDuration() == 32766) {
                player.removePotionEffect(StaticPotions.BLOODBOIL);
            }
        }

        if (posY > ConfigHandler.yHeightInstantDeath) {
            player.attackEntityFrom(randomFromArray(SOURCES_OUTERSPACE), Float.MAX_VALUE);
        }
    }

    /**
     * Common is by default a server environment, the client listener overrides this method in order to differentiate between client and server players
     * @param player
     */
    protected void processSidedPlayer(EntityPlayer player) {
        this.processServerPlayer(player);
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        EntityPlayer player = event.player;
        if (event.phase == TickEvent.Phase.END) {
            this.processSidedPlayer(player);

            // Set and get special air
            if (player.isPotionActive(StaticPotions.ASPHYXIATION)) {
                Integer specialAirBox = this.getSpecialAir(player);
                int specialAir;
                if (specialAirBox == null) {
                    specialAir = player.getAir();
                }
                else {
                    specialAir = specialAirBox;
                }

                // Decrease air
                specialAir--;


                if (specialAir <= -20)
                {
                    this.setSpecialAir(player, 0);

                    player.attackEntityFrom(randomFromArray(SOURCES_ASPHYXIATION), 2.0F);
                }
                else {
                    this.setSpecialAir(player, specialAir);
                }
            }
            else {
                this.setSpecialAir(player, 300);
            }
        }
    }

    protected void incrementFreezeCounter(EntityPlayer player) {}
    protected void decrementFreezeCounter(EntityPlayer player) {}

    public void setSpecialAir(EntityPlayer player, int air) {
        this.serverMap.put(player, air);
    }

    public Integer getSpecialAir(EntityPlayer player) {
        return this.serverMap.get(player);
    }

//    private static int decreaseAir(int inAir) {
//        return i > 0 && this.rand.nextInt(i + 1) > 0 ? air : air - 1;
//    }
}

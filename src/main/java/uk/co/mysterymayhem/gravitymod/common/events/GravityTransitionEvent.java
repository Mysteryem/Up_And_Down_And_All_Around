package uk.co.mysterymayhem.gravitymod.common.events;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import static net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import uk.co.mysterymayhem.gravitymod.api.EnumGravityDirection;

/**
 * Created by Mysteryem on 2016-08-04.
 */
public class GravityTransitionEvent<T extends EntityPlayer> extends Event {

    public final EnumGravityDirection newGravityDirection;
    public final EnumGravityDirection oldGravityDirection;
    public final T player;
    public final Side side;
    public final Phase phase;

    private GravityTransitionEvent(EnumGravityDirection newGravityDirection, EnumGravityDirection oldGravityDirection, T player, Side side, Phase phase) {
        this.newGravityDirection = newGravityDirection;
        this.oldGravityDirection = oldGravityDirection;
        this.player = player;
        this.side = side;
        this.phase = phase;
    }

    public static class Server extends GravityTransitionEvent<EntityPlayerMP> {
        private Server(EnumGravityDirection newGravityDirection, EnumGravityDirection oldGravityDirection, EntityPlayerMP player, Phase phase) {
            super(newGravityDirection, oldGravityDirection, player, Side.SERVER, phase);
        }

        @Override
        public boolean isCancelable() {
            return true;
        }

        public static class Pre extends Server {
            public Pre(EnumGravityDirection newGravityDirection, EnumGravityDirection oldGravityDirection, EntityPlayerMP player) {
                super(newGravityDirection, oldGravityDirection, player, Phase.START);
            }
        }

        public static class Post extends Server {
            public Post(EnumGravityDirection newGravityDirection, EnumGravityDirection oldGravityDirection, EntityPlayerMP player) {
                super(newGravityDirection, oldGravityDirection, player, Phase.START);
            }
        }
    }

    public static class Clone extends GravityTransitionEvent<EntityPlayerMP> {
        public final EntityPlayerMP oldPlayer;
        public final PlayerEvent.Clone cloneEvent;

        private Clone(EnumGravityDirection newGravityDirection, EnumGravityDirection oldGravityDirection, EntityPlayerMP newPlayer, Phase phase, EntityPlayerMP oldPlayer, PlayerEvent.Clone cloneEvent) {
            super(newGravityDirection, oldGravityDirection, newPlayer, Side.SERVER, phase);
            this.oldPlayer = oldPlayer;
            this.cloneEvent = cloneEvent;
        }

        public static class Pre extends Clone {
            public Pre(EnumGravityDirection newGravityDirection, EnumGravityDirection oldGravityDirection, EntityPlayerMP newPlayer, EntityPlayerMP oldPlayer, PlayerEvent.Clone cloneEvent) {
                super(newGravityDirection, oldGravityDirection, newPlayer, Phase.START, oldPlayer, cloneEvent);
            }
        }

        public static class Post extends Clone {
            public Post(EnumGravityDirection newGravityDirection, EnumGravityDirection oldGravityDirection, EntityPlayerMP newPlayer, EntityPlayerMP oldPlayer, PlayerEvent.Clone cloneEvent) {
                super(newGravityDirection, oldGravityDirection, newPlayer, Phase.START, oldPlayer, cloneEvent);
            }
        }
    }

    public static class Client extends GravityTransitionEvent<AbstractClientPlayer> {
        private Client(EnumGravityDirection newGravityDirection, EnumGravityDirection oldGravityDirection, AbstractClientPlayer player, Phase phase) {
            super(newGravityDirection, oldGravityDirection, player, Side.CLIENT, phase);
        }

        public static class Pre extends Client {
            public Pre(EnumGravityDirection newGravityDirection, EnumGravityDirection oldGravityDirection, AbstractClientPlayer player) {
                super(newGravityDirection, oldGravityDirection, player, Phase.START);
            }
        }

        public static class Post extends Client {
            public Post(EnumGravityDirection newGravityDirection, EnumGravityDirection oldGravityDirection, AbstractClientPlayer player) {
                super(newGravityDirection, oldGravityDirection, player, Phase.START);
            }
        }
    }
}

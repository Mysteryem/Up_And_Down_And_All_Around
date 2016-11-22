package uk.co.mysterymayhem.gravitymod.api.events;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.relauncher.Side;
import uk.co.mysterymayhem.gravitymod.api.EnumGravityDirection;

import static net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

/**
 * Events fired before and after player gravity is set
 * Created by Mysteryem on 2016-08-04.
 */
@SuppressWarnings("WeakerAccess")
public class GravityTransitionEvent<T extends EntityPlayer> extends Event {

    public final EnumGravityDirection newGravityDirection;
    public final EnumGravityDirection oldGravityDirection;
    public final T player;
    public final Side side;
    public final Phase phase;

    public GravityTransitionEvent(EnumGravityDirection newGravityDirection, EnumGravityDirection oldGravityDirection, T player, Side side, Phase phase) {
        this.newGravityDirection = newGravityDirection;
        this.oldGravityDirection = oldGravityDirection;
        this.player = player;
        this.side = side;
        this.phase = phase;
    }

    /**
     * Superclass for server side GravityTransitionEvents
     * @see Clone.Pre
     * @see Clone.Post
     */
    public static class Server extends GravityTransitionEvent<EntityPlayerMP> {
        public Server(EnumGravityDirection newGravityDirection, EnumGravityDirection oldGravityDirection, EntityPlayerMP player, Phase phase) {
            super(newGravityDirection, oldGravityDirection, player, Side.SERVER, phase);
        }

        /**
         * Called server side immediately before a player's gravity direction changes
         * If the event is cancelled, gravity direction is not changed
         */
        public static class Pre extends Server {
            public Pre(EnumGravityDirection newGravityDirection, EnumGravityDirection oldGravityDirection, EntityPlayerMP player) {
                super(newGravityDirection, oldGravityDirection, player, Phase.START);
            }

            @Override
            public boolean isCancelable() {
                return true;
            }
        }

        /**
         * Called server side immediately after a player's gravity direction changes
         */
        public static class Post extends Server {
            public Post(EnumGravityDirection newGravityDirection, EnumGravityDirection oldGravityDirection, EntityPlayerMP player) {
                super(newGravityDirection, oldGravityDirection, player, Phase.END);
            }
        }

        /**
         * Superclass for special handling of server side GravityTransitionEvents when players are cloned, but not due to death.
         * Usually occurs when players return from the end.
         * @see GravityTransitionEvent.Server.Clone.Pre
         * @see GravityTransitionEvent.Server.Clone.Post
         */
        public static class Clone extends Server {
            public final EntityPlayerMP oldPlayer;
            public final PlayerEvent.Clone cloneEvent;

            public Clone(EnumGravityDirection newGravityDirection, EnumGravityDirection oldGravityDirection, EntityPlayerMP newPlayer, Phase phase, EntityPlayerMP oldPlayer, PlayerEvent.Clone cloneEvent) {
                super(newGravityDirection, oldGravityDirection, newPlayer, phase);
                this.oldPlayer = oldPlayer;
                this.cloneEvent = cloneEvent;
            }

            /**
             * Called server side immediately before a cloned player has their gravity direction set to the original player object's gravity
             * direction, but not due to death. This will usually occur when a player returns from the End.
             */
            public static class Pre extends Clone {
                public Pre(EnumGravityDirection newGravityDirection, EnumGravityDirection oldGravityDirection, EntityPlayerMP newPlayer, EntityPlayerMP oldPlayer, PlayerEvent.Clone cloneEvent) {
                    super(newGravityDirection, oldGravityDirection, newPlayer, Phase.START, oldPlayer, cloneEvent);
                }
            }

            /**
             * Called server side immediately after a cloned player has their gravity direction set to the original player object's gravity
             * direction, but not due to death. This will usually occur when a player returns from the End.
             */
            public static class Post extends Clone {
                public Post(EnumGravityDirection newGravityDirection, EnumGravityDirection oldGravityDirection, EntityPlayerMP newPlayer, EntityPlayerMP oldPlayer, PlayerEvent.Clone cloneEvent) {
                    super(newGravityDirection, oldGravityDirection, newPlayer, Phase.END, oldPlayer, cloneEvent);
                }
            }
        }
    }

    /**
     * Superclass for client side GravityTransitionEvents
     * @see uk.co.mysterymayhem.gravitymod.api.events.GravityTransitionEvent.Client.Pre
     * @see uk.co.mysterymayhem.gravitymod.api.events.GravityTransitionEvent.Client.Post
     */
    public static class Client extends GravityTransitionEvent<AbstractClientPlayer> {
        public Client(EnumGravityDirection newGravityDirection, EnumGravityDirection oldGravityDirection, AbstractClientPlayer player, Phase phase) {
            super(newGravityDirection, oldGravityDirection, player, Side.CLIENT, phase);
        }

        /**
         * Called immediately before the client changes the gravity direction of a client player due to gravity direction information sent from the server.
         * It's possible that the new and old gravity directions will be the same, though this behaviour usually indicates that
         * a bug has occurred.
         */
        public static class Pre extends Client {
            public Pre(EnumGravityDirection newGravityDirection, EnumGravityDirection oldGravityDirection, AbstractClientPlayer player) {
                super(newGravityDirection, oldGravityDirection, player, Phase.START);
            }
        }

        /**
         * Called immediately after the client changes the gravity direction of a client player due to gravity direction information sent from the server.
         * It's possible that the new and old gravity directions will be the same, though this behaviour usually indicates that
         * a bug has occurred.
         */
        public static class Post extends Client {
            public Post(EnumGravityDirection newGravityDirection, EnumGravityDirection oldGravityDirection, AbstractClientPlayer player) {
                super(newGravityDirection, oldGravityDirection, player, Phase.END);
            }
        }
    }
}

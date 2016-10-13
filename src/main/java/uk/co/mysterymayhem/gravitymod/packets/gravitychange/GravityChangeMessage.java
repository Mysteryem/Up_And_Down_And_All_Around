package uk.co.mysterymayhem.gravitymod.packets.gravitychange;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import uk.co.mysterymayhem.gravitymod.api.EnumGravityDirection;

import java.util.Collection;

/**
 * Created by Mysteryem on 2016-08-07.
 */
public class GravityChangeMessage implements IMessage {

    public GravityChangeMessage(){

    }

    String toSend;
    EnumGravityDirection newGravityDirection;
    private EnumPacketType packetType;
    Collection<String> toSendMultiple;
    boolean noTimeout;

    public EnumPacketType getPacketType() {
        return this.packetType;
    }

    public String getStringData() {
        return this.toSend;
    }

    public boolean getNoTimeout() {
        return this.noTimeout;
    }

    public EnumGravityDirection getNewGravityDirection() {
        return this.newGravityDirection;
    }

    public GravityChangeMessage(String stringToSend, EnumGravityDirection newGravityDirection, boolean noTimeout) {
        this.toSend = stringToSend;
        this.newGravityDirection = newGravityDirection;
        this.noTimeout = noTimeout;
        this.packetType = EnumPacketType.SINGLE;
    }

    public GravityChangeMessage(Collection<String> stringsToSend, boolean noTimeout) {
        this.toSendMultiple = stringsToSend;
        this.packetType = EnumPacketType.ALL_UPSIDE_DOWN;
    }

    public GravityChangeMessage(String playerToRequest) {
        this.toSend = playerToRequest;
        this.packetType = EnumPacketType.CLIENT_REQUEST_GRAVITY_OF_PLAYER;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.packetType.ordinal());
        this.packetType.toBytesFunction.accept(this, buf);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int packetTypeOrdinal = buf.readInt();
        EnumPacketType type = EnumPacketType.values()[packetTypeOrdinal];
        this.packetType = type;
        type.fromBytesFunction.accept(this, buf);
    }

}

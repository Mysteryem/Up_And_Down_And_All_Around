package uk.co.mysterymayhem.gravitymod.common.packets.gravitychange;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import uk.co.mysterymayhem.gravitymod.api.EnumGravityDirection;
import uk.co.mysterymayhem.mystlib.annotations.UsedReflexively;

/**
 * Created by Mysteryem on 2016-08-07.
 */
public class GravityChangeMessage implements IMessage {

    EnumGravityDirection newGravityDirection;
    boolean noTimeout;
    String toSend;
    private EnumChangePacketType packetType;

    @UsedReflexively
    public GravityChangeMessage() {/**/}

    public GravityChangeMessage(String stringToSend, EnumGravityDirection newGravityDirection, boolean noTimeout) {
        this.toSend = stringToSend;
        this.newGravityDirection = newGravityDirection;
        this.noTimeout = noTimeout;
        this.packetType = EnumChangePacketType.SINGLE;
    }

    public GravityChangeMessage(String playerToRequest) {
        this.toSend = playerToRequest;
        this.packetType = EnumChangePacketType.CLIENT_REQUEST_GRAVITY_OF_PLAYER;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        final int packetTypeOrdinal = buf.readInt();
        final EnumChangePacketType type = EnumChangePacketType.values()[packetTypeOrdinal];
        this.packetType = type;
        type.readFromBuff(this, buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.packetType.ordinal());
        this.packetType.writeToBuff(this, buf);
    }

    public EnumGravityDirection getNewGravityDirection() {
        return this.newGravityDirection;
    }

    public boolean getNoTimeout() {
        return this.noTimeout;
    }

    public EnumChangePacketType getPacketType() {
        return this.packetType;
    }

    public String getStringData() {
        return this.toSend;
    }

}

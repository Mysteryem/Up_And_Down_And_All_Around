package uk.co.mysterymayhem.gravitymod.common.packets.config;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import uk.co.mysterymayhem.mystlib.annotations.UsedReflexively;

/**
 * Created by Mysteryem on 2016-10-24.
 */
public class ModCompatConfigCheckMessage implements IMessage {

    private int modCompatHashcode;
    private EnumConfigPacketType packetType;
    private ByteBuf configData;

    @UsedReflexively
    public ModCompatConfigCheckMessage() {/**/}

    public ModCompatConfigCheckMessage(int modCompatHashcode) {
        this(EnumConfigPacketType.SERVER_TO_CLIENT_SERVER_HASH_CHECK);
        this.modCompatHashcode = modCompatHashcode;
    }

    public ModCompatConfigCheckMessage(EnumConfigPacketType type) {
        this.packetType = type;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.packetType = EnumConfigPacketType.values()[buf.readInt()];
        this.packetType.readFromBuff(this, buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.packetType.ordinal());
        this.packetType.writeToBuff(this, buf);
    }

    public int getModCompatHashcode() {
        return this.modCompatHashcode;
    }

    public EnumConfigPacketType getPacketType() {
        return this.packetType;
    }

    public ByteBuf getConfigData() {
        return this.configData;
    }

    void setConfigData(ByteBuf buf) {
        this.configData = buf;
    }

    void setModCompatHashcode(int modCompatHashcode) {
        this.modCompatHashcode = modCompatHashcode;
    }
}

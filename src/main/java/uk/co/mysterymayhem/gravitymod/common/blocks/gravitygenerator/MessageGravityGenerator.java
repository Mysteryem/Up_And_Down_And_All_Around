package uk.co.mysterymayhem.gravitymod.common.blocks.gravitygenerator;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import uk.co.mysterymayhem.gravitymod.GravityMod;
import uk.co.mysterymayhem.mystlib.annotations.UsedReflexively;

/**
 * Created by Mysteryem on 2017-01-10.
 */
public class MessageGravityGenerator implements IMessage {
    private int newXRadius;
    private int newYHeight;
    private int newZRadius;
    private boolean sendX;
    private boolean sendY;
    private boolean sendZ;
    private int tileX;
    private int tileY;
    private int tileZ;

    @UsedReflexively
    public MessageGravityGenerator() {/**/}

    public MessageGravityGenerator(TileGravityGenerator tile, int xRadius, int yHeight, int zRadius, boolean sendX, boolean sendY, boolean sendZ) {
        BlockPos pos = tile.getPos();
        this.tileX = pos.getX();
        this.tileY = pos.getY();
        this.tileZ = pos.getZ();
        this.newXRadius = xRadius;
        this.newYHeight = yHeight;
        this.newZRadius = zRadius;
        this.sendX = sendX;
        this.sendY = sendY;
        this.sendZ = sendZ;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.tileX = buf.readInt();
        this.tileY = buf.readInt();
        this.tileZ = buf.readInt();
        this.sendX = buf.readBoolean();
        this.sendY = buf.readBoolean();
        this.sendZ = buf.readBoolean();
        if (this.sendX) {
            this.newXRadius = buf.readInt();
        }
        if (this.sendY) {
            this.newYHeight = buf.readInt();
        }
        if (this.sendZ) {
            this.newZRadius = buf.readInt();
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.tileX);
        buf.writeInt(this.tileY);
        buf.writeInt(this.tileZ);
        buf.writeBoolean(this.sendX);
        buf.writeBoolean(this.sendY);
        buf.writeBoolean(this.sendZ);
        if (this.sendX) {
            buf.writeInt(this.newXRadius);
        }
        if (this.sendY) {
            buf.writeInt(this.newYHeight);
        }
        if (this.sendZ) {
            buf.writeInt(this.newZRadius);
        }
    }

    void process(EntityPlayerMP playerMP) {
        if (playerMP != null) {
            World worldObj = playerMP.world;
            if (worldObj != null) {
                TileEntity tileEntity = worldObj.getTileEntity(new BlockPos(this.tileX, this.tileY, this.tileZ));
                if (tileEntity instanceof TileGravityGenerator) {
                    TileGravityGenerator tileGravityGenerator = (TileGravityGenerator)tileEntity;
                    if (this.sendX) {
                        tileGravityGenerator.setRelativeXRadius(this.newXRadius);
                    }
                    if (this.sendY) {
                        tileGravityGenerator.setRelativeYHeight(this.newYHeight);
                    }
                    if (this.sendZ) {
                        tileGravityGenerator.setRelativeZRadius(this.newZRadius);
                    }
                    if (worldObj instanceof WorldServer) {
                        BlockPos pos = tileGravityGenerator.getPos();
                        if (worldObj.isBlockLoaded(pos)) {
                            IBlockState blockState = worldObj.getBlockState(pos);
                            // I don't know what value this should be
                            final int flag = 3;
                            worldObj.notifyBlockUpdate(pos, blockState, blockState, flag);
                        }
                    }
                    else {
                        GravityMod.logWarning("%s' world object is a %s and not a WorldServer, something's not right", playerMP, worldObj.getClass());
                    }
                }
                else if (tileEntity == null) {
                    GravityMod.logWarning("Received invalid MessageGravityGenerator from %s, no tile entity at %d, %d, %d", playerMP, this.tileX, this.tileY, this.tileZ);
                }
                else {
                    GravityMod.logWarning("Received invalid MessageGravityGenerator from %s, found a %s at %d, %d, %d", playerMP, tileEntity.getClass(), this.tileX, this.tileY, this.tileZ);
                }
            }
        }
    }
}

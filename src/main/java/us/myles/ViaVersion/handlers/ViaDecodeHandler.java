package us.myles.ViaVersion.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.ByteToMessageDecoder;
import us.myles.ViaVersion.CancelException;
import us.myles.ViaVersion.ConnectionInfo;
import us.myles.ViaVersion.util.PacketUtil;
import us.myles.ViaVersion.transformers.IncomingTransformer;

import java.util.List;

public class ViaDecodeHandler extends ByteToMessageDecoder {
    private final IncomingTransformer incomingTransformer;
    private final ByteToMessageDecoder minecraftDecoder;
    private final ConnectionInfo info;

    public ViaDecodeHandler(ConnectionInfo info, ByteToMessageDecoder minecraftDecoder) {
        this.info = info;
        this.minecraftDecoder = minecraftDecoder;
        this.incomingTransformer = new IncomingTransformer(info);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf bytebuf, List<Object> list) throws Exception {
        // use transformers
        if(bytebuf.readableBytes() > 0) {
            if(info.isActive()) {
                int id = PacketUtil.readVarInt(bytebuf);
                // Transform
                ByteBuf newPacket = ctx.alloc().buffer();
                try {
                    incomingTransformer.transform(id, bytebuf, newPacket);
                    bytebuf = newPacket;
                } catch (CancelException e) {
                    return;
                }
            }
            // call minecraft decoder
            list.addAll(PacketUtil.callDecode(this.minecraftDecoder, ctx, bytebuf));
        }
    }


}

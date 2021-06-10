package org.sab.service.controllerbackdoor;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.sab.service.Service;


@ChannelHandler.Sharable  // Ensures that the code is shareable between channels
public class ServerHandler extends ChannelInboundHandlerAdapter {
    private Service service;

    public ServerHandler(Service service) {
        this.service = service;
    }

    private String toString(Object msg) {
        ByteBuf buf = (ByteBuf) msg;
        StringBuilder sb = new StringBuilder();
        while (buf.isReadable()) {
            sb.append((char) buf.readByte());
        }
        return sb.toString();

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) { // Reads input stream and fire the ChannelHandlerContext on Channel
        String controllerCmd = toString(msg);
        System.out.printf("%s received %s from Controller\n", service.getAppUriName(), controllerCmd);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        // Empty the buffer and flush the buffer then close the channel
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }


}

package org.sab.service.controllerbackdoor;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.json.JSONObject;
import org.sab.service.Service;


@ChannelHandler.Sharable
public class BackdoorServerHandler extends ChannelInboundHandlerAdapter {
    private Service service;

    public BackdoorServerHandler(Service service) {
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
    public void channelRead(ChannelHandlerContext ctx, Object msg) { //
        String controllerCmd = toString(msg);
        service.handleControllerMessage(new JSONObject(controllerCmd));
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }


}

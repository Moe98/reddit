package org.sab.controller;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

@ChannelHandler.Sharable
public class ControllerClientHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private final String cmd;

    ControllerClientHandler(String cmd) {
        this.cmd = cmd;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.printf("Channel %s is active\n", ctx.channel().id());
        ctx.writeAndFlush(Unpooled.copiedBuffer(cmd, CharsetUtil.UTF_8));


    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, ByteBuf in) {
        throw new UnsupportedOperationException("Controller shouldn't receive anything");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
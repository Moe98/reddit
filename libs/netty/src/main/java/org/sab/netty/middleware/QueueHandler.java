package org.sab.netty.middleware;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
import org.sab.netty.Server;
import org.sab.rabbitmq.RPCClient;
import org.sab.rabbitmq.SingleClientChannel;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class QueueHandler extends SimpleChannelInboundHandler<Object> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object object) {
        ByteBuf buffer = (ByteBuf) object;
        String request = buffer.toString(CharsetUtil.UTF_8);

        String queueName = ctx.channel().attr(Server.QUEUE_KEY).get();

        queueName = queueName.toUpperCase();

        String reqQueueName = queueName + "_REQ";
        String resQueueName = queueName + "_RES";

        try (SingleClientChannel channelExecutor = RPCClient.getSingleChannelExecutor()) {
            String response = channelExecutor.call(request, reqQueueName, resQueueName);
            ByteBuf content = Unpooled.copiedBuffer(response, CharsetUtil.UTF_8);
            ctx.fireChannelRead(content.copy());
        } catch (IOException | TimeoutException | InterruptedException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
        ctx.fireChannelReadComplete();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}

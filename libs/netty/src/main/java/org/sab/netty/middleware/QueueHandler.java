package org.sab.netty.middleware;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpObject;
import io.netty.util.CharsetUtil;
import org.json.JSONObject;
import org.sab.netty.Server;
import org.sab.rabbitmq.Sender;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class QueueHandler extends SimpleChannelInboundHandler<HttpObject> {

    ExecutorService executorService = Executors.newCachedThreadPool();
    JSONObject responseBody;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject httpObject) throws Exception {
        ByteBuf buffer = (ByteBuf) httpObject;
        JSONObject request = new JSONObject(buffer.toString(CharsetUtil.UTF_8));

        String uri =  ctx.channel().attr(Server.URI_KEY).get();
        String reqQueueName = uri + "_REQ";

        Sender sender = new Sender();
        sender.send(request, reqQueueName);

        // TODO add something that waits on the response queue
        String resQueueName = uri + "_RES";
        String correlationId = ctx.channel().attr(Server.CORR_KEY).get();
        Notifier notifier = new Notifier(resQueueName, correlationId);
        Future future = executorService.submit(notifier);
        this.responseBody = new JSONObject( (String) future.get());

        // TODO add correlation id
        // TODO make queue distributed

        ByteBuf content = Unpooled.copiedBuffer(responseBody.toString(), CharsetUtil.UTF_8);
        ctx.fireChannelRead(content.copy());



    }
}

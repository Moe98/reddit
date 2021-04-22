package org.sab.netty.middleware;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
import org.json.JSONObject;
import org.sab.netty.Server;
import org.sab.rabbitmq.RPCClient;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;


public class QueueHandler extends SimpleChannelInboundHandler<Object> {

    ExecutorService executorService = Executors.newCachedThreadPool();
    JSONObject responseBody;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object object) throws Exception {
        ByteBuf buffer = (ByteBuf) object;
        JSONObject request = new JSONObject(buffer.toString(CharsetUtil.UTF_8));

        String uri =  ctx.channel().attr(Server.URI_KEY).get();
        String reqQueueName = uri + "_REQ";
        String resQueueName = uri + "_RES";

        String response = null;
        try (RPCClient rpcClient = RPCClient.getInstance()) {
            response = rpcClient.call(request.toString(), reqQueueName, resQueueName);
        } catch (IOException | TimeoutException | InterruptedException e) {
            e.printStackTrace();
        }


        // TODO change temporary test code
//        JSONObject helloWorldResponse = new JSONObject("{\"msg\":\"Hello World\"}");
        ByteBuf content = Unpooled.copiedBuffer(response, CharsetUtil.UTF_8);
        ctx.fireChannelRead(content.copy());
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

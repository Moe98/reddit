package org.sab.chat.server.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.sab.rabbitmq.RPCClient;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class QueueHandler extends SimpleChannelInboundHandler<JSONObject> {
    private final String queueName;

    public QueueHandler(String queueName) {
        this.queueName = queueName;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, JSONObject requestJson) {
        String request = requestJson.toString();

        String reqQueueName = queueName + "_REQ";
        String resQueueName = queueName + "_RES";

        try (RPCClient rpcClient = RPCClient.getInstance()) {
            String response = rpcClient.call(request, reqQueueName, resQueueName);
            JSONParser parser = new JSONParser();
            JSONObject responseJson = (JSONObject) parser.parse(response);
            responseJson.put("type", requestJson.get("functionName"));
            System.out.println(responseJson.toJSONString());
            ctx.fireChannelRead(responseJson);
        } catch (IOException | TimeoutException | InterruptedException | NullPointerException | ParseException e) {
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


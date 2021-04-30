package org.sab.netty.middleware;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.sab.netty.Server;

import java.util.List;
import java.util.Map;


public class RequestHandler extends SimpleChannelInboundHandler<HttpObject> {
    String methodType;
    String uri;
    JSONObject body;
    Map<String, List<String>> uriParams;
    HttpRequest req;
    HttpHeaders headers;
    String queueName;
    boolean badRequest;
    String[] uriFields;

    static Map<String, List<String>> getURIParams(String uri) {
        QueryStringDecoder decoder = new QueryStringDecoder(uri);
        return decoder.parameters();
    }

    JSONObject packRequest() {
        JSONObject request = new JSONObject();

        request.put("body", body);
        request.put("uriParams", uriParams);
        request.put("methodType", methodType);
        request.put("headers", headers);
        request.put("functionName", headers.get("Function-Name"));

        return request;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
        ctx.fireChannelReadComplete();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
        if (msg instanceof HttpRequest) {
            req = (HttpRequest) msg;
            uri = req.uri();
            methodType = req.method().toString();
            uriParams = getURIParams(uri);
            headers = req.headers();

            ctx.channel().attr(Server.REQ_KEY).set(req);
        }
        if (msg instanceof HttpContent) {
            HttpContent content = (HttpContent) msg;
            ByteBuf jsonBuf = content.content();
            String jsonStr = jsonBuf.toString(CharsetUtil.UTF_8);
            if (!methodType.equals("GET") && jsonBuf.isReadable()) {
                try {
                    body = new JSONObject(jsonStr);
                } catch (JSONException e){
                    badRequest = true;
                }
            }
        }
        if (msg instanceof FullHttpRequest) {
            // TODO what's the point of this?
            System.out.println("FullHttpRequest");
            System.out.println(msg);
        }
        if (msg instanceof LastHttpContent) {
            if(badRequest){
                errorResponse(ctx, 400, "Incorrect Body");
            }
            uriFields = uri.substring(1).split("/");

            if(uriFields.length >= 2) {
                queueName = uriFields[1];
                if (Server.apps.contains(queueName.toLowerCase())) {
                    ctx.channel().attr(Server.QUEUE_KEY).set(queueName);
                    JSONObject request = packRequest();
                    ByteBuf content = Unpooled.copiedBuffer(request.toString(), CharsetUtil.UTF_8);
                    ctx.fireChannelRead(content.copy());
                } else
                    errorResponse(ctx, 404, "Not Found");
            } else
                errorResponse(ctx, 404, "Not Found");
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public String toString() {
        return "UserHandler{" +
                "methodType='" + methodType + '\'' +
                ", uri='" + uri + '\'' +
                ", body=" + body +
                ", uriParams=" + uriParams +
                '}';
    }

    private void errorResponse(ChannelHandlerContext ctx, int code, String msg){
        JSONObject response = new JSONObject().put("statusCode", code).put("msg", msg);
        ByteBuf content = Unpooled.copiedBuffer(response.toString(), CharsetUtil.UTF_8);
        ctx.pipeline().context("QueueHandler").fireChannelRead(content.copy());
    }
}

package org.sab.netty.middleware;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
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
    String correlationId;

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
        request.put("function_name", headers.get("function_name"));

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
            correlationId = req.headers().get("id");
            ctx.channel().attr(Server.REQ_KEY).set(req);
            ctx.channel().attr(Server.URI_KEY).set(uri);
            ctx.channel().attr(Server.CORR_KEY).set(correlationId);
        }
        if (msg instanceof HttpContent) {
            HttpContent content = (HttpContent) msg;
            ByteBuf jsonBuf = content.content();
            String jsonStr = jsonBuf.toString(CharsetUtil.UTF_8);
            if (!methodType.equals("GET"))
                body = new JSONObject(jsonStr);
        }
        if (msg instanceof FullHttpRequest) {
            System.out.println("FullHttpRequest");
            System.out.println(msg);
        }
        if (msg instanceof LastHttpContent) {
            uri = uri.substring(1);
            if (uri.equals("api")) {
                // TODO The passed object that will generate this response be
                //  |body|. The generated response will come from the backend app.
                JSONObject JSONRequest = packRequest();
                ByteBuf content = Unpooled.copiedBuffer(JSONRequest.toString(), CharsetUtil.UTF_8);
                ctx.fireChannelRead(content.copy());
            } else {
                String resource = uri.split("/")[1];
                //Server Waiting Infinitely For Response   Solution: (Return RabbitMQ Response or Get ack from RabbitMQ and End the request with status message)
                //enqueue(resource,packRequest());
            }
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
}

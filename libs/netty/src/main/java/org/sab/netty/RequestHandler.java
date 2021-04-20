package org.sab.netty;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.json.JSONObject;


import java.util.List;
import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderValues.CLOSE;
import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;


public class RequestHandler extends SimpleChannelInboundHandler<HttpObject> {
    static Map<String, List<String>> getURIParams(String uri){
        QueryStringDecoder decoder = new QueryStringDecoder(uri);
        return decoder.parameters();
    }
    enum MethodType{
        GET,POST,PUT,DELETE

    }
    JSONObject packRequest(){
        JSONObject request=new JSONObject();
       
        request.put("body",body);
        request.put("uriParams",uriParams);
        request.put("methodType",methodType);
        request.put("headers",headers);
        request.put("function_name",headers.get("function_name"));
        return request;
    }


    String methodType;
    String uri;
    JSONObject body;
    Map<String, List<String>> uriParams;
    HttpRequest req;
    HttpHeaders headers;
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {

        if (msg instanceof HttpRequest){
            req = (HttpRequest) msg;
            uri=req.uri();
            methodType=req.method().toString();
            uriParams = getURIParams(uri);
            headers = req.headers();
        }
        if(msg instanceof HttpContent){
            HttpContent content = (HttpContent) msg;
            ByteBuf jsonBuf = content.content();
            String jsonStr = jsonBuf.toString(CharsetUtil.UTF_8);
            if(!methodType.equals("GET"))
                body = new JSONObject(jsonStr);
            // attempt();
        }
        if(msg instanceof FullHttpRequest){
            System.out.println("FullHttpRequest");
            System.out.println(msg);
        }
        if(msg instanceof LastHttpContent){
            uri=uri.substring(1);
            if(uri.equals("api")){
                returnDefaultResponse(ctx);
            }
            else
            {
                String resource = uri.split("/")[1];
                //Server Waiting Infinitely For Response   Solution: (Return RabbitMQ Response or Get ack from RabbitMQ and End the request with status message)
                //enqueue(resource,packRequest());
            }
            
          
        }
    }
    void returnDefaultResponse(ChannelHandlerContext ctx){
        JSONObject JSONresponse = new JSONObject("{\"msg\":\"Hello World\"}");
        ByteBuf bufferResponse=Unpooled.copiedBuffer(JSONresponse.toString(), CharsetUtil.UTF_8);
        FullHttpResponse response = new DefaultFullHttpResponse(req.protocolVersion(), OK,
                Unpooled.wrappedBuffer(bufferResponse));
        response.headers()
                .set(CONTENT_TYPE, "application/json")
                .setInt(CONTENT_LENGTH, response.content().readableBytes());
        boolean keepAlive = HttpUtil.isKeepAlive(req);
        if (keepAlive) {
            if (!req.protocolVersion().isKeepAliveDefault()) {
                response.headers().set(CONNECTION, KEEP_ALIVE);
            }
        } else {
            // Tell the client we're going to close the connection.
            response.headers().set(CONNECTION, CLOSE);
        }

        ChannelFuture f = ctx.write(response);

        if (!keepAlive) {
            f.addListener(ChannelFutureListener.CLOSE);
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
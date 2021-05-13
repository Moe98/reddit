package org.sab.netty.middleware;


import com.auth0.jwt.exceptions.JWTVerificationException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.base64.Base64;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;
import io.netty.util.CharsetUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.sab.netty.Server;
import org.sab.service.authentication.Jwt;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class RequestHandler extends SimpleChannelInboundHandler<HttpObject> {
    String methodType;
    String uri;
    JSONObject body = new JSONObject();
    JSONObject uriParams;
    HttpRequest req;
    JSONObject headers;
    String queueName;
    JSONObject authenticationParams;
    HttpPostRequestDecoder requestDecoder;
    String contentChunks = "";

    JSONObject getURIParams() {
        QueryStringDecoder decoder = new QueryStringDecoder(uri);
        String[] uriPathFields = decoder.path().substring(1).split("/");
        if (uriPathFields.length >= 2)
            queueName = uriPathFields[1];
        uriParams = new JSONObject();
        Set<Map.Entry<String, List<String>>> uriParamsSet = decoder.parameters().entrySet();
        uriParamsSet.forEach(entry -> uriParams.put(entry.getKey(), entry.getValue().get(0)));
        return uriParams;
    }

    JSONObject packRequest() throws IOException, JSONException {
        JSONObject request = new JSONObject();

        request.put("body", body);
        request.put("uriParams", uriParams);
        request.put("methodType", methodType);
        request.put("headers", headers);
        request.put("functionName", headers.getString("Function-Name"));

        if (requestDecoder != null) {
            JSONObject httpData = readHttpData();
            httpData.keySet().forEach(key -> request.put(key, httpData.getJSONObject(key)));
        }

        return authenticate(request);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        cleanUp();
        ctx.flush();
        ctx.fireChannelReadComplete();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
        if (msg instanceof HttpRequest) {
            req = (HttpRequest) msg;
            uri = req.uri();
            methodType = req.method().toString();
            uriParams = getURIParams();
            headers = getHeaders();
            ctx.channel().attr(Server.REQ_KEY).set(req);
            if (!methodType.equals("GET") && headers.getString("Content-Type").split(";")[0].equals("multipart/form-data")) {
                requestDecoder = new HttpPostRequestDecoder(req);
                requestDecoder.setDiscardThreshold(0);
            }
        }
        if (msg instanceof HttpContent) {
            HttpContent content = (HttpContent) msg;
            if (requestDecoder != null)
                requestDecoder.offer(content);
            else {
                ByteBuf jsonBuf = content.content();
                contentChunks += jsonBuf.toString(CharsetUtil.UTF_8);
            }
        }
        if (msg instanceof FullHttpRequest) {
            // TODO what's the point of this?
            System.out.println("FullHttpRequest");
            System.out.println(msg);
        }
        if (msg instanceof LastHttpContent) {
            if (!methodType.equals("GET") && !contentChunks.isEmpty() && requestDecoder == null) {
                try {
                    body = new JSONObject(contentChunks);
                } catch (JSONException e) {
                    errorResponse(ctx, 400, "Incorrect Body");
                    return;
                }
            }
            if (queueName != null && Server.apps.contains(queueName.toLowerCase())) {
                ctx.channel().attr(Server.QUEUE_KEY).set(queueName);
                try {
                    JSONObject request = packRequest();
                    ByteBuf content = Unpooled.copiedBuffer(request.toString(), CharsetUtil.UTF_8);
                    ctx.fireChannelRead(content.copy());
                } catch (IOException | JSONException e) {
                    errorResponse(ctx, 400, e.getMessage());
                }
            } else
                errorResponse(ctx, 404, "Not Found");

        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cleanUp();
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

    private void errorResponse(ChannelHandlerContext ctx, int code, String msg) {
        JSONObject response = new JSONObject().put("statusCode", code).put("msg", msg);
        ByteBuf content = Unpooled.copiedBuffer(response.toString(), CharsetUtil.UTF_8);
        ctx.pipeline().context("QueueHandler").fireChannelRead(content.copy());
    }

    private JSONObject getHeaders() {
        headers = new JSONObject();
        req.headers().entries().forEach(entry -> headers.put(entry.getKey(), entry.getValue()));
        return headers;
    }

    private JSONObject authenticate(JSONObject request) {
        // JWT token
        // set token if auth header is set
        // format: Bearer xxxxxx-xxxxxx-xxxxxx
        authenticationParams = new JSONObject();
        Boolean authenticated = false;
        String authHeader = headers.has("Authorization") ? headers.getString("Authorization") : null;
        if (authHeader != null) {
            String[] auth = authHeader.split(" ");
            if (auth.length > 1) {
                try {
                    Map<String, Object> claims = Jwt.verifyAndDecode(auth[1]);
                    authenticated = true;
                    authenticationParams.put("username", claims.get("username"));
                    authenticationParams.put("jwt", auth[1]);
                } catch (JWTVerificationException jwtVerificationException) {
                    System.out.println(jwtVerificationException.getMessage());
                    authenticated = false;
                }
            }
        }
        authenticationParams.put("isAuthenticated", authenticated);
        request.put("authenticationParams", authenticationParams);
        return request;
    }

    private JSONObject readHttpData() throws IOException, JSONException {
        JSONObject data = new JSONObject();
        JSONObject files = new JSONObject();
        while (requestDecoder.hasNext()) {
            InterfaceHttpData httpData = requestDecoder.next();
            if (httpData.getHttpDataType() == HttpDataType.Attribute) {
                Attribute attribute = (Attribute) httpData;
                data.put(attribute.getName(), new JSONObject(attribute.getValue()));
            } else if (httpData.getHttpDataType() == HttpDataType.FileUpload) {
                FileUpload fileUpload = (FileUpload) httpData;
                JSONObject jsonFile = new JSONObject();
                String encodedData = Base64.encode(fileUpload.getByteBuf()).toString(StandardCharsets.UTF_8);
                jsonFile.put("data", encodedData);
                jsonFile.put("type", fileUpload.getContentType());
                files.put(fileUpload.getName(), jsonFile);
            }
            httpData.release();
        }
        return data.put("files", files);
    }

    private void cleanUp() {
        if (requestDecoder != null) {
            requestDecoder.destroy();
            requestDecoder = null;
        }
    }
}

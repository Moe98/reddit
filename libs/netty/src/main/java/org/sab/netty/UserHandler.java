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


import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderValues.CLOSE;
import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;


import java.sql.PreparedStatement;
import java.sql.Statement;
public class UserHandler extends SimpleChannelInboundHandler<HttpObject> {
    static Map<String, List<String>> getURIParams(String uri){
        QueryStringDecoder decoder = new QueryStringDecoder(uri);
        return decoder.parameters();
    }
    enum MethodType{
        GET,POST,PUT,DELETE

    }


    String methodType;
    String uri;
    JSONObject body;
    Map<String, List<String>> uriParams;
    HttpRequest req;
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

        }
        if(msg instanceof HttpContent){
            HttpContent content = (HttpContent) msg;
            ByteBuf jsonBuf = content.content();
            String jsonStr = jsonBuf.toString(CharsetUtil.UTF_8);
            body = new JSONObject(jsonStr);
            // attempt();

        }
        if(msg instanceof FullHttpRequest){
            System.out.println("FullHttpRequest");
            System.out.println(msg);
        }
        if(msg instanceof LastHttpContent){
            JSONObject JSONresponse = getResponse();
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
            System.out.println(this);
        }
    }

    private JSONObject getResponse() {
        if (uri.endsWith("/signUp")) {
            return signUp();
        }
        return  new JSONObject("{USERS:HELLO}");
    }

    private JSONObject signUp() {
        String url = "jdbc:postgresql://localhost:5432/postgres";
        String user = "scaleabull";
        String password = "12345678";
        String query = "INSERT INTO users(username,email,password,birthdate,hasprofilephoto,user_id) VALUES(?,?,?,?,?,?)";
        String username = (body.get("username")).toString();
        String email = (body.get("email")).toString();
        String password2 = (body.get("password")).toString();
        String birthdate = (body.get("birthdate")).toString();
        // SimpleDateFormat formatter1=new SimpleDateFormat("dd/MM/yyyy");  

        // Date birthdate=formatter1.parse((body.get("birthdate")).toString()); 
        boolean hasprofilephoto = Boolean.parseBoolean((body.get("hasprofilephoto")).toString());
        String user_id = (body.get("user_id")).toString();
        JSONObject response;
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement pst = con.prepareStatement(query)) {
            pst.setString(1, username);
            pst.setString(2, email);
            pst.setString(3, password2);
            pst.setDate(4, java.sql.Date.valueOf(birthdate));
            pst.setBoolean(5, hasprofilephoto);
            pst.setString(6, user_id);

            pst.executeUpdate();
            response=new JSONObject();
            response.put("email",email);
            response.put("username",username);
            response.put("birthdate",birthdate);
            response.put("hasprofilephoto",hasprofilephoto);
            response.put("status","Success");

        } catch (SQLException ex) {

            Logger lgr = Logger.getLogger(UserHandler.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);
            response=new JSONObject();
            response.put("status","Fail");

        }

        return response;
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
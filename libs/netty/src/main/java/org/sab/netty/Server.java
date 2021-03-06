package org.sab.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

import javax.net.ssl.SSLException;

import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;


public final class Server {

    static final boolean SSL = System.getProperty("ssl") != null;
    static final int PORT = Integer.parseInt(System.getProperty("port", SSL ? "8443" : "8080"));
    public static final AttributeKey<HttpRequest> REQ_KEY = AttributeKey.valueOf("req");
    public static final AttributeKey<String> QUEUE_KEY = AttributeKey.valueOf("queue");
    public static final ArrayList<String> apps = new ArrayList<>(Arrays.asList("example", "thread", "subthread", "recommendation", "notification", "chat", "search", "user", "authentication", "useraction"));
    // TODO: fetch nThreads from somewhere consistante most probably a config file
    public static final int nThreads = 8;
    protected static final EventExecutorGroup queueExecutorGroup = new DefaultEventExecutorGroup(nThreads);
    public static EventLoopGroup bossGroup;
    public static EventLoopGroup workerGroup;
    
    public static void main(String[] args) throws CertificateException, SSLException, InterruptedException {
        // Configure SSL.
        final SslContext sslCtx;
        if (SSL) {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
        } else {
            sslCtx = null;
        }

        // Configure the server.
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 1024);
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ServerInitializer(sslCtx));

            Channel ch = b.bind(PORT).sync().channel();

            System.out.println("Open your web browser and navigate to " +
                    (SSL ? "https" : "http") + "://127.0.0.1:" + PORT + '/');

            ch.closeFuture().sync();
        } finally {
            shutdownGracefully();
        }
    }

    public static void shutdownGracefully() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}

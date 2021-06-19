package org.sab.service.controllerbackdoor;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.sab.service.Service;

import java.net.InetSocketAddress;

public class BackdoorServer {
    private final int port;
    private Service service;

    public BackdoorServer(int port, Service service) {
        this.port = port;
        this.service = service;
    }

    public void start() throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup(1); //Creates an EventLoop that is shareable across clients
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(group) // Bootstrap the server to a specific group
                    .channel(NioServerSocketChannel.class) // Specifies transport protocol for channel
                    .localAddress(new InetSocketAddress(port)) // Specifies address for channel
                    .childHandler(new ChannelInitializer<SocketChannel>() { // Specifies channel handler to call when connection is accepted
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new BackdoorServerHandler(service)); // Adds channel handler to pipeline
                        }
                    });

            ChannelFuture f = b.bind().sync(); // Bind server to address, and block (sync method) until it does so

            System.out.println(BackdoorServer.class.getSimpleName() + " started and listen on " + f.channel().localAddress());
            f.channel().closeFuture().sync(); // Returns a future channel that will be notified when shutdown

        } finally {
            group.shutdownGracefully().sync(); // Terminates all threads
        }
    }
}

package ankang.zookeeper.homework1.rpc.service;

import ankang.zookeeper.homework1.rpc.endoder.RpcRequestDecoder;
import ankang.zookeeper.homework1.rpc.endoder.RpcResponseEncoder;
import ankang.zookeeper.homework1.rpc.service.handler.SayHelloInboundHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @author: ankang
 * @email: dreedisgood@qq.com
 * @create: 2020-12-07
 */
public class Service {

    private static final int PORT = 6969;

    public static void main(String[] args) throws InterruptedException {
        final ServerBootstrap bootstrap = new ServerBootstrap();

        final NioEventLoopGroup parentGroup = new NioEventLoopGroup();
        final NioEventLoopGroup childGroup = new NioEventLoopGroup();

        bootstrap.group(parentGroup , childGroup)
                .localAddress(PORT)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new RpcRequestDecoder())
                                .addLast(new RpcResponseEncoder())
                                .addLast(new SayHelloInboundHandler());
                    }

                });

        try {
            final ChannelFuture cf = bootstrap.bind().sync();
            cf.channel().closeFuture().sync();
        } finally {
            childGroup.shutdownGracefully().sync();
            parentGroup.shutdownGracefully().sync();
        }
    }

}

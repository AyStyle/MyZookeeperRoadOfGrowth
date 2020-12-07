package ankang.zookeeper.homework1.rpc.client.channel;

import ankang.zookeeper.homework1.rpc.http.RpcRequest;
import ankang.zookeeper.homework1.rpc.http.RpcResponse;
import ankang.zookeeper.homework1.rpc.server.SayHelloServer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.SneakyThrows;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * @author: ankang
 * @email: dreedisgood@qq.com
 * @create: 2020-12-07
 */
public class SayHelloOutboundHandler extends ChannelInboundHandlerAdapter implements SayHelloServer {

    private static final String HOST = "localhost";
    private static final int PORT = 6969;

    private final Bootstrap bootstrap;
    private ChannelHandlerContext ctx;

    private RpcResponse response;

    public SayHelloOutboundHandler() throws InterruptedException {
        final NioEventLoopGroup group = new NioEventLoopGroup();

        this.bootstrap = new Bootstrap();

        bootstrap.group(group)
                .remoteAddress(HOST , PORT)
                .channel(SocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(this);
                    }
                });
        try {
            final ChannelFuture cf = bootstrap.connect().sync();
            cf.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx , Object msg) throws Exception {
        response = (RpcResponse) msg;
        notify();
    }

    @SneakyThrows
    @Override
    public String sayHello() {
        ctx.writeAndFlush(new RpcRequest(SayHelloServer.class , "sayHello"));
        wait();
        final String string = response.getObj().toString();
        response = null;
        return string;
    }

}

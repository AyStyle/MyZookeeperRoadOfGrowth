package ankang.zookeeper.homework1.rpc.client.proxy;

import ankang.zookeeper.homework1.rpc.client.handler.SayHelloInboundHandler;
import ankang.zookeeper.homework1.rpc.endoder.RpcRequestEncoder;
import ankang.zookeeper.homework1.rpc.endoder.RpcResponseDecoder;
import ankang.zookeeper.homework1.rpc.server.SayHelloServer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * @author: ankang
 * @email: dreedisgood@qq.com
 * @create: 2020-12-07
 */
public class SayHelloServerProxy implements SayHelloServer {

    private static final String HOST = "localhost";
    private static final int PORT = 6969;

    private final Bootstrap bootstrap;
    private final SayHelloInboundHandler handler;

    public SayHelloServerProxy() throws InterruptedException {
        final NioEventLoopGroup group = new NioEventLoopGroup();
        handler = new SayHelloInboundHandler();

        this.bootstrap = new Bootstrap();
        bootstrap.group(group)
                .remoteAddress(HOST , PORT)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new RpcResponseDecoder())
                                .addLast(new RpcRequestEncoder())
                                .addLast(handler);
                    }
                });

        bootstrap.connect().sync();
    }


    @Override
    public String sayHello() {
        return handler.sayHello();
    }
}

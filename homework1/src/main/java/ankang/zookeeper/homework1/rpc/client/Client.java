package ankang.zookeeper.homework1.rpc.client;

import ankang.zookeeper.homework1.rpc.server.SayHelloServer;
import ankang.zookeeper.homework1.rpc.service.channel.SayHelloInboundHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;

/**
 * @author: ankang
 * @email: dreedisgood@qq.com
 * @create: 2020-12-07
 */
public class Client {

    public static void main(String[] args) {
        final SayHelloServer sayHelloServer = new SayHelloInboundHandler();

        sayHelloServer.sayHello();
    }

}

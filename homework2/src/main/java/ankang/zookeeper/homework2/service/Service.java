package ankang.zookeeper.homework2.service;

import ankang.zookeeper.homework2.endoder.RpcRequestDecoder;
import ankang.zookeeper.homework2.endoder.RpcResponseEncoder;
import ankang.zookeeper.homework2.service.handler.SayHelloInboundHandler;
import com.google.gson.JsonObject;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryForever;
import org.apache.zookeeper.CreateMode;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author: ankang
 * @email: dreedisgood@qq.com
 * @create: 2020-12-07
 */
public class Service {
    public static final String SERVER_PATH = "/ankang/netty";


    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException("缺少参数端口号！");
        }
        final int port = Integer.parseInt(args[0]);


        // 1. 连接ZK服务
        try (final CuratorFramework zk = CuratorFrameworkFactory.newClient("localhost:2181" , new RetryForever(3000))) {
            zk.start();

            // 2. 注册Netty连接服务
            zk.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).inBackground().forPath(SERVER_PATH);

            final JsonObject json = new JsonObject();
            json.addProperty("last_time" , LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            json.addProperty("service" , String.format("localhost:%d" , port));

            zk.create().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).inBackground().forPath(SERVER_PATH + "/service" , json.toString().getBytes());

            // 3. 启动Netty服务
            initNettyServer(port);
        }

    }

    /**
     * 初始化端口
     *
     * @param port 服务端口号
     */
    private static void initNettyServer(int port) throws InterruptedException {
        final ServerBootstrap bootstrap = new ServerBootstrap();

        final NioEventLoopGroup parentGroup = new NioEventLoopGroup();
        final NioEventLoopGroup childGroup = new NioEventLoopGroup();

        bootstrap.group(parentGroup , childGroup)
                .localAddress(port)
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

package ankang.zookeeper.homework1.rpc.client.proxy;

import ankang.zookeeper.homework1.rpc.client.handler.SayHelloInboundHandler;
import ankang.zookeeper.homework1.rpc.endoder.RpcRequestEncoder;
import ankang.zookeeper.homework1.rpc.endoder.RpcResponseDecoder;
import ankang.zookeeper.homework1.rpc.server.SayHelloServer;
import ankang.zookeeper.homework1.rpc.service.Service;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.SneakyThrows;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryForever;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.zookeeper.Watcher.Event.EventType.NodeChildrenChanged;

/**
 * @author: ankang
 * @email: dreedisgood@qq.com
 * @create: 2020-12-07
 */
public class SayHelloServerProxy implements SayHelloServer {

    private final CuratorFramework zk;
    /**
     * 数据格式，节点信息->服务
     */
    private Map<String, SayHelloInboundHandler> services;

    public SayHelloServerProxy() throws Exception {
        services = new HashMap<>();
        zk = CuratorFrameworkFactory.newClient("localhost:2181" , new RetryForever(3000));
        zk.start();

        connect();
    }


    @Override
    public String sayHello() {
        return handler.sayHello();
    }

    /**
     * 创建一个Netty客户端服务
     *
     * @param host
     * @param port
     * @return
     */
    private ChannelHandlerContext createNettyCtx(String host , int port) {
        final NioEventLoopGroup group = new NioEventLoopGroup();
        final SayHelloInboundHandler handler = new SayHelloInboundHandler();
        final Bootstrap bootstrap = new Bootstrap();

        bootstrap.group(group)
                .remoteAddress(host , port)
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

        bootstrap.connect();
        return handler.getCtx();
    }

    /**
     * 连接服务节点
     */
    private synchronized void connect() throws Exception {
        try (zk) {
            // 1. 获取服务端节点然后连接服务端节点并监听服务端连接信息
            final List<String> subNodes = zk.getChildren().usingWatcher(watcher(zk)).forPath(Service.SERVER_PATH);

            // 2. 获取子节点信息，并连接
            for (String subNode : subNodes) {
                final String path = String.format("%s/%s" , Service.SERVER_PATH , subNode);
                final byte[] bytes = zk.getData().inBackground().forPath(path);

                final String data = new String(bytes);
                final String[] split = data.split(":");

                final ChannelHandlerContext bootstrap = createBootstrap(split[0] , Integer.parseInt(split[1]));
                services.put(data , bootstrap);
            }

        }
    }

    private Watcher watcher(CuratorFramework cf) {
        new Watcher() {
            @SneakyThrows
            @Override
            public void process(WatchedEvent event) {
                if (event.getType() == NodeChildrenChanged) {
                    event.getPath()
                    final Map<String, Bootstrap> serviceNew = new HashMap<>();

                    final List<String> subNodes = cf.getChildren().forPath(Service.SERVER_PATH);
                    for (String subNode : subNodes) {
                        final String path = String.format("%s/%s" , Service.SERVER_PATH , subNode);
                        final byte[] bytes = cf.getData().forPath(path);

                        final String data = new String(bytes);
                        final String[] split = data.split(":");
                        final Bootstrap bootstrap = services.containsKey(data) ? serviceNew.get(data) : createBootstrap(split[0] , Integer.parseInt(split[1]));

                        serviceNew.put(data , bootstrap);
                    }

                    final Map<String, Bootstrap> serviceOld = services;
                    services = serviceNew;

                    // 删除并关闭失效的服务
                    // 1. 从旧的服务中移除有效的服务
                    for (Map.Entry<String, Bootstrap> entry : serviceNew.entrySet()) {
                        final Bootstrap remove = serviceOld.remove(entry.getKey());
                        if (remove != null) {
                            remove.connect().channel().close();
                        }
                    }

                }


            }
        }
    }


}

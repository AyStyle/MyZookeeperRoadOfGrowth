package ankang.zookeeper.homework1.rpc.client.proxy;

import ankang.zookeeper.homework1.rpc.client.handler.SayHelloInboundHandler;
import ankang.zookeeper.homework1.rpc.endoder.RpcRequestEncoder;
import ankang.zookeeper.homework1.rpc.endoder.RpcResponseDecoder;
import ankang.zookeeper.homework1.rpc.server.SayHelloServer;
import ankang.zookeeper.homework1.rpc.service.Service;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.retry.RetryForever;

import java.io.Closeable;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

/**
 * @author: ankang
 * @email: dreedisgood@qq.com
 * @create: 2020-12-07
 */
public class SayHelloServerProxy implements SayHelloServer, Closeable {

    private final CuratorFramework cf;
    private final CuratorCache cc;

    /**
     * 数据格式，节点信息->服务
     */
    private final ConcurrentHashMap<String, SayHelloInboundHandler> services = new ConcurrentHashMap<>();

    public SayHelloServerProxy() throws Exception {
        cf = CuratorFrameworkFactory.newClient("localhost:2181" , new RetryForever(3000));
        cf.start();

        cc = CuratorCache.bridgeBuilder(cf , Service.SERVER_PATH).build();
        cc.start();

        // 监听服务端节点
        cc.listenable().addListener(CuratorCacheListener.builder().forPathChildrenCache(Service.SERVER_PATH , cf , (CuratorFramework client , PathChildrenCacheEvent event) -> {
            final String server = event.getData().getPath() + "===>" + new String(event.getData().getData());
            switch (event.getType()) {
                case CHILD_ADDED:
                    connectNetty(server);
                    break;
                case CHILD_UPDATED:
                    disconnectNetty(server);
                    connectNetty(server);
                    break;
                case CHILD_REMOVED:
                    disconnectNetty(server);
                    break;
                default:
                    System.err.println(event.getType());
                    break;
            }
        }).build());

        // 获取服务端已经注册的节点并连接
        try {
            final List<String> nodes = cf.getChildren().forPath(Service.SERVER_PATH);
            for (String node : nodes) {
                final String path = Service.SERVER_PATH + "/" + node;
                final byte[] bytes = cf.getData().forPath(path);
                final String server = path + "===>" + new String(bytes);
                connectNetty(server);
            }
        } catch (Exception e) {
            cc.close();
            cf.close();
            throw e;
        }
    }


    @Override
    public synchronized String sayHello() {
        for (Map.Entry<String, SayHelloInboundHandler> entry : services.entrySet()) {
            final String server = entry.getKey();
            final SayHelloInboundHandler service = entry.getValue();
            try {
                return String.format("%s Connect to %s, %s" , LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) , server , service.sayHello());
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(services);
            }
        }

        return null;
    }


    /**
     * 创建一个Netty客户端服务
     *
     * @param server 连接的服务, format: host:port
     * @return
     */
    private synchronized void connectNetty(String server) throws InterruptedException {
        final NioEventLoopGroup group = new NioEventLoopGroup();
        final SayHelloInboundHandler handler = new SayHelloInboundHandler();
        final Bootstrap bootstrap = new Bootstrap();

        final String[] split = server.split("===>")[1].split(":");
        bootstrap.group(group)
                .remoteAddress(split[0] , Integer.parseInt(split[1]))
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
        System.out.println("Connect Server " + server);
        services.put(server , handler);
    }

    /**
     * 断开Netty连接
     *
     * @param server 断来连接的服务, format: host:port
     */
    private synchronized void disconnectNetty(String server) {
        final SayHelloInboundHandler remove = services.remove(server);
        if (remove != null) {
            System.out.println("Disconnect Server: " + server);
            remove.getCtx().close();
        }
    }

    @Override
    public void close() throws IOException {
        for (Map.Entry<String, SayHelloInboundHandler> entry : services.entrySet()) {
            entry.getValue().getCtx().close();
        }

        services.clear();
        cc.close();
        cf.start();
    }
}

package ankang.zookeeper.homework2.client.proxy;

import ankang.zookeeper.homework2.client.handler.SayHelloInboundHandler;
import ankang.zookeeper.homework2.endoder.RpcRequestEncoder;
import ankang.zookeeper.homework2.endoder.RpcResponseDecoder;
import ankang.zookeeper.homework2.server.SayHelloServer;
import ankang.zookeeper.homework2.service.Service;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
            synchronized (this) {
                final String server = event.getData().getPath() + "===>" + new Gson().fromJson(new String(event.getData().getData()) , JsonObject.class).get("service").getAsString();
                switch (event.getType()) {
                    case CHILD_ADDED:
                        connectNetty(server);
                        break;
                    case CHILD_REMOVED:
                        disconnectNetty(server);
                        break;
                    default:
                        break;
                }
            }
        }).build());
    }


    @Override
    public synchronized String sayHello() {
        final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        final TreeMap<String, SayHelloInboundHandler> treeMap = new TreeMap<>((String o1 , String o2) -> {
            try {
                final Gson gson = new Gson();
                final LocalDateTime t1 = LocalDateTime.parse(gson.fromJson(new String(cf.getData().forPath(o1.split("===>")[0])) , JsonObject.class).get("last_time").getAsString() , fmt);
                final LocalDateTime t2 = LocalDateTime.parse(gson.fromJson(new String(cf.getData().forPath(o2.split("===>")[0])) , JsonObject.class).get("last_time").getAsString() , fmt);

                return -t1.compareTo(t2);
            } catch (Exception e) {
                return 0;
            }
        });
        treeMap.putAll(services);


        for (Map.Entry<String, SayHelloInboundHandler> entry : treeMap.entrySet()) {
            final String server = entry.getKey();
            final SayHelloInboundHandler service = entry.getValue();
            try {
                final String[] split = server.split("===>");
                final JsonObject json = new JsonObject();
                json.addProperty("service" , split[1]);
                json.addProperty("last_time" , LocalDateTime.now().format(fmt));

                cf.setData().inBackground().forPath(split[0] , json.toString().getBytes());
                return String.format("%s Connect to %s, %s" , LocalDateTime.now().format(fmt) , server , service.sayHello());
            } catch (Exception e) {
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

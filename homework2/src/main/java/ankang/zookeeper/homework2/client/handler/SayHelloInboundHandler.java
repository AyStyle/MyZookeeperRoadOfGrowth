package ankang.zookeeper.homework2.client.handler;

import ankang.zookeeper.homework2.http.RpcRequest;
import ankang.zookeeper.homework2.http.RpcResponse;
import ankang.zookeeper.homework2.server.SayHelloServer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.Getter;
import lombok.SneakyThrows;

import java.util.concurrent.*;

/**
 * @author: ankang
 * @email: dreedisgood@qq.com
 * @create: 2020-12-07
 */
public class SayHelloInboundHandler extends ChannelInboundHandlerAdapter implements SayHelloServer, Callable<String> {

    @Getter
    private ChannelHandlerContext ctx;
    private RpcResponse response;
    private final ExecutorService pool = Executors.newSingleThreadExecutor();

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public synchronized void channelRead(ChannelHandlerContext ctx , Object msg) {
        response = (RpcResponse) msg;
        notify();
    }

    @SneakyThrows
    @Override
    public String sayHello() {
        final Future<String> future = pool.submit(this);

        return future.get(100 , TimeUnit.MILLISECONDS);
    }

    @Override
    public synchronized String call() throws Exception {
        ctx.writeAndFlush(new RpcRequest("ankang.zookeeper.homework1.rpc.ankang.zookeeper.homework2.server.SayHelloServer" , "sayHello")).sync();
        wait();
        final String string = response.getObj().toString();
        response = null;

        return string;
    }
}

package ankang.zookeeper.homework1.rpc.client.handler;

import ankang.zookeeper.homework1.rpc.http.RpcRequest;
import ankang.zookeeper.homework1.rpc.http.RpcResponse;
import ankang.zookeeper.homework1.rpc.server.SayHelloServer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.Getter;
import lombok.SneakyThrows;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author: ankang
 * @email: dreedisgood@qq.com
 * @create: 2020-12-07
 */
public class SayHelloInboundHandler extends ChannelInboundHandlerAdapter implements SayHelloServer, Callable<String> {

    @Getter
    private ChannelHandlerContext ctx;
    private RpcResponse response;
    private ExecutorService pool = Executors.newSingleThreadExecutor();


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
    }

    @Override
    public synchronized void channelRead(ChannelHandlerContext ctx , Object msg) throws Exception {
        response = (RpcResponse) msg;
        notify();
    }

    @SneakyThrows
    @Override
    public String sayHello() {
        final Future<String> future = pool.submit(this);

        return future.get();

    }

    @Override
    public synchronized String call() throws Exception {
        ctx.writeAndFlush(new RpcRequest("ankang.zookeeper.homework1.rpc.server.SayHelloServer" , "sayHello")).sync();
        wait();
        final String string = response.getObj().toString();
        response = null;
        return string;
    }
}

package ankang.zookeeper.homework1.rpc.service.channel;

import ankang.zookeeper.homework1.rpc.http.RpcRequest;
import ankang.zookeeper.homework1.rpc.http.RpcResponse;
import ankang.zookeeper.homework1.rpc.server.SayHelloServer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author: ankang
 * @email: dreedisgood@qq.com
 * @create: 2020-12-07
 */
public class SayHelloInboundHandler extends ChannelInboundHandlerAdapter implements SayHelloServer {

    @Override
    public void channelRead(ChannelHandlerContext ctx , Object msg) throws Exception {
        final RpcRequest request = (RpcRequest) msg;

        if (request.getServerClass() == SayHelloServer.class) {
            final RpcResponse response = new RpcResponse(request , sayHello());

            ctx.channel().writeAndFlush(response);
        }
    }

    @Override
    public String sayHello() {
        return "hello";
    }
}

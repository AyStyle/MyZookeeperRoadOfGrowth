package ankang.zookeeper.homework2.service.handler;

import ankang.zookeeper.homework2.http.RpcRequest;
import ankang.zookeeper.homework2.http.RpcResponse;
import ankang.zookeeper.homework2.server.SayHelloServer;
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

         if ("ankang.zookeeper.homework1.rpc.ankang.zookeeper.homework2.server.SayHelloServer".equals(request.getServerClass()) && "sayHello".equals(request.getServerMethod())) {
            final RpcResponse response = new RpcResponse(request , sayHello());

            ctx.channel().writeAndFlush(response);
        }
    }

    @Override
    public String sayHello() {
        return "hello";
    }
}

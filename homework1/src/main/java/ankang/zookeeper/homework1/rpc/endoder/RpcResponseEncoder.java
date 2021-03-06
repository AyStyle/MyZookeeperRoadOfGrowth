package ankang.zookeeper.homework1.rpc.endoder;

import ankang.zookeeper.homework1.rpc.http.RpcRequest;
import ankang.zookeeper.homework1.rpc.http.RpcResponse;
import com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author: ankang
 * @email: dreedisgood@qq.com
 * @create: 2020-12-08
 */
public class RpcResponseEncoder extends MessageToByteEncoder<RpcResponse> {
    @Override
    protected void encode(ChannelHandlerContext ctx , RpcResponse msg , ByteBuf out) throws Exception {
        final byte[] bytes = new Gson().toJson(msg , RpcResponse.class).getBytes();

        out.writeInt(bytes.length);
        out.writeBytes(bytes);
    }
}

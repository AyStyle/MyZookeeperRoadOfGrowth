package ankang.zookeeper.homework2.endoder;

import ankang.zookeeper.homework2.http.RpcResponse;
import com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @author: ankang
 * @email: dreedisgood@qq.com
 * @create: 2020-12-08
 */
public class RpcResponseDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx , ByteBuf in , List<Object> out) throws Exception {
        final int i = in.readInt();
        final byte[] bytes = new byte[i];

        in.readBytes(bytes);

        final RpcResponse rpcResponse = new Gson().fromJson(new String(bytes) , RpcResponse.class);
        out.add(rpcResponse);
    }
}

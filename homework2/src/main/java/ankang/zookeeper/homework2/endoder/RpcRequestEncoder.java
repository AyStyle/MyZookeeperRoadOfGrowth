package ankang.zookeeper.homework2.endoder;

import com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author: ankang
 * @email: dreedisgood@qq.com
 * @create: 2020-12-08
 */
public class RpcRequestEncoder extends MessageToByteEncoder<RpcRequest> {
    @Override
    protected void encode(ChannelHandlerContext ctx , RpcRequest msg , ByteBuf out) throws Exception {
        final byte[] bytes = new Gson().toJson(msg , msg.getClass()).getBytes();

        out.writeInt(bytes.length);
        out.writeBytes(bytes);
    }
}

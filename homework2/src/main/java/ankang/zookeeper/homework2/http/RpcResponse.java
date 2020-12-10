package ankang.zookeeper.homework2.http;

import lombok.Getter;

/**
 * @author: ankang
 * @email: dreedisgood@qq.com
 * @create: 2020-12-07
 */
public class RpcResponse {

    @Getter
    private final RpcRequest request;

    @Getter
    private final Object obj;

    public RpcResponse(RpcRequest request , Object obj) {
        this.request = request;
        this.obj = obj;
    }
}

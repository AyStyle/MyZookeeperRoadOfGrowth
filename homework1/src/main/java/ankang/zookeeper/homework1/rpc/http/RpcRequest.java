package ankang.zookeeper.homework1.rpc.http;

import lombok.Getter;

/**
 * RPC 请求对象
 * 对象的格式：
 *
 * @author: ankang
 * @email: dreedisgood@qq.com
 * @create: 2020-12-07
 */
public class RpcRequest {

    @Getter
    private final String serverClass;

    @Getter
    private final String serverMethod;

    public RpcRequest(String serverClass , String serverMethod) {
        this.serverClass = serverClass;
        this.serverMethod = serverMethod;
    }
}

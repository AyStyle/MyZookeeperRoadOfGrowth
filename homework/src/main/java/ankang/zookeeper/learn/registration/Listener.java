package ankang.zookeeper.learn.registration;

import java.util.HashSet;
import java.util.Set;

/**
 * @author: ankang
 * @email: dreedisgood@qq.com
 * @create: 2020-12-03
 */
public abstract class Listener {

    /**
     * 监听的路径
     */
    private final String path;

    /**
     * 监听路径下的全部节点信息
     */
    private final Set<Node> nodes;

    public Listener(String path) {
        this.path = path;
        this.nodes = new HashSet<>();
    }

    /**
     * 监听到节点变化后，要响应的操作
     */
    public abstract void listen(Node node);

    /**
     * 订阅注册中心的
     *
     * @param path
     */
    public abstract void subcribe(String path)；

    public String getPath() {
        return path;
    }

    public Set<Node> getNodes() {
        return nodes;
    }
}

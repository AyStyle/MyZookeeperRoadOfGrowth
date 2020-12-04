package ankang.zookeeper.learn.registration;

/**
 * @author: ankang
 * @email: dreedisgood@qq.com
 * @create: 2020-12-02
 */
public interface Registration {

    /**
     * 注册节点信息
     * 当节点有变化时，会通知所有监听对象
     *
     * @param node 节点
     */
    void register(Node node);

    /**
     * 取消注册的节点
     * 当节点有变化时，通知所有监听的节点
     *
     * @param node 节点
     */
    void unregister(Node node);

    /**
     * 监听节点路径信息变化
     *
     * @param listener 监听器
     */
    void listener(Listener listener);

    /**
     * 取消监听
     * @param listener 监听器
     */
    void unlistener(Listener listener);
}

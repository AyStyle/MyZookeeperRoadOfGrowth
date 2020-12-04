package ankang.zookeeper.learn.registration;

import java.util.HashSet;
import java.util.Set;

/**
 * @author: ankang
 * @email: dreedisgood@qq.com
 * @create: 2020-12-03
 */
public class Node {

    private final String path;
    private final String nodeName;
    private String data;
    private final Set<Listener> listeners;

    public Node(String path , String nodeName , String data) {
        this.path = path;
        this.nodeName = nodeName;
        this.data = data;

        this.listeners = new HashSet<>();
    }

    /**
     * 添加一个监听器
     *
     * @param listener
     */
    public void addListener(Listener listener) {
        this.listeners.add(listener);
    }

    /**
     * 删除一个监听器
     *
     * @param listener
     */
    public void removeListener(Listener listener) {
        this.listeners.remove(listener);
    }

    public String getPath() {
        return path;
    }

    public String getNodeName() {
        return nodeName;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Set<Listener> getLiseners() {
        return listeners;
    }
}

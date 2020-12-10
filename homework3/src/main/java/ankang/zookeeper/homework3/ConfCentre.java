package ankang.zookeeper.homework3;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.retry.RetryForever;

import java.util.concurrent.Executor;

/**
 * @author: ankang
 * @email: dreedisgood@qq.com
 * @create: 2020-12-11
 */
public class ConfCentre {

    private final CuratorFramework cf;
    private final CuratorCache cc;

    /**
     * 创建一个注册中心
     */
    public ConfCentre() {
        cf = CuratorFrameworkFactory.newClient("localhost:2181" , new RetryForever(3000));
        cf.start();

        cc = CuratorCache.bridgeBuilder(cf , "/").build();
        cc.start();
    }

    /**
     * 给注册中心添加一个监听器。
     * 监听器里实现了需要的业务逻辑。
     *
     * @param listener 监听器
     */
    public void addListener(CuratorCacheListener listener) {
        cc.listenable().addListener(listener);
    }

    /**
     * 给注册中心添加一个监听器。
     * 监听器里实现了需要的业务逻辑，且监听器使用给定的Executor调度监听器
     *
     * @param listener 监听器
     * @param executor 执行器
     */
    public void addListener(CuratorCacheListener listener , Executor executor) {
        cc.listenable().addListener(listener , executor);
    }

    /**
     * 移除监听器
     *
     * @param listener 监听器
     */
    public void removeListener(CuratorCacheListener listener) {
        cc.listenable().removeListener(listener);
    }

    /**
     * 关闭注册中心
     */
    public void close() {
        cc.close();
        cf.close();
    }
}

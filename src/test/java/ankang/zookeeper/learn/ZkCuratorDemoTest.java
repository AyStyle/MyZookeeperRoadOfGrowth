package ankang.zookeeper.learn;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.watch.PersistentWatcher;
import org.apache.curator.retry.RetryForever;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;

class ZkCuratorDemoTest {

    private static String TEST_PATH = "/curator_test";

    private static CuratorFramework cf;

    @BeforeAll
    static void init() throws Exception {
        cf = CuratorFrameworkFactory.newClient("localhost:2181" , new RetryForever(3000));

        cf.start();
        cf.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).inBackground().forPath(TEST_PATH);
    }

    @AfterAll
    static void end() throws Exception {
        try {
            cf.delete().deletingChildrenIfNeeded().forPath(TEST_PATH);
        } finally {
            cf.close();
        }
    }

    /**
     * 测试获取children节点
     * 结论：返回的信息为节点名称，不包含路径
     */
    @Test
    void testGetChildren() throws Exception {
        final String parentPath = TEST_PATH + "/parent";
        final String childNode1 = "child_0";
        final String childNode2 = "child_1";
        final String childNode3 = "child_2";

        cf.create().withMode(CreateMode.PERSISTENT).inBackground().forPath(parentPath);

        cf.create().withMode(CreateMode.EPHEMERAL).inBackground().forPath(parentPath + "/" + childNode1);
        cf.create().withMode(CreateMode.EPHEMERAL).inBackground().forPath(parentPath + "/" + childNode2);
        cf.create().withMode(CreateMode.EPHEMERAL).inBackground().forPath(parentPath + "/" + childNode3);

        final List<String> children = cf.getChildren().forPath(parentPath);
        children.sort(Comparator.naturalOrder());
        for (int i = 0 ; i < children.size() ; i++) {
            final String child = children.get(i);
            Assertions.assertEquals("child_" + i , child);
        }

        cf.delete().deletingChildrenIfNeeded().inBackground().forPath(parentPath);
    }

    /**
     * 测试watcher机制
     * 结论：一次注册N次使用
     * 收获：
     * 1. 使用缓存设置监听时，一定要使用 {@link CuratorCache#bridgeBuilder}方法，这个构造方法在节点信息变更时会自动监听
     * 2. 使用{@link CuratorCache#bridgeBuilder#withDataNotCached}方法构造的缓存无法监听节点删除事件
     *
     * @throws Exception
     */
    @Test
    void testCuratorCache() throws Exception {
        final String parentPath = TEST_PATH + "/cache";
        final String childNode1 = "child_0";
        final String childNode2 = "child_1";
        final String childNode3 = "child_2";

        final CuratorCache cc = CuratorCache.bridgeBuilder(cf , parentPath).build();
        cc.start();

        cc.listenable().addListener(CuratorCacheListener.builder().forPathChildrenCache(parentPath , cf , (CuratorFramework client , PathChildrenCacheEvent event) -> {
            switch (event.getType()) {
                case INITIALIZED:
                    System.out.println(String.format("init path: %s, data: %s, listData: %s" , event.getData().getPath() , new String(event.getData().getData()) , event.getInitialData().toString()));
                    break;
                case CHILD_ADDED:
                    System.out.println(String.format("add path: %s, data: %s" , event.getData().getPath() , new String(event.getData().getData())));
                    break;
                case CHILD_UPDATED:
                    System.out.println(String.format("update path: %s, data: %s" , event.getData().getPath() , new String(event.getData().getData())));
                    break;
                case CHILD_REMOVED:
                    System.out.println(String.format("remove path: %s, data: %s" , event.getData().getPath() , new String(event.getData().getData())));
                    break;
                default:
                    System.out.println(event.getType());
                    break;
            }
        }).build());

        try {
            cf.create().withMode(CreateMode.PERSISTENT).inBackground().forPath(parentPath);

            cf.create().inBackground().forPath(parentPath + "/" + childNode1 , childNode1.getBytes());
            Thread.sleep(1000);

            cf.create().inBackground().forPath(parentPath + "/" + childNode2 , childNode2.getBytes());
            Thread.sleep(1000);

            cf.create().withMode(CreateMode.EPHEMERAL).inBackground().forPath(parentPath + "/" + childNode3 , childNode3.getBytes());
            Thread.sleep(1000);

            cf.delete().inBackground().forPath(parentPath + "/" + childNode1);
            Thread.sleep(1000);

            cf.setData().inBackground().forPath(parentPath + "/" + childNode2 , (childNode2 + childNode2).getBytes());
            Thread.sleep(1000);
        } finally {
            cc.close();
        }
    }

}
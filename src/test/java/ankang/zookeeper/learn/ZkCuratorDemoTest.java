package ankang.zookeeper.learn;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.retry.RetryForever;
import org.apache.zookeeper.CreateMode;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;

class ZkCuratorDemoTest {

    private static String TEST_PATH = "/curator_test";

    private static CuratorFramework cf;
    private static CuratorCache cc;

    @BeforeAll
    static void init() throws Exception {
        cf = CuratorFrameworkFactory.newClient("localhost:2181" , new RetryForever(3000));

        cf.start();
        cf.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).inBackground().forPath(TEST_PATH);

        cc = CuratorCache.bridgeBuilder(cf , TEST_PATH).build();
        cc.start();
    }

    @AfterAll
    static void end() throws Exception {
        try {
            cf.delete().deletingChildrenIfNeeded().forPath(TEST_PATH);
        } finally {
            cc.close();
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

        cf.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).inBackground().forPath(parentPath);

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

    /**
     * 测试CuratorCache的createAndChange缓存
     * 结论：
     * 1. 当创建节点时：oldNode为null，newNode不为null
     * 2. 当更新节点时：oldNode不为null，且数据为更新之前的数据；newNode不为null，数据为更新后的数据
     * 3. 监听是递归的，如果节点是递归（多层子节点）的，那么监听器也能监听到。
     */
    @Test
    void testCuratorCacheCreateAndChange() throws Exception {
        final String path = TEST_PATH + "/cacheCreateAndChange/aaa/bbb/ccc/ddd/eee/fff/ggg";

        cc.listenable().addListener(CuratorCacheListener.builder().forCreatesAndChanges((ChildData oldNode , ChildData newNode) -> {
            System.out.println("============================");
            if (oldNode != null) {
                System.out.println("old: " + oldNode.getPath());
                System.out.println(String.format("old: %s, %s" , oldNode.getPath() , new String(oldNode.getData())));
            } else {
                System.out.println("old: null");
            }
            if (newNode != null) {
                System.out.println("new: " + newNode.getPath());
                System.out.println(String.format("new: %s, %s" , newNode.getPath() , new String(newNode.getData())));
            } else {
                System.out.println("new: null");
            }
            System.out.println("---------------------------");
        }).build() , Executors.newSingleThreadExecutor());

        cf.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path , "data1111111".getBytes());
        cf.setData().forPath(path , "data2222222".getBytes());
        Thread.sleep(10000);
    }

}
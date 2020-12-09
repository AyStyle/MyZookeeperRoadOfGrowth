package ankang.zookeeper.learn;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryForever;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.proto.WatcherEvent;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
     * 收获：watched()方法会设置一个监听，并等待所有的inBackground方法执行完成
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

        // watched()：会设置一个监听，并等待所有inBackground操作完成
        final List<String> children = cf.getChildren().watched().forPath(parentPath);
        children.sort(Comparator.naturalOrder());
        for (int i = 0 ; i < children.size() ; i++) {
            final String child = children.get(i);
            Assertions.assertEquals("child_" + i , child);
        }

        cf.delete().deletingChildrenIfNeeded().inBackground().forPath(parentPath);
    }

}
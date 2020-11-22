package ankang.zookeeper.learn;

import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * @author: ankang
 * @email: dreedisgood@qq.com
 * @create: 2020-11-21
 */
public class ZookeeperDemo {

    public static void main(String[] args) throws Exception {
        final ZooKeeper zk = createZk("localhost:2181" , 1000);
        final String nodePath = "/ankang_ephemeral";

        createNode(zk , nodePath , "hello zookeeper".getBytes());
        System.out.println(new String(getNode(zk , nodePath)));

        updateNode(zk , nodePath , "are you ok ?".getBytes());
        System.out.println(new String(getNode(zk , nodePath)));

        System.out.println(zk.getChildren("/" , null));
        deleteNode(zk , nodePath);
        System.out.println(zk.getChildren("/" , null));

        zk.close();
    }

    private static ZooKeeper createZk(String connectString , int sessionTimeout) throws IOException, InterruptedException {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final ZooKeeper zk = new ZooKeeper(connectString , sessionTimeout , (WatchedEvent event) -> countDownLatch.countDown());
        countDownLatch.await();

        return zk;
    }


    /**
     * 创建节点
     *
     * @param zk
     * @param path
     * @param data
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */
    private static String createNode(ZooKeeper zk , String path , byte[] data) throws KeeperException, InterruptedException {
        return zk.create(path , data , ZooDefs.Ids.OPEN_ACL_UNSAFE , CreateMode.EPHEMERAL);
    }

    private static byte[] getNode(ZooKeeper zk , String path) throws KeeperException, InterruptedException {
        return zk.getData(path , false , null);
    }

    private static void updateNode(ZooKeeper zk , String path , byte[] data) throws KeeperException, InterruptedException {
        zk.setData(path , data , -1);
    }

    private static void deleteNode(ZooKeeper zk , String path) throws KeeperException, InterruptedException {
        zk.delete(path , -1);
    }

}

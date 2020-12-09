package ankang.zookeeper.learn;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryForever;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

/**
 * @author: ankang
 * @email: dreedisgood@qq.com
 * @create: 2020-11-23
 */
public class ZkCuratorDemo {

    public static void main(String[] args) throws Exception {
        final CuratorFramework cf = createCurator();
        final String path = "/curator";

        System.out.println(createNode(cf , path , "hello curator!".getBytes()));

        System.out.println(new String(readNode(cf , path)));

        System.out.println(writeNode(cf , path , "modify data to hello world!".getBytes()).getAversion());

        System.out.println(new String(readNode(cf , path)));

        deleteNode(cf , path);
        cf.close();
    }

    /**
     * 创建Curator
     *
     * @return
     */
    private static CuratorFramework createCurator() {
        final CuratorFramework cf = CuratorFrameworkFactory.newClient("127.0.0.1:2181" , new RetryForever(3000));

        cf.start();
        return cf;
    }

    /**
     * 创建节点
     *
     * @param cf
     * @param path
     * @param data
     * @return
     * @throws Exception
     */
    private static String createNode(CuratorFramework cf , String path , byte[] data) throws Exception {
        return cf.create().withMode(CreateMode.EPHEMERAL).forPath(path , data);
    }

    /**
     * 删除节点
     *
     * @param cf
     * @param path
     * @throws Exception
     */
    private static void deleteNode(CuratorFramework cf , String path) throws Exception {
        cf.delete().inBackground().forPath(path);
    }

    /**
     * 获取节点信息
     *
     * @param cf
     * @param path
     * @return
     * @throws Exception
     */
    private static byte[] readNode(CuratorFramework cf , String path) throws Exception {
        return cf.getData().forPath(path);
    }

    /**
     * 修改节点数据
     *
     * @param cf
     * @param path
     * @param data
     * @return
     * @throws Exception
     */
    private static Stat writeNode(CuratorFramework cf , String path , byte[] data) throws Exception {
        return cf.setData().forPath(path , data);
    }
}

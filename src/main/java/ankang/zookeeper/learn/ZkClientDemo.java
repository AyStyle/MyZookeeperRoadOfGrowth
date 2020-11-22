package ankang.zookeeper.learn;

import org.I0Itec.zkclient.ZkClient;

import java.io.Serializable;

/**
 * @author: ankang
 * @email: dreedisgood@qq.com
 * @create: 2020-11-22
 */
public class ZkClientDemo {

    public static void main(String[] args) {
        final ZkClient zkClient = createZkClient();
        final String path = "/zk/client/tmp";

        createNode(zkClient , path);

        writeNode(zkClient , path , "hello zkclient !");
        System.out.println(readNode(zkClient , path));

        writeNode(zkClient, path, new Data(123, "hello ZkClient !"));
        System.out.println(readNode(zkClient, path));

        deleteNode(zkClient , "/zk");

        zkClient.close();
    }

    /**
     * 创建一个ZkClient
     * zkClient通过对Zookeeper API内部的封装，实现了将异步创建会话Session改为同步创建
     */
    private static ZkClient createZkClient() {
        return new ZkClient("localhost:2181");
    }

    /**
     * 创建节点
     * 节点的创建是递归的，当没有父节点时会自动创建
     *
     * @param zkClient
     */
    private static void createNode(ZkClient zkClient , String path) {
        zkClient.createPersistent(path , true);
    }

    /**
     * 删除节点
     * 这里使用的是递归删除节点
     *
     * @param zkClient
     * @param path
     */
    private static void deleteNode(ZkClient zkClient , String path) {
        zkClient.deleteRecursive(path);
    }

    /**
     * 写入数据
     *
     * @param zkClient
     * @param path
     * @param data
     */
    private static void writeNode(ZkClient zkClient , String path , Object data) {
        zkClient.writeData(path , data);
    }

    /**
     * 读取数据
     *
     * @param zkClient
     * @param path
     * @return
     */
    private static Object readNode(ZkClient zkClient , String path) {
        return zkClient.readData(path);
    }

    /**
     * 自定义数据
     */
    private static class Data implements Serializable {
        private Integer id;
        private String data;

        public Data(Integer id , String data) {
            this.id = id;
            this.data = data;
        }

        @Override
        public String toString() {
            return "Data{" +
                    "id=" + id +
                    ", data='" + data + '\'' +
                    '}';
        }
    }
}

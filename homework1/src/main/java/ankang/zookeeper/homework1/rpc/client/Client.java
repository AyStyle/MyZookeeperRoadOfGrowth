package ankang.zookeeper.homework1.rpc.client;

import ankang.zookeeper.homework1.rpc.client.proxy.SayHelloServerProxy;
import ankang.zookeeper.homework1.rpc.server.SayHelloServer;

/**
 * @author: ankang
 * @email: dreedisgood@qq.com
 * @create: 2020-12-07
 */
public class Client {

    public static void main(String[] args) throws InterruptedException {
        final SayHelloServer sayHelloServer = new SayHelloServerProxy();

        while (true) {
            final String s = sayHelloServer.sayHello();
            System.out.println(s);
            Thread.sleep(1000);
        }

    }

}

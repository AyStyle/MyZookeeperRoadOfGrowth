package ankang.zookeeper.homework2.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheBridge;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.retry.RetryForever;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author: ankang
 * @email: dreedisgood@qq.com
 * @create: 2020-12-11
 */
public class ZkService {

    public static void main(String[] args) {
        final CuratorFramework cf = CuratorFrameworkFactory.newClient("localhost:2181" , new RetryForever(3000));
        cf.start();

        final CuratorCacheBridge cc = CuratorCache.bridgeBuilder(cf , Service.SERVER_PATH).build();
        cc.start();

        while (true) {
            cc.stream().forEach(node -> {
                if (!Service.SERVER_PATH.equals(node.getPath())) {
                    final byte[] data = node.getData();
                    final String dataStr = new String(data);
                    final JsonObject json = new Gson().fromJson(dataStr , JsonObject.class);

                    final String lastTimeStr = json.get("last_time").getAsString();
                    final LocalDateTime lastTime = LocalDateTime.parse(lastTimeStr , DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                    if (Duration.between(lastTime , LocalDateTime.now()).toSeconds() > 5) {
                        try {
                            json.addProperty("last_time" , "1000-01-01 00:00:00");
                            cf.setData().inBackground().forPath(node.getPath() , json.toString().getBytes());
                        } catch (Exception e) {

                        }
                    }
                }
            });
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
            }
        }
    }

}

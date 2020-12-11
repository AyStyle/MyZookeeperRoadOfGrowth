package ankang.zookeeper._homework3.conf;

import com.alibaba.druid.pool.DruidDataSource;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheBridge;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.retry.RetryForever;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.lang.reflect.Method;

/**
 * @author: ankang
 * @email: dreedisgood@qq.com
 * @create: 2020-12-11
 */
@Configuration
public class DataSourceConfiguration {

    @Bean
    public DataSource dataSource() {
        return (DataSource) Enhancer.create(DruidDataSource.class , new MethodInterceptor() {

            private DruidDataSource dataSource;
            private final String confPath = "/ankang/config/datasource";
            private final CuratorFramework cf = CuratorFrameworkFactory.newClient("localhost:2181" , new RetryForever(3000));
            private final CuratorCacheBridge cc = CuratorCache.bridgeBuilder(cf , confPath).build();

            {
                cf.start();
                cc.start();
                cc.listenable().addListener(CuratorCacheListener.builder().forCreatesAndChanges((ChildData oldNode , ChildData newNode) -> {
                    if (confPath.equals(newNode.getPath())) {
                        if (dataSource != null) {
                            System.err.println("更新数据连接池...");
                            dataSource.close();
                        } else {
                            System.err.println("初始化数据库连接池...");
                        }

                        final JsonObject json = new Gson().fromJson(new String(newNode.getData()) , JsonObject.class);
                        dataSource = new DruidDataSource();

                        dataSource.setDriverClassName(json.get("driver").getAsString());
                        dataSource.setUrl(json.get("url").getAsString());
                        dataSource.setUsername(json.get("username").getAsString());
                        dataSource.setPassword(json.get("password").getAsString());
                        System.err.println("数据库连接池创建完成...");
                    }
                }).build());
            }

            @Override
            public Object intercept(Object obj , Method method , Object[] args , MethodProxy proxy) throws Throwable {
                if (dataSource == null) {
                    return null;
                }
                return method.invoke(dataSource , args);
            }
        });

    }

}

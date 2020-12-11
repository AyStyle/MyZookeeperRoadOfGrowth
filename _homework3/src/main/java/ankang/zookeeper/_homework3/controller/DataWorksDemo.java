package ankang.zookeeper._homework3.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author: ankang
 * @email: dreedisgood@qq.com
 * @create: 2020-12-11
 */

@RestController
@RequestMapping("/dataworks")
public class DataWorksDemo {

    private final DataSource dataSource;

    @Autowired
    public DataWorksDemo(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/one/{id}")
    public String getById(@PathVariable("id") String id) throws SQLException {
        try (final Connection connection = dataSource.getConnection()) {
            final PreparedStatement ps = connection.prepareStatement("SELECT * FROM db_dataworks_metadata.tb_task_info WHERE id = ?");
            ps.setString(1 , id);

            final ResultSet resultSet = ps.executeQuery();
            if (resultSet.next()) {
                final JsonObject json = new JsonObject();
                json.addProperty("id" , resultSet.getString("id"));
                json.addProperty("task_name" , resultSet.getString("task_name"));
                json.addProperty("crontab" , resultSet.getString("crontab"));
                json.addProperty("owner_name" , resultSet.getString("owner_name"));
                json.addProperty("deploy_date" , resultSet.getString("deploy_date"));

                return json.toString();
            } else {
                return null;
            }
        }

    }
}

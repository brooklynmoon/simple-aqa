package qa.frame.component;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
@Component
public class DatabaseConnection {

    private final DataSource dataSource;

    public DatabaseConnection() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://localhost:4502/db");
        config.setUsername("admin_user");
        config.setPassword("password");
        config.setMaximumPoolSize(5);
        config.setPoolName("Тест");

        this.dataSource = new HikariDataSource(config);
        log.info("[JDBC] Initialized HikariCP connection pool");
    }

    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            log.error("[JDBC] Не удалось получить соединение из пула ", e);
            throw new RuntimeException("Ошибка при подключении к базе данных ", e);
        }
    }
}

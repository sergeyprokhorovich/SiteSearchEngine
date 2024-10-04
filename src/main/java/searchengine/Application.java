package searchengine;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.sql.*;


@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", "myuser", "mypass"); Statement statement = connection.createStatement()) {
            statement.executeQuery("SELECT count(*) FROM pg_database WHERE datname = 'search_engine'");
            ResultSet resultSet = statement.getResultSet();
            resultSet.next();
            int count = resultSet.getInt(1);
            if (count <= 0) {
                statement.executeUpdate("CREATE DATABASE search_engine");
            }
        } catch (SQLException ignored) {
        }
        SpringApplication.run(Application.class, args);
    }
}

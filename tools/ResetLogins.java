import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class ResetLogins {
    public static void main(String[] args) throws Exception {
        Properties properties = loadProperties("src/main/resources/application.properties");
        String url = resolveProperty(properties, "spring.datasource.url");
        String username = resolveProperty(properties, "spring.datasource.username");
        String password = resolveProperty(properties, "spring.datasource.password");

        try (Connection connection = DriverManager.getConnection(url, username, password);
             Statement statement = connection.createStatement()) {
            connection.setAutoCommit(false);

            statement.execute("SET FOREIGN_KEY_CHECKS=0");
            statement.executeUpdate("TRUNCATE TABLE medical_records");
            statement.executeUpdate("TRUNCATE TABLE appointments");
            statement.executeUpdate("TRUNCATE TABLE bills");
            statement.executeUpdate("TRUNCATE TABLE doctors");
            statement.executeUpdate("TRUNCATE TABLE patients");
            statement.executeUpdate("TRUNCATE TABLE departments");
            statement.executeUpdate("TRUNCATE TABLE users");
            statement.executeUpdate("TRUNCATE TABLE hospitals");
            statement.execute("SET FOREIGN_KEY_CHECKS=1");

            connection.commit();
            System.out.println("All login-related data cleared successfully.");
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to reset login data: " + ex.getMessage(), ex);
        }
    }

    private static Properties loadProperties(String path) throws IOException {
        Properties properties = new Properties();
        try (FileInputStream inputStream = new FileInputStream(path)) {
            properties.load(inputStream);
        }
        return properties;
    }

    private static String resolveProperty(Properties properties, String key) {
        String rawValue = properties.getProperty(key);
        if (rawValue == null) {
            return null;
        }

        if (!rawValue.startsWith("${") || !rawValue.endsWith("}")) {
            return rawValue;
        }

        String body = rawValue.substring(2, rawValue.length() - 1);
        int separatorIndex = body.indexOf(':');
        if (separatorIndex < 0) {
            return System.getenv(body);
        }

        String envKey = body.substring(0, separatorIndex);
        String defaultValue = body.substring(separatorIndex + 1);
        String envValue = System.getenv(envKey);
        return (envValue == null || envValue.isBlank()) ? defaultValue : envValue;
    }
}

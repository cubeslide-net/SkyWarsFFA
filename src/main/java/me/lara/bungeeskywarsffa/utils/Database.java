package me.lara.bungeeskywarsffa.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.lara.bungeeskywarsffa.BungeeSkywarsFFA;
import org.bukkit.configuration.Configuration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class Database {

    private final HikariDataSource dataSource;
    private final Connection connection;

    public Database() {
        HikariConfig hikariConfig = new HikariConfig();
        final Configuration spigotConfig = BungeeSkywarsFFA.getInstance().getConfig();
        hikariConfig.setJdbcUrl("jdbc:mysql://" + spigotConfig.getString("MYSQL.HOSTNAME") + ":" + spigotConfig.getInt("MYSQL.PORT") + "/" + spigotConfig.getString("MYSQL.DATABASE"));
        hikariConfig.setUsername(spigotConfig.getString("MYSQL.USERNAME"));
        hikariConfig.setPassword(spigotConfig.getString("MYSQL.PASSWORD"));
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");
        this.dataSource = new HikariDataSource(hikariConfig);
        try {
            connection = dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void update(String query) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public boolean doesPlayerExistByUUID(UUID uuid) {
        try {
            final PreparedStatement prepareStatement = getConnection().prepareStatement("SELECT * FROM `SkyWarsFFAStats` WHERE UUID=?;");
            prepareStatement.setString(1, uuid.toString());
            final ResultSet resultSet = prepareStatement.executeQuery();
            final boolean result = resultSet.next();
            prepareStatement.close();
            resultSet.close();
            return result;

        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return false;
    }

    private Connection getConnection() throws SQLException {
        return connection;
    }


    public void createTable() {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = dataSource.getConnection();
            ps = conn.prepareStatement("CREATE TABLE IF NOT EXISTS `SkyWarsFFAStats` " + "(" + "UUID varchar(40), KILLS int, DEATHS int" + ");");
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getKills(UUID uuid) {
        try {
            final Connection connection = getConnection();
            final PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM SkyWarsFFAStats WHERE uuid = '" + uuid + "';");
            ResultSet results = preparedStatement.executeQuery();
            if (results.next()) {
                return results.getInt("KILLS");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
        return -1;
    }

    public int getDeaths(UUID uuid) {
        try {
            final Connection connection = getConnection();
            final PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM SkyWarsFFAStats WHERE uuid = '" + uuid + "';");
            ResultSet results = preparedStatement.executeQuery();
            if (results.next()) {
                return results.getInt("DEATHS");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
        return -1;
    }

    public void addDeath(UUID uuid) {
        try {
            Connection connection = getConnection();
            PreparedStatement prepareStatement = connection.prepareStatement("UPDATE SkyWarsFFAStats SET DEATHS=" + (getDeaths(uuid) + 1) + " WHERE UUID='" + uuid.toString() + "'");
            prepareStatement.execute();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void addKill(UUID uuid) {
        try {
            Connection connection = getConnection();
            PreparedStatement prepareStatement = connection.prepareStatement("UPDATE SkyWarsFFAStats SET KILLS=" + (getKills(uuid) + 1) + " WHERE UUID='" + uuid.toString() + "'");
            prepareStatement.execute();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void createNewUser(UUID uuid) {
        if (doesPlayerExistByUUID(uuid)) return;

        try {
            Connection connection = getConnection();
            PreparedStatement prepareStatement = connection.prepareStatement("INSERT INTO SkyWarsFFAStats(UUID, KILLS, DEATHS) VALUES(?, ?, ?);");
            prepareStatement.setString(1, uuid.toString());
            prepareStatement.setInt(2, 0);
            prepareStatement.setInt(3, 0);
            prepareStatement.execute();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void close() throws SQLException {

        if (getConnection() == null || getConnection().isClosed()) return;
        try {
            getConnection().close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


}

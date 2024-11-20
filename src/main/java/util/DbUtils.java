package util;

import model.User;

import java.sql.*;
import java.util.Properties;

public class DbUtils {
    private final Connection _conn;
    private final Properties _props = PropertiesUtils.GetProperties();;

    public DbUtils() {
        Connection c;
        if (_props != null) {
            String connStr = "jdbc:mysql://localhost:33061/authdatabase";

            try {
                System.out.println(_props.getProperty("MYSQL_ROOT_PW"));
                c = DriverManager.getConnection(
                        connStr,
                        _props.getProperty("MYSQL_ROOT_USER"),
                        _props.getProperty("MYSQL_ROOT_PW")
                );
            } catch (SQLException e) {
                c = null;
                e.printStackTrace();
            }
        }
        else c = null;

        _conn = c;
    }

    public boolean addUser(String username, String password, String role) {
        String sql = "INSERT INTO Users (Username, Password, Role) VALUES (?, ?, ?)";

        try (PreparedStatement addUserSql = this._conn.prepareStatement(sql)) {
            addUserSql.setString(1, username);
            addUserSql.setString(2, password);
            addUserSql.setString(3, role);
            addUserSql.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean userExists(String username) {
        String query = "SELECT Username FROM Users WHERE Username = ?";

        try(PreparedStatement ps = this._conn.prepareStatement(query)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            return rs.next();
        } catch(SQLException e) {
                e.printStackTrace();
        }

        return false;
    }

    public User getUser(String username) {
        String query = "SELECT * FROM Users WHERE Username = ?";

        System.out.println(query);

        try(PreparedStatement ps = this._conn.prepareStatement(query)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            rs.next();

            return new User(
                    rs.getString("Username"),
                    rs.getString("Password"),
                    rs.getString("Role")
            );


        } catch(SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}

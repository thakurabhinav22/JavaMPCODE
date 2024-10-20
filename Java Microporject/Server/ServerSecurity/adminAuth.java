package Server.ServerSecurity;
import java.sql.*;

public class adminAuth {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/gamezone";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "root";

    public boolean authenticate(String password) {
        String userid = "admin";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        boolean isAuthenticated = false;

        try {

            Class.forName("com.mysql.cj.jdbc.Driver");


            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);


            String sql = "SELECT password FROM admin WHERE userid = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, userid); 


            rs = stmt.executeQuery();

            if (rs.next()) {
                String retrievedPassword = rs.getString("password");

                if (retrievedPassword.equals(password)) {
                    isAuthenticated = true;
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace(); 
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return isAuthenticated; 
    }
}
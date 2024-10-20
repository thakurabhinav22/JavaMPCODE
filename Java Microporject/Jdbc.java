import java.sql.*;
import java.io.*;

class Jdbc{
    public static void main(String[] args) throws Exception {
        String url = "jdbc:mysql://localhost:3306/mysql";
        String username = "root";
        String password = "root";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url, username, password);
            System.out.println("Connected to Database: "+conn);
            conn.close();
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    
    }
}
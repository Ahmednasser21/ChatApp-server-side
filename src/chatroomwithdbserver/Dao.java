package chatroomwithdbserver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import org.apache.derby.jdbc.ClientDriver;

public class Dao {
    private static Dao instance = null;
    static Connection conn;
    
    private Dao() throws SQLException{
        DriverManager.registerDriver(new ClientDriver());
        conn = DriverManager.getConnection("jdbc:derby://localhost:1527/Users", "root", "root");
    }
    
    public static Dao getInstance() throws SQLException{
        if(instance == null){
            instance = new Dao();
        }
        return instance;
    }
    
    
    public static void insertUser(UserDto user) throws SQLException{
        String query = "INSERT INTO USERS (username,email,password) VALUES(?,?,?)";
        
        PreparedStatement statment = conn.prepareStatement(query);
        statment.setString(1, user.getUsername());
        statment.setString(2,user.getEmail());
        statment.setString(3, user.getPassword());
        
        statment.executeUpdate();
        
    }
   
    
    public static ArrayList<UserDto> getAllUsers() throws SQLException{
        String query = "SELECT * FROM USERS";
        UserDto user = new UserDto();
        ArrayList<UserDto> users = new ArrayList<>();
        PreparedStatement statment = conn.prepareStatement(query);
        ResultSet rst = statment.executeQuery();
        while (rst.next()) {            
            user.setUsername(rst.getString("username"));
            user.setEmail(rst.getNString("email"));
            user.setPassword(rst.getString("password"));
            users.add(user);
        }
        return users;
    }
    
    
    public static boolean isUsernameExist(String username) throws SQLException{
        String query = "SELECT USERNAME FROM USERS WHERE USERNAME=?";
        PreparedStatement statment = conn.prepareStatement(query);
        statment.setString(1, username);
        ResultSet rst = statment.executeQuery();
        
        return rst.next();
    }
    
     public static boolean isUsernameAndPasswordExist(String username, String password) throws SQLException{
        String query = "SELECT USERNAME, PASSWORD FROM USERS WHERE USERNAME = ? AND PASSWORD = ?";
        
         PreparedStatement statment = conn.prepareStatement(query);
         statment.setString(1, username);
         statment.setString(2, password);
         
         ResultSet rst = statment.executeQuery();
         
         return rst.next();
    }
     
     public void changeStatus(String username, boolean status) throws SQLException{
         String query = "UPDATE USERS SET isActive=? WHERE USERNAME=?";
         
         PreparedStatement statment = conn.prepareStatement(query);
         statment.setBoolean(1, status);
         statment.setString(2, username);
         statment.executeUpdate();
         
     }
     
     
     
   
    
}

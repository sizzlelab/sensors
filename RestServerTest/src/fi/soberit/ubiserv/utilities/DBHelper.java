package fi.soberit.ubiserv.utilities;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DBHelper {
	public static void main(String[] args) {
		Connection conn = null;
	 conn = ConnectToDB(conn);
 
		
	}
	public static Connection ConnectToDB(Connection conn){
		//TODO: move db address to resources
		String url = "jdbc:mysql://localhost/valeriy_ubiserv_test";
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection(url,"root","root");
	 
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return conn;
	}
	
	public static void closeDBConnection(Connection conn){
		if (conn != null)
			try {
				conn.close();
			} catch (SQLException e) {

				e.printStackTrace();
			}
	}
	
 
}

package fi.soberit.ubiserv.utilities;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DBHelper {
	public static void main(String[] args) {
		Connection conn = null;
	 conn = ConnectToDB(conn);
	 if (conn != null) {
		 InsertValue(conn);
		 closeDBConnection(conn);
	 	}
		
	}
	public static Connection ConnectToDB(Connection conn){
		String url = "jdbc:mysql://localhost/valeriy_ubiserv_test";
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection(url,"root","root");
			System.out.print("OK!");
			
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
	
	private static void InsertValue(Connection conn) {
		try {
			PreparedStatement s = conn.prepareStatement(
					"INSERT INTO ip_addresses (address,imei) VALUES(?,?)"
					);
			s.setString(1, "1.1.1.1");
			s.setString(2, "21345673");
			s.executeUpdate();
			s.close();
			System.out.print("Inserted");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
		
	 
		
	
	}
}

package fi.soberit.ubiserv.Data;

import java.sql.Connection;
import java.util.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
 

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import fi.soberit.ubiserv.utilities.DBHelper;

public class DataAddServerResource extends ServerResource implements IDataAdd {
	public DataAddServerResource() {}
	
	@Get
	public String AddData(){
		
		
		return "OK";
	}

	
	@Override
	public String DataAdd(DataRecord data) {
		System.out.println(data.getData());
		
		java.util.Date now = new java.util.Date();  
		Timestamp tStamp =  new java.sql.Timestamp( now.getTime() ) ;  
		
		Connection conn = null;
		conn = DBHelper.ConnectToDB(conn);
		 if (conn != null) {
			 InsertValue(conn,"adadad","data",tStamp);
			 DBHelper.closeDBConnection(conn);
		 	}
		
		
		return "test test";
	}
	
	private void InsertValue(Connection conn,String imei,String data,Timestamp date) {
		try {
			PreparedStatement s = conn.prepareStatement(
					"INSERT INTO `valeriy_ubiserv_test`.`" +
					"sensorsdata` (`idPhone`, `Data`, `Date`) VALUES (?, ?, ?);"

					);
			s.setString(1, imei);
			s.setString(2, data);
			s.setTimestamp( 3, date );   
			s.executeUpdate();
			s.close();
			System.out.print("Inserted");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
	}
	

}

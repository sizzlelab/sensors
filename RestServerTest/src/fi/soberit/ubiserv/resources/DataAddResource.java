package fi.soberit.ubiserv.resources;

import java.sql.Connection;
import java.util.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
 

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import fi.soberit.ubiserv.Data.DataRecord;
import fi.soberit.ubiserv.Data.IDataAdd;
import fi.soberit.ubiserv.utilities.DBHelper;

public class DataAddResource extends ServerResource implements IDataAdd {
	public DataAddResource() {}
	
	@Get
	public String AddData(){
		return "OK";
	}

	/**
	 * Handling data sent from a phone
	 */
	@Override
	public String DataAdd(DataRecord data) {
		System.out.println(data.getData());
		
		//java.util.Date now = new java.util.Date();  
		//Timestamp tStamp =  new java.sql.Timestamp( now.getTime() ) ;  
		
		Connection conn = null;
		conn = DBHelper.ConnectToDB(conn);
		 if (conn != null) {
			 //InsertValue(conn,"adadad","data",tStamp);
			 InsertValue(conn,data);
			 DBHelper.closeDBConnection(conn);
		 	}
		
		
		return "test test";
	}
	
	/**
	 * Saving data to DB
	 * @param conn DB connection
	 * @param dataRecord Class for data to be saved
	 */
	private void InsertValue(Connection conn, DataRecord dataRecord){ //(Connection conn,String imei,String data,Timestamp date) {
		try {
			PreparedStatement s = conn.prepareStatement(
					"INSERT INTO `valeriy_ubiserv_test`.`" +
					"sensorsdata` (`idPhone`, `Data`, `Date`) VALUES (?, ?, ?);"

					);
			s.setString(1, dataRecord.getPhoneId());
			s.setString(2, dataRecord.getData());
			s.setTimestamp( 3, dataRecord.getDate());   
			s.executeUpdate();
			s.close();
			System.out.print("Inserted");
		} catch (SQLException e) {
			// TODO: Write to a logger
			e.printStackTrace();
			
		}
	}
	

}

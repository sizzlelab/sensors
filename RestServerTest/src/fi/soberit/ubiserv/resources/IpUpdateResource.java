package fi.soberit.ubiserv.resources;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.restlet.Request;
import org.restlet.data.Form;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import fi.soberit.ubiserv.utilities.DBHelper;
/**
 * Handles phones' IP updates
 * @author Valeriy Volodenkov
 *
 */
public class IpUpdateResource extends ServerResource{
	public IpUpdateResource() {
		
	}
	
	@Get
	public String getIp(){
		return "returning get for ip";
	}

	//TODO: return server states
	@Post
	public String postUpdateIp(Representation data){
		
		Form form = new Form(data);
		
		
		/*Form form = request.getResourceRef().getQueryAsForm();
		String params = "";
		for (Parameter parameter : form) {
		  params += " parameter " + parameter.getName() +
		  ": " + parameter.getValue();
		}
		if (params.length()>0){
			//Log.w(getString(R.string.tag), params);
			params = System.getProperty("line.separator") + params;
			System.out.print(params);
		}*/
		
		String ip = form.getFirstValue("ip");
		String imei = form.getFirstValue("imei");
		System.out.print("!!!INSERTED!!!!!!!!!!"+ ip);
		
		Connection conn = null;
		conn = DBHelper.ConnectToDB(conn);
		 if (conn != null) {
			 InsertValue(conn,ip,imei);
			 DBHelper.closeDBConnection(conn);
		 	}
		
		return "ok";
	}
	
	
	private void InsertValue(Connection conn,String ip,String imei) {
		try {
			PreparedStatement s = conn.prepareStatement(
					"INSERT INTO ip_addresses (address,imei) VALUES(?,?)"
					);
			s.setString(1, ip);
			s.setString(2, imei);
			s.executeUpdate();
			s.close();
			System.out.print("Inserted");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
	}
	
}
package fi.soberit.ubiserv.resources;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.restlet.Request;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import fi.soberit.ubiserv.Data.IpAdressRecord;
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

 
	
	@Post
	public String postUpdateIp(IpAdressRecord ipRecord){
		
		SessionFactory sessionFactory = new Configuration().
				configure().buildSessionFactory();
		Session session = sessionFactory.openSession();
		session.beginTransaction();
		session.save(ipRecord);
		session.getTransaction().commit();
		session.close();	
		
		setStatus(Status.SUCCESS_OK);
		return null;
	}
	
 
	
}
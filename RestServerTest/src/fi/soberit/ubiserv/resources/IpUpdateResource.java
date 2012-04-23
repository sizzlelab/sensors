package fi.soberit.ubiserv.resources;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import fi.soberit.ubiserv.Data.IpAdressRecord;
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
package fi.soberit.ubiserv.resources;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MessagesResource extends ServerResource{

		@Get
		public String getMessages(){
		
			   SessionFactory sessionFactory = new Configuration().
							configure().buildSessionFactory();
			   Session session = sessionFactory.openSession();
			   session.beginTransaction();
			   Query query = session.getNamedQuery("findLastRecords").setMaxResults(100);
			   List list = query.list();
			   session.getTransaction().commit();
			   session.close();	
			   Gson gson = new GsonBuilder().
				   		excludeFieldsWithoutExposeAnnotation().create();
			   String json = gson.toJson(list);
			
		
		
		
			return json;
		}
}




 
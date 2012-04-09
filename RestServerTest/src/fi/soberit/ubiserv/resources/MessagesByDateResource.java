package fi.soberit.ubiserv.resources;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MessagesByDateResource extends ServerResource{
	
	@Get
	public String getMessages(){
		int year;
		int month;
		int day;
		//not correct request
		try {
			year = Integer.parseInt((String) getRequestAttributes().get("year"));
			month = Integer.parseInt((String) getRequestAttributes().get("month"));
			day = Integer.parseInt((String) getRequestAttributes().get("day"));
		}
		catch (NumberFormatException e) {
			setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Incorrect date format in the request "); 
	   		 return null;	
		} 
		//Months start from 0
		Calendar from = new GregorianCalendar(year,month-1,day); 
		Calendar to   = new GregorianCalendar(year,month-1,day);
			
		from.add(Calendar.DATE, -1);
		to.add(Calendar.DATE, 1);
			
		SessionFactory sessionFactory = new Configuration().
						configure().buildSessionFactory();
		Session session = sessionFactory.openSession();
		session.beginTransaction();
		Query query = session.getNamedQuery("findByDate").
				   setParameter("dateFrom",from.getTime()).
				   setParameter("dateTo", to.getTime());
		List list = query.list();
		session.getTransaction().commit();
		session.close();	
		   
		//no data, return 404 code
		if (list.isEmpty())
		   	{setStatus(Status.CLIENT_ERROR_NOT_FOUND, "There are no records with that date "); 
		   	 return null;	
		   	}
		   
		Gson gson = new GsonBuilder().
			   		excludeFieldsWithoutExposeAnnotation().create();
		String json = gson.toJson(list);

		return json;	
	}
	
}

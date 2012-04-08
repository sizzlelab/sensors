package fi.soberit.ubiserv;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.restlet.resource.ServerResource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fi.soberit.ubiserv.Data.DataRecord;

 

public class Test extends ServerResource {    

	   public static void main(String[] args) throws Exception {  
	      // Create the HTTP server and listen on port 8182  
	    
		   DataRecord dRecord = new DataRecord();
		   dRecord.setData("hiber");
		   dRecord.setPhoneId("hiber");
		   
		   java.util.Date now = new java.util.Date();  
		   dRecord.setDate(new Timestamp(now.getTime()));
		   
		   SessionFactory sessionFactory = new Configuration().
				   							configure().buildSessionFactory();
		   Session session = sessionFactory.openSession();
		   session.beginTransaction();
		   Query query = session.getNamedQuery("findLastRecords").setMaxResults(100);
		   List list = query.list();
		  // session.save(dRecord);
		   session.getTransaction().commit();
		   session.close();
		   
		   
		   System.out.println("num of elements " + list.size());
		   
		   Gson gson = new GsonBuilder().
				   		excludeFieldsWithoutExposeAnnotation().create();
			String json = gson.toJson(list);
			System.out.println(json);
		   
		  
		   
	   }

 

	}  
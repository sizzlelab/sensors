package fi.soberit.ubiserv;

import org.restlet.Application;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.routing.Router;

import fi.soberit.ubiserv.resources.DataAddResource;
import fi.soberit.ubiserv.resources.IpUpdateResource;
import fi.soberit.ubiserv.resources.MessagesByDateResource;
import fi.soberit.ubiserv.resources.MessagesByPhoneIdAndDateResource;
import fi.soberit.ubiserv.resources.MessagesByPhoneIdResource;
import fi.soberit.ubiserv.resources.MessagesResource;

public class RestletServerApplication extends Application{

	public static void main(String[] args) throws Exception { 
 
		}
	 
	@Override
	public Restlet createInboundRoot() { 
		Router router = new Router (getContext());
		//100 latest messages
		router.attach("/messages/",MessagesResource.class);
		router.attach("/messages/{year}/{month}/{day}",MessagesByDateResource.class);
		router.attach("/messages/imei/{phoneId}",MessagesByPhoneIdResource.class);
		router.attach("/messages/imei/{phoneId}/{year}/{month}/{day}",MessagesByPhoneIdAndDateResource.class);
		router.attach("/ip/",IpUpdateResource.class);
		router.attach("/data/add/",DataAddResource.class);
		return router;
	}
	
	
	// Create the orders handler
	Restlet orders = new Restlet(getContext()) {
	    @Override
	    public void handle(Request request, Response response) {
	        // Print the user name of the requested orders
	        String message = "Orders of user \""
	                 + "\"";
	        response.setEntity(message, MediaType.TEXT_PLAIN);
	    }
	};
	
	
}
	
	

package fi.soberit.ubiserv;

import org.restlet.Application;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.routing.Router;

import fi.soberit.ubiserv.Data.DataAddServerResource;

public class RestletServerApplication extends Application{

	public static void main(String[] args) throws Exception { 
		//Server mailServer = new Server(Protocol.HTTP, 8311);
		//mailServer.setNext(new AppTest());
		//mailServer.start();
		}
	 
	@Override
	public Restlet createInboundRoot() { 
		Router router = new Router (getContext());
		router.attach("/",MessageServiceResource.class);
		//100 latest messages
		router.attach("/messages/",MessagesServerResource.class);
		router.attach("/ip/",IpUpdateServerResource.class);
		router.attach("/data/add/",DataAddServerResource.class);
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
	
	

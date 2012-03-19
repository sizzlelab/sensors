package fi.soberit.ubiserv;

import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;

 

public class Test extends ServerResource {    

	   public static void main(String[] args) throws Exception {  
	      // Create the HTTP server and listen on port 8182  
	      new Server(Protocol.HTTP, 8182, Test.class).start();  
	   }

	   @Get  
	   public String data() {  
	      return "hello, data.";  
	   }
	   
	  @Put
	  public String add(){
		  return "data was added";
	  }

	}  
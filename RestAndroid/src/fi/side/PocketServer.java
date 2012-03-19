package fi.side;

 

import org.restlet.Request;
import org.restlet.Server;
import org.restlet.data.Form;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import android.util.Log;

public class PocketServer extends ServerResource {

		public PocketServer() throws Exception{
			    
		}
		 
		
		@Get
		public String toString() {	
			Request request = getRequest();
			/*Form form = request.getResourceRef().getQueryAsForm();
			String params = "";
			for (Parameter parameter : form) {
			  params += " parameter " + parameter.getName() +
			  ": " + parameter.getValue();
			}
			if (params.length()>0){
				//Log.w(getString(R.sWtring.tag), params);
				params = System.getProperty("line.separator") + params;
				System.out.print(params);
			}*/
			return "This is test response from Android phone.";// + params;
		}
		
		
}

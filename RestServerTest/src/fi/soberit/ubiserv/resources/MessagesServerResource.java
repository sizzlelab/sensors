package fi.soberit.ubiserv.resources;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class MessagesServerResource extends ServerResource{

	@Get
		public String getMessages(){
			return "messages list";
		}
}




//SELECT * FROM valeriy_ubiserv_test.sensorsdata ORDER BY DATE LIMIT 0, 100 
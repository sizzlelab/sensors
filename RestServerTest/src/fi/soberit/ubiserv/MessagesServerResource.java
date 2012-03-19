package fi.soberit.ubiserv;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class MessagesServerResource extends ServerResource{

	@Get
		public String getMessages(){
			return "messages list";
		}
}

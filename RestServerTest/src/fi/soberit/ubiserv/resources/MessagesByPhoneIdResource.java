package fi.soberit.ubiserv.resources;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class MessagesByPhoneIdResource extends ServerResource{

	@Get
	public String getMessages(){
		String phoneId = (String) getRequestAttributes().get("phoneId");
		return "messages list by device " + phoneId;
	}

}
//(String) getRequestAttributes().get("accountId"));
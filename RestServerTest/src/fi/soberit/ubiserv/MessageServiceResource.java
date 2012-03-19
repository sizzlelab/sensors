package fi.soberit.ubiserv;

import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

public class MessageServiceResource extends ServerResource {
	public MessageServiceResource( ) {
		 
 }
	
	 

	@Override
	protected Representation get() throws ResourceException { 
	//System.out.println("The GET method of root resource was invoked.");
	return new StringRepresentation("This is a root resource");
	}
	
}

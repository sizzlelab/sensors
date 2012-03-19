package fi.soberit.ubiserv;

import java.io.IOException;

import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

public class ClientResourceTest {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws ResourceException 
	 */
	public static void main(String[] args) throws ResourceException, IOException {
		ClientResource mailRoot =
				new ClientResource("http://google.com/");
				mailRoot.get().write(System.out);

	}

}

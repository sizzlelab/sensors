package fi.soberit.testing;

import org.restlet.data.Form;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

public class IpUpdateCaller {
	public static void main(String[] args) throws Exception {
		ClientResource tester =
		new ClientResource("http://localhost:8321/ip/");
 
		Form form = new Form();
		form.add("ip","192.168.1.1");
		tester.post(form);
		 
}
	
}

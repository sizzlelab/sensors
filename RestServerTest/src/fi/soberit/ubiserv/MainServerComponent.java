package fi.soberit.ubiserv;

import org.restlet.Component;
import org.restlet.data.Protocol;

/**
 * Class that runs Restlet on the server
 * @author Valeriy Volodenkov
 *
 */
public class MainServerComponent extends Component {
	
	private final int port = 8321;
	
	/**
	 * Runs Restlet
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception { 
		new MainServerComponent().start();
		}
		public MainServerComponent() {
		setName("RESTful Server component");
		setDescription("Restlet");
		setOwner("Aalto university");
		setAuthor("Aalto university");
		getServers().add(Protocol.HTTP, port); 
		getDefaultHost().attachDefault(new RestletServerApplication()); 
		}
		
}

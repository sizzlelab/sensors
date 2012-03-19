package fi.soberit.ubiserv;

import org.restlet.Component;
import org.restlet.data.Protocol;

public class MainServerComponent extends Component {

	public static void main(String[] args) throws Exception { 
		new MainServerComponent().start();
		}
		public MainServerComponent() {
		setName("RESTful   Server component");
		setDescription(" ");
		setOwner(" ");
		setAuthor("");
		getServers().add(Protocol.HTTP, 8321); 
		getDefaultHost().attachDefault(new RestletServerApplication()); 
		}
		
}

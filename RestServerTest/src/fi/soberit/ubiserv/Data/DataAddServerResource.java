package fi.soberit.ubiserv.Data;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class DataAddServerResource extends ServerResource implements IDataAdd {
	public DataAddServerResource() {}
	
	@Get
	public String AddData(){
		
		
		return "OK";
	}

	
	@Override
	public String DataAdd(String data) {
		System.out.println(data);
		return "test test";
	}
	
	
	

}

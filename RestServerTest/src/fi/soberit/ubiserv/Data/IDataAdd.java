package fi.soberit.ubiserv.Data;

import org.restlet.resource.Post;

public interface IDataAdd {
	@Post("txt")
	public String DataAdd(String data);
}

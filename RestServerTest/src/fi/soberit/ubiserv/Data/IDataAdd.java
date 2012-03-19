package fi.soberit.ubiserv.Data;

import org.restlet.resource.Post;

public interface IDataAdd {
	@Post
	public String DataAdd(DataRecord data);
}

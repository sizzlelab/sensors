package fi.soberit.testing;

import org.restlet.resource.ClientResource;

import fi.soberit.ubiserv.Data.IDataAdd;

public class DataAddCaller {
	public static void main(String[] args) throws Exception {
		IDataAdd mailRoot = ClientResource.create(
		"http://localhost:8321/data/add/", IDataAdd.class);
		//String result = mailRoot.DataAdd("sd");
		//System.out.println(result);
		}
}

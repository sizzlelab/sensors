package fi.soberit.ubiserv.Data;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

import javax.print.attribute.standard.DateTimeAtCompleted;

public class DataRecord implements Serializable {
	private String phoneId;
	private String data;
	private Timestamp date;
	
	public String getPhoneId() {
		return phoneId;
	}
	public void setPhoneId(String phoneId) {
		this.phoneId = phoneId;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	public Timestamp getDate() {
		return date;
	}
	public void setDate(Timestamp date) {
		this.date = date;
	}
}

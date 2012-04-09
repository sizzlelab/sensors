package fi.soberit.ubiserv.Data;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity (name="ip_address")
public class IpAdressRecord implements Serializable {
	@Id
	private long id;
	private String address;
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getImei() {
		return imei;
	}
	public void setImei(String imei) {
		this.imei = imei;
	}
	private String imei;	
}

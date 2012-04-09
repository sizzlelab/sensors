package fi.soberit.ubiserv.Data;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity (name="sensorsdata")
@NamedQueries({
	@NamedQuery(
	name = "findLastRecords",
	query = "from sensorsdata dr ORDER BY dr.date"
	),
	@NamedQuery(
	name = "findByPhoneId",
	query = "FROM sensorsdata dr WHERE dr.phoneId= :phoneId"
			),
	@NamedQuery(
	name = "findByDate",
	query = "FROM sensorsdata dr WHERE dr.date > :dateFrom AND dr.date < :dateTo "
			),	
	@NamedQuery(
	name = "findByPhoneIdAndDate",
	query = "FROM sensorsdata dr WHERE dr.phoneId= :phoneId AND dr.date > :dateFrom AND dr.date < :dateTo "
			)		
})
public class DataRecord implements Serializable {
	@Id
	@Column(name="idSensorsData")
	private int idSensorsData; 
	
	@Expose
	@Column(name="idPhone")
	private String phoneId;
	
	@Expose
	private String data;
	@Expose
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

package fi.side.restlet;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.sql.Timestamp;
import java.util.Enumeration;

import org.restlet.data.Form;
import org.restlet.resource.ClientResource;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import fi.side.R;
import fi.side.sensors.Sensor;
import fi.side.sensors.SensorListener;
import fi.soberit.ubiserv.Data.DataRecord;
import fi.soberit.ubiserv.Data.IDataAdd;

public class AndroidRestlet {

	   final static String tag = "RestApp";
	   public static String ipAddress = "";
	   static public Sensor sensor;
	   static private String phoneUID;
	   
	   public static void setPhoneUID(String phoneUID) {
		AndroidRestlet.phoneUID = phoneUID;
	}





	public AndroidRestlet(){
	    	
	    	
	    	//ipAddress = getString(R.string.server_address);
	    	
	    	
	    	//registerReceivers();
	    	
	   }
	   

	   public static void init(){
		sensor = new Sensor();
	    sensor.addListener(sensorListener);
	   }
	   
	   
			public static String getLocalIpAddress() {
		        try {
		            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
		                NetworkInterface intf = en.nextElement();
		                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
		                    InetAddress inetAddress = enumIpAddr.nextElement();
		                    if (!inetAddress.isLoopbackAddress()) {
		                        return inetAddress.getHostAddress().toString();
		                    }
		                }
		            }
		        } catch (SocketException ex) {
		            Log.e(tag, ex.toString());
		        }
		        return null;
		    }
	   
	   
			/**
			 * Send updated(new) phone's IP to the server so it'll know it.
			 * @param ip
			 */
			  public static void sendIp() {
				  String ip = getLocalIpAddress();
				   ClientResource client =
							new ClientResource("http://" + ipAddress + ":8321/ip/");
							Form form = new Form();
							form.add("ip",ip);
							form.add("imei",phoneUID); 
							client.post(form);		 
			   }
	   
	
			   private static SensorListener sensorListener = new SensorListener(){
				   public void HandleSensorData(String data) {
						SendData(formData(data));
				   };
			   };
			  
				private static DataRecord formData(String data){
					DataRecord dataRecord = new DataRecord();
					dataRecord.setData("sent from phone " + data);
					dataRecord.setPhoneId(phoneUID);
					
					java.util.Date now = new java.util.Date();  
					Timestamp tStamp =  new java.sql.Timestamp( now.getTime()) ; 
					dataRecord.setDate(tStamp);
					return dataRecord;
				}
				
			  
				/**
				 * Sending gathered data to the server
				 * @param data 
				 */
				private static void SendData(DataRecord data){
					try {
						IDataAdd DataAddServerRes = ClientResource.create(
								"http://"+ AndroidRestlet.ipAddress +":8321/data/add/", IDataAdd.class);
								String result = DataAddServerRes.DataAdd(data);
								System.out.println(result);
					} catch (Exception e) {
						Log.w(tag, e.toString());
						//TODO: save error to the logger storage
					}

				}
					
				
				
}

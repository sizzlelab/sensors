package fi.side.restlet;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.sql.Timestamp;
import java.util.Enumeration;

import org.restlet.resource.ClientResource;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.Log;
import fi.side.R;
import fi.side.sensors.Sensor;
import fi.side.sensors.SensorListener;
import fi.soberit.ubiserv.Data.DataRecord;
import fi.soberit.ubiserv.Data.IDataAdd;
import fi.soberit.ubiserv.Data.IpAdressRecord;

public class AndroidRestlet {
	  static Context context;

	final static String tag = "RestApp";
	   public static String ipAddress = "";
	   static public Sensor sensor;
	   static private String phoneUID;
	   
 

	   public static void setContext(Context ctx) {
		 context = ctx;
	}

		  /**
		   * Getting phone's imei. Used as a unique identifier for a phone
		   * @return
		   */
	   private static String getPhoneUID() {
		   	TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
			String uid = telephonyManager.getDeviceId();
			return uid;
	   }
	   


	public AndroidRestlet(){
	    	
	    	
	    	//ipAddress = getString(R.string.server_address);
	    	
	    	
	    	//registerReceivers();
	    	
	   }
	   
	   private BroadcastReceiver mConnReceiver = new BroadcastReceiver() {
		   
		   /**
		    * Runs when phone's IP address has been changed
		    */
		   public void onReceive(Context context, Intent intent) {
			  //no network available
			   boolean connectivity = !intent.getBooleanExtra(ConnectivityManager. EXTRA_NO_CONNECTIVITY, false);
			   String reason = intent.getStringExtra(ConnectivityManager.EXTRA_REASON);
		       boolean isFailover = intent.getBooleanExtra(ConnectivityManager.EXTRA_IS_FAILOVER, false);
		       NetworkInfo currentNetworkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
		       NetworkInfo otherNetworkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_OTHER_NETWORK_INFO);
		       if (connectivity) 
		    	   try {
		    		   AndroidRestlet.sendIp();
				} catch (Exception e) {
					Log.w(tag, e.toString());
				}
		    	 
		   }  
		   };
	
	
	 private void registerReceivers() {    
	       context.registerReceiver(mConnReceiver, 
	           new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
	   }
	 
	 
	 
	 
	 

	   public static void init(Context ctx){
		context = ctx;
		sensor = new Sensor();
	    sensor.addListener(sensorListener);
	    phoneUID = getPhoneUID(); 
	    ipAddress = context.getString(R.string.server_address);
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
				   
							IpAdressRecord ipRecord = new IpAdressRecord();
							ipRecord.setAddress(ip);
							ipRecord.setImei(phoneUID);
							
							client.post(ipRecord);		 
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
								//String result =
								DataAddServerRes.DataAdd(data);
								//System.out.println(result);
					} catch (Exception e) {
						Log.w(tag, e.toString());
						//TODO: save error to the logger storage
					}

				}
					
				
				
}

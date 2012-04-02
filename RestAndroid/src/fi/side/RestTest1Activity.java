package fi.side;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.sql.Timestamp;
import java.util.Enumeration;

import org.restlet.Server;
import org.restlet.data.Form;
import org.restlet.data.Protocol;
import org.restlet.resource.ClientResource;

import fi.side.sensors.Sensor;
import fi.side.sensors.SensorListener;
import fi.soberit.ubiserv.Data.DataRecord;
import fi.soberit.ubiserv.Data.IDataAdd;

 

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class RestTest1Activity extends Activity {
    /** Called when the activity is first created. */
   final String tag = "RestApp";
   String ipAddress = "";
   Sensor sensor;
   
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
	    		   sendIp(getLocalIpAddress());
			} catch (Exception e) {
				// TODO: handle exception
			}
	    	 
	   }  
	   };
   
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
    	Button btnStart = (Button)findViewById(R.id.btnStart);
    	btnStart.setOnClickListener(onStart);
    		
    	Button btnShowIp = (Button)findViewById(R.id.btnShowIp);
    	btnShowIp.setOnClickListener(onBtnShowIp);
    	
    	Button btnSendIp = (Button)findViewById(R.id.btnSendIp);
    	btnSendIp.setOnClickListener(onBtnSendIp);
    	
    	Button btnSendHeartRate = (Button)findViewById(R.id.btnSendHeartRate);
    	btnSendHeartRate.setOnClickListener(onBtnSendHeartRate);
    	
    	registerReceivers();
    	
    	sensor = new Sensor();
    	sensor.addListener(sensorListener);
    	
    	ipAddress = getString(R.string.server_address);
        
    } 
    
	private View.OnClickListener onBtnShowIp = new View.OnClickListener() {
		
		public void onClick(View arg0) {
			TextView tvIP = (TextView)findViewById(R.id.tvIP);
			tvIP.setText(getLocalIpAddress());				
		}
	};
	
	//Sending mock data to the server. Just for test purposes.
	private View.OnClickListener onBtnSendHeartRate = new View.OnClickListener() {
		
		public void onClick(View v) {
			
			sensor.updateSensor("new sensor data");
			//SendData(formData(""));
	};
	};
	
	private DataRecord formData(String data){
		DataRecord dataRecord = new DataRecord();
		dataRecord.setData("sent from phone " + data);
		dataRecord.setPhoneId(getPhoneUID());
		
		java.util.Date now = new java.util.Date();  
		Timestamp tStamp =  new java.sql.Timestamp( now.getTime()) ; 
		dataRecord.setDate(tStamp);
		return dataRecord;
	}
	
	/**
	 * Sending gathered data to the server
	 * @param data 
	 */
	private void SendData(DataRecord data){
		try {
			IDataAdd DataAddServerRes = ClientResource.create(
					"http://"+ ipAddress +":8321/data/add/", IDataAdd.class);
					String result = DataAddServerRes.DataAdd(data);
					System.out.println(result);
		} catch (Exception e) {
			Log.w(tag, e.toString());
			//TODO: save error to the logger storage
		}
		
	}
	
	//Test method for getting phone's IP not used in production
	private View.OnClickListener onBtnSendIp = new View.OnClickListener() {
		
		public void onClick(View arg0) {
			sendIp(getLocalIpAddress());
 
		}
	};
	
	// Used for phone's internal REST server
    private View.OnClickListener onStart = new View.OnClickListener() {
		
		public void onClick(View v) {
			try {
				Server serv = new Server(Protocol.HTTP, 8190, PocketServer.class); 
				serv.start();
			} catch (Exception e) {
			 
				Log.w(tag, e.toString());
			 
			}
			TextView tvHello = (TextView) findViewById(R.id.tvHello);
			tvHello.setText("Test");
		}
	};
    
	public String getLocalIpAddress() {
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
	            Log.e("MenuApp", ex.toString());
	        }
	        return null;
	    }
	  
	/**
	 * Send updated(new) phone's IP to the server so it'll know it.
	 * @param ip
	 */
	  private void sendIp(String ip) {
		   ClientResource client =
					new ClientResource("http://" + ipAddress + ":8321/ip/");
					Form form = new Form();
					form.add("ip",ip);
					form.add("imei",getPhoneUID()); 
					client.post(form);		 
	   }
	   
	  /**
	   * Getting phone's imei. Used as a unique identifier for a phone
	   * @return
	   */
	   private String getPhoneUID() {
		   	TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
			String uid = telephonyManager.getDeviceId();
			return uid;
	   }
	   
	   private void registerReceivers() {    
	       registerReceiver(mConnReceiver, 
	           new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
	   }
	 
	   
	   private SensorListener sensorListener = new SensorListener(){
		   public void HandleSensorData(String data) {
				SendData(formData(data));
		   };
	   };
    
}
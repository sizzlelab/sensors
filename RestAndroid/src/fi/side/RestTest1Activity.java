package fi.side;

import org.restlet.Server;
import org.restlet.data.Protocol;

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
import fi.side.restlet.AndroidRestlet;

public class RestTest1Activity extends Activity {
    /** Called when the activity is first created. */

	   final String tag = "RestApp";
  
   
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
    	
     
    	

    	AndroidRestlet.init(getApplicationContext());
   
   
 
        
    } 
    
	private View.OnClickListener onBtnShowIp = new View.OnClickListener() {
		
		public void onClick(View arg0) {
			TextView tvIP = (TextView)findViewById(R.id.tvIP);
			tvIP.setText(AndroidRestlet.getLocalIpAddress());				
		}
	};
	
	//Sending mock data to the server. Just for test purposes.
	private View.OnClickListener onBtnSendHeartRate = new View.OnClickListener() {
		
		public void onClick(View v) {
			AndroidRestlet.sensor.updateSensor("new sensor data");
	};
	};
	


	
	//Test event for getting phone's IP not used in production
	private View.OnClickListener onBtnSendIp = new View.OnClickListener() {
		public void onClick(View arg0) {
			AndroidRestlet.sendIp();
		}
	};
	
	// Used for phone's internal REST server
	// For test only
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
    

	  

 
 
	   
	
    
	  
	   
}
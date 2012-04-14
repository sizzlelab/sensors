package fi.soberit.sensors;

import java.util.ArrayList;
import java.util.Collections;

import fi.soberit.sensors.util.IntentFactory;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class DriverConnectionImpl extends Handler 
	implements ServiceConnection, DriverConnection {

	protected String TAG = this.getClass().getSimpleName();
	
	protected Messenger outgoingMessenger = null;
	
	protected Messenger incomingMessager = new Messenger(this);
	
	protected final String driverAction;
		

	private String clientId;
		
	int status = UNBOUND;

	private ArrayList<DriverStatusListener> driverStatusListeners = new ArrayList<DriverStatusListener>();

	private ArrayList<Integer> executedCommands = new ArrayList<Integer>();
	
	public DriverConnectionImpl(String driver, String clientId) {
		
		this.driverAction = driver;
		this.clientId = clientId;		
	}
	
	@Override
	public void bind(Context context) {
		final Intent driverIntent = new Intent();
		driverIntent.setAction(driverAction);
				
		final boolean result = context.bindService(
				IntentFactory.create(driverAction), 
				this, 
				Context.BIND_AUTO_CREATE);
		
		Log.d(TAG, "binding to " + driverAction + " for " + clientId);
		Log.d(TAG, "result: " + result);
	}

	@Override
	public void unbind(Context context) {		
		try {
			
			if (outgoingMessenger == null || status == UNBOUND) {
				Log.d(TAG, "already disconnected");
				return;
			}
			
			sendMessage(SinkService.REQUEST_REGISTER_OUT_CLIENT);
			
			Log.d(TAG, "unbinding from " + driverAction);
			
			context.unbindService(this);
			
			setDriverStatus(UNBOUND);

		} catch(IllegalArgumentException ex) {
			Log.v(TAG, "", ex);
		}
	}
	
	
	@Override
	public String getDriverAction() {
		return driverAction;
	}

	@Override
	public int getStatus() {
		return status;
	}
	
	protected void sendRequestRegisterClient() {
		
		setExecutedCommand(SinkService.REQUEST_REGISTER_CLIENT);
		
		final Bundle b = new Bundle();
		b.putString(SinkService.REQUEST_FIELD_CLIENT_ID, clientId);
		b.putParcelable(SinkService.REQUEST_FIELD_REPLY_TO, incomingMessager);
		
		sendMessage(SinkService.REQUEST_REGISTER_CLIENT, 0, 0, b);
	}
	
	protected void sendMessage(int id) {
		sendMessage(id, 0, 0);
	}

	protected void sendMessage(int id, int arg1) {
		sendMessage(id, arg1, 0);
	}

	
	public synchronized void sendMessage(int id, int arg1, int arg2) {
		Log.d(TAG, "sendMessage " + id);

		sendMessage(id, arg1, arg2, null);
	}
	
	public synchronized void sendMessage(int id, int arg1, int arg2, Bundle b) {
		Log.d(TAG, "sendMessage " + id);

		if (outgoingMessenger == null) {
			throw new RuntimeException("Outgoing messenger is not connected!");
		}
		
		try {
			Message msg = Message.obtain(null, id);
			
			msg.replyTo = incomingMessager;

			msg.arg1 = arg1;
			msg.arg2 = arg2;
			
			final Bundle bundle = b == null ? new Bundle() : b;
			
			bundle.putString(SinkService.REQUEST_FIELD_CLIENT_ID, clientId);
			
			msg.setData(bundle);
			
			outgoingMessenger.send(msg);
		} catch(RemoteException re) {
			Log.v(TAG, "-", re);
		}			
	}

	protected void setExecutedCommand(int command) {
		if (status == UNBOUND) {
			throw new RuntimeException("Driver is unbound");
		}
				
		executedCommands.add(command);
	}
	
	protected void setDriverStatus(int newStatus) {

		final int oldStatus = status;
		status = newStatus;
		
		for (DriverStatusListener listener: driverStatusListeners) {
			listener.onDriverStatusChanged(this, oldStatus, newStatus);
		}
	}
	
	public void setServiceMessenger(Messenger serviceMessenger) {
		this.outgoingMessenger = serviceMessenger;
	}

	public Messenger getServiceMessenger() {
		return outgoingMessenger;
	}
	
	@Override
	public String toString() {
		return "DriverConnection [driver=" 
			+ driverAction 
			+ ", status=" 
			+ getStatus()
			+ "]";
	}

	protected ArrayList<Integer> getExecutedCommands() {
		return executedCommands;
	}

	protected void setExecutedCommands(ArrayList<Integer> executedCommands) {
		this.executedCommands = executedCommands;
	}

	@Override
	public void handleMessage(Message msg) {
		Log.d(TAG, String.format("handleMessage, what = %d", msg.what));
				
		if (msg.what > 0 && executedCommands.size() > 0 && Collections.binarySearch(executedCommands, msg.what -1) == -1) {
			throw new RuntimeException(String.format(
					"Wrong response. Executed command: %s, received: %d", 
					executedCommands.toString(), msg.what -1));
		} else 
		if (msg.what > 0) {
			executedCommands.remove((Object)  (msg.what -1));
		}
				
		if (status == UNBOUND) {
			
			Log.d(TAG, String.format("Received %d. Skipping as driver is unbound", msg.what));
			return;
		}
		
		onReceivedMessage(msg);
		
	}
	
	public void onReceivedMessage(Message msg) {
		for (DriverStatusListener listener: driverStatusListeners) {
			listener.onReceivedMessage(this, msg);
		}		
	}
	
	public void addDriverStatusListener(DriverStatusListener driverStatusListener) {
		driverStatusListeners.add(driverStatusListener);
	}
	
	public void removeDriverStatusListener(DriverStatusListener driverStatusListener) {
		driverStatusListeners.remove(driverStatusListener);
	}
	
	public void onServiceConnected(ComponentName className, IBinder service) {
		Log.d(TAG, "onServiceConnected.");

		outgoingMessenger = new Messenger(service);	
				
		setDriverStatus(BOUND);
		
		sendRequestRegisterClient();
 	}
	
	public void onServiceDisconnected(ComponentName className) {		
		Log.d(TAG, "onServiceDisconnected");

		outgoingMessenger = null;
		
		setDriverStatus(UNBOUND);
	}

}

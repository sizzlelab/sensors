/*******************************************************************************
 * Copyright (c) 2011 Aalto University
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package fi.soberit.sensors;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class SensorSinkConnection extends DriverConnectionImpl 
	implements ServiceConnection, SensorDriverConnection  {

	private static final int NO_COMMAND = -1;

	public static final int COUNTING = 25;
	public static final int DOWNLOADING = 26;
	
	String address;

	private boolean requestStatusOnConnect;
		
	
	public SensorSinkConnection(String driverAction, String clientId) {
		super(driverAction, clientId);	
	}
	
	public void bind(Context context) {
		bind(context, false);
	}
	
	public void bind(Context context, boolean requestStatusOnConnect) {
		super.bind(context);
			
		this.requestStatusOnConnect = requestStatusOnConnect;
	}

	
	public void unbind(Context context) {		
		try {
			
			if (outgoingMessenger == null || status == UNBOUND) {
				Log.d(TAG, "already disconnected");
				return;
			}
			
			sendMessage(DriverService.REQUEST_REGISTER_OUT_CLIENT);
			
			Log.d(TAG, "unbinding from " + driverAction);
			
			context.unbindService(this);
			
			setDriverStatus(UNBOUND);

		} catch(IllegalArgumentException ex) {
			Log.v(TAG, "", ex);
		}
	}
	
	@Override
	public void onServiceConnected(ComponentName className, IBinder service) {
		super.onServiceConnected(className, service);
		
 		if (requestStatusOnConnect) {
 			sendRequestConnectionStatus();
 			requestStatusOnConnect = false;
 		}
 	}
	
	public static int unwrapConnectionStatusMessage(Message msg) {
		switch(msg.arg1) {
		case SensorSinkService.RESPONSE_ARG1_CONNECTION_STATUS_CONNECTED:
			return CONNECTED;

		case SensorSinkService.RESPONSE_ARG1_CONNECTION_STATUS_CONNECTING:
			return CONNECTING;

		case SensorSinkService.RESPONSE_ARG1_CONNECTION_STATUS_DISCONNECTED:
			return BOUND;
		default:
			throw new RuntimeException("This may be not the ConnectionStatusMessage");
		}
	}
	
	@Override
	protected void setExecutedCommand(int command) {		
		if (status != CONNECTED && (
				command == SensorSinkService.REQUEST_COUNT_OBSERVATIONS ||
				command == SensorSinkService.REQUEST_READ_OBSERVATIONS)) {
			throw new RuntimeException("Driver is not connected");
		}
		
		super.setExecutedCommand(command);
	}
	
	
	@Override
	public void onReceivedMessage(Message msg) {
		
		switch (msg.what) {
		case SensorSinkService.RESPONSE_REGISTER_CLIENT:
		case SensorSinkService.RESPONSE_CONNECTION_STATUS:
		case SensorSinkService.BROADCAST_CONNECTION_STATUS:

			
			final Bundle data = msg.getData();
			if (data.containsKey(SensorSinkService.RESPONSE_FIELD_BT_ADDRESS)) {
				address = data.getString(SensorSinkService.RESPONSE_FIELD_BT_ADDRESS);
			}
			
			setDriverStatus(unwrapConnectionStatusMessage(msg));
			break;
			
		case SensorSinkService.RESPONSE_CONNECTION_TIMEOUT:			
			setDriverStatus(BOUND);
			break;
			
		case SensorSinkService.RESPONSE_COUNT_OBSERVATIONS:
		case SensorSinkService.RESPONSE_READ_OBSERVATIONS:
			setDriverStatus(CONNECTED);
			
			break;
		}	
		
		super.onReceivedMessage(msg);
	}
	
	public void sendReadObservationNumberMessage() {
		Log.d(TAG, "REQUEST_COUNT_OBSERVATIONS");
		
		setExecutedCommand(SensorSinkService.REQUEST_COUNT_OBSERVATIONS);
		setDriverStatus(COUNTING);
		
		sendMessage(SensorSinkService.REQUEST_COUNT_OBSERVATIONS);
	}

	public void sendReadObservations(long[] types, int start, int end) {
		Log.d(TAG, "REQUEST_READ_OBSERVATIONS");
		
		setExecutedCommand(SensorSinkService.REQUEST_READ_OBSERVATIONS);
		setDriverStatus(DOWNLOADING);
		
		final Bundle bundle = new Bundle();
		
		final TypeFilter filter = new TypeFilter();
		
		for(long type : types) {
			filter.add(type);
		}
		
		bundle.putParcelable(SensorSinkService.REQUEST_FIELD_DATA_TYPES, filter);
		
		sendMessage(SensorSinkService.REQUEST_READ_OBSERVATIONS, start, end, bundle);
	}

	
	@Override
	public void sendStartConnecting(String address) {
		Log.d(TAG, "REQUEST_START_CONNECTING " + address);
		
		this.address = address;
		
		final Bundle b = new Bundle();
		b.putString(SensorSinkService.REQUEST_FIELD_BT_ADDRESS, address);
		
		sendMessage(SensorSinkService.REQUEST_START_CONNECTING, 0, 0, b);
	}
	
	public void sendStartConnecting(String address, int timeout) {
		Log.d(TAG, "REQUEST_START_CONNECTING " + address + " timeout: " + timeout);
		
		this.address = address;
		
		final Bundle b = new Bundle();
		b.putString(SensorSinkService.REQUEST_FIELD_BT_ADDRESS, address);
		b.putInt(SensorSinkService.REQUEST_FIELD_TIMEOUT, timeout);
		
		sendMessage(SensorSinkService.REQUEST_START_CONNECTING, 0, 0, b);
	}

	
	public void sendRequestConnectionStatus() {
		Log.d(TAG, "REQUEST_CONNECTION_STATUS");
				
		setExecutedCommand(SensorSinkService.REQUEST_CONNECTION_STATUS);
		
		sendMessage(SensorSinkService.REQUEST_CONNECTION_STATUS, 0);
	}	

	@Override
	public int getStatus() {
		return status;
	}
		
	@Override
	public void sendDisconnectRequest() {
		Log.d(TAG, "REQUEST_DISCONNECT");
		
		setExecutedCommand(NO_COMMAND);
		setDriverStatus(BOUND);
		
		sendMessage(SensorSinkService.REQUEST_DISCONNECT, 0);
	}	
	
	@Override
	public String getSensorAddress() {
		return address;
	}
}

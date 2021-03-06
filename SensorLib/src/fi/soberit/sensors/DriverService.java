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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public abstract class DriverService extends Service {

	public final String TAG = this.getClass().getSimpleName();
	
	public static String STARTED_PREFIX = ".STARTED";

	public static final long STOP_DRIVER_DELAY = 60 * 1000;
	
	public static final String REQUEST_FIELD_MSG_ID = "msg id";
	
	public static final String REQUEST_FIELD_CLIENT_ID = "client id";
	
	public static final String REQUEST_FIELD_REPLY_TO = "reply to";	
	
	public static final String RESPONSE_FIELD_REPLY_TO_MSG_ID = "reply msg id";

	public static final long NO_ORGIN_MSG_ID = -1;
	
    public static final int REQUEST_REGISTER_CLIENT = 2;
    
    public static final int REQUEST_REGISTER_OUT_CLIENT = 4;
    
    public static final String ACTION_SESSION_STOP = "action session stop";
    
    
	protected HashMap<String, Messenger> clients = new HashMap<String, Messenger>();
			
	private IncomingHandler incomingHandler = new IncomingHandler();
	
    private final Messenger messenger = new Messenger(incomingHandler);

	private BroadcastReceiver broadcastControlReceiver = new BroadcastControlReceiver();

	private IntentFilter broadcastControlMessageFilter;

	public HashMap<String, ArrayList<Message>> messageQueues = new HashMap<String, ArrayList<Message>>();
		
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand");
		if (intent == null) {
			return START_REDELIVER_INTENT;
		}

		broadcastControlMessageFilter = new IntentFilter();
		broadcastControlMessageFilter.addAction(ACTION_SESSION_STOP);
		
		broadcastControlReceiver = new BroadcastControlReceiver();
		registerReceiver(broadcastControlReceiver, broadcastControlMessageFilter);		
		
		final Intent pingBack = new Intent();
		pingBack.setAction(getDriverAction() + STARTED_PREFIX);
		Log.d(TAG, "sending ping back " + pingBack.getAction());
		sendBroadcast(pingBack);	
		
		
		return START_REDELIVER_INTENT;
    }

	@Override
	public IBinder onBind(Intent intent) {
				
		Log.d(TAG, "onBind ");
		
		return messenger.getBinder();
	}


	private void registerClient(final String clientId, final Messenger replyTo, long msgId) {
		synchronized(clients) {					
			clients.put(clientId, replyTo);
			
			final ArrayList<Message> waitingMessages = messageQueues.get(clientId);
			
			if (waitingMessages == null) {
				messageQueues.put(clientId, new ArrayList<Message>());
			} else if (waitingMessages.size() > 0){
				for (Message waitingMsg: waitingMessages) {
					send(clientId, true, waitingMsg);
				}
			}
			onRegisterClient(clientId, msgId);			
		}		
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		super.onUnbind(intent);		
		
		Log.d(TAG, "onUnbind ");
		
		return false;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy");		
		
		try {
			unregisterReceiver(broadcastControlReceiver);
		} catch(IllegalArgumentException iae) {
			Log.d(TAG, "illegal argument", iae);
		}
	}
	
	class IncomingHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			
			final Bundle bundle = msg.getData();
			bundle.setClassLoader(getClassLoader());
			
			if (!bundle.containsKey(REQUEST_FIELD_MSG_ID)) {
				bundle.putLong(REQUEST_FIELD_MSG_ID, System.currentTimeMillis());
			}
			
			final long msgId = bundle.getLong(REQUEST_FIELD_MSG_ID);
			
			final String clientId = bundle.getString(REQUEST_FIELD_CLIENT_ID);
			
			Log.d(TAG, "received "+ msg.what + " from " + clientId);
			
			if (clientId == null) {
				throw new RuntimeException("Client must supply an id; in message " + msg.what);
			}
			
			switch(msg.what) {
			case REQUEST_REGISTER_CLIENT:
		
				final Messenger replyTo = (Messenger) bundle.get(REQUEST_FIELD_REPLY_TO);		
	
				registerClient(clientId, replyTo, msgId);	
				return;

			case REQUEST_REGISTER_OUT_CLIENT:				
				/**
				 * We synchronize on clients in order to defend against situations, 
				 * where messages are being sent, at the same time 
				 * as clients being unregistered
				 */
				synchronized(clients) {
					
					clients.remove(clientId);
					
					Log.d(TAG, "Clients left: " + clients.size());
					
					if (clients.size() == 0) {
						incomingHandler.postDelayed(
								new StopDriver(DriverService.this), 
								STOP_DRIVER_DELAY);
						break;
					}
					
					onUnregisterClient(clientId, msgId);
				}		
				return;
			}

			onReceivedMessage(msg, clientId, msgId);
		}
	}

	protected void onRegisterClient(String clientId, long msgId) {
	}
	
	protected void onUnregisterClient(String clientId, long msgId) {
	
		
	}
	
	protected void onReceivedMessage(Message msg, String clientId, long msgId) {
		
	}
		
	public void send(String clientId, boolean persistent, Message msg) {
		Log.v(TAG, String.format("send %d to %s", msg.what, clientId));
		
		synchronized(clients) {
			final Messenger messanger = clients.get(clientId); 
			
			try {
				if (messanger != null) {
					messanger.send(msg);
					return;
				}
			} catch (RemoteException re) {
				Log.d(TAG, "shouldn't happen:", re);
				
				if (persistent) {
					messageQueues.get(clientId).add(msg);
				}
				return;
			}

			if (persistent) {
				messageQueues.get(clientId).add(msg);
			}
		}
	}

	
	public void send(String clientId, int what, int arg1, Bundle b) {
		
		final Message msg = Message.obtain(null, what);
		
		msg.arg1 = arg1;
		msg.setData(b);
		
		send(clientId, true, msg);
	}	
	
	public void send(String clientId, int what, int arg1) {
		
		final Message msg = Message.obtain(null, what);
		
		msg.arg1 = arg1;
		
		send(clientId, true, msg);
	}
	
	public void send(String clientId, boolean persistent, int what, int arg1) {
		
		final Message msg = Message.obtain(null, what);
		
		msg.arg1 = arg1;
		
		send(clientId, persistent, msg);
	}


	public void broadcastTemporary(int what) {
		
		for (String clientId : clients.keySet()) {
			send(clientId, true, what, 0);
		}
	}

	public Collection<String> getClients() {
		return clients.keySet();
	}
	
	public class BroadcastControlReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "onReceive " + intent.getAction());
			
			if (!ACTION_SESSION_STOP.equals(intent.getAction())) {
				return;
			}

			onStopSession();
		}
	}

	protected void onStopSession() {
		DriverService.this.stopSelf();
	}
	
	public abstract String getDriverAction();
	
	
	class StopDriver implements Runnable {

		private DriverService sink;

		public StopDriver(DriverService sink) {
			this.sink = sink;
		}
		
		@Override
		public void run() {			
			if (sink.getClients().size() > 0) {
				Log.d(TAG, "Och, more clients came!");

				return;
			}
			
			Log.d(TAG, "No more clients. Bye, bye!");
			
			sink.stopSelf();
		}
		
	}
}

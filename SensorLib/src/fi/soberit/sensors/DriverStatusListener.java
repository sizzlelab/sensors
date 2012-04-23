package fi.soberit.sensors;

import android.os.Message;

public interface DriverStatusListener {	
	public void onDriverStatusChanged(DriverConnection connection, int oldStatus, int newStatus);
	
	void onReceivedMessage(DriverConnection connection, Message msg);
}

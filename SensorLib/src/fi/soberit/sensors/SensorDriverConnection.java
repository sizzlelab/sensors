package fi.soberit.sensors;

public interface SensorDriverConnection extends DriverConnection {

	int CONNECTING = 23;
	int CONNECTED = 24;

	public void sendStartConnecting(String address);
	
	public void sendStartConnecting(String address, int timeout);
	
	public void sendRequestConnectionStatus();
	
	public void sendDisconnectRequest();
	
	public String getSensorAddress();
}

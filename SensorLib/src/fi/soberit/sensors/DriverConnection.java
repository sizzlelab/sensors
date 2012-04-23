package fi.soberit.sensors;

import android.content.Context;
import android.os.Bundle;

public interface DriverConnection {
	
	int UNBOUND = 21;
	int BOUND = 22;
	
	public String getDriverAction();
	
	public void bind(Context context);
	
	public void unbind(Context context);
		
	public int getStatus();
}

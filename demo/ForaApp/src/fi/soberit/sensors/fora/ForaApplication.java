/*******************************************************************************
 * Copyright (c) 2011 Aalto University
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 * Authors:
 * Maksim Golivkin <maksim@golivkin.eu>
 ******************************************************************************/
package fi.soberit.sensors.fora;


import android.app.Application;
import android.preference.PreferenceManager;
import android.util.Log;
import fi.side.restlet.AndroidRestlet;
import fi.soberit.fora.D40CachedSink;
import fi.soberit.fora.IR21Sink;
import fi.soberit.sensors.fora.R;	
import fi.soberit.sensors.util.IntentFactory;

public class ForaApplication extends Application {

	private static final String TAG = ForaApplication.class.getSimpleName();

	@Override 
	public void onCreate() {
		Log.d(TAG, "onCreate");
		
		PreferenceManager.setDefaultValues(
				this, 
				ForaSettings.APP_PREFERENCES_FILE,
				MODE_PRIVATE,
				R.xml.preferences, 
				false);
			
		
		/**
		 * Starting a service on application start is the simpliest approach to an
		 * always accessible service. Services below have a hara-kiri timer. 
		 * Once they have no clients connected a count-down is going to start, 
		 * which will terminate threads, if there are no binds or connections in between. 
		 * 
		 */
		startService(IntentFactory.create(D40CachedSink.ACTION));
		startService(IntentFactory.create(IR21Sink.ACTION));
		
		/**
		 * Initialize phone-server interaction object.
		 */
		AndroidRestlet.init(getApplicationContext());
		
		
	}
}

package fi.soberit.sensors.fora;

import java.util.Collection;
import java.util.List;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import fi.soberit.fora.D40CachedSink;
import fi.soberit.sensors.DriverConnection;
import fi.soberit.sensors.DriverStatusListener;
import fi.soberit.sensors.Observation;
import fi.soberit.sensors.R;
import fi.soberit.sensors.SensorDriverConnection;
import fi.soberit.sensors.SensorSinkConnection;
import fi.soberit.sensors.SensorSinkService;
import fi.soberit.sensors.bluetooth.BluetoothPairingActivity;
import fi.soberit.sensors.fora.db.Record;


public class ForaBrowser 
	extends FragmentActivity 
	implements DriverStatusListener, LoaderManager.LoaderCallbacks<Collection<Record>>, OnClickListener  {

	private static final String TAG = ForaBrowser.class.getSimpleName();

	private static final int STATUS_INDICATOR_DISCONNECTED = 1;

	private static final int STATUS_INDICATOR_CONNECTING = 2;

	private static final int STATUS_INDICATOR_CONNECTED = 4;

	private static final int STATUS_INDICATOR_DOWNLOADING = 3;

	private static final int REQUEST_CHOOSE_D40_DEVICE = 1;

	private static final long[] d40Types = new long[] {
			Observation.TYPE_INDEX_BLOOD_PRESSURE,
			Observation.TYPE_INDEX_GLUCOSE,
			Observation.TYPE_INDEX_PULSE };


	private static final int CONNECTION_TIMEOUT = 5000;

	private static final int OBSERVATIONS_LOADER_ID = 0;

	public static String FORA_DEVICES_PREFIX = "taidoc";

	private SensorSinkConnection d40Connection;

	private ListView listView;

	private View progressView;

	private ImageView statusIndicator;

	private TextView statusLine;

	private ObservationArrayAdapter adapter;

	private TextView emptyView;

	@Override
	protected void onCreate(Bundle sis) {
		super.onCreate(sis);
		Log.d(TAG, "onCreate()");

		setContentView(R.layout.observation_list);
		
		progressView = (View) findViewById(R.id.progress_spinner);
		
		statusIndicator = (ImageView) findViewById(R.id.status_indicator);
		statusLine = (TextView) findViewById(R.id.status_line);
		emptyView = (TextView) findViewById(R.id.empty_list_message);
		
		listView = (ListView) findViewById(R.id.observations_list);
		adapter = new ObservationArrayAdapter(this);
		listView.setAdapter(adapter);
		
		((Button) findViewById(R.id.setting)).setOnClickListener(this);
		((Button) findViewById(R.id.refresh)).setOnClickListener(this);
				
		d40Connection = new SensorSinkConnection(D40CachedSink.ACTION, getClass().getName());
		d40Connection.bind(this);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		Log.d(TAG, "onResume");	
		
		d40Connection.addDriverStatusListener(this);		
		if (d40Connection.getStatus() != SensorDriverConnection.UNBOUND) {
			d40Connection.sendRequestConnectionStatus();
		}
	}
	
	@Override
	public void onBackPressed() {
		if (d40Connection.getStatus() == SensorSinkConnection.CONNECTED) {
			d40Connection.sendDisconnectRequest();
		}

		super.onBackPressed();	
	}

	@Override 
    public void onPause() {
		Log.d(TAG, "onStop");
		super.onPause();
				
		d40Connection.removeDriverStatusListener(this);
	}

	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		d40Connection.unbind(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent result) {
		Log.d(TAG, String.format("onActivityResult(%d, %d, ..)", requestCode, resultCode));
		
		if (requestCode == REQUEST_CHOOSE_D40_DEVICE && resultCode == Activity.RESULT_OK) {
			final String d40Address = result
					.getStringExtra(BluetoothPairingActivity.AVAILABLE_DEVICE_ADDRESS);

			final SharedPreferences prefs = getSharedPreferences(
					ForaSettings.APP_PREFERENCES_FILE, MODE_PRIVATE);
			final Editor editor = prefs.edit();
			final String name = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(d40Address).getName();
			editor.putString(ForaSettings.D40_BLUETOOTH_NAME, String.format("%s (%s)", name, d40Address));
			editor.putString(ForaSettings.D40_BLUETOOTH_ADDRESS, d40Address);
			editor.commit();

		} 
	}
	
	@Override
	public void onClick(View v) {
		
		switch(v.getId()) {
		case R.id.setting:
			final Intent settings = new Intent(this, ForaSettings.class);
			startActivity(settings);
			break;
			
		case R.id.refresh:
			onRefresh();
			break;
		}
		
	}
	
	public void onRefresh() {
		switch(d40Connection.getStatus()) {
		case SensorDriverConnection.CONNECTING:
		case SensorSinkConnection.COUNTING:
		case SensorSinkConnection.DOWNLOADING:
			
			Toast.makeText(this, R.string.communication_in_process, Toast.LENGTH_LONG).show();
			
			return;
			
		case SensorDriverConnection.CONNECTED:

			d40Connection.sendReadObservationNumberMessage();
			break;
					
		case SensorDriverConnection.UNBOUND:
		case SensorDriverConnection.BOUND:
		{	

    		setListShown(false);
			connect();
			break;
		}
		}		
	}
	
	public void connect() {
		
		final SharedPreferences prefs = getSharedPreferences(ForaSettings.APP_PREFERENCES_FILE, MODE_PRIVATE);
		
		final String d40Address = prefs.getString(ForaSettings.D40_BLUETOOTH_ADDRESS, null);
				
		if (d40Address == null) {
			chooseBtDevice(d40Connection);
			return;
		}
		
		d40Connection.sendStartConnecting(d40Address, CONNECTION_TIMEOUT);
	}
	
	
	public void chooseBtDevice(SensorSinkConnection connection) {
		Log.d(TAG, "chooseBtDevice");
		
		final Intent settings = new Intent(this,
				BluetoothPairingActivity.class);
		settings.putExtra(
				BluetoothPairingActivity.DRIVER_ACTION,
				connection.getDriverAction());
		
		settings.putExtra(
				BluetoothPairingActivity.INTERESTING_DEVICE_NAME_PREFIX,
				FORA_DEVICES_PREFIX);
		startActivityForResult(settings, REQUEST_CHOOSE_D40_DEVICE);
	}
	
	@Override
	public void onDriverStatusChanged(DriverConnection connection, int oldStatus, int newStatus) {
		Log.d(TAG, String.format("onSensorSinkStatusChanged %s %d", connection.getDriverAction(), newStatus));
		
		switch(newStatus) {
		
		case SensorDriverConnection.CONNECTING:
			setListShown(false);
			
			statusIndicator.setImageLevel(STATUS_INDICATOR_CONNECTING);
			statusLine.setText(R.string.connecting);
			
			break;
			
		case SensorDriverConnection.CONNECTED:
		{
			statusIndicator.setImageLevel(STATUS_INDICATOR_CONNECTED);
			statusLine.setText(R.string.connected);
			
			if (oldStatus == SensorSinkConnection.DOWNLOADING) {
				// this is taken care of in onObservationsSaved() 
				
				return;
			} else 
			if (oldStatus != SensorSinkConnection.COUNTING) {
				((SensorSinkConnection) connection).sendReadObservationNumberMessage();
			}
				
			
			break;
		}
			
		case SensorSinkConnection.DOWNLOADING:
			setListShown(false);
			
			statusLine.setText(R.string.downloading_data);
			statusIndicator.setImageLevel(STATUS_INDICATOR_DOWNLOADING);
			break;
		
		case SensorDriverConnection.UNBOUND:
		case SensorDriverConnection.BOUND: 
		{
			statusLine.setText(R.string.disconnected);

			statusIndicator.setImageLevel(STATUS_INDICATOR_DISCONNECTED);
									
			if (oldStatus != SensorDriverConnection.CONNECTED) {
				getSupportLoaderManager().initLoader(OBSERVATIONS_LOADER_ID, null, this);
			} else 
			break;
		}
		}
	}
	
	@Override
	public void onReceivedMessage(DriverConnection connection, Message msg) {
		Log.d(TAG, String.format("onReceivedMessage %s %d",
						connection.getDriverAction(), msg.what));

		switch (msg.what) {
		case SensorSinkService.RESPONSE_CONNECTION_TIMEOUT:
			Toast.makeText(this, "Timeout", Toast.LENGTH_LONG).show();

			chooseBtDevice((SensorSinkConnection) connection);

			break;
		
		case SensorSinkService.RESPONSE_COUNT_OBSERVATIONS:
			final int observationNum = msg.arg1;
			Log.d(TAG, "Sink object number is " + observationNum);

			((SensorSinkConnection) connection).sendReadObservations(
					d40Types, 0,
					observationNum);
			break;

		case SensorSinkService.RESPONSE_READ_OBSERVATIONS:
			final Bundle bundle = msg.getData();
			bundle.setClassLoader(this.getClass().getClassLoader());

			Log.d(TAG, String.format("Received observations"));

			final List<Parcelable> observations = (List<Parcelable>) bundle.getParcelableArrayList(
					SensorSinkService.RESPONSE_FIELD_OBSERVATIONS);
			Log.d(TAG, String.format("Received observations from " + connection.getDriverAction()));

			final SaveObservationsTask saveObservationsTask = new SaveObservationsTask(this);
			saveObservationsTask.execute(observations);
			break;
			
		}		
	}

	public void onObservationsSaved() {
		getSupportLoaderManager().restartLoader(OBSERVATIONS_LOADER_ID, null, this);
	}
	
	@Override
	public Loader<Collection<Record>> onCreateLoader(int id, Bundle args) {
		Log.d(TAG, "onCreateLoader");

		AsyncTaskLoader<Collection<Record>> loader = new ObservationsLoader(this, d40Types);

		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Collection<Record>> loader, Collection<Record> data) {
		Log.d(TAG, "onLoadFinished ");

		adapter.clear();
		for (Record r : data) {
			adapter.add(r);
		}

		Log.d(TAG, "connection: " + d40Connection);
		
		final int status = d40Connection.getStatus();
		Log.d(TAG, String.format("onLoadFinished %d %d", status, adapter.getCount()));
		boolean noCommunicationInProgress = 
				(status == SensorDriverConnection.CONNECTED || 
				 status == SensorDriverConnection.BOUND || 
				 status == SensorDriverConnection.UNBOUND);
		setListShown(noCommunicationInProgress);
		
	}
	
	@Override
	public void onLoaderReset(Loader<Collection<Record>> loader) {
		adapter.clear();
	}
	
	public void setListShown(boolean shown) {
		
		if (shown) {
			boolean emptyList = listView.getAdapter().getCount() <= 1;
			emptyView.setVisibility(emptyList ? View.VISIBLE : View.GONE);
			listView.setVisibility(emptyList ? View.GONE : View.VISIBLE);
			
			progressView.setVisibility(View.GONE);
		} else {
			emptyView.setVisibility(View.GONE);
			listView.setVisibility(View.GONE);
			
			progressView.setVisibility(View.VISIBLE);
		}
	}

}
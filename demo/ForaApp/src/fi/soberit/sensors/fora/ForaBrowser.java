package fi.soberit.sensors.fora;

import java.util.HashMap;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.viewpagerindicator.TabPageIndicator;

import fi.soberit.fora.D40CachedSink;
import fi.soberit.fora.IR21Sink;
import fi.soberit.sensors.DriverConnection;
import fi.soberit.sensors.DriverStatusListener;
import fi.soberit.sensors.Observation;
import fi.soberit.sensors.SinkSensorConnection;
import fi.soberit.sensors.bluetooth.BluetoothPairingActivity;


public class ForaBrowser extends SherlockFragmentActivity  {

	private static final String TAG = ForaBrowser.class.getSimpleName();

	private static final String TAB_INDEX = "index";

	private static final int REQUEST_CHOOSE_D40_DEVICE = 1;

	private static final int REQUEST_CHOOSE_IR21_DEVICE = 2;

	ViewPager mViewPager;
	TabsAdapter mTabsAdapter;

	private HashMap<String, DriverConnection> connections = new HashMap<String, DriverConnection>();

	private String clientId = getClass().getName();
	private TabPageIndicator mIndicator;

	private static final long[] d40Types = new long[] {
			Observation.TYPE_INDEX_BLOOD_PRESSURE,
			Observation.TYPE_INDEX_GLUCOSE,
			Observation.TYPE_INDEX_PULSE };

	private static final long[] ir21Types = new long[] {
			Observation.TYPE_INDEX_TEMPERATURE,
			Observation.TYPE_INDEX_AMBIENT_TEMPERATURE };

	private static final int CONNECTION_TIMEOUT = 5000;

	public static String FORA_DEVICES_PREFIX = "taidoc";

	@Override
	protected void onCreate(Bundle sis) {
		super.onCreate(sis);
		Log.d(TAG, "onCreate()");
		
		setContentView(R.layout.actionbar_tabs_pager);

		final ActionBar actionBar = getSupportActionBar();
		final ActionBar.Tab tab1 = actionBar.newTab().setText(
				R.string.bpm_bgm_tab);
		final ActionBar.Tab tab2 = actionBar.newTab().setText(
				R.string.thermometer_tab);
		
		actionBar.setHomeButtonEnabled(false);

		mViewPager = (ViewPager) findViewById(R.id.pager);
		mTabsAdapter = new TabsAdapter(this, mViewPager);

		mTabsAdapter.addTab(tab1, SimpleObservationListFragment.class,
				bundleFactory(D40CachedSink.ACTION, d40Types));
		mTabsAdapter.addTab(tab2, SimpleObservationListFragment.class,
				bundleFactory(IR21Sink.ACTION, ir21Types));

		mIndicator = (TabPageIndicator) findViewById(R.id.indicator);
		mIndicator.setViewPager(mViewPager);

		if (sis != null) {
			mTabsAdapter.setTabSelected(sis.getInt(TAB_INDEX));
		}
		
		final DriverConnection d40Connection = new SinkSensorConnection(D40CachedSink.ACTION, clientId);
		d40Connection.bind(this);
		connections.put(D40CachedSink.ACTION, d40Connection);
		
		final DriverConnection ir21Connection = new SinkSensorConnection(IR21Sink.ACTION, clientId);
		ir21Connection.bind(this);
		connections.put(IR21Sink.ACTION, ir21Connection);
	}

	Bundle bundleFactory(String driverAction, long[] driverTypes) {
		final Bundle bundle = new Bundle();

		bundle.putString(
				SimpleObservationListFragment.DRIVER_ACTION_PARAM,
				driverAction);
		bundle.putLongArray(
				SimpleObservationListFragment.TYPES_PARAM,
				driverTypes);

		return bundle;
	}


	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(TAB_INDEX, getSupportActionBar().getSelectedNavigationIndex());
	}
	
	@Override
	public void onBackPressed() {
		for (DriverConnection connection : connections.values()) {
			if (((SinkSensorConnection) connection).getStatus() != SinkSensorConnection.CONNECTED) {
				continue;
			}
			
			((SinkSensorConnection) connection).sendDisconnectRequest();
		}
				
		super.onBackPressed();
	}

	@Override
	protected void onPause() {
		Log.d(TAG, "onPause");
		super.onPause();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		for (DriverConnection connection : connections.values()) {
			connection.unbind(this);
		}

		connections.clear();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent result) {
		Log.d(TAG, String.format("onActivityResult(%d, %d, ..)", requestCode, resultCode));
		
		if (requestCode == REQUEST_CHOOSE_D40_DEVICE
				&& resultCode == Activity.RESULT_OK) {
			final String d40Address = result
					.getStringExtra(BluetoothPairingActivity.AVAILABLE_DEVICE_ADDRESS);

			final SharedPreferences prefs = getSharedPreferences(
					ForaSettings.APP_PREFERENCES_FILE, MODE_PRIVATE);
			final Editor editor = prefs.edit();
			final String name = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(d40Address).getName();
			editor.putString(ForaSettings.D40_BLUETOOTH_NAME, String.format("%s (%s)", name, d40Address));
			editor.putString(ForaSettings.D40_BLUETOOTH_ADDRESS, d40Address);
			editor.commit();

		} else if (requestCode == REQUEST_CHOOSE_IR21_DEVICE
				&& resultCode == Activity.RESULT_OK) {
			final String ir21Address = result
					.getStringExtra(BluetoothPairingActivity.AVAILABLE_DEVICE_ADDRESS);

			final SharedPreferences prefs = getSharedPreferences(
					ForaSettings.APP_PREFERENCES_FILE, MODE_PRIVATE);
			final Editor editor = prefs.edit();
			final String name = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(ir21Address).getName();
			editor.putString(ForaSettings.IR21_BLUETOOTH_NAME, String.format("%s (%s)", name, ir21Address));
			editor.putString(ForaSettings.IR21_BLUETOOTH_ADDRESS, ir21Address);
			editor.commit();

		} else if (requestCode == REQUEST_CHOOSE_IR21_DEVICE
				&& resultCode == Activity.RESULT_CANCELED) {
			Toast.makeText(this, R.string.no_ir21_bluetooth_address,
					Toast.LENGTH_LONG).show();
			
		} else if (requestCode == REQUEST_CHOOSE_D40_DEVICE
				&& resultCode == Activity.RESULT_CANCELED) {
			Toast.makeText(this, R.string.no_d40_bluetooth_address,
					Toast.LENGTH_LONG).show();
		}
	}

	public void connect(String driverAction) {
		
		final SinkSensorConnection connection = (SinkSensorConnection) connections.get(driverAction);
		final SharedPreferences prefs = getSharedPreferences(ForaSettings.APP_PREFERENCES_FILE, MODE_PRIVATE);
		
		if (D40CachedSink.ACTION.equals(driverAction)) {
			final String d40Address = prefs.getString(ForaSettings.D40_BLUETOOTH_ADDRESS, null);
			
			Log.d(TAG, "d40Address = " + d40Address );
			
			if (d40Address == null) {
				chooseBtDevice(connection);
				return;
			}
			
			connection.sendStartConnecting(d40Address, CONNECTION_TIMEOUT);

		} else {
			final String ir21Address = prefs.getString(ForaSettings.IR21_BLUETOOTH_ADDRESS, null);

			Log.d(TAG, "ir21Address = " + ir21Address );
			
			if (ir21Address == null) {
				chooseBtDevice(connection);
				return;
			}
			
			connection.sendStartConnecting(ir21Address);
		}
	}

	
	public void chooseBtDevice(SinkSensorConnection connection) {
		Log.d(TAG, "chooseBtDevice");

		final boolean d40 = D40CachedSink.ACTION.equals(connection.getDriverAction());
		final int requestCode = d40 ? REQUEST_CHOOSE_D40_DEVICE : REQUEST_CHOOSE_IR21_DEVICE;
		
		final Intent settings = new Intent(this,
				BluetoothPairingActivity.class);
		settings.putExtra(
				BluetoothPairingActivity.DRIVER_ACTION,
				connection.getDriverAction());
		
		settings.putExtra(
				BluetoothPairingActivity.INTERESTING_DEVICE_NAME_PREFIX,
				FORA_DEVICES_PREFIX);
		startActivityForResult(settings, requestCode);
	}
	
	public SinkSensorConnection getConnection(String driverAction) {
		Log.d(TAG, "getConnection(" + driverAction + ")");
		
		final SinkSensorConnection conn = (SinkSensorConnection) connections.get(driverAction);
		
		Log.d(TAG, "getConnection(" + driverAction + ") = " + conn);

		return conn;
	}
}
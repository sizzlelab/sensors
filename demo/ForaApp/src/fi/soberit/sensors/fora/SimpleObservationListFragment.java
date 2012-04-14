package fi.soberit.sensors.fora;

import java.util.Collection;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import fi.soberit.sensors.DriverConnection;
import fi.soberit.sensors.SensorDriverConnection;
import fi.soberit.sensors.DriverStatusListener;
import fi.soberit.sensors.R;
import fi.soberit.sensors.SensorSinkService;
import fi.soberit.sensors.SinkSensorConnection;
import fi.soberit.sensors.fora.db.Record;

public class SimpleObservationListFragment extends SherlockFragment implements
		LoaderManager.LoaderCallbacks<Collection<Record>>, 
		OnRefreshListener, 
		DriverStatusListener {

	private static final int STATUS_INDICATOR_DISCONNECTED = 1;

	private static final int STATUS_INDICATOR_CONNECTING = 2;

	private static final int STATUS_INDICATOR_CONNECTED = 4;

	private static final int STATUS_INDICATOR_DOWNLOADING = 3;

	public String TAG = SimpleObservationListFragment.class.getSimpleName();
	
	public static final String DRIVER_ACTION_PARAM = "action";
	
	public static final String TYPES_PARAM = "types";

	public static final int OBSERVATIONS_LOADER_ID = 12346;
	
	ObservationArrayAdapter mAdapter;

	private ForaBrowser activity;

	private PullToRefreshListView pullToRefreshView;

	private TextView emptyView;

	private ProgressBar progressView;

	private TextView statusLine;

	private ListView listView;

	private ImageView statusIndicator;

	private String driverAction;

	private long[] types;
	
	private SinkSensorConnection connection;

	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);


        String tag2 = getTag();
        int pos = 0;
        do {
            pos = tag2.indexOf(':', pos + 1);
        } while(tag2.indexOf(':', pos + 1) != -1);


        TAG = TAG + tag2.substring(pos);
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
		
		final ViewGroup root = (ViewGroup) inflater.inflate(R.layout.observation_list, null);
		
		emptyView = (TextView) root.findViewById(android.R.id.empty);
		
		pullToRefreshView = (PullToRefreshListView) root.findViewById(R.id.observations_list);
		pullToRefreshView.setOnRefreshListener(this);
		
		listView = pullToRefreshView.getRefreshableView();
		
		progressView = (ProgressBar) root.findViewById(R.id.progress_spinner);
		statusIndicator = (ImageView) root.findViewById(R.id.status_indicator);
		statusLine = (TextView) root.findViewById(R.id.status_line);
		
		return root;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.d(TAG, "onActivityCreated");
		super.onActivityCreated(savedInstanceState);
		
		mAdapter = new ObservationArrayAdapter(getActivity());
		listView.setAdapter(mAdapter);
		
		setHasOptionsMenu(true);	

		final Bundle bundle = getArguments();

		driverAction = bundle.getString(DRIVER_ACTION_PARAM);
		types = bundle.getLongArray(TYPES_PARAM);
		
		activity = (ForaBrowser) getActivity();
		
		connection = this.activity.getConnection(driverAction);
		
	}

	@Override
	public void onStart() {
		super.onStart();
				
		connection.addDriverStatusListener(this);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		Log.d(TAG, "onResume");	
		
		
		if (connection.getStatus() != SensorDriverConnection.UNBOUND) {
			connection.sendRequestConnectionStatus();
		}

	}
	
	@Override 
    public void onStop() {
		Log.d(TAG, "onStop");
		super.onStop();
		
		getLoaderManager().destroyLoader(OBSERVATIONS_LOADER_ID);
		
		connection.removeDriverStatusListener(this);
	}
	

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		Log.d(TAG, "onCreateOptionsMenu");
		final MenuItem refreshMenuItem = menu.add(
				R.id.observations_fragment_menu, 
				R.id.refresh_menu, 
				Menu.CATEGORY_SYSTEM, 
				R.string.refresh_menu);
		
		refreshMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		refreshMenuItem.setIcon(R.drawable.refresh);
		
		final MenuItem settingsMenuItem = menu.add(
				R.id.observations_fragment_menu, 
				R.id.settings_menu, 
				Menu.CATEGORY_SYSTEM, 
				R.string.settings);
		
		settingsMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		settingsMenuItem.setIcon(R.drawable.settings);		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch(item.getItemId()) {
		case R.id.settings_menu:
			final Intent settings = new Intent(activity, ForaSettings.class);
			startActivity(settings);
			return true;
			
		case R.id.refresh_menu:
			onRefresh();
			return true;
		}
		
		return false;
	}

	@Override
	public Loader<Collection<Record>> onCreateLoader(int id, Bundle args) {
		Log.d(TAG, "onCreateLoader");

		AsyncTaskLoader<Collection<Record>> loader = new ObservationsLoader(
				getActivity(), 
				types);

		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Collection<Record>> loader, Collection<Record> data) {
		Log.d(TAG, "onLoadFinished " + driverAction);

		mAdapter.clear();

		for (Record record : data) {
			mAdapter.add(record);
		}

		Log.d(TAG, "connection: " + connection);
		
		final int status = connection.getStatus();
		Log.d(TAG, String.format("onLoadFinished %d %d", status, mAdapter.getCount()));
		boolean noCommunicationInProgress = 
				(status == SensorDriverConnection.CONNECTED || 
				 status == SensorDriverConnection.BOUND || 
				 status == SensorDriverConnection.UNBOUND);
		setListShown(noCommunicationInProgress);
		
		pullToRefreshView.onRefreshComplete();
	}

	public void setListShown(boolean shown) {
		Log.d(TAG, String.format("setListShow %b %d", shown, listView.getAdapter().getCount()));
		if (shown) {
			boolean emptyList = listView.getAdapter().getCount() <= 1;
			emptyView.setVisibility(emptyList ? View.VISIBLE : View.GONE);
			pullToRefreshView.setVisibility(emptyList ? View.GONE : View.VISIBLE);
			listView.setVisibility(emptyList ? View.GONE : View.VISIBLE);
			
			progressView.setVisibility(View.GONE);
		} else {
			emptyView.setVisibility(View.GONE);
			pullToRefreshView.setVisibility(View.GONE);
			listView.setVisibility(View.GONE);
			
			progressView.setVisibility(View.VISIBLE);
		}
	}
	
	@Override
	public void onLoaderReset(Loader<Collection<Record>> loader) {
		mAdapter.clear();
	}
	
	@Override
	public void onDriverStatusChanged(DriverConnection connection, int oldStatus, int newStatus) {
		Log.d(TAG, String.format("onSensorSinkStatusChanged %s %d", driverAction, newStatus));
		
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
			
			if (oldStatus == SinkSensorConnection.DOWNLOADING) {
				// this is taken care of in onObservationsSaved() 
				
				return;
			} else 
			if (oldStatus != SinkSensorConnection.COUNTING) {
				((SinkSensorConnection) connection).sendReadObservationNumberMessage();
			}
				
			
			break;
		}
			
		case SinkSensorConnection.DOWNLOADING:
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
				getLoaderManager().initLoader(OBSERVATIONS_LOADER_ID, getArguments(), this);
			} else 
			break;
		}
		}
	}

	@Override
	public void onRefresh() {
		final SinkSensorConnection conn = (SinkSensorConnection) connection;

		switch(conn.getStatus()) {
		case SensorDriverConnection.CONNECTING:
		case SinkSensorConnection.COUNTING:
		case SinkSensorConnection.DOWNLOADING:
			
			Toast.makeText(activity, R.string.communication_in_process, Toast.LENGTH_LONG).show();
			
			return;
			
		case SensorDriverConnection.CONNECTED:
			setListShown(false);

			connection.sendReadObservationNumberMessage();
			break;
					
		case SensorDriverConnection.UNBOUND:
		case SensorDriverConnection.BOUND:
		{	

    		setListShown(false);
			activity.connect(driverAction);
			break;
		}
		}		
	}

	@Override
	public void onReceivedMessage(DriverConnection connection, Message msg) {
		Log.d(TAG,
				String.format("onReceivedMessage %s %d",
						connection.getDriverAction(), msg.what));

		switch (msg.what) {
		case SensorSinkService.RESPONSE_CONNECTION_TIMEOUT:
			Toast.makeText(activity, "Timeout", Toast.LENGTH_LONG).show();

			activity.chooseBtDevice((SinkSensorConnection) connection);

			break;
		
		case SensorSinkService.RESPONSE_COUNT_OBSERVATIONS:
			final int observationNum = msg.arg1;
			Log.d(TAG, "Sink object number is " + observationNum);

			((SinkSensorConnection) connection).sendReadObservations(
					types, 0,
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
		getLoaderManager().restartLoader(OBSERVATIONS_LOADER_ID, getArguments(), this);
	}
}
package fi.soberit.sensors.fora;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import fi.soberit.sensors.R;
import fi.soberit.sensors.fora.db.Ambient;
import fi.soberit.sensors.fora.db.BloodPressure;
import fi.soberit.sensors.fora.db.Glucose;
import fi.soberit.sensors.fora.db.Pulse;
import fi.soberit.sensors.fora.db.Record;
import fi.soberit.sensors.fora.db.Temperature;

class ObservationArrayAdapter extends ArrayAdapter<Record> {
	private SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm:ss");

	LayoutInflater inflater;

	private Context context;

	public ObservationArrayAdapter(Context context) {
		super(context, R.layout.observations_item, android.R.id.text1,
				new ArrayList<Record>());

		inflater = LayoutInflater.from(context);
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.observations_item, parent,
					false);
		}

		final TextView typeView = (TextView) convertView
				.findViewById(R.id.type);
		final TextView timeView = (TextView) convertView
				.findViewById(R.id.time);
		final TextView valuesView = (TextView) convertView
				.findViewById(R.id.values);

		final Record item = getItem(position);

		if (item instanceof Ambient) {
			typeView.setText(R.string.ambient);
			timeView.setText(R.string.unknown);
	
			final Ambient ambient = (Ambient) item;
			valuesView.setText(context.getString(R.string.ambient_values,
					ambient.getTemperature()));
		} else if (item instanceof Temperature) {
			typeView.setText(R.string.temperature);
			timeView.setText(R.string.unknown);

			final Temperature ambient = (Temperature) item;
			valuesView.setText(context.getString(R.string.temperature_values,
					ambient.getTemperature()));
		} else if (item instanceof BloodPressure) {
			typeView.setText(R.string.blood_pressure);
			timeView.setText(dateFormat.format(item.getTime()));

			final BloodPressure bloodPressure = (BloodPressure) item;
			valuesView.setText(context.getString(
					R.string.blood_pressure_values,
					bloodPressure.getSystolic(), bloodPressure.getDiastolic()));
		} else if (item instanceof Glucose) {
			typeView.setText(R.string.glucose);
			timeView.setText(dateFormat.format(item.getTime()));

			valuesView.setText(context.getString(R.string.glucose_values,
					((Glucose) item).getGlucose()));
		} else if (item instanceof Pulse) {
			typeView.setText(R.string.pulse);
			timeView.setText(dateFormat.format(item.getTime()));

			valuesView.setText(context.getString(
					R.string.pulse_values,
					((Pulse) item).getPulse()));
		}

		return convertView;
	}
}
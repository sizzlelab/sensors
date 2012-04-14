package fi.soberit.sensors.fora;

import java.util.List;

import android.os.AsyncTask;
import android.os.Parcelable;
import fi.soberit.sensors.Observation;
import fi.soberit.sensors.db.MGDatabaseHelper;
import fi.soberit.sensors.fora.db.Ambient;
import fi.soberit.sensors.fora.db.AmbientDao;
import fi.soberit.sensors.fora.db.BloodPressure;
import fi.soberit.sensors.fora.db.BloodPressureDao;
import fi.soberit.sensors.fora.db.DatabaseHelper;
import fi.soberit.sensors.fora.db.Glucose;
import fi.soberit.sensors.fora.db.GlucoseDao;
import fi.soberit.sensors.fora.db.Pulse;
import fi.soberit.sensors.fora.db.PulseDao;
import fi.soberit.sensors.fora.db.Temperature;
import fi.soberit.sensors.fora.db.TemperatureDao;
import fi.soberit.sensors.util.LittleEndian;

class SaveObservationsTask extends AsyncTask<List<Parcelable>, Void, Void> {
	private BloodPressureDao pressureDao;
	private PulseDao pulseDao;
	private GlucoseDao glucoseDao;
	private TemperatureDao temperatureDao;
	private AmbientDao ambientDao;

	private SimpleObservationListFragment fragment;

	public SaveObservationsTask(SimpleObservationListFragment fragment) {

		MGDatabaseHelper dbHelper = new DatabaseHelper(fragment.getActivity());
		
		pressureDao = new BloodPressureDao(dbHelper);
		pulseDao = new PulseDao(dbHelper);
		glucoseDao = new GlucoseDao(dbHelper);
		temperatureDao = new TemperatureDao(dbHelper);
		ambientDao = new AmbientDao(dbHelper);
		
		this.fragment = fragment;
	}

	@Override
	protected Void doInBackground(List<Parcelable>... params) {
		for (Parcelable parcelable : params[0]) {
			Observation value = (Observation) parcelable;

			long typeId = value.getObservationTypeId();
			if (typeId == Observation.TYPE_INDEX_BLOOD_PRESSURE) {
				pressureDao.insert(new BloodPressure(value.getTime(),
						LittleEndian.readInt(value.getValue(), 0),
						LittleEndian.readInt(value.getValue(), 4)));
			} else if (typeId == Observation.TYPE_INDEX_GLUCOSE) {
				glucoseDao.insert(new Glucose(value.getTime(), LittleEndian
						.readInt(value.getValue(), 0), LittleEndian
						.readInt(value.getValue(), 4)));
			} else if (typeId == Observation.TYPE_INDEX_PULSE) {
				pulseDao.insert(new Pulse(value.getTime(), LittleEndian
						.readInt(value.getValue(), 0)));
			} else if (typeId == Observation.TYPE_INDEX_TEMPERATURE) {
				temperatureDao.insert(new Temperature(value.getTime(),
						LittleEndian.readFloat(value.getValue(), 0)));
			} else if (typeId == Observation.TYPE_INDEX_AMBIENT_TEMPERATURE) {
				ambientDao.insert(new Ambient(value.getTime(), LittleEndian
						.readFloat(value.getValue(), 0)));
			}

		}
		return null;
	}

	public void onPostExecute(Void result) {

		fragment.onObservationsSaved();
	}
}
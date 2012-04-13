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

import fi.soberit.sensors.util.LittleEndian;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class Observation implements Parcelable {

	public static final long TYPE_INDEX_GLUCOSE = 1;
	public static final long TYPE_INDEX_PULSE = 2;
	public static final long TYPE_INDEX_BLOOD_PRESSURE = 3;
	public static final long TYPE_INDEX_STRIDES = 4;
	public static final long TYPE_INDEX_TEMPERATURE = 5;
	public static final long TYPE_INDEX_AMBIENT_TEMPERATURE = 5;
	public static final long TYPE_INDEX_RESPIRATION = 6;
	
//	public static final String TYPE_GLUCOSE = "application/vnd.sensor.bloodglucose";
//	public static final String TYPE_PULSE = "application/vnd.sensor.pulse";
//	public static final String TYPE_ACCELEROMETER = "application/vnd.sensor.accelerometer";
//	public static final String TYPE_BLOOD_PRESSURE = "application/vnd.sensor.bloodpressure";
//	public static final String TYPE_STRIDES = "application/vnd.sensor.strides";
//	public static final String TYPE_TEMPERATURE = "application/vnd.sensor.temperature";
//	public static final String TYPE_AMBIENT_TEMPERATURE = "application/vnd.sensor.ambient_temperature";
//	public static final String TYPE_RESPIRATION = "application/vnd.sensor.respiration";
//	public static final String TYPE_SKIN_CONDUCTIVITY = "application/vnd.sensor.skin_conductivity";
	
	protected long observationTypeId;
		
	protected long time; 
	
	private byte [] values;
	
	private static String TAG = Observation.class.getSimpleName();
	
	public Observation(long observationTypeId, long time, byte[] values) {
		this.observationTypeId = observationTypeId;
		this.time = time;
		this.values = values;
	}

	public long getTime() {
		return time;
	}

	public byte[] getValue() {
		return values;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	public long getObservationTypeId() {
		return observationTypeId;
	}

	public void setObservationTypeId(long observationTypeId) {
		this.observationTypeId = observationTypeId;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(observationTypeId);
		dest.writeLong(time);
		
		dest.writeInt(values.length);
		dest.writeByteArray(values);
	}

	public static final Parcelable.Creator<Observation> CREATOR
		= new Parcelable.Creator<Observation>() {
	
		@Override
	    public Observation[] newArray(int size) {
	        return new Observation[size];
	    }
		
		@Override
		public Observation createFromParcel(Parcel source) {
			final long observationTypeId = source.readLong();
			final long time = source.readLong();
			
			int size = source.readInt();
			final byte [] values = new byte[size];
			source.readByteArray(values);
			
			return new Observation(observationTypeId, time, values);
		}
	};

	public int getValuesNum() {
		return this.values.length;
	}

	public int getInteger(int pos) {
		return LittleEndian.readInt(values, pos);
	}
	
	public float getFloat(int pos) {
		return LittleEndian.readFloat(values, pos);
	}
	
	public String toString() {
		return String.format("generic of type %d , recorded %d, has [%d]",  
				observationTypeId,
				time,
				values.length);
	}
}

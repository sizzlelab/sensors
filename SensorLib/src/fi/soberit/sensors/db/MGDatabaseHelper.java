/*******************************************************************************
 * Copyright (c) 2011 Aalto University
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package fi.soberit.sensors.db;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public abstract class MGDatabaseHelper extends SQLiteOpenHelper {

	protected final String TAG = this.getClass().getSimpleName();
	
    public final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	private SQLiteDatabase readOnlyDatabase;

	private SQLiteDatabase readWriteDatabase;
    
    final static Calendar calendar;
    static {
    	calendar = Calendar.getInstance();
    }
    
	public MGDatabaseHelper(Context context, String name, int version) {
		super(context, name, null, version);
	}
    
	public static String getUtcDateString(Date date) {
		return getUtcDateString(date.getTime());
	}   
	
	public static String getUtcDateString(long millis) {
		final int timezoneOffset = calendar.get(Calendar.DST_OFFSET) + calendar.get(Calendar.ZONE_OFFSET);
		return dateFormat.format(millis - timezoneOffset);
	}

	public static long getLongFromUtcDateString(String time) {
		final int timezoneOffset = calendar.get(Calendar.DST_OFFSET) + calendar.get(Calendar.ZONE_OFFSET);
		try {
			return dateFormat.parse(time).getTime() + timezoneOffset;
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	public static Date getDateFromUtcDateString(String time) {
		final int timezoneOffset = calendar.get(Calendar.DST_OFFSET) + calendar.get(Calendar.ZONE_OFFSET);
		try {
			final Date intermediary = dateFormat.parse(time);
			intermediary.setTime(intermediary.getTime() + timezoneOffset);
			return intermediary;
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}
	
	public SQLiteDatabase getReadableDatabase() {
		if (readOnlyDatabase == null) {
			readOnlyDatabase = super.getReadableDatabase();
		}
		
		return readOnlyDatabase;
	}

	public SQLiteDatabase getWritableDatabase()  {
		if (readWriteDatabase == null) {
			readWriteDatabase = super.getWritableDatabase();
		}
		return readWriteDatabase;
	}

	public void closeDatabases() {
		readOnlyDatabase = null;		
		readWriteDatabase = null;
		
		close();
	}
	
	public static final String IN = "IN";
	
	public static final String NOT_IN = "NOT IN";
	
	public static int getBooleanInt(SharedPreferences prefs, String key) { 
		return getBooleanInt(prefs, key, Boolean.TRUE);
	}
	
	public static int getBooleanInt(SharedPreferences prefs, String key, Boolean defValue) {
		return prefs.getBoolean(key, defValue)
			? 1
			: 0;
	}

	public static boolean getBooleanFromDBInt(Cursor c, String columnName) {
		return c.getInt(c.getColumnIndex(columnName)) == 1;
	}
	
	public static String getSetClause(String fieldName, Collection col, String in) {
		final StringBuilder builder = new StringBuilder();
		
		if (col.size() > 0) {
			builder.append(fieldName + " " + in + " (");
		}
		
		for (int i = 0; i< col.size(); i++) {
			builder.append("?, ");
		}
		
		if (col.size() > 0) {
			builder.setLength(builder.length() -2);
			builder.append(")");
		}
		
		return builder.toString();
	}

	public static String getSetClause(String fieldName, String[] arr, String in) {
		final StringBuilder builder = new StringBuilder();
		
		if (arr.length > 0) {
			builder.append(fieldName + " " + in + " (");
		}
		
		for (int i = 0; i< arr.length; i++) {
			builder.append("?, ");
		}
		
		if (arr.length > 0) {
			builder.setLength(builder.length() -2);
			builder.append(")");
		}
		
		return builder.toString();
	}
}

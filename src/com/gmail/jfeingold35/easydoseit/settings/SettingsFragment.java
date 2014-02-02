package com.gmail.jfeingold35.easydoseit.settings;

import com.gmail.jfeingold35.easydoseit.R;
import com.gmail.jfeingold35.easydoseit.classes.AlarmSetter;
import com.gmail.jfeingold35.easydoseit.database.MedTable;
import com.gmail.jfeingold35.easydoseit.medprovider.MedProvider;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.content.CursorLoader;
import android.util.Log;

public class SettingsFragment extends PreferenceFragment
							implements OnSharedPreferenceChangeListener,
							           LoaderCallbacks<Cursor> {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.preferences);
		initializeSummaries();
	}
	
	/**
	 * The purpose of this function is to set the initial summaries for any
	 * preferences that require it. Currently, the only one that requires such
	 * is the time preference.
	 */
	public void initializeSummaries() {
		// TODO: IMPLEMENT THIS FUNCTION
		final SharedPreferences sh = getPreferenceManager().getSharedPreferences();
		String timeKey = getString(R.string.pref_key_alarm_time);
		Preference timePreference = findPreference(timeKey);
		int time = sh.getInt(timeKey, 1200);
		setTimeSummary(time, timePreference);
	}
	
	/**
	 * Turns the int 'time' into a string formatted "HH:MM AM/PM",
	 * and sets that string as the summary of 'timePref'.
	 * @param time - The time obtained from the preferences
	 * @param timePref - The preference whose summary shall be set.
	 */
	public void setTimeSummary(int time, Preference timePref) {
		int hour = time / 100;
		int minute = time % 100;
		String hourString;
		String minuteString;
		String amOrPm;
		
		// Format the hour string and amOrPm.
		if(hour == 0) {
			hourString = "12:";
			amOrPm = " AM";
		} else if(hour < 12) {
			hourString = Integer.toString(hour) + ":";
			amOrPm = " AM";
		} else if(hour == 12) {
			hourString = "12:";
			amOrPm = " PM";
		} else {
			hourString = Integer.toString(hour - 12) + ":";
			amOrPm = " PM";
		}
		
		// Format the minute string.
		if(minute < 10) {
			minuteString = "0" + Integer.toString(minute);
		} else {
			minuteString = Integer.toString(minute);
		}
		String timeString = hourString + minuteString + amOrPm;
		timePref.setSummary(timeString);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences()
					.registerOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences()
					.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		Log.d("SF", "function called");
		String timeKey = "pref_alarmTime";
		if(key.equals(getString(R.string.pref_key_alarm_time))) {
			int time = sharedPreferences.getInt(timeKey, 1200);
			Preference timePref = findPreference(key);
			// Set preference summary to the time the user selected
			setTimeSummary(time, timePref);
			getLoaderManager().initLoader(0, null, this);
			getLoaderManager().getLoader(0).forceLoad();
		}
	}

	// These are the fields we will be retrieving
	static final String[] MED_PROJECTION = new String[] {
		MedTable.MED_ID,
		MedTable.MED_NAME,
		MedTable.MED_DATE_FILLED, 
		MedTable.MED_DURATION,
		MedTable.MED_WARNING,
		MedTable.MED_REMINDER_ON };
	
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		// This is called when a new Loader needs to be created.
		Uri baseUri = MedProvider.CONTENT_URI;
		Log.d("SF", "Loader Init");
		// Now create and return a CursorLoader that will take care of
		// creating a Cursor for the data being displayed.
		String select = "((" + MedTable.MED_NAME + " NOTNULL) AND ("
				+ MedTable.MED_NAME + " != '' ) AND ("
				+ MedTable.MED_REMINDER_ON + " != 0))";
		return new CursorLoader(getActivity(), baseUri,
				MED_PROJECTION, select, null,
				MedTable.MED_NAME + " COLLATE LOCALIZED ASC");
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		// In the while loop, reset every alarm
		if(cursor != null) {
			Log.d("SF", "Loader finished");
			AlarmSetter alarmSetter = new AlarmSetter();
			int idIndex = cursor.getColumnIndexOrThrow(MedTable.MED_ID);
			int nameIndex = cursor.getColumnIndexOrThrow(MedTable.MED_NAME);
			int dateIndex = cursor.getColumnIndexOrThrow(MedTable.MED_DATE_FILLED);
			int durationIndex = cursor.getColumnIndexOrThrow(MedTable.MED_DURATION);
			int warningIndex = cursor.getColumnIndexOrThrow(MedTable.MED_WARNING);
			// Moves cursor to the next row
			while(cursor.moveToNext()) {
				int medId = cursor.getInt(idIndex);
				String name = cursor.getString(nameIndex);
				long date = cursor.getLong(dateIndex);
				int duration = cursor.getInt(durationIndex);
				int warning = cursor.getInt(warningIndex);
				alarmSetter.setRefillAlarm(getActivity(), medId, name, date, duration, warning);
			}
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
	}
}

package com.gmail.jfeingold35.easydoseit;



import com.gmail.jfeingold35.easydoseit.alarmprovider.AlarmProvider;
import com.gmail.jfeingold35.easydoseit.classes.AlarmSetter;
import com.gmail.jfeingold35.easydoseit.database.DailyAlarmTable;
import com.gmail.jfeingold35.easydoseit.database.MedTable;
import com.gmail.jfeingold35.easydoseit.dialogs.AlarmCreationDialogFragment;
import com.gmail.jfeingold35.easydoseit.dialogs.AlarmCreationDialogFragment.TimePickedListener;
import com.gmail.jfeingold35.easydoseit.dialogs.AlarmEditDialogFragment.TimeEditListener;
import com.gmail.jfeingold35.easydoseit.fragments.MedDetailFragment;
import com.gmail.jfeingold35.easydoseit.medprovider.MedProvider;
import com.gmail.jfeingold35.easydoseit.settings.SettingsActivity;
import com.gmail.jfeingold35.easydoseit.R;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

/**
 * An activity representing a single Med detail screen. This activity is only
 * used on handset devices. On tablet-size devices, item details are presented
 * side-by-side with a list of items in a {@link MedListActivity}.
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing more than
 * a {@link MedDetailFragment}.
 */
public class MedDetailActivity extends FragmentActivity implements TimePickedListener,
                                                                   TimeEditListener, LoaderCallbacks<Cursor>{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_med_detail);

		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);

		// savedInstanceState is non-null when there is fragment state
		// saved from previous configurations of this activity
		// (e.g. when rotating the screen from portrait to landscape).
		// In this case, the fragment will automatically be re-added
		// to its container so we don't need to manually add it.
		// For more information, see the Fragments API guide at:
		//
		// http://developer.android.com/guide/components/fragments.html
		//
		if (savedInstanceState == null) {
			// Create the detail fragment and add it to the activity
			// using a fragment transaction.
			Bundle arguments = new Bundle();
			arguments.putLong(MedDetailFragment.ARG_MED_ID, getIntent()
					.getLongExtra(MedDetailFragment.ARG_MED_ID, 0));
			arguments.putBoolean(MedDetailFragment.ARG_TWO_PANE, false);
			MedDetailFragment fragment = new MedDetailFragment();
			fragment.setArguments(arguments);
			getSupportFragmentManager().beginTransaction()
					.add(R.id.med_detail_container, fragment).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.med_detail_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpTo(this, new Intent(this, MedListActivity.class));
			return true;
		case R.id.action_add_alarm:
			DialogFragment frag = new AlarmCreationDialogFragment();
			frag.show(getSupportFragmentManager(), "alarmcreation");
			return true;
		case R.id.action_delete_med:
			// Initialize the loader to handle deactivating and deleting all alarms,
			// and handle deleting the med itself.
			getLoaderManager().initLoader(0, null, this);
			return true;
		case R.id.action_edit_med:
			long editMedId = getIntent().getLongExtra(MedDetailFragment.ARG_MED_ID, 0);
			Intent updateMedIntent = new Intent(this, UpdateMedActivity.class);
			updateMedIntent.putExtra(UpdateMedActivity.ARG_MED_ID, editMedId);
			startActivity(updateMedIntent);
			return true;
		case R.id.action_refill_med:
			long medId = getIntent().getLongExtra(MedDetailFragment.ARG_MED_ID, 0);
			Intent refillMedIntent = new Intent(this, RefillMedActivity.class);
			refillMedIntent.putExtra(RefillMedActivity.ARG_MED_ID, medId);
			startActivity(refillMedIntent);
			return true;
		case R.id.action_settings:
			Intent settingsIntent = new Intent(this, SettingsActivity.class);
			startActivity(settingsIntent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * This function will turn the hour and day into an "HH:mm AM/PM" string,
	 * calculate the timestamp, and then inserts them into the table.
	 */
	@Override
	public void onTimePicked(int hourOfDay, int minute) {
		Log.d("MedManager", "onTimePicked triggered");
		// Create the alarmSetter
		AlarmSetter alarmSetter = new AlarmSetter();
		
		// Convert the hour and minute into a string
		String alarmString = alarmSetter.formatAlarmString(hourOfDay, minute);
		
		// Convert the hour and minute into a timestamp
		long alarmTimestamp = alarmSetter.getAlarmTimestamp(hourOfDay, minute);
		
		// Define the URI to receive the results of the insertion
		Uri newUri = null;
		
		// Define a contentValues object to contain the new Values
		ContentValues mValues = new ContentValues();
		
		// Add medId;
		long medId = getIntent().getLongExtra(MedDetailFragment.ARG_MED_ID, 0);
		mValues.put(DailyAlarmTable.ALARM_MEDNUM, medId);
		
		// Add the timestamp
		mValues.put(DailyAlarmTable.ALARM_TIMESTAMP, alarmTimestamp);
		
		// Add the time string
		mValues.put(DailyAlarmTable.ALARM_TIME, alarmString);
		
		// IsActive is automatically set to 1
		mValues.put(DailyAlarmTable.ALARM_ISACTIVE, 1);
		
		// IsLoud is automatically set to 1
		mValues.put(DailyAlarmTable.ALARM_ISLOUD, 1);
		
		// Insert the new alarm
		newUri = getContentResolver().insert(AlarmProvider.CONTENT_URI, mValues);
		
		// Add the new alarm to the alarm manager
		int alarmId = (int) ContentUris.parseId(newUri);
		alarmSetter.setDailyAlarm(getApplicationContext(), medId, alarmId, alarmString, alarmTimestamp, true);		
	}

	/**
	 * Query all of the alarms associated with the given medID.
	 */
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		CursorLoader loader = null;
		long id = getIntent().getLongExtra(MedDetailFragment.ARG_MED_ID, 0);
		Uri baseUri = AlarmProvider.CONTENT_URI;
		
		// Create the cursorloader
		String[] alarmProjection = { DailyAlarmTable.ALARM_ID };
		String selection = "((" + DailyAlarmTable.ALARM_MEDNUM + " NOTNULL) AND ("
				+ DailyAlarmTable.ALARM_MEDNUM + " = " + id + "))";
		loader = new CursorLoader(getApplicationContext(), baseUri,
				alarmProjection, selection, null,
				DailyAlarmTable.ALARM_TIMESTAMP + " ASC");
		
		return loader;
	}

	/**
	 * Deactivate every alarm in the given cursor, then delete them.
	 * Then, delete the med and finish the activity.
	 */
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		// In the while loop, deactivate every alarm
		if(cursor != null) {
			AlarmSetter alarmSetter = new AlarmSetter();
			// Moves cursor to the next row
			while(cursor.moveToNext()) {
				int idIndex = cursor.getColumnIndexOrThrow(DailyAlarmTable.ALARM_ID);
				int id = cursor.getInt(idIndex);
				alarmSetter.cancelDailyAlarm(getApplicationContext(), id);
			}
		}
		
		// Delete all alarms associated with this med
		long medNum = getIntent().getLongExtra(MedDetailFragment.ARG_MED_ID, 0);
		String alarmSelection = "((" + DailyAlarmTable.ALARM_MEDNUM + " NOTNULL) AND ("
				+ DailyAlarmTable.ALARM_MEDNUM + " = " + medNum + "))";
		getContentResolver().delete(AlarmProvider.CONTENT_URI, alarmSelection, null);
		
		// Cancel the refill alarm
		AlarmSetter alarmSetter = new AlarmSetter();
		alarmSetter.cancelRefillAlarm(getApplicationContext(),(int) medNum);
		// Delete the med
		String medSelection = "((" + MedTable.MED_ID + " NOTNULL) AND ("
				+ MedTable.MED_ID + " = " + medNum + "))";
		getContentResolver().delete(MedProvider.CONTENT_URI, medSelection, null);
		
		// Finish the activity
		finish();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		
	}

	/**
	 * Edits an alarm's entry in the database, and resets the alarm in the alarm manager.
	 */
	@Override
	public void onTimeEdit(int id, int hourOfDay, int minute, long medId,
			boolean isLoud, boolean isActive) {
		// Create the alarm setter
		AlarmSetter alarmSetter = new AlarmSetter();
		
		// Convert the hour and minute into a string
		String timeString = alarmSetter.formatAlarmString(hourOfDay, minute);
		
		// Convert the hour and minute into a timestamp
		long timeStamp = alarmSetter.getAlarmTimestamp(hourOfDay, minute);
		
		// Define and populate a content values to perform the update
		ContentValues values = new ContentValues();
		values.put(DailyAlarmTable.ALARM_TIMESTAMP, timeStamp);
		values.put(DailyAlarmTable.ALARM_TIME, timeString);
		String selection = "(" + DailyAlarmTable.ALARM_ID + " = " + id + ")";
		int rowsUpdated = 0;
		rowsUpdated = getContentResolver().update(AlarmProvider.CONTENT_URI, values, selection, null);
		Log.d("SCA", rowsUpdated + " rows updated");
		
		// Reset the alarm if isActive is true
		if(isActive) {
			alarmSetter.setDailyAlarm(getApplicationContext(), medId, id, timeString, timeStamp, isLoud);
		}
	}
}

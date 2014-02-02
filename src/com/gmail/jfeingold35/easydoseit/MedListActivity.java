package com.gmail.jfeingold35.easydoseit;

import com.gmail.jfeingold35.easydoseit.R;
import com.gmail.jfeingold35.easydoseit.alarmprovider.AlarmProvider;
import com.gmail.jfeingold35.easydoseit.classes.AlarmSetter;
import com.gmail.jfeingold35.easydoseit.database.DailyAlarmTable;
import com.gmail.jfeingold35.easydoseit.database.MedTable;
import com.gmail.jfeingold35.easydoseit.dialogs.AlarmCreationDialogFragment;
import com.gmail.jfeingold35.easydoseit.dialogs.AlarmCreationDialogFragment.TimePickedListener;
import com.gmail.jfeingold35.easydoseit.dialogs.AlarmEditDialogFragment.TimeEditListener;
import com.gmail.jfeingold35.easydoseit.fragments.MedDetailFragment;
import com.gmail.jfeingold35.easydoseit.fragments.MedListFragment;
import com.gmail.jfeingold35.easydoseit.medprovider.MedProvider;
import com.gmail.jfeingold35.easydoseit.settings.SettingsActivity;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

/**
 * An activity representing a list of Meds. This activity has different
 * presentations for handset and tablet-size devices. On handsets, the activity
 * presents a list of items, which when touched, lead to a
 * {@link MedDetailActivity} representing item details. On tablets, the activity
 * presents the list of items and item details side-by-side using two vertical
 * panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link MedListFragment} and the item details (if present) is a
 * {@link MedDetailFragment}.
 * <p>
 * This activity also implements the required {@link MedListFragment.Callbacks}
 * interface to listen for item selections.
 */
public class MedListActivity extends FragmentActivity implements MedListFragment.MedCallbacks,
                                                                 TimePickedListener, TimeEditListener,
																 LoaderCallbacks<Cursor> {

	/**
	 * Whether or not the activity is in two-pane mode, i.e. running on a tablet
	 * device.
	 */
	private boolean mTwoPane;
	
	/**
	 * Corresponds to the ID of the selected med. Initialized to -1 to indicate
	 * that there is no med currently selected.
	 */
	private long mId = -1;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_med_list);
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		if (findViewById(R.id.med_detail_container) != null) {
			// The detail container view will be present only in the
			// large-screen layouts (res/values-large and
			// res/values-sw600dp). If this view is present, then the
			// activity should be in two-pane mode.
			mTwoPane = true;

			// In two-pane mode, list items should be given the
			// 'activated' state when touched.
			((MedListFragment) getSupportFragmentManager().findFragmentById(
					R.id.med_list)).setActivateOnItemClick(true);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu. This adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.med_list_menu, menu);
		if(mId >= 0) {
			MenuItem refill = menu.findItem(R.id.action_refill_med);
			refill.setVisible(true);
			refill.setEnabled(true);
			
			MenuItem delete = menu.findItem(R.id.action_delete_med);
			delete.setVisible(true);
			delete.setEnabled(true);
			
			MenuItem addAlarm = menu.findItem(R.id.action_add_alarm);
			addAlarm.setVisible(true);
			addAlarm.setEnabled(true);
			
			MenuItem edit = menu.findItem(R.id.action_edit_med);
			edit.setVisible(true);
			edit.setEnabled(true);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.action_add_med:
			Intent addMedIntent = new Intent(this, NewMedActivity.class);
			startActivity(addMedIntent);
			return true;
		case R.id.action_settings:
			Intent settingsIntent = new Intent(this, SettingsActivity.class);
			startActivity(settingsIntent);
			return true;
		case R.id.action_add_alarm:
			DialogFragment frag = new AlarmCreationDialogFragment();
			frag.show(getSupportFragmentManager(), "alarmCreation");
			return true;
		case R.id.action_refill_med:
			Intent refillMedIntent = new Intent(this, RefillMedActivity.class);
			refillMedIntent.putExtra(RefillMedActivity.ARG_MED_ID, mId);
			startActivity(refillMedIntent);
			return true;
		case R.id.action_edit_med:
			Intent updateMedIntent = new Intent(this, UpdateMedActivity.class);
			updateMedIntent.putExtra(UpdateMedActivity.ARG_MED_ID, mId);
			startActivity(updateMedIntent);
			return true;
		case R.id.action_delete_med:
			// Initialize the loader to handle deactivating and deleting all alarms,
			// and handle deleting the med itself.
			getLoaderManager().initLoader(0, null, this);
			return true;
		}
		return false;
	}

	/**
	 * Callback method from {@link MedListFragment.MedCallbacks} indicating
	 * that the item with the given id was selected.
	 */
	@Override
	public void onMedSelected(long id) {

		if(mTwoPane) {
			// In two-pane mode, show the detail view in this activity by
			// adding or replacing the detail fragment using a
			// fragment transaction.
			Bundle arguments = new Bundle();
			arguments.putLong(MedDetailFragment.ARG_MED_ID, id);
			arguments.putBoolean(MedDetailFragment.ARG_TWO_PANE, true);
			MedDetailFragment fragment = new MedDetailFragment();
			fragment.setArguments(arguments);
			getSupportFragmentManager().beginTransaction()
				.replace(R.id.med_detail_container, fragment).commit();
			mId = id;
			invalidateOptionsMenu();
		} else {
			// In single-pane mode, simply start the detail activity
			// for the selected item ID.
			Intent detailIntent = new Intent(this, MedDetailActivity.class);
			detailIntent.putExtra(MedDetailFragment.ARG_MED_ID, id);
			startActivity(detailIntent);
		}
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
		mValues.put(DailyAlarmTable.ALARM_MEDNUM, mId);
		
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
		alarmSetter.setDailyAlarm(getApplicationContext(), mId, alarmId, alarmString, alarmTimestamp, true);		
	}
	
	/**
	 * Query all of the alarms associated with the given medID.
	 */
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		CursorLoader loader = null;
		Uri baseUri = AlarmProvider.CONTENT_URI;
		
		// Create the cursorloader
		String[] alarmProjection = { DailyAlarmTable.ALARM_ID };
		String selection = "((" + DailyAlarmTable.ALARM_MEDNUM + " NOTNULL) AND ("
				+ DailyAlarmTable.ALARM_MEDNUM + " = " + mId + "))";
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
		if(mId != -1) {
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
			String alarmSelection = "((" + DailyAlarmTable.ALARM_MEDNUM + " NOTNULL) AND ("
					+ DailyAlarmTable.ALARM_MEDNUM + " = " + mId + "))";
			getContentResolver().delete(AlarmProvider.CONTENT_URI, alarmSelection, null);
			
			// Cancel the refill alarm
			AlarmSetter alarmSetter = new AlarmSetter();
			alarmSetter.cancelRefillAlarm(getApplicationContext(), (int) mId);
			// Delete the med
			String medSelection = "((" + MedTable.MED_ID + " NOTNULL) AND ("
					+ MedTable.MED_ID + " = " + mId + "))";
			getContentResolver().delete(MedProvider.CONTENT_URI, medSelection, null);
			Fragment frag = getSupportFragmentManager().findFragmentById(R.id.med_detail_container);
			getSupportFragmentManager().beginTransaction().remove(frag).commit();
			mId = -1;
			Log.d("MDA", "Med deleted");
			getLoaderManager().destroyLoader(0);
		}
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
		// TODO: DO THAT.
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

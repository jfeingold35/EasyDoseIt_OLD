package com.gmail.jfeingold35.easydoseit;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.gmail.jfeingold35.easydoseit.classes.AlarmSetter;
import com.gmail.jfeingold35.easydoseit.classes.FieldValidator;
import com.gmail.jfeingold35.easydoseit.database.MedTable;
import com.gmail.jfeingold35.easydoseit.medprovider.MedProvider;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

public class UpdateMedActivity extends Activity implements LoaderCallbacks<Cursor> {

	/**
	 * Corresponds to the ID of the med to be updated.
	 */
	public static final String ARG_MED_ID = "med_id";
	private long mMedId;
	private boolean mAlarmAlreadyOn;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_update_med);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		// Retrieve the med Id
		mMedId = getIntent().getLongExtra(ARG_MED_ID, 0);
		getLoaderManager().initLoader(0, null, this);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case android.R.id.home:
			finish();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		CursorLoader loader = null;
		Uri singleUri = ContentUris.withAppendedId(MedProvider.CONTENT_URI, mMedId);
		String[] projection = { MedTable.MED_ID, MedTable.MED_NAME, MedTable.MED_DOSAGE,
				MedTable.MED_DATE_FILLED, MedTable.MED_DURATION, MedTable.MED_REMINDER_ON,
				MedTable.MED_WARNING };
		loader = new CursorLoader(getApplicationContext(), singleUri,
				projection, null, null,
				MedTable.MED_ID + " COLLATE LOCALIZED ASC");
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		int nameIndex = cursor.getColumnIndexOrThrow(MedTable.MED_NAME);
		int dosageIndex = cursor.getColumnIndexOrThrow(MedTable.MED_DOSAGE);
		int dateIndex = cursor.getColumnIndexOrThrow(MedTable.MED_DATE_FILLED);
		int durationIndex = cursor.getColumnIndexOrThrow(MedTable.MED_DURATION);
		int reminderIndex = cursor.getColumnIndexOrThrow(MedTable.MED_REMINDER_ON);
		int warningIndex = cursor.getColumnIndexOrThrow(MedTable.MED_WARNING);
		if(cursor != null) {
			while(cursor.moveToNext()) {
				// Get the name
				String name = cursor.getString(nameIndex);
				// Get the dosage
				String dosage = cursor.getString(dosageIndex);
				// Get and format the date
				long dateEpoch = cursor.getLong(dateIndex);
				DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
				String date;
				if(df instanceof SimpleDateFormat) {
					SimpleDateFormat sdf = (SimpleDateFormat) df;
					String pattern = sdf.toPattern().replaceAll("y+", "yyyy");
					sdf.applyPattern(pattern);
					date = sdf.format(new Date(dateEpoch * 1000));
				} else {
					date = df.format(new Date(dateEpoch * 1000));
				}
				// Get the duration
				String duration = cursor.getString(durationIndex);
				// Check if reminder is on
				int reminderBit = cursor.getInt(reminderIndex);
				boolean reminderIsOn = reminderBit == 1;
				mAlarmAlreadyOn = reminderIsOn;
				// Get the warning period
				String warning = cursor.getString(warningIndex);
				
				// Find the text edits
				EditText nameEdit = (EditText) findViewById(R.id.nameEdit);
				EditText dosageEdit = (EditText) findViewById(R.id.dosageEdit);
				EditText dateEdit = (EditText) findViewById(R.id.dateEdit);
				EditText durationEdit = (EditText) findViewById(R.id.durationEdit);
				CheckBox reminderCheckbox = (CheckBox) findViewById(R.id.reminderCheckbox);
				EditText reminderEdit = (EditText) findViewById(R.id.reminderEdit);
				
				// Insert the text
				nameEdit.setText(name);
				dosageEdit.setText(dosage);
				dateEdit.setText(date);
				durationEdit.setText(duration);
				reminderCheckbox.setChecked(reminderIsOn);
				if(reminderCheckbox.isChecked()) {
					reminderEdit.setEnabled(true);
					reminderEdit.setInputType(InputType.TYPE_CLASS_NUMBER);
					reminderEdit.setFocusableInTouchMode(true);
					reminderEdit.setVisibility(View.VISIBLE);
					reminderEdit.setText(warning);
				} else {
					reminderEdit.setEnabled(false);
					reminderEdit.setInputType(InputType.TYPE_NULL);
					reminderEdit.setFocusable(false);
					reminderEdit.setVisibility(View.INVISIBLE);
				}
			}
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		
	}
	
	public void reminderCheckboxClicked(View view) {
		CheckBox cbox = (CheckBox) view;
		boolean checked = cbox.isChecked();
		EditText reminderEdit = (EditText) findViewById(R.id.reminderEdit);
		if(checked) {
			reminderEdit.setEnabled(true);
			reminderEdit.setInputType(InputType.TYPE_CLASS_NUMBER);
			reminderEdit.setFocusableInTouchMode(true);
			reminderEdit.setVisibility(View.VISIBLE);
		} else {
			reminderEdit.setEnabled(false);
			reminderEdit.setInputType(InputType.TYPE_NULL);
			reminderEdit.setFocusable(false);
			reminderEdit.setVisibility(View.INVISIBLE);
			reminderEdit.clearFocus();
		}
	}
	
	public void updateMed(View view) {
		int anyInvalid = validateFields();
		
		if(anyInvalid != 0) {
			return;
		}
		
		// If we're here, all fields check out, so we can start the update
		ContentValues newValues = new ContentValues();
		
		// Add the new name
		EditText nameEdit = (EditText) findViewById(R.id.nameEdit);
		String name = nameEdit.getText().toString();
		newValues.put(MedTable.MED_NAME, name);
		
		// Add the dosage
		EditText dosageEdit = (EditText) findViewById(R.id.dosageEdit);
		String dosage = dosageEdit.getText().toString();
		newValues.put(MedTable.MED_DOSAGE, dosage);
		
		// Add the date
		// Step 1: Convert the date to a UNIX timestamp
		EditText dateEdit = (EditText) findViewById(R.id.dateEdit);
		String date = dateEdit.getText().toString();
		long epoch = 0;
		try {
			DateFormat sdf = DateFormat.getDateInstance(DateFormat.SHORT);
			epoch = sdf.parse(date).getTime()/1000;
			newValues.put(MedTable.MED_DATE_FILLED, epoch);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		// Add the duration
		EditText durationEdit = (EditText) findViewById(R.id.durationEdit);
		int duration = Integer.parseInt(durationEdit.getText().toString());
		newValues.put(MedTable.MED_DURATION, duration);
		
		// Add the warning preference. If the box is checked, add the value inside.
		// If not, add 0.
		CheckBox reminderCheck = (CheckBox) findViewById(R.id.reminderCheckbox);
		int reminder;
		if(reminderCheck.isChecked()) {
			newValues.put(MedTable.MED_REMINDER_ON, 1);
			EditText reminderEdit = (EditText) findViewById(R.id.reminderEdit);
			reminder = Integer.parseInt(reminderEdit.getText().toString());
		} else {
			newValues.put(MedTable.MED_REMINDER_ON, 0);
			reminder = 0;
		}
		newValues.put(MedTable.MED_WARNING, reminder);
		
		// Create the selection string
		String selection = MedTable.MED_ID + " = " + mMedId;
		
		int rowsUpdated = getContentResolver().update(MedProvider.CONTENT_URI,
				newValues,
				selection,
				null);
		Log.d("UMA", "updated " + rowsUpdated + " rows");
		
		if(reminderCheck.isChecked()) {
			AlarmSetter alarmSetter = new AlarmSetter();
			alarmSetter.setRefillAlarm(getApplicationContext(),
					(int) mMedId, name, epoch, duration, reminder);
		} else if(mAlarmAlreadyOn) {
			AlarmSetter alarmSetter = new AlarmSetter();
			alarmSetter.cancelRefillAlarm(getApplicationContext(), (int) mMedId);
		}
		finish();
	}
	
	/**
	 * Validates each field by checking that it isn't null,
	 * and checking that its contents fit the desired formats.
	 * If all fields are valid, returns 0. If any field is not valid,
	 * returns an int corresponding to that field.
	 */
	public int validateFields() {
		FieldValidator validator = new FieldValidator();
		// CHECK THE NAME
		EditText medNameText = (EditText) findViewById(R.id.nameEdit);
		if(!validator.validateAlphaNum(medNameText, "Name")) {
			return 1;
		}
		// CHECK THE DOSAGE
		EditText medDosageText = (EditText) findViewById(R.id.dosageEdit);
		if(!validator.validateAlphaNum(medDosageText, "Dosage")) {
			return 2;
		}
		// CHECK DATE FILLED
		EditText medDateFilledText = (EditText) findViewById(R.id.dateEdit);
		if(!validator.validateDate(medDateFilledText, "Date Filled")) {
			return 3;
		}
		// CHECK DURATION
		EditText medDurationText = (EditText) findViewById(R.id.durationEdit);
		if(!validator.validateAlphaNum(medDurationText, "Duration")) {
			return 4;
		}
		// CHECK REFILL WARNING AND DATE
		CheckBox reminderBox = (CheckBox) findViewById(R.id.reminderCheckbox);
		boolean checked = reminderBox.isChecked();
		if(checked) {
			EditText reminderText = (EditText) findViewById(R.id.reminderEdit);
			if(!validator.validateAlphaNum(reminderText, "Days Notice")) {
				return 5;
			}
		}
		// If we're here, it's because all fields have checked out,
		// so we can just return 0 and be done with it.
		return 0;
	}
}

package com.gmail.jfeingold35.easydoseit;

import java.text.DateFormat;
import java.text.ParseException;

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
import android.widget.TextView;

public class RefillMedActivity extends Activity implements LoaderCallbacks<Cursor> {
	/**
	 * Corresponds to the ID of the med to be refilled
	 */
	public static final String ARG_MED_ID = "med_id";
	private long mMedId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_refill_med);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		// Retrieve the medID
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
		Log.d("RefillMedActivity", "med ID is " + mMedId);
		Uri singleUri = ContentUris.withAppendedId(MedProvider.CONTENT_URI, mMedId);
		String[] projection = { MedTable.MED_ID, MedTable.MED_NAME, MedTable.MED_DOSAGE };
		loader = new CursorLoader(getApplicationContext(), singleUri,
				projection, null, null,
				MedTable.MED_ID + " COLLATE LOCALIZED ASC");
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		int nameIndex = cursor.getColumnIndexOrThrow(MedTable.MED_NAME);
		int dosageIndex = cursor.getColumnIndexOrThrow(MedTable.MED_DOSAGE);
		if(cursor != null) {
			while(cursor.moveToNext()) {
				String name = cursor.getString(nameIndex);
				String dosage = cursor.getString(dosageIndex);
				TextView nameView = (TextView) findViewById(R.id.nameView);
				TextView dosageView = (TextView) findViewById(R.id.dosageView);
				nameView.setText(name);
				dosageView.setText(dosage);
			}
		}
		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
	}
	
	/**
	 * Enables the EditText that allows the user to enter how many days in
	 * advance they want to be warned to refill their prescription.
	 * @param view - the view clicked. In this case, limited to the reminder checkbox
	 */
	public void reminderCheckboxClicked(View view) {
		CheckBox cBox = (CheckBox) view;
		boolean checked = cBox.isChecked();
		EditText reminderText = (EditText) findViewById(R.id.reminderText);
		if(checked) {
			reminderText.setEnabled(true);
			reminderText.setInputType(InputType.TYPE_CLASS_NUMBER);
			reminderText.setFocusableInTouchMode(true);
			reminderText.setVisibility(View.VISIBLE);
			reminderText.requestFocus();
		} else {
			reminderText.setEnabled(false);
			reminderText.setInputType(InputType.TYPE_NULL);
			reminderText.setFocusable(false);
			reminderText.setVisibility(View.INVISIBLE);
			reminderText.clearFocus();
		}
	}
	
	/**
	 * Updates the database with a refill.
	 * @param view
	 */
	public void refillMed(View view) {
		int anyInvalid = validateFields();
		
		if(anyInvalid != 0) {
			return;
		}
		
		// If we're here, all fields check out, so we can start the update.
		ContentValues mUpdateValues = new ContentValues();
		
		// Add date filled
		// Step 1: Convert date filled to a unix timestamp.
		EditText dateEdit = (EditText) findViewById(R.id.dateFilledText);
		String mDate = dateEdit.getText().toString();
		long epoch = 0;
		try {
			DateFormat shortdf = DateFormat.getDateInstance(DateFormat.SHORT);
			epoch = shortdf.parse(mDate).getTime()/1000;
			mUpdateValues.put(MedTable.MED_DATE_FILLED, epoch);
		} catch (ParseException e) {
			// It should be largely irrelevant what happens in this block, because the
			// validation routine should ensure that we only ever attempt to parse a
			// valid date, meaning that exceptions should be impossible.
			e.printStackTrace();
		}
		
		
		// Add the duration
		EditText durationEdit = (EditText) findViewById(R.id.durationText);
		int iDuration = Integer.parseInt(durationEdit.getText().toString());
		mUpdateValues.put(MedTable.MED_DURATION, iDuration);
		
		// Add the warning preference. If the box is checked, add whatever value
		// is inside it. If the box is not checked, the default value is 0.
		CheckBox reminderCheck = (CheckBox) findViewById(R.id.reminderCheckBox);
		int iReminder;
		if(reminderCheck.isChecked()) {
			mUpdateValues.put(MedTable.MED_REMINDER_ON, 1);
			EditText reminderEdit = (EditText) findViewById(R.id.reminderText);
			iReminder = Integer.parseInt(reminderEdit.getText().toString());
		} else {
			mUpdateValues.put(MedTable.MED_REMINDER_ON, 0);
			iReminder = 0;
		}
		mUpdateValues.put(MedTable.MED_WARNING, iReminder);
		
		// Create the selection string
		String selection = MedTable.MED_ID + " = " + mMedId;
		
		int rowsUpdated = getContentResolver().update(MedProvider.CONTENT_URI,
				mUpdateValues,
				selection,
				null);
		Log.d("Refill Activity", "Updated " + rowsUpdated + " rows");
		if(reminderCheck.isChecked()) {
			AlarmSetter setter = new AlarmSetter();
			TextView nameView = (TextView) findViewById(R.id.nameView);
			String medName = nameView.getText().toString();
			setter.setRefillAlarm(this, (int) mMedId, medName, epoch, iDuration, iReminder);
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
		
		// CHECK THE DATE
		EditText dateFilled = (EditText) findViewById(R.id.dateFilledText);
		if(!validator.validateDate(dateFilled, "Date Filled")) {
			return 1;
		}
		// CHECK THE DURATION
		EditText durationText = (EditText) findViewById(R.id.durationText);
		if(!validator.validateAlphaNum(durationText, "Duration")) {
			return 2;
		}
		// CHECK THE REFILL WARNING AND DATE
		CheckBox reminderCheckbox = (CheckBox) findViewById(R.id.reminderCheckBox);
		boolean checked = reminderCheckbox.isChecked();
		if(checked) {
			EditText reminderText = (EditText) findViewById(R.id.reminderText);
			if(!validator.validateAlphaNum(reminderText, "Days Notice")) {
				return 3;
			}
		}
		// If we're here, every field has checked out, so we can just return 0.
		return 0;
	}
}

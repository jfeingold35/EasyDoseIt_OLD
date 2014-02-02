package com.gmail.jfeingold35.easydoseit;



import java.text.DateFormat;
import java.text.ParseException;

import com.gmail.jfeingold35.easydoseit.classes.AlarmSetter;
import com.gmail.jfeingold35.easydoseit.classes.FieldValidator;
import com.gmail.jfeingold35.easydoseit.database.MedTable;
import com.gmail.jfeingold35.easydoseit.medprovider.MedProvider;

import com.gmail.jfeingold35.easydoseit.R;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.support.v4.app.NavUtils;
import android.text.InputType;


/**
 * This activity represents an instance of the screen which allows a user
 * to create a new med entry in the database. 
 * @author Josh Feingold
 *
 */
public class NewMedActivity extends Activity {
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_med);
		// Show the Up button in the action bar.
		setupActionBar();
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(true);

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
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * Handles adding the new medication to the database. Called
	 * when the user clicks the Add button.
	 * @param view - the view that was clicked. Will always be the Add button.
	 */
	public void addToDB(View view) {
		// First, we need to make sure that no fields are null
		int anyInvalid = validateFields();
		// If any fields are null, return
		if(anyInvalid != 0) {
			return;
		}
		// If we're here, it's because all fields are valid. We can
		// now proceed to create the content values and add them to
		// the database.
		
		// Define the URI to receive the results of the insertion.
		Uri mNewUri = null;
		// Define a contentValues object to contain the new values
		ContentValues mNewValues = new ContentValues();
		
		// Populate mNewValues
		// Add name
		EditText nameEdit = (EditText) findViewById(R.id.nameText);
		String mMedName = nameEdit.getText().toString();
		mNewValues.put(MedTable.MED_NAME, mMedName);
		
		// Add dosage
		EditText dosageEdit = (EditText) findViewById(R.id.dosageText);
		String mDosage = dosageEdit.getText().toString();
		mNewValues.put(MedTable.MED_DOSAGE, mDosage);
		
		// Add date filled
		// Step 1: Convert date filled to a unix timestamp.
		EditText dateEdit = (EditText) findViewById(R.id.dateFilledText);
		String mDate = dateEdit.getText().toString();
		long epoch = 0;
		try {
			DateFormat shortdf = DateFormat.getDateInstance(DateFormat.SHORT);
			epoch = shortdf.parse(mDate).getTime()/1000;
			mNewValues.put(MedTable.MED_DATE_FILLED, epoch);
		} catch (ParseException e) {
			// It should be largely irrelevant what happens in this block, because the
			// validation routine should ensure that we only ever attempt to parse a
			// valid date, meaning that exceptions should be impossible.
			e.printStackTrace();
		}
		
		
		// Add the duration
		EditText durationEdit = (EditText) findViewById(R.id.durationText);
		int iDuration = Integer.parseInt(durationEdit.getText().toString());
		mNewValues.put(MedTable.MED_DURATION, iDuration);
		
		// Add the warning preference. If the box is checked, add whatever value
		// is inside it. If the box is not checked, the default value is 0.
		CheckBox reminderCheck = (CheckBox) findViewById(R.id.reminderCheckBox);
		int iReminder;
		if(reminderCheck.isChecked()) {
			mNewValues.put(MedTable.MED_REMINDER_ON, 1);
			EditText reminderEdit = (EditText) findViewById(R.id.reminderText);
			iReminder = Integer.parseInt(reminderEdit.getText().toString());
		} else {
			mNewValues.put(MedTable.MED_REMINDER_ON, 0);
			iReminder = 0;
		}
		mNewValues.put(MedTable.MED_WARNING, iReminder);
		
		// Insert new med
		mNewUri = getContentResolver().insert(
				MedProvider.CONTENT_URI, mNewValues);
		// Here we need to start the alarm for the refill reminder. To give each alarm a unique ID,
		// the return code for the alarm's intent shall be the ID number corresponding to that
		// med in the med table. In order to prevent clashes with alarms set by the Daily Alarms table,
		// all return codes for refills will be positive, and all return codes for daily alarms
		// will be negative.
		if(reminderCheck.isChecked()) {
			AlarmSetter setter = new AlarmSetter();
			int returnCode = (int) ContentUris.parseId(mNewUri);
			setter.setRefillAlarm(this, returnCode, mMedName, epoch, iDuration, iReminder);
		}
		// Return to the MedList Activity
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
		EditText medNameText = (EditText) findViewById(R.id.nameText);
		if(!validator.validateAlphaNum(medNameText, "Name")) {
			return 1;
		}
		// CHECK THE DOSAGE
		EditText medDosageText = (EditText) findViewById(R.id.dosageText);
		if(!validator.validateAlphaNum(medDosageText, "Dosage")) {
			return 2;
		}
		// CHECK DATE FILLED
		EditText medDateFilledText = (EditText) findViewById(R.id.dateFilledText);
		if(!validator.validateDate(medDateFilledText, "Date Filled")) {
			return 3;
		}
		// CHECK DURATION
		EditText medDurationText = (EditText) findViewById(R.id.durationText);
		if(!validator.validateAlphaNum(medDurationText, "Duration")) {
			return 4;
		}
		// CHECK REFILL WARNING AND DATE
		CheckBox reminderBox = (CheckBox) findViewById(R.id.reminderCheckBox);
		boolean checked = reminderBox.isChecked();
		if(checked) {
			EditText reminderText = (EditText) findViewById(R.id.reminderText);
			if(!validator.validateAlphaNum(reminderText, "Days Notice")) {
				return 5;
			}
		}
		// If we're here, it's because all fields have checked out,
		// so we can just return 0 and be done with it.
		return 0;
	}
	
	/**
	 * Enables the EditText that allows the user to enter how many days
	 * in advance they want to be warned to refill their prescription.
	 * @param view - the clicked view. Always the warning checkbox.
	 */
	public void reminderCheckboxClicked(View view) {
		boolean checked = ((CheckBox) view).isChecked();
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
}
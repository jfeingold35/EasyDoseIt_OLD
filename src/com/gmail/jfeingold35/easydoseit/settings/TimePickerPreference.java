package com.gmail.jfeingold35.easydoseit.settings;

import com.gmail.jfeingold35.easydoseit.R;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TimePicker;

public class TimePickerPreference extends DialogPreference {

	/**
	 * The widget for picking a time.
	 */
	private TimePicker timePicker;
	
	/**
	 * Creates a preference for choosing a time based on its XML declaration.
	 * @param context
	 * @param attrs
	 */
	public TimePickerPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		setDialogLayoutResource(R.layout.dialog_time_picker);
		setPositiveButtonText(android.R.string.ok);
		setNegativeButtonText(android.R.string.cancel);
	}
	
	/**
	 * Initializes the TimePicker to the currently stored time preferences.
	 * The time preference is stored as an int. For example, 12:30 AM would be
	 * stored as 30, and parsed into an hour and minute using division by 100,
	 * with the hour as the quotient and the minute as the remainder.
	 * @param view - the dialog preference's host view.
	 */
	@Override
	public void onBindDialogView(View view) {
		timePicker = (TimePicker)view.findViewById(R.id.timePicker);
		timePicker.setIs24HourView(false);
		int currentTime = getSharedPreferences().getInt(getKey(), 1200);
		int currentHour = currentTime / 100;
		int currentMinute = currentTime % 100;
		timePicker.setCurrentHour(currentHour);
		timePicker.setCurrentMinute(currentMinute);
	}
	
	/**
	 * Handles the closing of the dialog. If the user intended to save the setting,
	 * the selected hour and minute are stored in the preferences by multiplying the
	 * hour by 100 and adding it to the minute. For example, 3:15 AM gets saved
	 * as 315.
	 * @param positiveResult - is true if the user clicked the positive button.
	 */
	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);
		if(positiveResult) {
			timePicker.clearFocus();
			// Get the time 
			int currentHour = timePicker.getCurrentHour();
			int currentMinute = timePicker.getCurrentMinute();
			int currentTime = (currentHour * 100) + currentMinute;
			persistInt(currentTime);
		}
	}
	
	@Override
	protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
		Log.d("TPP", "setting initial value");
		Log.d("TPP", "default is " + (Integer) defaultValue);
		if(restorePersistedValue) { // If we're restoring a persisted value
			Log.d("TPP", "restoring");
			int time = this.getPersistedInt(1200);
			int hour = time / 100;
			int minute = time % 100;
			String hourString;
			String minuteString = Integer.toString(minute);
			String amOrPm;
			if(hour < 12) {
				hourString = Integer.toString(hour + 12);
				amOrPm = " AM";
			} else if(hour == 12) {
				hourString = Integer.toString(hour);
				amOrPm = " PM";
			} else {
				hourString = Integer.toString(hour - 12);
				amOrPm = " PM";
			}
			String timeString = hourString + ":" + minuteString + amOrPm;
			this.setSummary(timeString);
		} else { // If not
			Log.d("TPP", "not restoring");
			this.setSummary("12:00 PM");
			this.persistInt(1200);
		}
	}
}

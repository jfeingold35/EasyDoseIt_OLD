package com.gmail.jfeingold35.easydoseit.dialogs;


import com.gmail.jfeingold35.easydoseit.R;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TimePicker;

public class AlarmCreationDialogFragment extends DialogFragment {

	/**
	 * Listener that notifies when the time has been set.
	 */
	TimePickedListener mListener;
	
	public static interface TimePickedListener {
		public void onTimePicked(int hourOfDay, int minute);
	}
	
	/**
	 * NOTE: This class is a bit of a hack. I can't use the TimePickerDialog class,
	 * because of a bug that Google hasn't seen fit to correct in which the OnTimeSet
	 * function is called even when the dialog is canceled. As such, I've had to make my own
	 * class that uses a custom layout. What the hell, Google?
	 */
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		// Get the inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();
		
		// Inflate and set the layout for this dialog
		View view = inflater.inflate(R.layout.dialog_alarm_creation, null);
		builder.setView(view);
		
		final TimePicker picker = (TimePicker) view.findViewById(R.id.alarmPicker);
		picker.setCurrentHour(0);
		picker.setCurrentMinute(0);
		builder.setTitle(R.string.time_picker_title);
		// Add the action buttons
		builder.setPositiveButton(android.R.string.ok, 
				new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						int hour = picker.getCurrentHour();
						int minute = picker.getCurrentMinute();
						Log.d("MedManager", "Got values");
						mListener.onTimePicked(hour, minute);
					}
				});
		builder.setNegativeButton(android.R.string.cancel,
				new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
		return builder.create();
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		// Verify that the activity implements TimePickedListener
		try {
			// Instantiate the listener so we can send events back to the host
			mListener = (TimePickedListener) activity;
		} catch (ClassCastException e) {
			// The activity doesn't implement the listener, throw an exception
			throw new ClassCastException(activity.toString() 
					 + " must implement TimePickedListener");
		}
	}
}

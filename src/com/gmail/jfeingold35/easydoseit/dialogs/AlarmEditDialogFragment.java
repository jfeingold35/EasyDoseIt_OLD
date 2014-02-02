package com.gmail.jfeingold35.easydoseit.dialogs;

import com.gmail.jfeingold35.easydoseit.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TimePicker;

public class AlarmEditDialogFragment extends DialogFragment {

	/**
	 * Listener that notifies when the time has been set.
	 */
	TimeEditListener mListener;
	public static final String ALARM_ID = "alarm_id";
	public static final String ALARM_ISLOUD = "alarm_isloud";
	public static final String ALARM_ISACTIVE = "alarm_isactive";
	public static final String ALARM_MEDNUM = "alarm_mednum";
	/**
	 * The ID of the alarm, passed from the cursor adapter.
	 */
	private int mId = 0;
	
	/**
	 * True if the alarm should be loud. Passed from the cursor adapter
	 * because it's needed when we reset the alarm. Hacky, but kinda necessary.
	 */
	private boolean mIsLoud = false;
	
	/**
	 * True if the alarm should be activated after being edited. Passed from the cursor
	 * adapter because it's easier than querying again. Hacky, but simple.
	 */
	private boolean mIsActive = false;
	
	/**
	 * The med number corresponding to the alarm. Must be passed to onTimeEdit to reset
	 * the alarm.
	 */
	private long mMedNum = 0;
	
	public static interface TimeEditListener {
		public void onTimeEdit(int id, int hourOfDay, int minute, long medId,
				boolean isLoud, boolean isActive);
	}
	
    public static AlarmEditDialogFragment newInstance(int id, boolean isLoud,
    		boolean isActive, long medId) {
        AlarmEditDialogFragment frag = new AlarmEditDialogFragment();
        Bundle args = new Bundle();
        Log.d("AEDF", "new Instance id " + id);
        args.putInt(ALARM_ID, id);
        args.putBoolean(ALARM_ISLOUD, isLoud);
        args.putBoolean(ALARM_ISACTIVE, isActive);
        args.putLong(ALARM_MEDNUM, medId);
        frag.setArguments(args);
        return frag;
    }

	/**
	 * NOTE: This class is a bit of a hack. I can't use the TimePickerDialog class,
	 * because of a bug that Google hasn't seen fit to correct in which the OnTimeSet
	 * function is called even when the dialog is canceled. As such, I've had to make my own
	 * class that uses a custom layout. What the hell, Google?
	 */
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		mId = getArguments().getInt(ALARM_ID);
		mIsLoud = getArguments().getBoolean(ALARM_ISLOUD);
		mIsActive = getArguments().getBoolean(ALARM_ISACTIVE);
		mMedNum = getArguments().getLong(ALARM_MEDNUM);
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
						mListener.onTimeEdit(mId, hour, minute, mMedNum, mIsLoud, mIsActive);
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
			mListener = (TimeEditListener) activity;
		} catch (ClassCastException e) {
			// The activity doesn't implement the listener, throw an exception
			throw new ClassCastException(activity.toString() 
					 + " must implement TimeEditListener");
		}
	}
}

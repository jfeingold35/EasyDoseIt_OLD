package com.gmail.jfeingold35.easydoseit.classes;

import java.util.ArrayList;

import com.gmail.jfeingold35.easydoseit.R;
import com.gmail.jfeingold35.easydoseit.alarmprovider.AlarmProvider;
import com.gmail.jfeingold35.easydoseit.database.DailyAlarmTable;
import com.gmail.jfeingold35.easydoseit.dialogs.AlarmEditDialogFragment;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.ToggleButton;

public class AlarmCursorAdapter extends SimpleCursorAdapter {

	private Context mContext;
	private Activity mActivity;
	private int mLayout;
	private Cursor mCursor;
	private int mIdIndex;
	private int mAlarmIndex;
	private int mIsActiveIndex;
	private int mTimestampIndex;
	private int mMedNumIndex;
	private int mIsLoudIndex;
	private LayoutInflater mInflater;
	/**
	 * An array that stores the status of each row's active toggle button.
	 */
	private ArrayList<Boolean> isActiveArray = new ArrayList<Boolean>();
	
	/**
	 * An array that stores the status of each row's volume toggle button.
	 */
	private ArrayList<Boolean> isLoudArray = new ArrayList<Boolean>();
	
	public AlarmCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);
		Log.d("alarmAdapter", "calling constructor");
		this.mContext = context;
		this.mActivity = (Activity) context;
		this.mLayout = layout;
		this.mCursor = c;
		this.mIdIndex = c.getColumnIndexOrThrow(DailyAlarmTable.ALARM_ID);
		this.mAlarmIndex = c.getColumnIndexOrThrow(DailyAlarmTable.ALARM_TIME);
		this.mIsActiveIndex = c.getColumnIndexOrThrow(DailyAlarmTable.ALARM_ISACTIVE);
		this.mTimestampIndex = c.getColumnIndex(DailyAlarmTable.ALARM_TIMESTAMP);
		this.mMedNumIndex = c.getColumnIndexOrThrow(DailyAlarmTable.ALARM_MEDNUM);
		this.mIsLoudIndex = c.getColumnIndexOrThrow(DailyAlarmTable.ALARM_ISLOUD);
		this.mInflater = LayoutInflater.from(mContext);
		
		// Initialize the arrays to false
		for(int i = 0; i < this.getCount(); i++) {
			isActiveArray.add(i, false);
			isLoudArray.add(i, false);
		}
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(mCursor.moveToPosition(position)) {
			ViewHolder holder;
			final int fPosition = position;
			// If the view isn't inflated, we need to create it
			if(convertView == null) {
				convertView = mInflater.inflate(mLayout, null);
				
				// Creating and populating the holder
				holder = new ViewHolder();
				
				holder.alarmView = (TextView) convertView.findViewById(R.id.alarmView);
				holder.discardButton = (ImageButton) convertView.findViewById(R.id.alarmDiscard);
				holder.isActiveCheck = (CheckBox) convertView.findViewById(R.id.alarmCheck);
				holder.editButton = (ImageButton) convertView.findViewById(R.id.alarmEdit);
				holder.isLoudToggle = (ToggleButton) convertView.findViewById(R.id.ringtoneToggle);
				
				convertView.setTag(holder);
			} else { // If the view is already inflated, we need to retrieve the holder
				holder = (ViewHolder) convertView.getTag();
			}
			
			// Populate the views
			final String alarmString = mCursor.getString(mAlarmIndex);
			final int isActive = mCursor.getInt(mIsActiveIndex);
			final int isLoud = mCursor.getInt(mIsLoudIndex);
			final int id = mCursor.getInt(mIdIndex);
			final long timeStamp = mCursor.getLong(mTimestampIndex);
			final int medNum = mCursor.getInt(mMedNumIndex);
			
			// Populate the listview
			holder.alarmView.setText(alarmString);
			
			// Set the value of the isActive toggle, and its OnCheckedChangeListener
			if(isActive == 1) {
				isActiveArray.set(fPosition, true);
			} else {
				isActiveArray.set(fPosition, false);
			}
			holder.isActiveCheck.setOnCheckedChangeListener(null);
			holder.isActiveCheck.setChecked(isActiveArray.get(fPosition));
			holder.isActiveCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					if(isChecked) {
						// Change the array to the correct value
						isActiveArray.set(fPosition, true);
						
						// Turn on the alarm
						AlarmSetter alarmSetter = new AlarmSetter();
						// Convert the old timestamp to one that has yet to occur.
						long newStamp = alarmSetter.updateTimestamp(timeStamp);
						boolean loudBool = isLoudArray.get(fPosition);
						alarmSetter.setDailyAlarm(mContext, medNum, id, alarmString, newStamp, loudBool);
						
						// Change the avlue of ALARM_ISACTIVE in the cursor to 1.
						ContentValues newValues = new ContentValues();
						newValues.put(DailyAlarmTable.ALARM_ISACTIVE, 1);
						String selection = "(" + DailyAlarmTable.ALARM_ID + " = " + id + ")";
						int rowsUpdated = 0;
						rowsUpdated = mContext.getContentResolver().update(AlarmProvider.CONTENT_URI,
								newValues, selection, null);
						Log.d("SCA", rowsUpdated + " rows updated.");
					} else {
						isActiveArray.set(fPosition, false);
						
						// Turn off the alarm
						AlarmSetter alarmSetter = new AlarmSetter();
						alarmSetter.cancelDailyAlarm(mContext, id);
						
						// Change the value of ALARM_ISACTIVE in the cursor to 0
						ContentValues newValues = new ContentValues();
						newValues.put(DailyAlarmTable.ALARM_ISACTIVE, 0);
						String selection = "(" + DailyAlarmTable.ALARM_ID + " = " + id + ")";
						int rowsUpdated = 0;
						rowsUpdated = mContext.getContentResolver().update(AlarmProvider.CONTENT_URI,
								newValues, selection, null);
						Log.d("SCA", rowsUpdated + " rows updated");
						
					}
				}
			});
			// Set the value of the isLoud toggle, and its OnCheckedListener
			if(isLoud == 1) {
				isLoudArray.set(fPosition, true);
			} else {
				isLoudArray.set(fPosition, false);
			}
			holder.isLoudToggle.setOnCheckedChangeListener(null);
			holder.isLoudToggle.setChecked(isLoudArray.get(fPosition));
			holder.isLoudToggle.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton button, boolean isChecked) {
					if(isChecked) {
						// Change the array to the correct value
						isLoudArray.set(fPosition, true);
						
						// Change the value of ALARM_ISLOUD to 1
						ContentValues newValues = new ContentValues();
						newValues.put(DailyAlarmTable.ALARM_ISLOUD, 1);
						String selection = "(" + DailyAlarmTable.ALARM_ID + " = " + id + ")";
						int rowsUpdated = 0;
						rowsUpdated = mContext.getContentResolver().update(AlarmProvider.CONTENT_URI, 
								newValues, selection, null);
						Log.d("SCA", rowsUpdated + " rows updated");
						
						// Make sure we should reset the alarm. If isActiveArray is false here,
						// we shouldn't reset the alarm.
						if(!isActiveArray.get(fPosition)) {
							return;
						}
						
						// Reset the alarm so that it rings.
						AlarmSetter alarmSetter = new AlarmSetter();
						// Convert the old timestamp into one that has yet to occur.
						long newStamp = alarmSetter.updateTimestamp(timeStamp);
						alarmSetter.setDailyAlarm(mContext, medNum, id, alarmString, newStamp, true);
					} else {
						// Change the array to the correct value
						isLoudArray.set(fPosition, false);
						
						// Change the value of ALARM_ISLOUD to 0
						ContentValues newValues = new ContentValues();
						newValues.put(DailyAlarmTable.ALARM_ISLOUD, 0);
						String selection = "(" + DailyAlarmTable.ALARM_ID + " = " + id + ")";
						int rowsUpdated = 0;
						rowsUpdated = mContext.getContentResolver().update(AlarmProvider.CONTENT_URI,
								newValues, selection, null);
						Log.d("SCA", rowsUpdated + " rows updated");
						
						// Make sure we should reset the alarm. If isActiveArray is false here,
						// we shouldn't reset the alarm.
						if(!isActiveArray.get(fPosition)) {
							return;
						}
						
						// Reset the alarm so that it's silent.
						AlarmSetter alarmSetter = new AlarmSetter();
						// Convert the old timestamp into one that has yet to occur.
						long newStamp = alarmSetter.updateTimestamp(timeStamp);
						alarmSetter.setDailyAlarm(mContext, medNum, id, alarmString, newStamp, false);
					}
				}	
			});
			// Set up the OnClickListener for the discard ImageButton
			holder.discardButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// Turn off the alarm before deleting it!
					AlarmSetter alarmSetter = new AlarmSetter();
					alarmSetter.cancelDailyAlarm(mContext, id);
					
					// Delete the alarm from the database
					String selection = "(" + DailyAlarmTable.ALARM_ID + " = " + id + ")";
					int rowsDeleted = mContext.getContentResolver().delete(AlarmProvider.CONTENT_URI,
							selection,
							null);
					Log.d("SCA", rowsDeleted + " rows deleted");
				}
				
			});
			// Set up the OnClickListener for the edit ImageButton
			holder.editButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					FragmentManager fm = mActivity.getFragmentManager();
					Log.d("ACA", "ACA id " + id);
					boolean bIsLoud = isLoud == 1;
					boolean bIsActive = isActive == 1;
					DialogFragment frag = AlarmEditDialogFragment.newInstance(id, bIsLoud,
							bIsActive, medNum);
					frag.show(fm, "alarmEdit");
				}
				
			});
		}
		return convertView;
	}
	
	static class ViewHolder {
		CheckBox isActiveCheck;
		ToggleButton isLoudToggle;
		TextView alarmView;
		ImageButton editButton;
		ImageButton discardButton;
	}
}

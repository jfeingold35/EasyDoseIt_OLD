package com.gmail.jfeingold35.easydoseit.services;

import com.gmail.jfeingold35.easydoseit.alarmprovider.AlarmProvider;
import com.gmail.jfeingold35.easydoseit.classes.AlarmSetter;
import com.gmail.jfeingold35.easydoseit.database.DailyAlarmTable;
import com.gmail.jfeingold35.easydoseit.database.MedTable;
import com.gmail.jfeingold35.easydoseit.medprovider.MedProvider;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;

/**
 * This service is initiated by the BootReceiver when the phone is rebooted. Its
 * purpose is to iterate over the med database and the daily alarm database
 * and set up the various alarms, allowing them to survive a reboot.
 * @author Josh Feingold
 *
 */
public class AlarmSetupService extends IntentService {

	/** 
	 * Constructor
	 * @param name
	 */
	public AlarmSetupService() {
		super("AlarmSetupService");
	}

	/**
	 * Calls setUpRefills, which sets up the refill alarms,
	 * and setUpDailyAlarms, which sets up the daily alarms.
	 * Pretty self explanatory, really.
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		setUpRefills();
		setUpDailyAlarms();
	}
	
	public void setUpRefills() {
		// Get a cursor with the data from the MedTable
		String[] medProjection = { MedTable.MED_ID,
				MedTable.MED_NAME, MedTable.MED_DATE_FILLED,
				MedTable.MED_DURATION, MedTable.MED_WARNING,
				MedTable.MED_REMINDER_ON };
		String medSelection = "((" + MedTable.MED_ID + " NOTNULL) AND ("
				+ MedTable.MED_NAME + " != '') AND ("
				+ MedTable.MED_REMINDER_ON + " != 0))";
		Cursor medCursor = getContentResolver().query(MedProvider.CONTENT_URI,
				medProjection,
				medSelection,
				null,
				MedTable.MED_NAME + " COLLATE LOCALIZED ASC");
		int idIndex = medCursor.getColumnIndexOrThrow(MedTable.MED_ID);
		int nameIndex = medCursor.getColumnIndexOrThrow(MedTable.MED_NAME);
		int dateFilledIndex = medCursor.getColumnIndexOrThrow(MedTable.MED_DATE_FILLED);
		int durationIndex = medCursor.getColumnIndexOrThrow(MedTable.MED_DURATION);
		int reminderIndex = medCursor.getColumnIndexOrThrow(MedTable.MED_WARNING);
		if(medCursor != null) {
			AlarmSetter alarmSetter = new AlarmSetter();
			// Moves cursor to next row
			while(medCursor.moveToNext()) {
				
				int medId = medCursor.getInt(idIndex);
				String medName = medCursor.getString(nameIndex);
				long timestamp = medCursor.getLong(dateFilledIndex);
				int duration = medCursor.getInt(durationIndex);
				int reminder = medCursor.getInt(reminderIndex);
				
				alarmSetter.setRefillAlarm(getApplicationContext(), medId, medName, timestamp, duration, reminder);
			}
			// End of while loop
		}
	}
	
	public void setUpDailyAlarms() {
		// Get a cursor with the data from the AlarmTable
		String[] alarmProjection = { DailyAlarmTable.ALARM_ID,
				DailyAlarmTable.ALARM_MEDNUM, DailyAlarmTable.ALARM_TIME,
				DailyAlarmTable.ALARM_TIMESTAMP, DailyAlarmTable.ALARM_ISACTIVE,
				DailyAlarmTable.ALARM_ISLOUD };
		String alarmSelection = "((" + DailyAlarmTable.ALARM_ID + " NOTNULL) AND ("
				+ DailyAlarmTable.ALARM_ISACTIVE + " != 0))";
		Cursor alarmCursor = getContentResolver().query(AlarmProvider.CONTENT_URI,
				alarmProjection,
				alarmSelection,
				null,
				DailyAlarmTable.ALARM_TIMESTAMP + " COLLATE LOCALIZED ASC");
		int idIndex = alarmCursor.getColumnIndexOrThrow(DailyAlarmTable.ALARM_ID);
		int medNumIndex = alarmCursor.getColumnIndexOrThrow(DailyAlarmTable.ALARM_MEDNUM);
		int timeIndex = alarmCursor.getColumnIndexOrThrow(DailyAlarmTable.ALARM_TIME);
		int timestampIndex = alarmCursor.getColumnIndexOrThrow(DailyAlarmTable.ALARM_TIMESTAMP);
		int isLoudIndex = alarmCursor.getColumnIndexOrThrow(DailyAlarmTable.ALARM_ISLOUD);
		if(alarmCursor != null) {
			AlarmSetter alarmSetter = new AlarmSetter();
			// Moves cursor to next row
			while(alarmCursor.moveToNext()) {
				int alarmId = alarmCursor.getInt(idIndex);
				long medNum = alarmCursor.getLong(medNumIndex);
				String timeString = alarmCursor.getString(timeIndex);
				long timestamp = alarmCursor.getLong(timestampIndex);
				long trueTimeStamp = alarmSetter.updateTimestamp(timestamp);
				int isLoudInt = alarmCursor.getInt(isLoudIndex);
				boolean isLoud = isLoudInt == 1;
				alarmSetter.setDailyAlarm(getApplicationContext(), medNum,
						alarmId, timeString, trueTimeStamp, isLoud);
			}
			// End of while loop
		}
	}	
}

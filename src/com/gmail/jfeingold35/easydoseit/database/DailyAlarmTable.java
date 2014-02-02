package com.gmail.jfeingold35.easydoseit.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DailyAlarmTable {
	// Database table
	public static final String TABLE_ALARM = "alarm";
	
	/**
	 * ID is the unique number assigned to each alarm.
	 */
	public static final String ALARM_ID = "_id";
	
	/**
	 * MedNum corresponds to the ID of the med for which the alarm is a reminder.
	 */
	public static final String ALARM_MEDNUM = "med_num";
	
	/**
	 * Timestamp is the UNIX timestamp corresponding to the original alarm time.
	 * i.e., if the user sets an alarm on 1/1/2014 at 5 AM, then this field would
	 * be the timestamp for that date and time.
	 */
	public static final String ALARM_TIMESTAMP = "timestamp";
	
	/**
	 * Time is the time that the alarm will go off daily, expressed as a string
	 * of the format "HH:MM AM/PM". This is the value that will be displayed
	 * to the user.
	 */
	public static final String ALARM_TIME = "time";
	
	/**
	 * IsActive is a bit value. If IsOn == 1, the alarm is active. If IsOn == 0, the
	 * alarm is not active.
	 */
	public static final String ALARM_ISACTIVE = "is_active";
	
	/**
	 * IsLoud is a bit value. If IsLoud == 1, the alarm will ring. If IsLoud == 0, the
	 * alarm will vibrate only.
	 */
	public static final String ALARM_ISLOUD = "is_loud";
	// Table creation  SQL statement
	private static final String DATABASE_CREATE = "create table if not exists "
			+ TABLE_ALARM
			+ "("
			+ ALARM_ID + " integer primary key autoincrement, "
			+ ALARM_MEDNUM + " integer not null, " 
			+ ALARM_TIMESTAMP + " integer not null, "
			+ ALARM_TIME + " text not null, "
			+ ALARM_ISACTIVE + " bit not null, "
			+ ALARM_ISLOUD + " bit not null"
			+ ");";
	
	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
	}
	
	public static void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
		Log.w(MedTable.class.getName(), "Upgrading database from version "
				+ oldVersion + " to " + newVersion
				+ ", which will destroy all old data");
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_ALARM);
		onCreate(database);
	}
}

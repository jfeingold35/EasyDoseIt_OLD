package com.gmail.jfeingold35.easydoseit.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class MedTable {
	// Database table
	public static final String TABLE_MED = "med";
	// ID is a number unique to each med
	public static final String MED_ID = "_id";
	// The name of the med
	public static final String MED_NAME = "name";
	// The dosage of the med.
	public static final String MED_DOSAGE = "dosage";
	// Date filled, stored as a unix timestamp for midnight, local-time
	// on the day that the prescription was filled. This allows for easier
	// storage, and easier calculation of when to warn the user to get a refill.
	public static final String MED_DATE_FILLED = "date_filled";
	// A bit value, 0 if the user doesn't want to be reminded, and 1 if they do.
	public static final String MED_REMINDER_ON = "reminder_on";
	// Length of the prescription, in days
	public static final String MED_DURATION = "duration";
	// The number of days notice that the user wants.
	// Defaults to 0
	public static final String MED_WARNING = "warning";
	
	
	// Database creation SQL statement
	private static final String DATABASE_CREATE = "create table if not exists "
		+ TABLE_MED
		+ "("
		+ MED_ID + " integer primary key autoincrement, "
		+ MED_NAME + " text not null, "
		+ MED_DOSAGE + " text not null, "
		+ MED_REMINDER_ON + " bit not null, "
		+ MED_DATE_FILLED + " integer not null, "
		+ MED_DURATION + " integer not null, "
		+ MED_WARNING + " integer not null"
		+ ");";
	
	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
	}
	
	public static void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
		Log.w(MedTable.class.getName(), "Upgrading database from version "
				+ oldVersion + " to " + newVersion
				+ ", which will destroy all old data");
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_MED);
		onCreate(database);
	}
}
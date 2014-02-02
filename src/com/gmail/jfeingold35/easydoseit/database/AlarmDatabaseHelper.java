package com.gmail.jfeingold35.easydoseit.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AlarmDatabaseHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "alarmtable.db";
	private static final int DATABASE_VERSION = 6;
	
	public AlarmDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	// This method is called during the creation of the database	
	@Override
	public void onCreate(SQLiteDatabase database) {
		DailyAlarmTable.onCreate(database);
	}
	
	// Method called during an upgrade of the database,
	// e.g. if you increase the database version
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion,
			int newVersion) {
		DailyAlarmTable.onUpgrade(db, oldVersion, newVersion);
	}
}

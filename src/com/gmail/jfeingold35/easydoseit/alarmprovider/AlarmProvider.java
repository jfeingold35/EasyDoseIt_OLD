package com.gmail.jfeingold35.easydoseit.alarmprovider;

import java.util.Arrays;
import java.util.HashSet;

import com.gmail.jfeingold35.easydoseit.database.AlarmDatabaseHelper;
import com.gmail.jfeingold35.easydoseit.database.DailyAlarmTable;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class AlarmProvider extends ContentProvider {
	// Database
	private AlarmDatabaseHelper database;
	
	// Used for the UriMatcher
	private static final int ALARMS = 10;
	private static final int ALARM_ID = 20;
	
	private static final String AUTHORITY = "com.gmail.jfeingold35.easydoseit.alarmprovider";

	private static final String BASE_PATH = "alarms";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY 
			+ "/" + BASE_PATH);
	
	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/alarms";
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
			+ "/alarm";
	
	private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		sUriMatcher.addURI(AUTHORITY, BASE_PATH, ALARMS);
		sUriMatcher.addURI(AUTHORITY, BASE_PATH + "/#", ALARM_ID);
	}
	
	@Override
	public boolean onCreate() {
		database = new AlarmDatabaseHelper(getContext());
		return false;
	}
	
	/**
	 * Perform a query from the alarm database
	 */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// Using SQLiteQueryBuilder instead of the query() method
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		
		// Check if the caller requested a column which doesn't exist
		checkColumns(projection);
		
		// Set the table
		queryBuilder.setTables(DailyAlarmTable.TABLE_ALARM);
		
		int uriType = sUriMatcher.match(uri);
		switch(uriType) {
		case ALARMS:
			break;
		case ALARM_ID:
			// Adding the ID to the original query
			queryBuilder.appendWhere(DailyAlarmTable.ALARM_ID + "="
					+ uri.getLastPathSegment());
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		SQLiteDatabase db = database.getWritableDatabase();
		Cursor cursor = queryBuilder.query(db, projection, selection,
				selectionArgs, null, null, sortOrder);
		// Make sure that potential listeners are getting notified
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}
	
	/**
	 * Delete from the alarm database
	 */
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int uriType = sUriMatcher.match(uri);
		SQLiteDatabase db = database.getWritableDatabase();
		int rowsDeleted = 0;
		switch(uriType) {
		case ALARMS:
			rowsDeleted = db.delete(DailyAlarmTable.TABLE_ALARM, selection,
					selectionArgs);
			break;
		case ALARM_ID:
			String id = uri.getLastPathSegment();
			if(TextUtils.isEmpty(selection)) {
				rowsDeleted = db.delete(DailyAlarmTable.TABLE_ALARM, 
						DailyAlarmTable.ALARM_ID + "=" + id, null);
			} else {
				rowsDeleted = db.delete(DailyAlarmTable.TABLE_ALARM, 
						DailyAlarmTable.ALARM_ID + "=" + id + " and " + selection,
						selectionArgs);
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsDeleted;
	}
	
	@Override
	public String getType(Uri uri) {
		return null;
	}
	
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int uriType = sUriMatcher.match(uri);
		SQLiteDatabase db = database.getWritableDatabase();
		long id = 0;
		switch(uriType) {
		case ALARMS:
			id = db.insert(DailyAlarmTable.TABLE_ALARM, null, values);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return Uri.parse(BASE_PATH + "/" + id);
	}
	
	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int uriType = sUriMatcher.match(uri);
		SQLiteDatabase db = database.getWritableDatabase();
		int rowsUpdated = 0;
		switch(uriType) {
		case ALARMS:
			rowsUpdated = db.update(DailyAlarmTable.TABLE_ALARM,
					values,
					selection,
					selectionArgs);
			break;
		case ALARM_ID:
			Log.d("AlarmProvider", "alarm id");
			String id = uri.getLastPathSegment();
			if(TextUtils.isEmpty(selection)) {
				rowsUpdated = db.update(DailyAlarmTable.TABLE_ALARM,
						values,
						DailyAlarmTable.ALARM_ID + "=" + id,
						null);
			} else {
				rowsUpdated = db.update(DailyAlarmTable.TABLE_ALARM,
						values,
						DailyAlarmTable.ALARM_ID + "=" + id + " and " + selection,
						selectionArgs);
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsUpdated;
	}
	
	/**
	 * Confirms that the columns the user requested exist.
	 * @param projection
	 */
	public void checkColumns(String[] projection) {
		String[] available = { DailyAlarmTable.ALARM_ID,
				DailyAlarmTable.ALARM_MEDNUM,
				DailyAlarmTable.ALARM_TIMESTAMP,
				DailyAlarmTable.ALARM_TIME,
				DailyAlarmTable.ALARM_ISACTIVE,
				DailyAlarmTable.ALARM_ISLOUD };
		if(projection != null) {
			HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
			HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));
			// Check if all columns which are requested are available
			if(!availableColumns.containsAll(requestedColumns)) {
				throw new IllegalArgumentException("Unknown columsn in projection");
			}
		}
	}
}

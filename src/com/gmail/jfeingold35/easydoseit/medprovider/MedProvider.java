package com.gmail.jfeingold35.easydoseit.medprovider;

import java.util.Arrays;
import java.util.HashSet;

import com.gmail.jfeingold35.easydoseit.database.MedDatabaseHelper;
import com.gmail.jfeingold35.easydoseit.database.MedTable;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class MedProvider extends ContentProvider {
	// Database
	private MedDatabaseHelper database;
	
	// Used for the URImatcher
	private static final int MEDS = 10;
	private static final int MED_ID = 20;
	
	private static final String AUTHORITY = "com.gmail.jfeingold35.easydoseit.medprovider";
	
	private static final String BASE_PATH = "medications";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + BASE_PATH);
	
	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/meds";
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
			+ "/med";
	
	private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		sURIMatcher.addURI(AUTHORITY, BASE_PATH, MEDS);
		sURIMatcher.addURI(AUTHORITY, BASE_PATH+ "/#", MED_ID);
	}

	@Override
	public boolean onCreate() {
		database = new MedDatabaseHelper(getContext());
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// Using SQLiteQueryBuilder instead of the query() method
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		
		// Check if the caller has requested a column which doesn't exist
		checkColumns(projection);
		
		// Set the table
		queryBuilder.setTables(MedTable.TABLE_MED);
		
		int uriType = sURIMatcher.match(uri);
		switch(uriType) {
		case MEDS:
			break;
		case MED_ID:
			// Adding the ID to the original query
			queryBuilder.appendWhere(MedTable.MED_ID + "="
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

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		int rowsDeleted = 0;
		switch (uriType) {
		case MEDS:
			rowsDeleted = sqlDB.delete(MedTable.TABLE_MED, selection,
					selectionArgs);
			break;
		case MED_ID:
			String id = uri.getLastPathSegment();
			if(TextUtils.isEmpty(selection)) {
				rowsDeleted = sqlDB.delete(MedTable.TABLE_MED, 
						MedTable.MED_ID + "=" + id, 
						null);
			} else {
				rowsDeleted = sqlDB.delete(MedTable.TABLE_MED, 
						MedTable.MED_ID + "=" + id + " and " + selection,
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
	public String getType(Uri arg0) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		long id = 0;
		switch(uriType) {
		case MEDS:
			id = sqlDB.insert(MedTable.TABLE_MED, null, values);
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
		
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		int rowsUpdated = 0;
		switch(uriType) {
		case MEDS:
			rowsUpdated = sqlDB.update(MedTable.TABLE_MED,
					values,
					selection,
					selectionArgs);
			break;
		case MED_ID:
			String id = uri.getLastPathSegment();
			if(TextUtils.isEmpty(selection)) {
				rowsUpdated = sqlDB.update(MedTable.TABLE_MED,
						values,
						MedTable.MED_ID + "=" + id,
						null);
			} else {
				rowsUpdated = sqlDB.update(MedTable.TABLE_MED,
						values,
						MedTable.MED_ID + "=" + id
						+ " and "
						+ selection,
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
	 * Makes sure that all of the columns the user requested exist
	 * @param projection
	 */
	private void checkColumns(String[] projection) {
		String[] available = { MedTable.MED_NAME,
				MedTable.MED_DOSAGE, MedTable.MED_ID,
				MedTable.MED_DATE_FILLED, MedTable.MED_DURATION,
				MedTable.MED_WARNING, MedTable.MED_REMINDER_ON };
		if(projection != null) {
			HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
			HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));
			// Check if all columns which are requested are available
			if(!availableColumns.containsAll(requestedColumns)) {
				throw new IllegalArgumentException("Unknown columns in projection");
			}
		}
	}
}
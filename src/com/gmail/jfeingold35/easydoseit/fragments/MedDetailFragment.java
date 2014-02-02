package com.gmail.jfeingold35.easydoseit.fragments;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


import com.gmail.jfeingold35.easydoseit.alarmprovider.AlarmProvider;
import com.gmail.jfeingold35.easydoseit.classes.AlarmCursorAdapter;
import com.gmail.jfeingold35.easydoseit.database.DailyAlarmTable;
import com.gmail.jfeingold35.easydoseit.database.MedTable;
import com.gmail.jfeingold35.easydoseit.medprovider.MedProvider;


import android.content.ContentUris;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.gmail.jfeingold35.easydoseit.MedDetailActivity;
import com.gmail.jfeingold35.easydoseit.MedListActivity;
import com.gmail.jfeingold35.easydoseit.R;

/**
 * A fragment representing a single Med detail screen. This fragment is either
 * contained in a {@link MedListActivity} in two-pane mode (on tablets) or a
 * {@link MedDetailActivity} on handsets.
 */
public class MedDetailFragment extends Fragment implements LoaderCallbacks<Cursor> {
	/**
	 * The fragment argument representing the med ID that this fragment
	 * represents.
	 */
	public static final String ARG_MED_ID = "med_id";
	
	/**
	 * The fragment argument representing whether we're in two-pane mode or
	 * not. If we're in two-pane mode, we don't need to reset the title of the
	 * page. If not, we need to do that.
	 */
	public static final String ARG_TWO_PANE = "two_pane";
	private boolean mTwoPane = true;
	
	/**
	 * The fragment argument corresponding to the med name string.
	 */
	public static final String ARG_TITLE = "title";
	
	/**
	 * The SimpleCursorAdapter for the alarmList.
	 */
	AlarmCursorAdapter mAdapter = null;
	
	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public MedDetailFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("MDF", "oncreate");
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if(!mTwoPane) {
			String title = getActivity().getTitle().toString();
			outState.putString(ARG_TITLE, title);
		}
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
		if(getArguments().containsKey(ARG_TWO_PANE)) {
			mTwoPane = getArguments().getBoolean(ARG_TWO_PANE);
		}
		if(getArguments().containsKey(ARG_MED_ID)) {
			// initialize the loader to grab the specified row
			// from the content provider
			getLoaderManager().initLoader(0, savedInstanceState, this);
		}
		if(savedInstanceState != null) {
			getActivity().setTitle(savedInstanceState.getString(ARG_TITLE));
		}
        return inflater.inflate(R.layout.fragment_med_detail, container, false);
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);
        // Initialize the loader
        getLoaderManager().initLoader(1, savedInstanceState, this);
    }

    /**
     * Initializes the loaders.
     */
	@Override
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
		CursorLoader loader = null;
		long id = getArguments().getLong(ARG_MED_ID);
		switch(loaderId) {
		case 0: // MedList Loader
			Log.d("MedManager", "Loading med data");
			Uri singleUri = ContentUris.withAppendedId(MedProvider.CONTENT_URI, id);
			String[] projection = { MedTable.MED_ID,
					MedTable.MED_NAME,
					MedTable.MED_DOSAGE,
					MedTable.MED_DATE_FILLED,
					MedTable.MED_DURATION };
			
			loader = new CursorLoader(getActivity(), singleUri,
					projection, null, null,
					MedTable.MED_NAME + " COLLATE LOCALIZED ASC");
			break;
		case 1: // AlarmList Loader
			Log.d("MedManager", "Theoretically loading alarm list");
			Uri baseUri = AlarmProvider.CONTENT_URI;
			
			// Create and return a CursorLoader that will take care of
			// creating a Cursor for the data being displayed.
			String[] alarmProjection = { DailyAlarmTable.ALARM_ID,
					DailyAlarmTable.ALARM_MEDNUM,
					DailyAlarmTable.ALARM_TIME,
					DailyAlarmTable.ALARM_ISACTIVE,
					DailyAlarmTable.ALARM_TIMESTAMP,
					DailyAlarmTable.ALARM_ISLOUD };
			String select = "((" + DailyAlarmTable.ALARM_MEDNUM + " NOTNULL) AND ("
					+ DailyAlarmTable.ALARM_MEDNUM + " = " + id + "))";
			loader = new CursorLoader(getActivity(), baseUri,
					alarmProjection, select, null,
					DailyAlarmTable.ALARM_TIMESTAMP + " ASC");
			break;
		}
		return loader;
	}

	/**
	 * Customizes the various TextViews in the layout to match
	 * the values pulled from the MedTable, or swaps the alarm cursor
	 * into the adapter.
	 */
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		switch(loader.getId()) {
		case 0:
			setUpMedDetails(data);
			break;
		case 1:
			Log.d("MedManager", "Alarm finished loading");
	    	String[] from = { DailyAlarmTable.ALARM_TIME,
	    			DailyAlarmTable.ALARM_ISACTIVE, 
	    			DailyAlarmTable.ALARM_TIMESTAMP };
	    	int[] to = { R.id.alarmView, R.id.alarmCheck, 0 };
	    	Log.d("MDF", "Attempting adapter");
	    	ListView alarmList = (ListView) this.getActivity().findViewById(R.id.alarmListView);
	    	TextView emptyView = (TextView) this.getActivity().findViewById(R.id.empty);
	    	alarmList.setEmptyView(emptyView);
	    	int position = alarmList.getFirstVisiblePosition();
	    	int orientation = getResources().getConfiguration().orientation;
	    	if(orientation == Configuration.ORIENTATION_PORTRAIT) {
	    		mAdapter = new AlarmCursorAdapter(getActivity(),
	    				R.layout.alarm_list_row, data,
	    				from, to, 0);
	    		Log.d("MDF", "Portrait");
	    	} else if(orientation == Configuration.ORIENTATION_LANDSCAPE) {
	    		mAdapter = new AlarmCursorAdapter(getActivity(),
	    				R.layout.alarm_list_row_rightside_alignment, data,
	    				from, to, 0);
	    		Log.d("MDF", "Landscape");
	    	}
	    	alarmList.setAdapter(mAdapter);
	    	alarmList.setSelection(position);
	    	Log.d("MDF", "made adapter");
			break;
			
		}
	}
	
	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		Log.d("MDF", "Resetting");
		if(arg0.getId() == 1) {
			mAdapter.swapCursor(null);
		}
	}

	/**
	 * Processes the cursor from the medlist.
	 * Sets the texts on the TextViews to be the proper
	 * field from the med table.
	 */
	public void setUpMedDetails(Cursor mCursor) {

	
		int nameIndex = mCursor.getColumnIndex(MedTable.MED_NAME);
		int dosageIndex = mCursor.getColumnIndex(MedTable.MED_DOSAGE);
		int dateIndex = mCursor.getColumnIndex(MedTable.MED_DATE_FILLED);
		int durationIndex = mCursor.getColumnIndex(MedTable.MED_DURATION);
		if(mCursor != null) {
			// Moves to the next row in the cursor.
			while(mCursor.moveToNext()) {
				// Create the name string
				String medName = "Name: " + mCursor.getString(nameIndex);

				
				// Create the dosage string
				String medDosage = "Dosage: " + mCursor.getString(dosageIndex);

				
				// Create a date format to parse the date string
				String epochString = mCursor.getString(dateIndex);
				long epoch = Long.parseLong(epochString);
				DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
				String date;
				if(df instanceof SimpleDateFormat) {
					SimpleDateFormat sdf = (SimpleDateFormat) df;
					String pattern = sdf.toPattern().replaceAll("y+", "yyyy");
					sdf.applyPattern(pattern);
					date = sdf.format(new Date(epoch * 1000));
				} else {
					date = df.format(new Date(epoch * 1000));
				}
			
				// Create the date string
				String medDate = "Date Filled: " + date;

			
				// Create the duration string
				String medDuration = "Duration: " + mCursor.getString(durationIndex) + " days";
			
				// Setting the name
				TextView nameView = (TextView) getActivity().findViewById(R.id.nameDetailsView);
				nameView.setText(medName);
				Log.d("MDF", "setting name");
				// Setting dosage
				TextView dosageView = (TextView) getActivity().findViewById(R.id.dosageDetailsView);
				dosageView.setText(medDosage);
				Log.d("MDF", "setting dosage");
				// Setting the date
				TextView dateView = (TextView) getActivity().findViewById(R.id.dateFilledDetailsView);
				dateView.setText(medDate);
				Log.d("MDF", "setting date");
				// Setting the duration
				TextView durationView = (TextView) getActivity().findViewById(R.id.durationDetailsView);
				durationView.setText(medDuration);
	    	
				// Set the title of the activity to be the med name if not in two pane mode.
				if(!mTwoPane) {
					this.getActivity().setTitle(mCursor.getString(nameIndex));
				}
				// end of while loop
			}
		}
	}
}

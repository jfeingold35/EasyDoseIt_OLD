package com.gmail.jfeingold35.easydoseit.fragments;

import com.gmail.jfeingold35.easydoseit.database.MedTable;
import com.gmail.jfeingold35.easydoseit.medprovider.MedProvider;

import android.app.Activity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.content.CursorLoader;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;


import com.gmail.jfeingold35.easydoseit.R;


/**
 * A list fragment representing a list of Meds. This fragment also supports
 * tablet devices by allowing list items to be given an 'activated' state upon
 * selection. This helps indicate which item is currently being viewed in a
 * {@link MedDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link MedCallbacks}
 * interface.
 */
public class MedListFragment extends ListFragment implements LoaderCallbacks<Cursor> {

	/**
	 * The SimpleCursorAdapter for the medlist
	 */
	SimpleCursorAdapter mAdapter;
	        
	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * activated item position. Only used on tablets.
	 */
	private static final String STATE_ACTIVATED_POSITION = "activated_position";

	/**
	 * The fragment's current callback object, which is notified of list item
	 * clicks.
	 */
	private MedCallbacks mMedCallbacks = sDummyMedCallbacks;

	/**
	 * The current activated item position. Only used on tablets.
	 */
	private int mActivatedPosition = ListView.INVALID_POSITION;

	/**
	 * A callback interface that all activities containing this fragment must
	 * implement. This mechanism allows activities to be notified of item
	 * selections.
	 */
	public interface MedCallbacks {
		/**
		 * Callback for when an item has been selected.
		 */
		public void onMedSelected(long id);
	}
	
	/**
	 * A dummy implementation of the {@link MedCallbacks} interface that does
	 * nothing. Used only when this fragment is not attached to an activity.
	 */
	private static MedCallbacks sDummyMedCallbacks = new MedCallbacks() {
		@Override
		public void onMedSelected(long id) {
		}
	};
	
	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public MedListFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// Give some text to display if there is no data.
		setEmptyText(getString(R.string.list_empty_meds));
		
		// Implementing adapter
		// Create an empty adapter we will use to display the loaded data
		mAdapter = new SimpleCursorAdapter(getActivity(),
				android.R.layout.simple_list_item_activated_1, null,
				new String[] { MedTable.MED_NAME },
				new int[] { android.R.id.text1 }, 0);
		setListAdapter(mAdapter);

		// Prepare the loader. Either reconnect with an existing on,
		// or start a new one
		getLoaderManager().initLoader(0, null, this);
	}


	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		// Restore the previously serialized activated item position.
		if (savedInstanceState != null
				&& savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
			setActivatedPosition(savedInstanceState
					.getInt(STATE_ACTIVATED_POSITION));
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		// Activities containing this fragment must implement its callbacks.
		if(!(activity instanceof MedCallbacks)) {
			throw new IllegalStateException(
					"Activity must implement fragment's callbacks.");
		}
		mMedCallbacks = (MedCallbacks) activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();

		// Reset the active callbacks interface to the dummy implementation.
		mMedCallbacks = sDummyMedCallbacks;
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position,
			long id) {
		super.onListItemClick(listView, view, position, id);
		// Notify the active callbacks interface (the activity, if the
		// fragment is attached to one) that an item has been selected.
		mMedCallbacks.onMedSelected(id);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mActivatedPosition != ListView.INVALID_POSITION) {
			// Serialize and persist the activated item position.
			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
		}
	}

	/**
	 * Turns on activate-on-click mode. When this mode is on, list items will be
	 * given the 'activated' state when touched.
	 */
	public void setActivateOnItemClick(boolean activateOnItemClick) {
		// When setting CHOICE_MODE_SINGLE, ListView will automatically
		// give items the 'activated' state when touched.
		getListView().setChoiceMode(
				activateOnItemClick ? ListView.CHOICE_MODE_SINGLE
						: ListView.CHOICE_MODE_NONE);
	}

	private void setActivatedPosition(int position) {
		if (position == ListView.INVALID_POSITION) {
			getListView().setItemChecked(mActivatedPosition, false);
		} else {
			getListView().setItemChecked(position, true);
		}

		mActivatedPosition = position;
	}

	// These are the fields we will be retrieving
	static final String[] MED_PROJECTION = new String[] {
		MedTable.MED_ID,
		MedTable.MED_NAME,
		MedTable.MED_DOSAGE };
	
	@Override
	public android.support.v4.content.Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		// This is called when a new Loader needs to be created.
		Uri baseUri = MedProvider.CONTENT_URI;
		
		// Now create and return a CursorLoader that will take care of
		// creating a Cursor for the data being displayed.
		String select = "((" + MedTable.MED_NAME + " NOTNULL) AND ("
				+ MedTable.MED_NAME + " != '' ))";
		return new CursorLoader(getActivity(), baseUri,
				MED_PROJECTION, select, null,
				MedTable.MED_NAME + " COLLATE LOCALIZED ASC");
		
	}

	@Override
	public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader,
			Cursor data) {
		// Swap the new cursor in. (The framework will take care of closing the
		// old cursor once we return.)
		mAdapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mAdapter.swapCursor(null);

	}
}

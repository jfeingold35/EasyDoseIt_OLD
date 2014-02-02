package com.gmail.jfeingold35.easydoseit.settings;

import com.gmail.jfeingold35.easydoseit.R;

import android.os.Bundle;
import android.view.MenuItem;
import android.app.Activity;

public class SettingsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		SettingsFragment frag = new SettingsFragment();
		getFragmentManager().beginTransaction()
			.replace(R.id.settings_container, frag).commit();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}

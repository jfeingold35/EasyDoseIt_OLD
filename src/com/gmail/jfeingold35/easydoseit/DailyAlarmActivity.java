package com.gmail.jfeingold35.easydoseit;

import java.io.IOException;

import com.gmail.jfeingold35.easydoseit.R;
import com.gmail.jfeingold35.easydoseit.database.MedTable;
import com.gmail.jfeingold35.easydoseit.medprovider.MedProvider;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class DailyAlarmActivity extends Activity implements LoaderCallbacks<Cursor> {

	public static final String MED_NUM = "med_num";
	public static final String TIME_STRING = "time";
	public static final String SHOULD_RING = "isLoud";
	public static final String HAS_ALREADY_FIRED = "has_fired";
	public static int ALARM_COUNT = 0;
	private boolean mHaveIFired = false;
	public static MediaPlayer sMediaPlayer = null;
	public static Vibrator sVibrator = null;
	public static boolean sIsVibrating = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Retrieve information from previous existence.
		if(savedInstanceState != null) {
			mHaveIFired = savedInstanceState.getBoolean(HAS_ALREADY_FIRED);
		}
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				 WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_daily_alarm);
		getLoaderManager().initLoader(0, null, this);
		Button dismissAlarm = (Button) findViewById(R.id.dismissAlarmButton);
		dismissAlarm.setOnClickListener(new OnClickListener () {
			
			@Override
			public void onClick(View view) {
				Log.d("DAA", "OnClick Alarm Count " + ALARM_COUNT);
				ALARM_COUNT--;
				/* If ALARM_COUNT == 0, it means we're the last alarm to fire
				 * in what might have been a sequence of several alarms. Thus,
				 * it's our job to deactivate the vibrator and media player.
				 */
				if(ALARM_COUNT == 0) {
					if(sMediaPlayer.isPlaying()) {
						sMediaPlayer.stop();
						sMediaPlayer.release();
						sMediaPlayer = null;
					}
					if(sIsVibrating) {
						sVibrator.cancel();
						sIsVibrating = false;
					}
				}
				Log.d("DAA", "Lowered Alarm Count " + ALARM_COUNT);
				finish();
			}
		});
		Log.d("DAA", "Pre alarm count " + ALARM_COUNT);
		/* If we haven't fired the alarm, we need to fire the alarm. */
		if(!mHaveIFired) {
			ALARM_COUNT++;
			Log.d("DAA", "Updated Alarm count " + ALARM_COUNT);
			mHaveIFired = true;
			if(sMediaPlayer == null) {
				initializeMP(this, getAlarmUri());
				sMediaPlayer.setLooping(true);
			}
			/* If ALARM_COUNT == 1, it means that we're the first alarm to fire
			 * in what might be sequence of several alarms. Thus, it's our job
			 * to start the alarm.
			 */
			if(ALARM_COUNT == 1) {
				/* Start the media player if it's a loud alarm. */
				boolean canRing = getIntent().getBooleanExtra(SHOULD_RING, true);
				if(canRing) {
					sMediaPlayer.start();
				}
				/* Start the vibrator if it's a vibrating alarm. */
				SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
				boolean vibrationPermitted =
						sharedPref.getBoolean(getString(R.string.pref_key_alarm_vibration), true);
				if(vibrationPermitted) {
					if(sVibrator == null) {
						sVibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
					}
					long[] pattern = { 0, 500, 500 };
					if(sVibrator.hasVibrator()) {
						sVibrator.vibrate(pattern, 0);
						sIsVibrating = true;
					}
				}
			}
		}
	}
	
	/**
	 * Gets the URI specified by the user in Settings as the desired
	 * Alarm ringtone.
	 * @return The desired alarm ringtone.
	 */
	private Uri getAlarmUri() {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		String alertString = sharedPref.getString(getString(R.string.pref_key_alarm_ringtone), 
				getString(R.string.ringtone_default_value));
		Uri alert = Uri.parse(alertString);
		return alert;
	}
	
	/**
	 * Initializes the static media player, and prepares it to play the given
	 * alarm ringtone. Only called when the media player is null.
	 * @param context - the context
	 * @param alert - the alarm ringtone
	 */
	public void initializeMP(Context context, Uri alert) {
		sMediaPlayer = new MediaPlayer();
		try {
			sMediaPlayer.setDataSource(context, alert);
			final AudioManager am =
					(AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
			if(am.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
				sMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
				sMediaPlayer.prepare();
			}
		} catch (IOException e) {
			System.out.print("FAILURE TO PLAY SOUND");
		}
	}
	
	
	/**
	 * Exists to save status on orientation change.
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(HAS_ALREADY_FIRED, mHaveIFired);
	}
	
	/* =============== LEAVING ACTIVITY ==================== */
	
	/**
	 * If the back button was pressed, this must also deactivate the alarm.
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK) {
			Log.d("DAA", "back key pressed");
			Log.d("DAA", "OnBack Alarm Count " + ALARM_COUNT);
			ALARM_COUNT--;
			/* If ALARM_COUNT == 0, we might be the last alarm in a sequence.
			 * Thus it's our job to deactivate the media player and the
			 * vibrator.
			 */
			if(ALARM_COUNT == 0) {
				if(sMediaPlayer.isPlaying()) {
					sMediaPlayer.stop();
					sMediaPlayer.release();
					sMediaPlayer = null;
				}
				if(sIsVibrating) {
					sVibrator.cancel();
					sIsVibrating = false;
				}
			}
			Log.d("DAA", "Lowered Alarm Count " + ALARM_COUNT);
		}
		return super.onKeyDown(keyCode, event);
	}
	
	/* ======================= LOADER CODE ================= */
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		CursorLoader loader = null;
		long medId = getIntent().getLongExtra(MED_NUM, 0);
		Uri singleUri = ContentUris.withAppendedId(MedProvider.CONTENT_URI, medId);
		String[] projection = { MedTable.MED_ID,
				MedTable.MED_NAME };
		loader = new CursorLoader(getApplicationContext(), singleUri,
				projection, null, null,
				MedTable.MED_NAME + " COLLATE LOCALIZED ASC");
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor mCursor) {
		int nameIndex = mCursor.getColumnIndex(MedTable.MED_NAME);
		if(mCursor != null) {
			while(mCursor.moveToNext()) {
				String medName = mCursor.getString(nameIndex);
				Log.d("dailyAlarm", "med name is " + medName);
				TextView alarmView = (TextView) findViewById(R.id.dailyAlarmView);
				String timeString = getIntent().getStringExtra(TIME_STRING);
				String alarmText = "It is now " + timeString + ". It is time "
						+ "for you to take a dosage of " + medName + ".";
				alarmView.setText(alarmText);
			}
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
	}

}

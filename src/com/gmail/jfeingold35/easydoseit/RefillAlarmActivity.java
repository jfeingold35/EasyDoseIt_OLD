package com.gmail.jfeingold35.easydoseit;

import java.io.IOException;

import com.gmail.jfeingold35.easydoseit.R;


import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

/**
 * An activity representing a single Refill Alarm screen. This activity is called
 * whenever an alarm is triggered reminding the user to refill a prescription.
 * Displays as a fullscreen activity.
 * @author Josh Feingold
 *
 */
public class RefillAlarmActivity extends Activity {
	public static final String MED_NAME = "name";
	public static final String DAYS_LEFT = "days_left";
	public static final String HAS_ALREADY_FIRED = "has_fired";
	public static int ALARM_COUNT = 0;
	private boolean mHaveIFired = false;
	public static MediaPlayer sMediaPlayer = null;
	static Vibrator sVibrator = null;
	public static boolean sIsVibrating = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// Create the view, without a title, and set it to be
		// fullscreen.
		super.onCreate(savedInstanceState);
		// Retrieve information from previous existence.
		if(savedInstanceState != null) {
			mHaveIFired = savedInstanceState.getBoolean(HAS_ALREADY_FIRED);
		}
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_refill_alarm);
		
		// Get the extras from the intent, so that we can use them.
		Intent intent = getIntent();
		String medName;
		if(intent.hasExtra(MED_NAME)) {
			medName = intent.getStringExtra(MED_NAME);
		} else {
			medName = "\'no med declared\'";
		}
		int daysLeft = intent.getIntExtra(DAYS_LEFT, 0);

		// Customize the text in the TextView.
		String alarmPrefix = "Your prescription for " + medName + " runs out";
		String alarmSuffix;
		if(daysLeft==0) {
			alarmSuffix = " today!";
		} else if(daysLeft==1) {
			alarmSuffix = " tomorrow!";
		} else {
			alarmSuffix = " in " + daysLeft + " days!";
		}
		String alarmString = alarmPrefix + alarmSuffix;
		TextView refillAlarmView = (TextView) findViewById(R.id.refillAlarmView);
		refillAlarmView.setText(alarmString);
		
		// Set up the button so as to turn off the alarm.
		Button dismissAlarm = (Button) findViewById(R.id.dismissAlarmButton);
		dismissAlarm.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				ALARM_COUNT--;
				/* If ALARM_COUNT == 0, we're potentially the last alarm
				 * in a sequence. Thus it's our job to deactivate everything.
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
				finish();
			}
		});
		/* If we haven't fired the alarm, we need to fire the alarm. */
		if(!mHaveIFired) {
			ALARM_COUNT++;
			mHaveIFired = true;
			if(sMediaPlayer == null) {
				initializeMP(this, getAlarmUri());
				sMediaPlayer.setLooping(true);
			}
			/* If ALARM_COUNT == 1, it means we're the first in a sequence, so
			 * it's our job to activate everything.
			 */
			if(ALARM_COUNT == 1) {
				SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
				/* Start the media player if it's a loud alarm. */
				boolean refillsLoud =
						sharedPref.getBoolean(getString(R.string.pref_key_refills_loud), true);
				if(refillsLoud) {
					sMediaPlayer.start();
				}
				/* Start the vibrator if it's a vibrating alarm. */
				boolean vibrationPermitted =
						sharedPref.getBoolean(getString(R.string.pref_key_alarm_vibration), true);
				if(vibrationPermitted) {
					if(sVibrator == null) {
						sVibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
					}
					long[] pattern = {0, 500, 500};
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
			
		}
	}
	
	/** Exists to save status on orientation change. */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(HAS_ALREADY_FIRED, mHaveIFired);
	}
	
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
}

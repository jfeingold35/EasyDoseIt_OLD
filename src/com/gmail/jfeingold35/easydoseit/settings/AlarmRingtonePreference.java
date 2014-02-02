package com.gmail.jfeingold35.easydoseit.settings;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.RingtonePreference;
import android.util.AttributeSet;
import android.util.Log;

public class AlarmRingtonePreference extends RingtonePreference {

	public AlarmRingtonePreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	protected void onSaveRingtone(Uri ringtoneUri) {
		Log.d("ARP", "saving ringtone");
		String ringtoneString = ringtoneUri.toString();
		Ringtone ringtone = RingtoneManager.getRingtone(getContext(), ringtoneUri);
		String ringtoneTitle = ringtone.getTitle(getContext());
		this.setSummary(ringtoneTitle);
		this.persistString(ringtoneString);
	}

	@Override
	protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
		Log.d("ARP", "setting initial value");
		if(restorePersistedValue) { // If we're restoring a persisted value
			Log.d("TPP", "restoring");
			String alarmUri = this.getPersistedString("content://settings/system/alarm_alert");
			Uri ringtoneUri = Uri.parse(alarmUri);
			Ringtone ringtone = RingtoneManager.getRingtone(getContext(), ringtoneUri);
			String ringtoneTitle = ringtone.getTitle(getContext());
			this.setSummary(ringtoneTitle);
		} else { // If not
			Log.d("TPP", "not restoring");
			String uriString = "content://settings/system/alarm_alert";
			this.persistString(uriString);
			Uri ringtoneUri = Uri.parse(uriString);
			Ringtone ringtone = RingtoneManager.getRingtone(getContext(), ringtoneUri);
			String ringtoneTitle = ringtone.getTitle(getContext());
			this.setSummary(ringtoneTitle);
		}
	}
}

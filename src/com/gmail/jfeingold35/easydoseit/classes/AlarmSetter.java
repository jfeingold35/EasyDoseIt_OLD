package com.gmail.jfeingold35.easydoseit.classes;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.gmail.jfeingold35.easydoseit.DailyAlarmActivity;
import com.gmail.jfeingold35.easydoseit.R;
import com.gmail.jfeingold35.easydoseit.RefillAlarmActivity;

public class AlarmSetter {
	
	/**
	 * Handles setting the refill alarm for the newly created med.
	 * @param context - The application context
	 * @param returnCode - the ID of the med
	 * @param name - The name of the newly created med
	 * @param dateFilled - The day the med was filled, in the
	 *                     form of 'seconds since the epoch'
	 * @param duration - The length of the prescription
	 * @param daysWarning - How many days in advance to warn the user
	 */
	public void setRefillAlarm(Context context, int returnCode, String name, long dateFilled, int duration,
			int daysWarning) {
		
		// Create the time at which the alarm goes off.
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(dateFilled*1000);
		int daysUntilAlarm = duration - daysWarning;
		cal.add(Calendar.DAY_OF_YEAR, daysUntilAlarm);
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		int alarmOffset = sharedPref.getInt(context.getString(R.string.pref_key_alarm_time), 1200);
		int hour = alarmOffset / 100;
		int minute = alarmOffset % 100;
		cal.add(Calendar.HOUR_OF_DAY, hour);
		cal.add(Calendar.MINUTE, minute);
		cal.set(Calendar.SECOND, 0);
		
		// Get a second calendar, and check if the time we're supposed to give the warning
		// has already passed. If so, we don't set the alarm, because it would trigger immediately
		// and irritate the user.
		Calendar comparisonCal = Calendar.getInstance();
		boolean refillInPast = comparisonCal.after(cal);
		if(refillInPast) {
			return;
		}
		Log.d("AlarmSetter", "setting alarm");
		// Create the new pendingIntent and add it to the alarm manager
		Intent intent = new Intent(context, RefillAlarmActivity.class);
		intent.setAction("RefillAlarmActivity");
		intent.putExtra(RefillAlarmActivity.MED_NAME, name);
		intent.putExtra(RefillAlarmActivity.DAYS_LEFT, daysWarning);
		intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		PendingIntent pendingIntent =
				PendingIntent.getActivity(context, returnCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager am =
				(AlarmManager)context.getSystemService(Activity.ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
	}
	
	/**
	 * Cancels a refill alarm of the given ID.
	 * @param context - the application context
	 * @param returnCode - the ID of the given alarm in the database.
	 */
	public void cancelRefillAlarm(Context context, int returnCode) {
		Intent intent = new Intent(context, RefillAlarmActivity.class);
		intent.setAction("RefillAlarmActivity");
		PendingIntent pendingIntent =
				PendingIntent.getActivity(context, returnCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager am = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
		am.cancel(pendingIntent);
	}
	
	/**
	 * Cancels a daily alarm of the given ID.
	 * @param context - the application context
	 * @param alarmId - The ID of the given alarm in the database.
	 */
	public void cancelDailyAlarm(Context context, int alarmId) {
		int returnCode = alarmId * -1;
		
		Intent intent = new Intent(context, DailyAlarmActivity.class);
		intent.setAction("DailyAlarmActivity");
		PendingIntent pendingIntent = 
				PendingIntent.getActivity(context, returnCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager am = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
		am.cancel(pendingIntent);
	}
	
	/**
	 * The function responsible for setting the daily alarm. In order to avoid 
	 * conflict with the refill alarm return codes, which are all the value of
	 * the ID for the particular med, all return codes for daily alarms will be
	 * the ID for that alarm multiplied by -1.
	 * @param context - The application context
	 * @param medId - The ID of the med for which the alarm is set
	 * @param alarmId - The ID of the alarm
	 * @param alarmString - The time of the alarm, formatted as a string
	 * @param timestamp - The timestamp for the given alarm
	 * @param isLoud - The boolean that indicates if the alarm should ring
	 */
	public void setDailyAlarm(Context context, long medId, int alarmId, String alarmString, long timestamp,
			boolean isLoud) {
		// Create the return code
		int returnCode = alarmId * -1;
		
		// Create the time at which the alarm goes off.
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timestamp);
		
		// Create the new pendingIntent and add it to the alarm manager
		Intent intent = new Intent(context, DailyAlarmActivity.class);
		intent.putExtra(DailyAlarmActivity.MED_NUM, medId);
		intent.putExtra(DailyAlarmActivity.TIME_STRING, alarmString);
		intent.putExtra(DailyAlarmActivity.SHOULD_RING, isLoud);
		intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		intent.setAction("DailyAlarmActivity");
		PendingIntent pendingIntent =
				PendingIntent.getActivity(context, returnCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager am =
				(AlarmManager)context.getSystemService(Activity.ALARM_SERVICE);
		am.setRepeating(AlarmManager.RTC_WAKEUP,
				cal.getTimeInMillis(),
				AlarmManager.INTERVAL_DAY,
				pendingIntent);
	}
	
	/**
	 * This function turns an hour and a day into a string of the format HH:MM AM/PM"
	 */
	public String formatAlarmString(int hourOfDay, int minute) {
		String res = null;
		int trueHour;
		if(hourOfDay == 0) {
			trueHour = 12;
		} else if(hourOfDay <= 12) {
			trueHour = hourOfDay;
		} else { // if hourOfDay > 12
			trueHour = hourOfDay - 12;
		}
		
		boolean isAm = hourOfDay < 12;
		String hourString = Integer.toString(trueHour);
		String minuteString;
		if(minute < 10) {
			minuteString = "0" + Integer.toString(minute);
		} else {
			minuteString = Integer.toString(minute);
		}
		String amString;
		if(isAm) {
			amString = " AM";
		} else {
			amString = " PM";
		}
		res = hourString + ":" + minuteString + amString;
		
		return res;
	}
	
	/**
	 * Given a timestamp, checks if that timestamp is for a time in the future.
	 * If so, it returns the given timestamp. If not, it increments it by 24 hours
	 * until it is.
	 * @param oldStamp - the given timestamp
	 * @return newStamp - the updated timestamp
	 */
	public long updateTimestamp(long oldStamp) {
		// Set the oldStamp to a calendar
		Calendar oldCal = Calendar.getInstance();
		oldCal.setTimeInMillis(oldStamp);
		
		// Get a calendar for the current time
		Calendar currentCal = Calendar.getInstance();
		
		while(oldCal.before(currentCal)) {
			oldCal.add(Calendar.DATE, 1);
		}
		
		return oldCal.getTimeInMillis();
	}
	/**
	 * This function turns an hour and a day into a UNIX timestamp for the table.
	 * If the time is after the current time, the timestamp will be for that day.
	 * If the time is before the current time, the timestamp will be for the following day.
	 */
	public long getAlarmTimestamp(int desiredHour, int desiredMinute) {
		long res = 0;
		Calendar cal = Calendar.getInstance();
		int currentHour = cal.get(Calendar.HOUR_OF_DAY);
		int currentMinute = cal.get(Calendar.MINUTE);
		Log.d("AlarmSetter", "Hour: Desired " + desiredHour + " Current " + currentHour);
		Log.d("AlarmSetter", "Minute: Desired " + desiredMinute + " Current " + currentMinute);
		// If the desired time is before the current time, set the timestamp
		// for the following day.
		// The desired time is before the current time if either:
		// 1. desiredHour < currentHour.
		// 2. desiredHour == currentHour && desiredMinute <= currentMinute
		boolean isPast = (desiredHour < currentHour)
				|| (desiredHour == currentHour && desiredMinute < currentMinute);
		if(isPast) {
			cal.add(Calendar.DATE, 1);
		}
		cal.set(Calendar.HOUR_OF_DAY, desiredHour);
		cal.set(Calendar.MINUTE, desiredMinute);
		cal.set(Calendar.SECOND, 0);
		res = cal.getTimeInMillis();
		return res;
	}
	
	/**
	 * Given the time the alarm is set for, returns a string of the format
	 * "Alarm set for ____ hours and ___ minutes from now".
	 * Method: convert both times into amount of minutes since midnight,
	 * then calculate the difference
	 * @param alarmCal - the calendar set for the time of the alarm
	 * @return - string of the given format
	 */
	public String getTimeDelta(Calendar alarmCal) {
		String res = null;
		
		int alarmHour = alarmCal.get(Calendar.HOUR_OF_DAY);
		int alarmMinute = alarmCal.get(Calendar.MINUTE);
		int alarmTimeInMinutes = alarmHour*60 + alarmMinute;
		// Get current time
		Calendar cal = Calendar.getInstance();
		int currentHour = cal.get(Calendar.HOUR_OF_DAY);
		int currentMinute = cal.get(Calendar.MINUTE);
		int currentTimeInMinutes = currentHour*60 + currentMinute;
		
		// If the alarm time is before the current time, we must add a day's
		// worth of minutes before we can subtract
		if(alarmTimeInMinutes < currentTimeInMinutes) {
			alarmTimeInMinutes += (24*60);
		}
		int timeDeltaInMinutes = alarmTimeInMinutes - currentTimeInMinutes;
		
		int hourDelta = timeDeltaInMinutes / 60;
		int minuteDelta = timeDeltaInMinutes % 60;
		res = "Alarm set for " + hourDelta + " hours and " + minuteDelta
				+ " minutes from now.";
		return res;
	}
}

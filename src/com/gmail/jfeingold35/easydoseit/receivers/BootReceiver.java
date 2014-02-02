package com.gmail.jfeingold35.easydoseit.receivers;


import com.gmail.jfeingold35.easydoseit.services.AlarmSetupService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;



// This receiver is tripped when the phone reboots, and its
// function is to initiate a service that runs through the
// Med database to instantiate the refill alarms, and 
// run the through the Daily Alarm database to instantiate
// the daily alarms.
/**
 * This receiver is tripped when the phone reboots, and its
 * function is to initiate a service that runs through the
 * Med database to instantiate the refill alarms, and
 * run through the Daily Alarm database to instantiate the daily alarms. 
 * @author ASUS
 *
 */
public class BootReceiver extends BroadcastReceiver {
	
	
	@Override
	public void onReceive(Context context, Intent intent) {
		context.startService(new Intent(context, AlarmSetupService.class));
	}
	
}

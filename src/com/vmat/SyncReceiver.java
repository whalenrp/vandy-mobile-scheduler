package com.vmat;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import java.lang.System;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

/**
 * This class simply passes through the intent to the 
 * SyncService so that it can load the webpage in the background.
 */
public class SyncReceiver extends BroadcastReceiver{
	public static final String SYNC = "sync_meetings";
	public static final String RESET_ALARMS = "reset_alarms";

	@Override
	public void onReceive(Context context, Intent i){
		String action = i.getAction();

		if ( action.equals(SYNC) ){
			Log.i("SyncReceiver", "Sync Broadcast Received. Syncing meetings database.");
			Intent intent = new Intent(context, SyncService.class);
			intent.putExtra("action", SyncService.MEETING_SYNC);
			context.startService(intent);
		}

		if ( action.equals(Intent.ACTION_BOOT_COMPLETED) ||
			 action.equals(RESET_ALARMS)) 
		{
			Log.i("SyncReceiver", "Reregistering Alarms");
			EventsDB db = new EventsDB(context);
			Cursor c = db.getWritableDatabase().query(
				EventsDB.TABLE_NAME,
				new String[] {"_id", EventsDB.DATE, EventsDB.ALARM_TIME_PRIOR},
				EventsDB.ALARM_ACTIVE + "=1", 
				null, 
				null, 
				null, 
				null);

			if (c.getCount() == 0){
				c.close();
				db.close();
				return;
			}

			c.moveToFirst();
			while ( !c.isAfterLast() ){
				registerAlarm(context, c);
				c.moveToNext();
			}
			c.close();
			db.close();
		}
	}

	/**
	 * Registers an alarm for the database entry pointed to by c.
	 * If an alarm was set to go off prior to the current time, it will 
	 * not be set.
	 */ 
	private void registerAlarm(Context context, Cursor c){
		AlarmManager alarmManager = (AlarmManager)context.getSystemService(context.ALARM_SERVICE);	

		long millisPrior = c.getLong(c.getColumnIndex(EventsDB.ALARM_TIME_PRIOR));

		// Reads in the stored date, parses it, and stores it in a Calendar object
		// for the alarm service.
		String UTCdate = c.getString(c.getColumnIndex(EventsDB.DATE));
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		Date parsedDate = new Date();
		try{
			parsedDate = format.parse(UTCdate);
		}catch(ParseException e){
			e.printStackTrace();
		}
		Calendar alarmCalendar = Calendar.getInstance();
		alarmCalendar.setTime(parsedDate);

		long alarmTime = alarmCalendar.getTimeInMillis() - millisPrior;
		if ( alarmTime > System.currentTimeMillis() ){
		
			Intent intent = new Intent(context, DetailActivity.class);
			intent.putExtra("id", c.getInt(c.getColumnIndex("_id"))).
				putExtra("alarmReceived", 1);
			PendingIntent pi = PendingIntent.getActivity(
				context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

			alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pi);

		}
	}
}

package com.vmat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * This class simply passes through the intent to the 
 * SyncService so that it can load the webpage in the background.
 */
public class SyncReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent i){
		Intent intent = new Intent(context, SyncService.class);
		intent.putExtra("action", SyncService.MEETING_SYNC);
		context.startService(intent);
		Log.i("SyncReceiver", "Broadcast Received");
	}
}

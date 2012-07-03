package com.vmat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class OnAlarmReciever extends BroadcastReceiver{


@Override
public void onReceive(Context context, Intent intent) {
    WakeIntentService.acquireStaticLock(context);
    Intent i = new Intent(context, AlarmService.class);
    
    context.startService(i);
}}
	

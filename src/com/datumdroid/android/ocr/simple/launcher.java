package com.datumdroid.android.ocr.simple;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

public class launcher extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate() {
	super.onCreate();
	//Toast.makeText(this,"service created", Toast.LENGTH_SHORT).show();
	// register receiver that handles screen on and screen off logic

	IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
	filter.addAction(Intent.ACTION_SCREEN_OFF);
	BroadcastReceiver mReceiver = new Receiver(this);
	registerReceiver(mReceiver, filter);

	}

	@Override
	public void onStart(Intent intent, int startId) {

	}

}

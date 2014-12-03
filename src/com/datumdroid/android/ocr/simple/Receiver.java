package com.datumdroid.android.ocr.simple;

import java.util.Calendar;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class Receiver extends BroadcastReceiver {

	static int noOfPowerOff = 0;
	static long second = 0;
	private Service service = null;
	Calendar c;
	
	public Receiver (Service service)
	{
		this.service = service;
	}
	@Override
	public void onReceive(Context context, Intent intent) {

		if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
	
			if (noOfPowerOff == 0) {
				c = Calendar.getInstance();
				second = c.get(Calendar.SECOND);
				second = System.currentTimeMillis();
				noOfPowerOff++;
			} else {
				c = Calendar.getInstance();
				if (System.currentTimeMillis() - second >= 2000) {
				noOfPowerOff = 0;
					//Toast.makeText(context, "too long", Toast.LENGTH_LONG).show();
				} else {
					//Toast.makeText(context, "", Toast.LENGTH_LONG).show();
					second = System.currentTimeMillis();
					noOfPowerOff++;
				}
			}
		} else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
			if(noOfPowerOff >= 2) {
				noOfPowerOff = 0;
				//Toast.makeText(context, " ", Toast.LENGTH_LONG).show();
				Bundle bundle = new Bundle();
				Intent intent1 = new Intent(context,SimpleAndroidOCRActivity.class);
				intent1.putExtras(bundle);
				intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
				context.getApplicationContext().startActivity(intent1);
			}
		}

	}

}

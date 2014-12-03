package com.datumdroid.android.ocr.simple;

import java.util.Locale;
import java.util.Random;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class TTS extends Activity implements OnInitListener {

	private TextToSpeech myTTS;
	final String tag = "srikar";
	// status check code
	private int MY_DATA_CHECK_CODE = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v(tag, "reached even here");
		super.onCreate(savedInstanceState);
	    final String an = getIntent().getExtras().get("text").toString();
	    Log.v(tag, an);
	    //setContentView(R.layout.activity_main);
	    Intent checkTTSIntent = new Intent();
	    checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
	    startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
	    
	    Thread logoTimer = new Thread() {
	        public void run() {
	            try {
	                try {
	                	sleep(3500);
	                    
	                	speakWords("Your Text is " + an);
	                	
	                    sleep(1000);
	                } catch (InterruptedException e) {
	                    // TODO Auto-generated catch block
	                    e.printStackTrace();
	                }
	            }
	            finally {
	                finish();
	            }
	        }
	    };
	    logoTimer.start();
	}
	// speak the user text
	private void speakWords(String speech) {
	    // speak straight away
	   if(myTTS != null)
	   {
	        myTTS.speak(speech, TextToSpeech.QUEUE_FLUSH, null);
	   }
	}
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (requestCode == MY_DATA_CHECK_CODE) {
	        if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
	            myTTS = new TextToSpeech(this, this);
	        } else {
	            Intent installTTSIntent = new Intent();
	            installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
	            startActivity(installTTSIntent);
	        }
	    }
	}

	// setup TTS
	public void onInit(int initStatus) {

	    if (initStatus == TextToSpeech.SUCCESS) {
	        if (myTTS.isLanguageAvailable(Locale.US) == TextToSpeech.LANG_AVAILABLE)
	            myTTS.setLanguage(Locale.US);
	    } else if (initStatus == TextToSpeech.ERROR) {
	        Toast.makeText(this, "Sorry! Text To Speech failed...",
	                Toast.LENGTH_LONG).show();
	    }
	}
}
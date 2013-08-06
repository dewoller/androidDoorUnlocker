/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ca.wollersheim.dennis.keypad;

import java.util.Locale;

import ca.wollersheim.dennis.keypad.IOIOError;
import android.speech.tts.TextToSpeech;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;
import android.util.Log;
import android.widget.VideoView;

/**
 * This class provides a basic demonstration of how to write an Android
 * activity. Inside of its window, it places a single view: an EditText that
 * displays and edits some internal text.
 */
public class KeypadActivity extends IOIOActivity implements OnClickListener,
		TextToSpeech.OnInitListener {
	private String cmd2Send = "";
	private final String tempPrefix = "998";
	private final String waterPrefix = "999";
	private Button buttonReadout;

	private MQTT_Sender sender;
	private MQTT_Receiver receiver;
	private WifiManager.WifiLock wifilock;
	public KeypadIOIOLooper mIOIOLooper;
	private CalendarServiceProvider csp;
	private VideoCapture vidcap;
	private final String LOG = "KeypadActivity";
	private TextToSpeech tts;

	public KeypadActivity() {
	}

	@Override
	public void onDestroy() {
		// Don't forget to shutdown tts!
		if (tts != null) {
			tts.stop();
			tts.shutdown();
		}
		super.onDestroy();
	}

	private void speakOut(String textToSpeak) {

		tts.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null);
	}

	@Override
	public void onInit(int status) {

		if (status == TextToSpeech.SUCCESS) {

			int result = tts.setLanguage(Locale.US);

			if (result == TextToSpeech.LANG_MISSING_DATA
					|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
				Log.e("TTS", "This Language is not supported");
			}

		} else {
			Log.e("TTS", "Initilization Failed!");
		}

	}

	public void showToastFromBackground(final String message) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(KeypadActivity.this, (CharSequence) message,
						Toast.LENGTH_LONG).show();
			}
		});
	}

	void disableKeyguard() {
		KeyguardManager km = (KeyguardManager) this
				.getSystemService(Context.KEYGUARD_SERVICE);
		km.newKeyguardLock("test").disableKeyguard();
	}

	/** Called with the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setup window
		startWIFI();
		disableKeyguard();

		tts = new TextToSpeech(this, this);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		// Inflate our UI from its XML layout description.
		setContentView(R.layout.main);

		Button button0 = (Button) findViewById(R.id.Button0);
		Button button1 = (Button) findViewById(R.id.Button1);
		Button button2 = (Button) findViewById(R.id.Button2);
		Button button3 = (Button) findViewById(R.id.Button3);
		Button button4 = (Button) findViewById(R.id.Button4);
		Button button5 = (Button) findViewById(R.id.Button5);
		Button button6 = (Button) findViewById(R.id.Button6);
		Button button7 = (Button) findViewById(R.id.Button7);
		Button button8 = (Button) findViewById(R.id.Button8);
		Button button9 = (Button) findViewById(R.id.Button9);
		Button buttonSend = (Button) findViewById(R.id.ButtonSend);
		Button buttonBS = (Button) findViewById(R.id.ButtonBS);
		buttonReadout = (Button) findViewById(R.id.ButtonReadout);

		button0.setOnClickListener(this);
		button1.setOnClickListener(this);
		button2.setOnClickListener(this);
		button3.setOnClickListener(this);
		button4.setOnClickListener(this);
		button5.setOnClickListener(this);
		button6.setOnClickListener(this);
		button7.setOnClickListener(this);
		button8.setOnClickListener(this);
		button9.setOnClickListener(this);
		buttonSend.setOnClickListener(this);
		buttonBS.setOnClickListener(this);

		csp = new CalendarServiceProvider(this);
		VideoView vv = (VideoView) findViewById(R.id.TinyVideoView);
		vidcap = new VideoCapture(this, vv);

		// create comm classes
		sender = new MQTT_Sender();
		sender.SendCommand("Starting");
		receiver = MQTT_Receiver.getInstance(this);

	}


	public void startWIFI() {
       this.registerReceiver(this.WifiStateChangedReceiver,
               new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));

		WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		manager.setWifiEnabled(true);
		wifilock = manager.createWifiLock(WifiManager.WIFI_MODE_FULL, "wifi");

		while (manager.getWifiState() == WifiManager.WIFI_STATE_ENABLING) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		wifilock.acquire();

	}

	public void resetWIFI() {
		WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		wifilock.release();
		manager.setWifiEnabled(false);
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		manager.setWifiEnabled(true);
		wifilock = manager.createWifiLock(WifiManager.WIFI_MODE_FULL, "wifi");

		while (manager.getWifiState() == WifiManager.WIFI_STATE_ENABLING) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		wifilock.acquire();
	}
	
	private BroadcastReceiver WifiStateChangedReceiver
	   = new BroadcastReceiver(){	
	 @Override
	  public void onReceive(Context context, Intent intent) {
	   // TODO Auto-generated method stub
	   
	   int extraWifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE ,
	     WifiManager.WIFI_STATE_UNKNOWN);
	   
	   switch(extraWifiState){
	   case WifiManager.WIFI_STATE_DISABLED:
	    Log.i(LOG, "WIFI STATE DISABLED");
	    break;
	   case WifiManager.WIFI_STATE_DISABLING:
	    Log.i(LOG, "WIFI STATE DISABLING");
	    break;
	   case WifiManager.WIFI_STATE_ENABLED:
	    Log.i(LOG, "WIFI STATE ENABLED");
	    break;
	   case WifiManager.WIFI_STATE_ENABLING:
	    Log.i(LOG, "WIFI STATE ENABLING");
	    break;
	   case WifiManager.WIFI_STATE_UNKNOWN:
	    Log.i(LOG, "WIFI STATE UNKNOWN");
	    break;
	   }
	   
	  }};
	
	

	protected void onStart() {
		super.onStart();
		// Bind to LocalService

	}

	protected void onStop() {
		super.onStop();
		wifilock.release();
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		// disable all the other buttons, make it a dysfunctional device!
		int action = event.getAction();
		int keyCode = event.getKeyCode();
		switch (keyCode) {
		case KeyEvent.KEYCODE_VOLUME_UP:
			if (action == KeyEvent.ACTION_UP) {
				mIOIOLooper.resetAllPins();

				// TODO
			}
			return true;
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			if (action == KeyEvent.ACTION_DOWN) {
				// TODO
				mIOIOLooper.resetAllPins();

			}
			return true;
		case KeyEvent.KEYCODE_CAMERA:
			if (action == KeyEvent.ACTION_DOWN) {
				// TODO
				mIOIOLooper.resetAllPins();

			}
			return true;
		case KeyEvent.KEYCODE_BACK:
			if (action == KeyEvent.ACTION_DOWN) {
				// TODO
				mIOIOLooper.resetAllPins();

			}
			return true;
		default:
			return super.dispatchKeyEvent(event);
		}
	}

	/**
	 * Called when the activity is about to start interacting with the user.
	 */
	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.Button0:
			storeCommand("0");
			break;
		case R.id.Button1:
			storeCommand("1");
			break;
		case R.id.Button2:
			storeCommand("2");
			break;
		case R.id.Button3:
			storeCommand("3");
			break;
		case R.id.Button4:
			storeCommand("4");
			break;
		case R.id.Button5:
			storeCommand("5");
			break;
		case R.id.Button6:
			storeCommand("6");
			break;
		case R.id.Button7:
			storeCommand("7");
			break;
		case R.id.Button8:
			storeCommand("8");
			break;
		case R.id.Button9:
			storeCommand("9");
			break;
		case R.id.ButtonSend:
			processKeypadEntry();
			break;
		case R.id.ButtonBS:
			backspace();
			break;
		}
	}

	void processKeypadEntry() {
		String name = csp.queryCalendar(cmd2Send);
		if (cmd2Send.equals("0246813579")) {
			System.exit(0);
		} else if (cmd2Send.equals("1357902468")) {
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.setComponent(new ComponentName("com.android.launcher",
					"LauncherApplication"));
			startActivity(intent);
		} else if (cmd2Send.equals("666")) {
			mIOIOLooper.resetAllPins();
		} else if (cmd2Send.equals("667")) {
			csp.dumpCalendar();
		} else if (cmd2Send.startsWith(tempPrefix) && cmd2Send.length() == 4) {
			processIncomingMessage("temp " + cmd2Send.substring(3, 4));
		} else if (cmd2Send.startsWith(waterPrefix) && cmd2Send.length() == 4) {
			sayWater(cmd2Send.substring(3, 4));
		} else if (cmd2Send.startsWith(waterPrefix) && cmd2Send.length() > 4) {
			sayWater(cmd2Send.substring(3, 4),
					cmd2Send.substring(4, cmd2Send.length()));
		} else if (name != null) {
			unlockDoor(cmd2Send, name);
		} else {
			entryErrorCode();
		}

		cmd2Send = "";
		buttonReadout.setText(anonymise(cmd2Send));
	}

	public void entryErrorCode() {
		Bundle bundle = new Bundle();
		Account[] accounts = AccountManager.get(this).getAccounts();
		bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
		bundle.putBoolean(ContentResolver.SYNC_EXTRAS_FORCE, true);
		bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
		bundle.putBoolean(ContentResolver.SYNC_EXTRAS_IGNORE_SETTINGS, true);
		ContentResolver
				.requestSync(accounts[0], "com.android.calendar", bundle);

		showToastFromBackground("I dont understand what you are saying");
		sender.SendCommand("I dont understand what you entered at keypad:"
				+ cmd2Send);
		speakOut("I dont understand");
		// sComm.SendCommand(cmd2Send);

	}

	public void sayWater(String zone) {
		sayWater(zone, "8");
	}

	public void sayWater(String zone, String time) {
		speakOut("Watering zone " + zone + " for " + time + "seconds");
		processIncomingMessage("water " + zone + " " + time);
	}

	public void processIncomingMQTTMessage(String topic, String message) {
		if (topic.toString().equals("toDoorUnlocker")) {
			processIncomingMessage(message);
		}
	}

	public void processIncomingMessage(String message) {
		String[] part = message.split("[ ]+");
		sender.SendCommand("Recieved message " + message);
		int[] val = new int[part.length];
		for (int i = 0; i < part.length; i++) {
			try {
				val[i] = Integer.parseInt(part[i]);
			} catch (NumberFormatException e) {
				val[i] = 0;
			}
		}

		if (message.equals("doorUnlock")) {
			unlockDoor("From MQTT", "Stranger");
		} else if (part[0].equalsIgnoreCase("ping")) {
			sender.SendCommand("Ping recieved: " + message);

		} else if (part[0].equalsIgnoreCase("reset")) {
			mIOIOLooper.resetAllPins();
		} else if (part[0].equalsIgnoreCase("water")) {
					if (part.length == 2) {
				water(val[1], 8);
			} else if (part.length == 3) {
				water(val[1], val[2]);
			}
		} else if (part[0].equalsIgnoreCase("video")) {
			if (part.length == 1) {
				vidcap.oneRecord(8);
			} else if (part.length == 2) {
				vidcap.oneRecord(val[1]);
			}
		} else if (part[0].equalsIgnoreCase("temp")) {
			if (part.length == 2) {
				processTemperature(val[1]);
			}
		} else if (part[0].equalsIgnoreCase("say")) {
			if (part.length == 2) {
				speakOut(part[1]);
			} else
				sender.SendCommand("I dont understand this MQTT message:"
						+ message);
		}
	}

	private void processTemperature(int pin) {
		Double temp;
		try {
			Log.d(LOG, "Temperature Read " + pin);
			temp = mIOIOLooper.readAverageTemperature(pin);
			// showToastFromBackground("The temperature is: " + temp);
			sender.SendCommand("Temperature at zone " + pin + " is " + temp);
			Log.d(LOG, "Temperature was " + temp);
		} catch (IOIOError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			showToastFromBackground(e.getMessage());
		}

	}

	protected void water(int pin, int wateringDuration) {
		if (wateringDuration > 3600)
			showToastFromBackground("Watering duration too long: "
					+ wateringDuration / 60 + " minutes");
		else
			try {
				mIOIOLooper.safeSetPin(pin, wateringDuration);
			} catch (IOIOError e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				showToastFromBackground(e.getMessage());
			}
		Log.d(LOG, "SetPin " + pin + ", duration " + wateringDuration);
		sender.SendCommand("Tried to started watering for " + wateringDuration
				+ " seconds");
	}

	protected void unlockDoor(String doorCode, String name) {
		try {
			mIOIOLooper.unlockDoor();
			sayUnlock(name);
			sender.SendCommand("Unlocked door with code " + doorCode
					+ " by person " + name);
		} catch (IOIOError e) {
			e.printStackTrace();
			showToastFromBackground(e.getMessage());
		}
	}

	private void sayUnlock(String name) {
		// final MediaPlayer mMediaPlayer = MediaPlayer.create(this,
		// R.raw.beep);
		speakOut("Welcome " + name + ", the door is unlocked");
		// final MediaPlayer mMediaPlayer = MediaPlayer.create(this,
		// R.raw.unlocked);
		// mMediaPlayer.start();
		// while (mMediaPlayer.isPlaying()) {
		// try {
		// Thread.sleep(100);
		// } catch (InterruptedException e) {
		// }
		// }
		// mMediaPlayer.release();
	}

	void backspace() {
		synchronized (this) {
			if (cmd2Send.length() == 0) {
				return;
			}
			cmd2Send = cmd2Send.substring(0, cmd2Send.length() - 1);
			buttonReadout.setText(anonymise(cmd2Send));
		}
	}

	void storeCommand(String command) {
		cmd2Send += command;
		buttonReadout.setText(anonymise(cmd2Send));
	}

	String anonymise(String str) {
		if (str.length() < 1) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < str.length() - 1; ++i) {
			sb.append('*');
		}
		return sb.append(str.substring(str.length() - 1, str.length()))
				.toString();
	}

	@Override
	protected IOIOLooper createIOIOLooper() {
		if (mIOIOLooper == null)
			mIOIOLooper = new KeypadIOIOLooper((Context) this);
		return mIOIOLooper;
	}

}

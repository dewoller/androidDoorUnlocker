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

import ca.wollersheim.dennis.keypad.KeypadIOIOLooper.IOIOError;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
import android.app.KeyguardManager;
import android.content.Context;
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

/**
 * This class provides a basic demonstration of how to write an Android
 * activity. Inside of its window, it places a single view: an EditText that
 * displays and edits some internal text.
 */
public class KeypadActivity extends IOIOActivity implements OnClickListener {
	private String cmd2Send = "";
	private Button buttonReadout;

	private static MQTT_Sender sComm;
	private WifiManager.WifiLock wifilock;
	private KeypadIOIOLooper mIOIOLooper;
	private final String TAG = "KeypadActivity";

	public KeypadActivity() {
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

		WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		manager.setWifiEnabled(true);
		wifilock = manager.createWifiLock(WifiManager.WIFI_MODE_FULL, "wifi");
		disableKeyguard();
		while (manager.getWifiState() == WifiManager.WIFI_STATE_ENABLING) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (sComm == null) {
			// create comm class
			sComm = new MQTT_Sender();
			sComm.SendCommand("Starting");
		}

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

	}

	protected void onStart() {
		super.onStart();
		// Bind to LocalService

	}

	protected void onStop() {
		super.onStop();
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		// disable all the other buttons, make it a dysfunctional device!
		int action = event.getAction();
		int keyCode = event.getKeyCode();
		switch (keyCode) {
		case KeyEvent.KEYCODE_VOLUME_UP:
			if (action == KeyEvent.ACTION_UP) {
				// TODO
			}
			return true;
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			if (action == KeyEvent.ACTION_DOWN) {
				// TODO
			}
			return true;
		case KeyEvent.KEYCODE_CAMERA:
			if (action == KeyEvent.ACTION_DOWN) {
				// TODO
			}
			return true;
		case KeyEvent.KEYCODE_BACK:
			if (action == KeyEvent.ACTION_DOWN) {
				// TODO
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
			send();
			break;
		case R.id.ButtonBS:
			backspace();
			break;
		}
	}

	void send() {
		if (cmd2Send.equals("00000")) {
			System.exit(0);
		} else if (cmd2Send.equals("666")) {
			Rebooter.reboot(this);
		} else if (cmd2Send.startsWith("111") 
				&& cmd2Send.length()>=4 
				&& !cmd2Send.substring(3,4).equalsIgnoreCase("0")) {
			// we have a watering command, and it is not trying to unlock the door!
			int pin = -1;
			int wateringDuration=8;
			try {
				pin = Integer.parseInt(cmd2Send.substring(3, 4));
				if (cmd2Send.length() >4) {
					wateringDuration=Integer.parseInt(cmd2Send.substring(4,cmd2Send.length()));
				}
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			IOIOError rv = IOIOError.NONE;
			if (wateringDuration > 3600) 
					showToastFromBackground("Watering duration too long: " + wateringDuration/60 + " minutes");
			else
				rv = mIOIOLooper.setPin(pin, wateringDuration);
			Log.d(TAG, "SetPin " + pin + ", duration " + wateringDuration + ", return value " + rv);
			switch (rv) {
				case NONE: 
					break;
				case DISCONNECTED: 
					showToastFromBackground("Disconnected");
					break;
				case INVALIDPIN: 
					showToastFromBackground("Invalid Pin");
					break;
				case NEVERCONNECTED:
					showToastFromBackground("Never connected");
					break;
				}
	} else
			sComm.SendCommand(cmd2Send);
		cmd2Send = "";
		buttonReadout.setText(anonymise(cmd2Send));
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

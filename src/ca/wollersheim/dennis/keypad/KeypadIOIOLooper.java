package ca.wollersheim.dennis.keypad;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttDefaultFilePersistence;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;

import ioio.lib.api.DigitalOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

/**
 * An example IOIO service. While this service is alive, it will attempt to
 * connect to a IOIO and blink the LED. A notification will appear on the
 * notification bar, enabling the user to stop the service.
 */
public class KeypadIOIOLooper extends BaseIOIOLooper implements MqttCallback {
	private MediaPlayer mMediaPlayer;
	Context parent;
	MqttClient client;
	MqttClientPersistence persist;

	private DigitalOutput[] pin = new DigitalOutput[4];
	private int[] ioioPinID = { 20, 22, 21, 23 };
	//private int[] ioioPinID = { 23, 20, 21, 22 };
	private long[] pinStopTime = new long[4];
	private int lockIndex = 0;
	private boolean connected = false;

	public enum IOIOError {
		NONE, DISCONNECTED, INVALIDPIN, NEVERCONNECTED
	};

	private final String LOG = "KeypadIOIOLooper";

	public KeypadIOIOLooper(Context c) {
		SetupMQTT_Reciever(c);
	}

	public IOIOError setPin(int switchID, int seconds) {
		// sanity checking
		if (!connected)
			return IOIOError.NEVERCONNECTED;
		if (switchID >= pinStopTime.length) {
			return IOIOError.INVALIDPIN;
		}
		pinStopTime[switchID] = System.currentTimeMillis() + seconds * 1000;
		try {
			setPin(switchID);
		} catch (ConnectionLostException e) {
			// TODO Auto-generated catch block
			// showToastFromBackground("lost connection");
			return IOIOError.DISCONNECTED;
		}
		return IOIOError.NONE;
	}

	public void unlock() throws ConnectionLostException {
		Log.d(LOG, "Unlocking");
		pinStopTime[lockIndex] = System.currentTimeMillis() + 8 * 1000;
		setPin(lockIndex);
	}

	private void setPin(int switchID) throws ConnectionLostException {
		pin[switchID].write(false);
	}

	private void unsetPin(int switchID) throws ConnectionLostException {
		Log.d(LOG, "Unsetting Pin " + switchID);
		pin[switchID].write(true);
	}

	@Override
	protected void setup() throws ConnectionLostException {
		Log.d(LOG, "IOIO Setup");
		for (int i = 0; i < pin.length; i++) {
			pin[i] = ioio_.openDigitalOutput(ioioPinID[i],
					DigitalOutput.Spec.Mode.OPEN_DRAIN, true);
			pinStopTime[i] = 0;
		}
		connected = true;
	}

	protected void processPinStopEvents() throws ConnectionLostException {
		long currSec = System.currentTimeMillis();
		for (int i = 0; i < pin.length; i++) {
			if (pinStopTime[i] > 0 && currSec > pinStopTime[i]) {
				pinStopTime[i] = 0;
				unsetPin(i);
			}
		}
	}

	/**
	 * Called repetitively while the IOIO is connected.
	 * 
	 * @throws ConnectionLostException
	 *             When IOIO connection is lost.
	 * 
	 * @see ioio.lib.util.AbstractIOIOActivity.IOIOThread#loop()
	 */
	@Override
	public void loop() throws ConnectionLostException {
		// here, we respond to IOIO events; keypad events are handled
		// elsewhere
		processPinStopEvents();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
		}
	}

	private void SetupMQTT_Reciever(Context c) {
		parent = c;
		try {
			persist = new MqttDefaultFilePersistence("/sdcard/persist");
			client = getNewClient();
			MqttMessage message = new MqttMessage("Starting".getBytes());
			message.setQos(0);
			client.getTopic("keypad").publish(message);
			// client.disconnect();
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public MqttClient getNewClient() {
		MqttClient client = null;
		try {
			client = new MqttClient("tcp://192.168.1.31:1883",
					"KeypadUnlockListener", persist);
			client.connect();
			client.subscribe("toDoorUnlocker/#");
			client.setCallback(this);
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e(LOG, e.getMessage());
		}
		return client;
	}

	@Override
	public void connectionLost(Throwable cause) {
		// TODO Auto-generated method stub
		Log.i(LOG, "lost MQTT Connection; renewing ");
		client = getNewClient();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}

	}

	@Override
	public void messageArrived(MqttTopic topic, MqttMessage message)
			throws Exception {
		Log.i(LOG, "Topic:" + topic + ", Message: " + message);
		if (topic.toString().equals("toDoorUnlocker")
				&& message.toString().equals("doorUnlock")) {
			unlock();
			mMediaPlayer = MediaPlayer.create(parent, R.raw.beep);
			mMediaPlayer.start();
			while (mMediaPlayer.isPlaying()) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			}
			mMediaPlayer.release();
		}
		/*
		 * if (topic.toString().equals("toDoorBell/reboot")) {
		 * Rebooter.reboot(parent); }
		 */
	}

	@Override
	public void deliveryComplete(MqttDeliveryToken token) {
		// TODO Auto-generated method stub

	}
}

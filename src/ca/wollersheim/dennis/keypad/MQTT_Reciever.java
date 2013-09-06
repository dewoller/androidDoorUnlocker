package ca.wollersheim.dennis.keypad;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

public class MQTT_Reciever implements MqttCallback {
	private MediaPlayer mMediaPlayer;
	Context parent;
	MqttClient client;
	MqttClientPersistence persist;
	static final private String LOG = "MQTT_Reciever";

	public MQTT_Reciever(Context c) {
		parent = c;
		try {
			persist = new MemoryPersistence();
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
		}
		return client;
	}

	@Override
	public void connectionLost(Throwable cause) {
		// TODO Auto-generated method stub
		Log.i(LOG, "lost Connection");
		client = getNewClient();

	}

	public void messageArrived(MqttTopic topic, MqttMessage message)
			throws Exception {
		Log.i(LOG, "Topic:" + topic + ", Message: " + message);
		if (topic.toString().equals("toDoorUnlocker")
				&& message.toString().equals("doorUnlock")) {
			mMediaPlayer = MediaPlayer.create(parent, R.raw.beep);
			mMediaPlayer.start();

		}
/*		if (topic.toString().equals("toDoorBell/reboot")) {
			Rebooter.reboot(parent);
		}
*/
	}


	public void deliveryComplete(MqttDeliveryToken token) {
		// TODO Auto-generated method stub

	}

	@Override
	public void messageArrived(String topic, MqttMessage message)
			throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		// TODO Auto-generated method stub
		
	}

}

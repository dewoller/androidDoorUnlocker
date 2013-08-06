package ca.wollersheim.dennis.keypad;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;


import android.app.Activity;
import android.os.Bundle;

public class HelloWorldSendMQTTTest extends Activity {
   /** Called when the activity is first created. */
   @Override
   public void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.main );
       
   	MqttClient client;
	try {
		client = new MqttClient("tcp://localhost:1883", "SampleClient");
   	client.connect();
   	MqttMessage message = new MqttMessage("Hello world".getBytes());
   	message.setQos(0);
   	client.getTopic("a/adf").publish(message);
   	client.disconnect();}
	 catch (MqttException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	}
   }


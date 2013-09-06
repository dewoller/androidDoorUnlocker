/* 
 * Copyright (c) 2009, 2012 IBM Corp.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dave Locke - initial API and implementation and/or initial documentation
 */

package ca.wollersheim.dennis.keypad;

import java.io.IOException;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;


/**
 * This sample application demonstrates basic usage
 * of the MQTT v3 Client api.
 * 
 * It can be run in one of two modes:
 *  - as a publisher, sending a single message to a topic on the server
 *  - as a subscriber, listening for messages from the server
 *
 */
public class MQTT_Test implements MqttCallback {
	Object waiter = new Object();
	
	/**
	 * The main entry point of the sample.
	 * 
	 * This method handles parsing the arguments specified on the
	 * command-line before performing the specified action.
	 */
	public static void main(String[] args) {
		
		// Default settings:
	  	MqttClient client;
		try {
			client = new MqttClient("tcp://192.168.1.31:1883", "SampleClient");
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

	// Private instance variables
	private MqttClient client;
	private String brokerUrl;
	private boolean quietMode;
	private MqttConnectOptions conOpt;
	
	/**
	 * Constructs an instance of the sample client wrapper
	 * @param brokerUrl the url to connect to
	 * @param clientId the client id to connect with
	 * @param quietMode whether debug should be printed to standard out
	 * @throws MqttException
	 */
    public MQTT_Test(String brokerUrl, String clientId, boolean quietMode) throws MqttException {
    	this.brokerUrl = brokerUrl;
    	this.quietMode = quietMode;
    	
    	//This sample stores files in a temporary directory...
    	//..a real application ought to store them somewhere 
    	//where they are not likely to get deleted or tampered with
    	String tmpDir = System.getProperty("java.io.tmpdir");
    	MemoryPersistence dataStore = new MemoryPersistence(); 
    	
    	try {
    		// Construct the object that contains connection parameters 
    		// such as cleansession and LWAT
	    	conOpt = new MqttConnectOptions();
	    	conOpt.setCleanSession(false);

    		// Construct the MqttClient instance
			client = new MqttClient(this.brokerUrl,clientId, dataStore);
			
			// Set this wrapper as the callback handler
	    	client.setCallback(this);
	    	
		} catch (MqttException e) {
			e.printStackTrace();
			log("Unable to set up client: "+e.toString());
			System.exit(1);
		}
    }

    /**
     * Performs a single publish
     * @param topicName the topic to publish to
     * @param qos the qos to publish at
     * @param payload the payload of the message to publish 
     * @throws MqttException
     */
    public void publish(String topicName, int qos, byte[] payload) throws MqttException {
    	
    	// Connect to the server
    	client.connect();
    	log("Connected to "+brokerUrl);
    	
    	// Get an instance of the topic
    	MqttTopic topic = client.getTopic(topicName);

   		MqttMessage message = new MqttMessage(payload);
    	message.setQos(qos);
	
    	// Publish the message
    	log("Publishing at: "+System.currentTimeMillis()+ " to topic \""+topicName+"\" qos "+qos);
    	MqttDeliveryToken token = topic.publish(message);
	
    	// Wait until the message has been delivered to the server
    	token.waitForCompletion();
    	
    	// Disconnect the client
    	client.disconnect();
    	log("Disconnected");
    }
    
    /**
     * Subscribes to a topic and blocks until Enter is pressed
     * @param topicName the topic to subscribe to
     * @param qos the qos to subscibe at
     * @throws MqttException
     */
    public void subscribe(String topicName, int qos) throws MqttException {
    	
    	// Connect to the server
    	client.connect();
    	log("Connected to "+brokerUrl);

    	// Subscribe to the topic
    	log("Subscribing to topic \""+topicName+"\" qos "+qos);
    	client.subscribe(topicName, qos);

    	// Block until Enter is pressed
    	log("Press <Enter> to exit");
		try {
			System.in.read();
		} catch (IOException e) {
			//If we can't read we'll just exit
		}
		
		// Disconnect the client
		client.disconnect();
		log("Disconnected");
    }

    /**
     * Utility method to handle logging. If 'quietMode' is set, this method does nothing
     * @param message the message to log
     */
    private void log(String message) {
    	if (!quietMode) {
    		System.out.println(message);
    	}
    }


	
	/****************************************************************/
	/* Methods to implement the MqttCallback interface              */
	/****************************************************************/
    
    /**
     * @see MqttCallback#connectionLost(Throwable)
     */
	public void connectionLost(Throwable cause) {
		// Called when the connection to the server has been lost.
		// An application may choose to implement reconnection
		// logic at this point.
		// This sample simply exits.
		log("Connection to " + brokerUrl + " lost!");
		System.exit(1);
	}

    /**
     * @see MqttCallback#deliveryComplete(MqttDeliveryToken)
     */
	public void deliveryComplete(MqttDeliveryToken token) {
		// Called when a message has completed delivery to the
		// server. The token passed in here is the same one
		// that was returned in the original call to publish.
		// This allows applications to perform asychronous 
		// delivery without blocking until delivery completes.
		
		// This sample demonstrates synchronous delivery, by
		// using the token.waitForCompletion() call in the main thread.
	}

    /**
     * @see MqttCallback#messageArrived(MqttTopic, MqttMessage)
     */
	public void messageArrived(MqttTopic topic, MqttMessage message) throws MqttException {
		// Called when a message arrives from the server.
		
		System.out.println("Time:\t" +System.currentTimeMillis() +
                           "  Topic:\t" + topic.getName() + 
                           "  Message:\t" + new String(message.getPayload()) +
                           "  QoS:\t" + message.getQos());
	}

	/****************************************************************/
	/* End of MqttCallback methods                                  */
	/****************************************************************/

	

    static void printHelp() {
        System.out.println(
            "Syntax:\n\n"+
            "    Sample [-h] [-a publish|subscribe] [-t <topic>] [-m <message text>]\n"+
            "            [-s 0|1|2] -b <hostname|IP address>] [-p <brokerport>]\n\n"+
            "    -h  Print this help text and quit\n"+
            "    -q  Quiet mode (default is false)\n"+
            "    -a  Perform the relevant action (default is publish)\n" +
            "    -t  Publish/subscribe to <topic> instead of the default\n" +
            "            (publish: \"Sample/Java/v3\", subscribe: \"Sample/#\")\n" +
            "    -m  Use <message text> instead of the default\n" +
            "            (\"Message from MQTTv3 Java client\")\n" +
            "    -s  Use this QoS instead of the default (2)\n" +
            "    -b  Use this name/IP address instead of the default (localhost)\n" +
            "    -p  Use this port instead of the default (1883)\n\n" +
            "Delimit strings containing spaces with \"\"\n\n"+
            "Publishers transmit a single message then disconnect from the server.\n"+
            "Subscribers remain connected to the server and receive appropriate\n"+
            "messages until <enter> is pressed.\n\n"
            );
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
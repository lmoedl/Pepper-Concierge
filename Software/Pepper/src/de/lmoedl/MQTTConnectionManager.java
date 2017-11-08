/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.lmoedl;

import de.lmoedl.interfaces.MQTTSubscriberCallbackInterface;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 *
 * @author lothar
 */

public class MQTTConnectionManager implements MqttCallbackExtended{

    private String topicPublishBase = "/messages/commands/";
    private String topicSubscribeBase = "/messages/states/";
    //private String content = "Message from MqttPublishSample";
    private int qos = 2;
    private String broker = "tcp://192.168.0.5:1883";
    private String clientId = "Pepper";
    private MemoryPersistence persistence = new MemoryPersistence();
    private MqttClient client;
    private MQTTSubscriberCallbackInterface delegate;
    

    public MQTTConnectionManager(MQTTSubscriberCallbackInterface delegate) {
        this.delegate = delegate;
        try {
            client = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            System.out.println("Connecting to broker: " + broker);
            client.setCallback(this);
            client.connect(connOpts);
            System.out.println("Connected");
        } catch (MqttException ex) {
            Logger.getLogger(MQTTConnectionManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    

    public void publishToItem(String item, String payload) {
        try {
            
            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(qos);
            client.publish(topicPublishBase + item, message);
            System.out.println("Message published");

            
        } catch (MqttException me) {
            System.out.println("reason " + me.getReasonCode());
            System.out.println("msg " + me.getMessage());
            System.out.println("loc " + me.getLocalizedMessage());
            System.out.println("cause " + me.getCause());
            System.out.println("excep " + me);
            me.printStackTrace();
        }
    }
    
    public void subscribeToItem(String item){
        try {
            client.subscribe(topicSubscribeBase + item, qos);
        } catch (MqttException ex) {
            Logger.getLogger(MQTTConnectionManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void unsubscribeOfItem(String item){
        try {
            client.unsubscribe(topicSubscribeBase + item);
        } catch (MqttException ex) {
            Logger.getLogger(MQTTConnectionManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void disconnect(){
        try {
            client.disconnect();
            System.out.println("Disconnected");
        } catch (MqttException ex) {
            Logger.getLogger(MQTTConnectionManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void connectComplete(boolean bln, String string) {
        System.out.println("Connection complete");
    }

    @Override
    public void connectionLost(Throwable thrwbl) {
    }

    @Override
    public void messageArrived(String string, MqttMessage mm) throws Exception {
        System.out.println("Message Arrived: " + string + " payload: " + new String(mm.getPayload()));
        delegate.onSubscription(string, new String(mm.getPayload()));
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken imdt) {
    }

}

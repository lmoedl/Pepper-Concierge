/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.lmoedl;

import com.aldebaran.qi.Application;
import com.aldebaran.qi.CallError;
import com.aldebaran.qi.Session;
import com.aldebaran.qi.helper.EventCallback;
import com.aldebaran.qi.helper.proxies.ALAnimatedSpeech;
import com.aldebaran.qi.helper.proxies.ALAnimationPlayer;
import com.aldebaran.qi.helper.proxies.ALBasicAwareness;
import com.aldebaran.qi.helper.proxies.ALDialog;
import com.aldebaran.qi.helper.proxies.ALFaceDetection;
import com.aldebaran.qi.helper.proxies.ALMemory;
import com.aldebaran.qi.helper.proxies.ALMotion;
import com.aldebaran.qi.helper.proxies.ALNavigation;
import com.aldebaran.qi.helper.proxies.ALSoundLocalization;
import com.aldebaran.qi.helper.proxies.ALSpeechRecognition;
import com.aldebaran.qi.helper.proxies.ALTextToSpeech;
import com.aldebaran.qi.helper.proxies.ALTracker;
import com.vmichalak.sonoscontroller.SonosDevice;
import com.vmichalak.sonoscontroller.exception.SonosControllerException;
import de.lmoedl.interfaces.MQTTSubscriberCallbackInterface;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author lothar
 */
public class BasicBehaviour implements MQTTSubscriberCallbackInterface {

    private Session session;
    private Application application;

    private ALMemory memory;
    private ALBasicAwareness.AsyncALBasicAwareness awareness;
    private ALMotion motion;
    private ALTextToSpeech textToSpeech;
    private ALAnimatedSpeech animatedSpeech;
    private ALDialog dialog;
    private ALSpeechRecognition speechRecognition;
    private ALFaceDetection faceDetection;
    private ALSoundLocalization soundLocalization;
    private ALTracker tracker;
    private ALAnimationPlayer animationPlayer;
    private ALNavigation navigation;

    private ConnectionManager connectionManager;
    MQTTConnectionManager mQTTConnectionManager;

    private String currentState;

    private float x = 0f;
    private float y = 0f;
    private float teta = 0f;

    private long targetReachedId = 0;
    private long humanDetectedId = 0;
    private long humanDetectedDemoId = 0;
    private long speechRecognitionId = 0;
    private long eventSubscriptionIdStart = 0;

    private boolean isDancing = true;

    private String topic;

    public BasicBehaviour(Application application) {
        this.session = application.session();
        this.application = application;
    }

    public void start() {
        try {
            memory = new ALMemory(session);
            awareness = new ALBasicAwareness(session).async();
            motion = new ALMotion(session);
            textToSpeech = new ALTextToSpeech(session);
            animatedSpeech = new ALAnimatedSpeech(session);
            dialog = new ALDialog(session);
            speechRecognition = new ALSpeechRecognition(session);
            faceDetection = new ALFaceDetection(session);
            soundLocalization = new ALSoundLocalization(session);
            tracker = new ALTracker(session);
            animationPlayer = new ALAnimationPlayer(session);
            connectionManager = new ConnectionManager();
            mQTTConnectionManager = new MQTTConnectionManager(this);
            navigation = new ALNavigation(session);

            config();
        } catch (Exception ex) {
            Logger.getLogger(BasicBehaviour.class.getName()).log(Level.SEVERE, null, ex);
        }

        //stateMachine(Constants.Steps.STEP_STARUP);
        //stateMachine(Constants.Steps.STEP_MOVE_AROUND);
        //stateMachine(Constants.Steps.STEP_FACERECOGNITION);
        //stateMachine(Constants.Steps.STEP_SOUNDLOCALIZATION);
        //stateMachine(Constants.Steps.STEP_END);
        //stateMachine(Constants.Steps.STEP_COCHLOVIUS);
        //stateMachine(Constants.Steps.STEP_DIALOG);
        //stateMachine(Constants.Steps.STEP_MQTT);
    }

    public void run() throws CallError, InterruptedException, Exception {
        eventSubscriptionIdStart = memory.subscribeToEvent("RearTactilTouched", "onTouchHead::(f)", this);
        while (isDancing) {
            animationPlayer.run("animations/Stand/Waiting/AirGuitar_1");
        }

    }

    private void config() throws CallError, InterruptedException, Exception {
        textToSpeech.setLanguage(Constants.LANGUAGE);
        dialog.setLanguage(Constants.LANGUAGE);
        motion.setExternalCollisionProtectionEnabled("All", true);
        motion.setWalkArmsEnabled(Boolean.TRUE, Boolean.TRUE);
        speechRecognition.pause(true);
        speechRecognition.setLanguage(Constants.LANGUAGE);
        speechRecognition.pause(false);

        motion.wakeUp();
        memory.subscribeToEvent("RearTactilTouched", "onTouchEnd::(f)", this);
        startConcierge();

        memory.subscribeToEvent("SwitchLight", "onLightSwitch::(s)", this);
        //memory.subscribeToEvent("RearTactilTouched", "onTouchEnd::(f)", this); 
    }

    public void onTouchHead(Float value) throws InterruptedException, CallError, IOException, SonosControllerException {
        System.out.println(Constants.APP_NAME + " : Touch " + value);
        if (value == 1.0) {
            isDancing = false;
            animationPlayer.reset();
            memory.unsubscribeToEvent(eventSubscriptionIdStart);
            startConcierge();
        }
    }

    private void startConcierge() throws CallError, InterruptedException, IOException, SonosControllerException {
        //Start of tour - Move from door to switch cabinet
        loadTopicWithForceOutput("/home/nao/Willkommen.top");
        motion.moveTo(0f, 0f, new Float(Math.toRadians(180)));
        motion.moveTo(10f, 0f, 0f);
        motion.moveTo(0f, 0f, new Float(Math.toRadians(180)));
        loadTopicWithForceOutput("/home/nao/General.top");
        motion.moveTo(0f, 0f, new Float(Math.toRadians(-90)));

        //navigation.moveAlong("[\"Composed\", [\"Holonomic\", [\"Line\", [1.0, 0.0]], 0.0, 5.0], [\"Holonomic\", [\"Line\", [-1.0, 0.0]], 0.0, 10.0]]");
        //navigation.moveAlong(trajectory);
        
        //Move from switch cabinet to TV room
        motion.moveTo(10f, 0f, 0f);
        motion.moveTo(0f, 0f, new Float(Math.toRadians(90)));
        motion.moveTo(1f, 0f, new Float(Math.toRadians(-90)));
        motion.moveTo(2f, 0f, 0f);
        motion.moveTo(0f, 0f, new Float(Math.toRadians(180)));
        
        //Window open scene
        mQTTConnectionManager.subscribeToItems(Constants.MQTTTopics.Window.WINDOWS);
        String topic = loadTopic("/home/nao/TVRoom.top");
        dialog.forceOutput();
        //TODO: Testen Zeit in ms von >10sek
        dialog.setDelay("onWindowOpend", -1);
        dialog.forceOutput();
        dialog.setDelay("onWindowClosed", -1);
        dialog.forceOutput();
        
        //Gaming scene
        //Menschenerkennung einfügen
        
        dialog.setDelay("onFaceRecognizedDistance", -1);
        runGamingScene();
        dialog.forceOutput();
        unloadTopic(topic);
        
        //Move from TV room to working room
        motion.moveTo(2.5f, 0f, 0f);
        motion.moveTo(0f, 0f, new Float(Math.toRadians(-90)));
        motion.moveTo(2.5f, 0f, 0f);
        motion.moveTo(0f, 0f, new Float(Math.toRadians(-90)));
        motion.moveTo(2.0f, 0f, 0f);
        
        //Hier Arbeitszimmer einfügen
        
        float rFootFront = (float) memory.getData("Device/SubDeviceList/RFoot/FSR/FrontRight/Sensor/Value");
        System.out.println(rFootFront);
        
        

    }

    private void loadTopicWithForceOutput(String topicPath) throws CallError, InterruptedException {
        String topic = dialog.loadTopic(topicPath);
        dialog.activateTopic(topic);
        dialog.subscribe(Constants.APP_NAME);
        dialog.forceOutput();

        dialog.unsubscribe(Constants.APP_NAME);
        dialog.deactivateTopic(topic);
        dialog.unloadTopic(topic);
    }
    
    private String loadTopic(String topicPath) throws CallError, InterruptedException{
        String topic = dialog.loadTopic(topicPath);
        dialog.activateTopic(topic);
        dialog.subscribe(Constants.APP_NAME);
        return topic;
    }
    private void unloadTopic(String topic) throws CallError, InterruptedException{
        dialog.unsubscribe(Constants.APP_NAME);
        dialog.deactivateTopic(topic);
        dialog.unloadTopic(topic);
    }
    
    private void runGamingScene() throws IOException, SonosControllerException{
        SonosDevice sonos = new SonosDevice("192.168.0.30");
        sonos.playUri("x-rincon-mp3radio://http://listen.technobase.fm/tunein-mp3-pls", "Technobase.fm");
    }

    private void stateMachine(String step) {
        try {
            currentState = step;
            switch (step) {
                case Constants.Steps.STEP_STARUP:
                    motion.wakeUp();

                    //Configure the awareness of the robot
                    //humanDetectedId = 
                    memory.subscribeToEvent("ALBasicAwareness/HumanTracked", "onHumanTracked::(i)", this);

                    textToSpeech.say("Hallo");
                    break;

                case Constants.Steps.STEP_MOVE_AROUND:

                    //List of words that the robot will recognize
                    ArrayList<String> words1 = new ArrayList<>();
                    words1.add("vorwärts");
                    words1.add("links");
                    words1.add("rechts");
                    words1.add("stopp");
                    words1.add("schneller");
                    words1.add("langsamer");
                    words1.add("next");

                    speechRecognition.pause(true);
                    speechRecognition.setVocabulary(words1, Boolean.FALSE);
                    speechRecognition.pause(false);
                    speechRecognition.subscribe(Constants.APP_NAME);
                    memory.subscribeToEvent("WordRecognized", "onWordRecognizedForMoving::(m)", this);

                    textToSpeech.say("Wo soll ich hingehen?");
                    break;

                case Constants.Steps.STEP_FACERECOGNITION:
                    faceDetection.setRecognitionEnabled(true);
                    faceDetection.setTrackingEnabled(true);
                    faceDetection.subscribe(Constants.APP_NAME);

                    System.out.println("STEP_FACERECOGNITION");
                    memory.subscribeToEvent("FaceDetected", new EventCallback() {
                        @Override
                        public void onEvent(Object t) throws InterruptedException, CallError {
                            System.out.println(t.toString());
                            motion.stopMove();

                        }
                    });
                    motion.moveTo(0.0f, 0.0f, 1.4f);
                    break;

                case Constants.Steps.STEP_SOUNDLOCALIZATION:
                    //motion.setStiffnesses("WholeBody", 1);
                    motion.stiffnessInterpolation("WholeBody", 1.0f, 0.0f);
                    awareness.pauseAwareness();

                    soundLocalization.setParameter("Sensibility", 0.7);
                    //soundLocalization.subscribe(Constants.APP_NAME);

                    //Zweiten Tracker einführen
                    tracker.setMode("Navigate");
                    //tracker.registerTarget("Sound", new ArrayList<>(Arrays.asList(0.7f, 0.0f)));
                    tracker.registerTarget("People", new ArrayList<>());

                    tracker.track("People");
                    //tracker.track("Sound");

                    targetReachedId = memory.subscribeToEvent("ALTracker/TargetDetected", "onTargetDetected::(m)", this);

//                    if (tracker.getTargetPosition().size() > 0){
//                      motion.moveTo(tracker.getTargetPosition().get(0), tracker.getTargetPosition().get(2), 0.0f);
//
//                    }
                    //awareness.resumeAwareness();
//                    memory.subscribeToEvent("ALSoundLocalization/SoundLocated", new EventCallback() {
//                @Override
//                public void onEvent(Object t) throws InterruptedException, CallError {
//                    //System.out.println(t.toString());
//                    
//                    if (!isMoving){
//
//                    isMoving = false;
//                    }
//                }
//            });
                    break;

                case Constants.Steps.STEP_COCHLOVIUS:
                    //demo();
                    //animatedSpeech.async().say("^start(animations/Stand/Waiting/MysticalPower_1) Es werde Licht! ^wait(animations/Stand/Waiting/MysticalPower_1)");

                    //animationPlayer.run("animations/Stand/Waiting/Binoculars_1");
                    animatedSpeech.say("Was kann ich für dich tun?");

                    ArrayList<String> words2 = new ArrayList<>();
                    words2.add("licht ein");
                    words2.add("licht aus");
                    words2.add("spiele musik");

                    speechRecognition.pause(true);
                    speechRecognition.setVocabulary(words2, Boolean.FALSE);
                    speechRecognition.pause(false);
                    speechRecognition.subscribe(Constants.APP_NAME);
                    memory.subscribeToEvent("WordRecognized", "onWordRecognizedForMovingDemo::(m)", this);
                    //List<SonosDevice> devices = SonosDiscovery.discover();
                    //System.out.println(devices);

                    //SonosDevice sonos = new SonosDevice("192.168.0.30");
                    //sonos.playUri("x-rincon-mp3radio://http://listen.technobase.fm/tunein-mp3-pls", "Technobase.fm");
                    break;

                case Constants.Steps.STEP_DIALOG:
                    dialog.setLanguage(Constants.LANGUAGE);
                    //System.out.println(getClass().getClassLoader().getResource("TestDialog.top").getPath());
                    topic = dialog.loadTopic("/home/nao/TestDialog.top");
                    dialog.subscribe(Constants.APP_NAME);
                    dialog.activateTopic(topic);
                    break;

                case Constants.Steps.STEP_MQTT:
                    
                    //mQTTConnectionManager.publishToItem("Multimediawand_HUE1_Toggle", "ON");
                    while (true) {

                        mQTTConnectionManager.publishToItem("Multimediawand_HUE1_Toggle", "ON");
                        mQTTConnectionManager.publishToItem("Multimediawand_HUE6_Toggle", "ON");
                        mQTTConnectionManager.publishToItem("Multimediawand_HUE1_Toggle", "OFF");
                        mQTTConnectionManager.publishToItem("Multimediawand_HUE6_Toggle", "OFF");
                        Thread.sleep(300);

                        mQTTConnectionManager.publishToItem("Multimediawand_HUE2_Toggle", "ON");
                        mQTTConnectionManager.publishToItem("Multimediawand_HUE5_Toggle", "ON");
                        mQTTConnectionManager.publishToItem("Multimediawand_HUE2_Toggle", "OFF");
                        mQTTConnectionManager.publishToItem("Multimediawand_HUE5_Toggle", "OFF");
                        Thread.sleep(300);

                        mQTTConnectionManager.publishToItem("Multimediawand_HUE3_Toggle", "ON");
                        mQTTConnectionManager.publishToItem("Multimediawand_HUE4_Toggle", "ON");
                        mQTTConnectionManager.publishToItem("Multimediawand_HUE3_Toggle", "OFF");
                        mQTTConnectionManager.publishToItem("Multimediawand_HUE4_Toggle", "OFF");
                        Thread.sleep(300);
                    }

                //mQTTConnectionManager.subscribeToItem(Constants.MQTTTopics.Window.WINDOW_6);
                //break;
                case Constants.Steps.STEP_TRAJECTORY:
                    Object[] trajectory = new Object[3];
                    trajectory[0] = "Composed";
                    Object[] holonomic1 = new Object[4];
                    holonomic1[0] = "Holonomic";

                    Object[] line1 = new Object[2];
                    line1[0] = "Line";
                    line1[1] = new float[]{1.0f, 0.0f};

                    holonomic1[1] = line1;
                    holonomic1[2] = 0.0f;
                    holonomic1[3] = 5.0f;

                    trajectory[1] = holonomic1;

                    Object[] holonomic2 = new Object[4];
                    holonomic1[0] = "Holonomic";

                    Object[] line2 = new Object[2];
                    line1[0] = "Line";
                    line1[1] = new float[]{-1.0f, 0.0f};

                    holonomic1[1] = line2;
                    holonomic1[2] = 0.0f;
                    holonomic1[3] = 10.0f;

                    trajectory[1] = holonomic1;
                    trajectory[2] = holonomic2;
                    
                    navigation.moveAlong(trajectory);
                    break;

                case Constants.Steps.STEP_END:
                    //memory.unsubscribeToEvent(speechRecognitionId);
                    //speechRecognition.pause(true);
                    //speechRecognition.unsubscribe(Constants.APP_NAME);
                    //speechRecognition.removeAllContext();

                    dialog.deactivateTopic(topic);
                    dialog.unloadTopic(topic);
                    dialog.unsubscribe(Constants.APP_NAME);
                    dialog.resetAll();
                    //speechRecognition.stop(0);
                    //speechRecognition.exit();
                    //motion.rest();
                    //application.stop();
                    break;
            }
        } catch (Exception ex) {
            Logger.getLogger(BasicBehaviour.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void onTargetDetected(Object object) throws InterruptedException, CallError, Exception {
        //System.out.println(object.toString());
        memory.unsubscribeToEvent(targetReachedId);
        System.out.println("TargetPosition: " + tracker.getTargetPosition().toString());
        //List<Float> values = tracker.getTargetPosition();
        try {
            if (tracker.getTargetPosition().size() > 1) {
                motion.async().moveTo(tracker.getTargetPosition().get(0), tracker.getTargetPosition().get(1), tracker.getTargetPosition().get(2));

            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println(e.getLocalizedMessage());
        }
        targetReachedId = memory.subscribeToEvent("ALTracker/TargetDetected", "onTargetDetected::(m)", this);

    }

    public void onHumanTracked(Integer humanID) {
        System.out.println("onHumanTracked: " + humanID);
        if (humanID >= 0) {
            System.out.println("onHumanTracked");

            //stateMachine(Constants.Steps.STEP_MOVE_AROUND);
            try {
                awareness.stopAwareness();
                memory.unsubscribeToEvent(humanDetectedId);

                textToSpeech.say("Mensch gefunden");
                stateMachine(Constants.Steps.STEP_SOUNDLOCALIZATION);
            } catch (CallError ex) {
                Logger.getLogger(BasicBehaviour.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(BasicBehaviour.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void onTouchEnd(Float value) {
        System.out.println(Constants.APP_NAME + " : Touch " + value);
        if (value == 1.0) {
            stateMachine(Constants.Steps.STEP_END);
        }
    }

    public void onWordRecognizedForMoving(Object words) throws InterruptedException, CallError {
        //textToSpeech.say("Wort gefunden");
        speechRecognition.pause(true);
        awareness.pauseAwareness();

        String word = ((List<String>) words).get(0);
        System.out.println("Word " + word);

        //textToSpeech.say(word);
        if (word.equals("next")) {
//            x = 0;
//            y = 0;
//            teta = 0;
//            motion.moveToward(x, y, teta);
//            alMemory.unsubscribeToEvent("WordRecognized", "onWordRecognizedForMoving::(m)");
//            alSpeechRecognition.unsubscribe(APP_NAME);
//            stateMachine(STEP_JOKES);
            awareness.resumeAwareness();

        } else {
            switch (word) {
                case "vorwärts":
                    x = 1.0f;
                    teta = 0.0f;
                    break;
                case "links":
                    x = 0.0f;
                    teta = 1.0f;
                    break;
                case "rechts":
                    x = 0.0f;
                    teta = -1.0f;
                    break;
                case "stopp":
                    x = 0f;
                    y = 0f;
                    teta = 0f;
                    break;
                case "schneller":
                    if (x > 0) {
                        x += 0.2;
                    } else {
                        x -= 0.2;
                    }
                    break;
                case "langsamer":
                    if (x < 0) {
                        x += 0.2;
                    } else {
                        x -= 0.2;
                    }
                    break;
                default:
                    break;
            }

            motion.moveTo(x, y, teta);
            //Thread.sleep(1000);
            textToSpeech.say("Wohin jetzt?");
            speechRecognition.pause(false);
        }

    }

    public void demo() throws CallError, InterruptedException, Exception {
        //stateMachine(Constants.Steps.STEP_STARUP);
        //humanDetectedDemoId = memory.subscribeToEvent("ALBasicAwareness/HumanTracked", "onHumanTrackedDemo::(i)", this);

        String[] jointNames = new String[]{"LShoulderPitch", "LShoulderRoll", "LElbowYaw", "LElbowRoll"};
        double[] arm1 = {Math.toRadians(-40), Math.toRadians(25), Math.toRadians(0), Math.toRadians(-40)};

        //double[] arm2 = {-40, 25, 0, -40};
        System.out.println(arm1);
        motion.angleInterpolationWithSpeed(jointNames, arm1, 0.7f);
        //animationPlayer.run("animations/Stand/BodyTalk/BodyTalk_9");

    }

    public void onHumanTrackedDemo(Integer humanID) throws Exception {
        System.out.println("onHumanTracked: " + humanID);
        if (humanID >= 0) {
            System.out.println("onHumanTracked");

            //stateMachine(Constants.Steps.STEP_MOVE_AROUND);
            try {
                awareness.pauseAwareness();
                memory.unsubscribeToEvent(humanDetectedDemoId);

                textToSpeech.say("Mensch gefunden");
                animatedSpeech.say("Hallo i bims 1 dummer Roboter. ^startSound(Aldebaran/enu_ono_laugh_10)");
                animatedSpeech.say("Ich bin so ein schlauer Hengst, ich kann dir sogar mit meinem Kopf folgen. Was sagst du dazu?");
                awareness.resumeAwareness();

                ArrayList<String> words1 = new ArrayList<>();
                words1.add("vorwärts");
                words1.add("links");
                words1.add("rechts");
                words1.add("stopp");
                words1.add("schneller");
                words1.add("langsamer");
                words1.add("next");

                speechRecognition.pause(true);
                speechRecognition.setVocabulary(words1, Boolean.FALSE);
                speechRecognition.pause(false);
                speechRecognition.subscribe(Constants.APP_NAME);
                memory.subscribeToEvent("WordRecognized", "onWordRecognizedForMovingDemo::(m)", this);

                //humanDetectedDemoId = memory.subscribeToEvent("ALBasicAwareness/HumanTracked", "onHumanTrackedDemo::(i)", this);
            } catch (CallError ex) {
                Logger.getLogger(BasicBehaviour.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(BasicBehaviour.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void onWordRecognizedForMovingDemo(Object words) throws InterruptedException, CallError, IOException, SonosControllerException {
        speechRecognition.pause(true);
        awareness.pauseAwareness();

        String word = ((List<String>) words).get(0);
        System.out.println("Word " + word);

        switch (word) {
            case "licht ein":
                animatedSpeech.async().say("^start(animations/Stand/Waiting/MysticalPower_1) Es werde Licht! ^wait(animations/Stand/Waiting/MysticalPower_1)");

                connectionManager.sendPostRequest("items/Multimediawand_HUE6_Toggle", "ON");
                connectionManager.sendPostRequest("items/HMScheibentransparenz2_1_State", "OFF");
                restartSpeecheRecognition();
                break;

            case "licht aus":
                connectionManager.sendPostRequest("items/Multimediawand_HUE6_Toggle", "OFF");
                connectionManager.sendPostRequest("items/HMScheibentransparenz2_1_State", "ON");
                restartSpeecheRecognition();
                break;

            case "spiele musik":
                SonosDevice sonos = new SonosDevice("192.168.0.30");
                sonos.playUri("x-rincon-mp3radio://http://listen.technobase.fm/tunein-mp3-pls", "Technobase.fm");

                restartSpeecheRecognition();
                break;
        }

    }

    public void restartSpeecheRecognition() throws CallError, InterruptedException {
        animatedSpeech.say("Und was jetzt?");
        speechRecognition.pause(false);
    }

    public void onLightSwitch(String value) {
        System.out.println(Constants.APP_NAME + " : LightSwitch " + value);
        connectionManager.sendPostRequest("items/Multimediawand_HUE6_Toggle", value);
    }

    @Override
    public void onSubscription(String item, String value) {
        String itemDescription = item.split("\\/")[3];

        switch (itemDescription) {
            case Constants.MQTTTopics.Window.WINDOW_1:
            case Constants.MQTTTopics.Window.WINDOW_2:
            case Constants.MQTTTopics.Window.WINDOW_3:
            case Constants.MQTTTopics.Window.WINDOW_4:
            case Constants.MQTTTopics.Window.WINDOW_5:
            case Constants.MQTTTopics.Window.WINDOW_6:
            case Constants.MQTTTopics.Window.WINDOW_7:
            case Constants.MQTTTopics.Window.WINDOW_8:
            case Constants.MQTTTopics.Window.WINDOW_9:

                try {
                    if (value.equals("OPEN")) {
                        System.out.println("onSubscription: " + itemDescription + " " + value);
                        memory.raiseEvent("onWindowOpend", itemDescription);
                        //animatedSpeech.say("Oh, ich sehe, ein Fenster ist geöffnet");
                    } else if (value.equals("CLOSED")) {
                        //animatedSpeech.say("Sehr gut, das Fenster ist wieder geschlossen.");
                        System.out.println("onSubscription: " + itemDescription + " " + value);
                        memory.raiseEvent("onWindowClosed", itemDescription);
                        mQTTConnectionManager.unsubscribeOfItems(Constants.MQTTTopics.Window.WINDOWS);
                    }
                } catch (CallError ex) {
                    Logger.getLogger(BasicBehaviour.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InterruptedException ex) {
                    Logger.getLogger(BasicBehaviour.class.getName()).log(Level.SEVERE, null, ex);
                }

                break;

        }
    }

}

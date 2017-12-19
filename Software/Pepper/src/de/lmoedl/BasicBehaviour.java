/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.lmoedl;

import com.aldebaran.qi.Application;
import com.aldebaran.qi.CallError;
import com.aldebaran.qi.Future;
import com.aldebaran.qi.Session;
import com.aldebaran.qi.helper.ALTabletService;
import com.aldebaran.qi.helper.EventCallback;
import com.aldebaran.qi.helper.proxies.ALAnimatedSpeech;
import com.aldebaran.qi.helper.proxies.ALAnimationPlayer;
import com.aldebaran.qi.helper.proxies.ALBasicAwareness;
import com.aldebaran.qi.helper.proxies.ALDialog;
import com.aldebaran.qi.helper.proxies.ALFaceDetection;
import com.aldebaran.qi.helper.proxies.ALMemory;
import com.aldebaran.qi.helper.proxies.ALMotion;
import com.aldebaran.qi.helper.proxies.ALNavigation;
import com.aldebaran.qi.helper.proxies.ALSonar;
import com.aldebaran.qi.helper.proxies.ALSoundLocalization;
import com.aldebaran.qi.helper.proxies.ALSpeechRecognition;
import com.aldebaran.qi.helper.proxies.ALTextToSpeech;
import com.aldebaran.qi.helper.proxies.ALTracker;
import com.vmichalak.sonoscontroller.SonosDevice;
import com.vmichalak.sonoscontroller.exception.SonosControllerException;
import de.lmoedl.interfaces.MQTTSubscriberCallbackInterface;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author lothar
 */
public class BasicBehaviour implements MQTTSubscriberCallbackInterface {

    private Session session;
    private Application application;
    //for testing true, else false
    private boolean isFullConcierge = true;

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
    private ALTabletService tabletService;

    private ConnectionManager connectionManager;
    private MQTTConnectionManager mQTTConnectionManager;
    private SonosDevice sonos;

    private String currentState;

    private long targetReachedId = 0;
    private long humanDetectedId = 0;
    private long humanDetectedDemoId = 0;
    private long speechRecognitionId = 0;
    private long eventSubscriptionIdStart = 0;
    private long windowOpendID = 0;
    private long windowClosedID = 0;
    private long redballID = 0;

    private boolean isDancing = true;
    private boolean isPlaying = true;

    private String topic;

    private Logger logger;
    private FileHandler fh;

    public BasicBehaviour(Application application) {
        if (Constants.Config.DEBUG) {
            logger = Logger.getLogger(BasicBehaviour.class.getName());
            SimpleDateFormat sdf = new SimpleDateFormat("dd_MM_yyyy_HH_mm");
            try {
                fh = new FileHandler("pepper_" + sdf.format(new Date()) + ".log");
                logger.addHandler(fh);
                SimpleFormatter formatter = new SimpleFormatter();
                fh.setFormatter(formatter);
            } catch (IOException | SecurityException ex) {
                log(Level.SEVERE, ex);
            }
        }

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
            tabletService = new ALTabletService(session);
            sonos = new SonosDevice(Constants.Config.SONOS_URL);

            config();
        } catch (Exception ex) {
            log(Level.SEVERE, ex);
        }

        //stateMachine(Constants.Steps.STEP_DIALOG);

    }

    /*    public void run() throws CallError, InterruptedException, Exception {
        eventSubscriptionIdStart = memory.subscribeToEvent("RearTactilTouched", "onTouchHead::(f)", this);
        while (isDancing) {
            animationPlayer.run("animations/Stand/Waiting/AirGuitar_1");
        }

    }*/
    private void config() throws CallError, InterruptedException, Exception {
        textToSpeech.setLanguage(Constants.LANGUAGE);
        dialog.setLanguage(Constants.LANGUAGE);
        motion.setExternalCollisionProtectionEnabled("All", true);
        motion.setWalkArmsEnabled(Boolean.TRUE, Boolean.TRUE);
        speechRecognition.pause(true);
        speechRecognition.setLanguage(Constants.LANGUAGE);
        speechRecognition.pause(false);

        tabletService.enableWifi();
        tabletService.hideWebview();

        motion.setOrthogonalSecurityDistance(0.15f);
        motion.wakeUp();
        memory.subscribeToEvent("RearTactilTouched", "onTouchEnd::(f)", this);

        memory.subscribeToEvent("PlayMusic", "onPlayMusic::(s)", this);

        memory.subscribeToEvent("SubscribeMQTTTopic", "onSubscribeMQTTTopic::(s)", this);
        memory.subscribeToEvent("UnsubscribeMQTTTopic", "onUnsubscribeMQTTTopic::(s)", this);
        memory.subscribeToEvent("GetValue", "onGetValue::(s)", this);
        memory.subscribeToEvent("UnsubscribeMQTTTopic", "onUnsubscribeMQTTTopic::(s)", this);
        memory.subscribeToEvent("PublishMQTTMessage", "onPublishMQTTMessage::(s)", this);
        memory.subscribeToEvent("SpeechRecognitionOff", "onSpeechRecognitionOff::(s)", this);
        //startConcierge();

        memory.subscribeToEvent("ALMotion/MoveFailed", new EventCallback() {
            @Override
            public void onEvent(Object t) throws InterruptedException, CallError {
                log(Level.SEVERE, "movefailed: " + t.toString());
            }
        });
        memory.subscribeToEvent("Navigation/AvoidanceNavigator/ObstacleDetected", new EventCallback() {
            @Override
            public void onEvent(Object t) throws InterruptedException, CallError {
                System.out.println("obstacleDetected: " + t.toString());
            }
        });

        memory.subscribeToEvent("Navigation/SafeNavigator/Status", new EventCallback() {
            @Override
            public void onEvent(Object t) throws InterruptedException, CallError {
                System.out.println("navigationStatus: " + t.toString());
            }
        });

        /*textToSpeech.say("Alexa");
        Thread.sleep(1000);
        textToSpeech.say(" schalte Privatmodus ein");*/
        //resumeTvRoom();
        //tvRoom();
        //goodby();
        //Window open scene
        /*mQTTConnectionManager.subscribeToItem(Constants.MQTTTopics.Window.WINDOW_4);
        loadTopic("/home/nao/TVRoom.top");
        dialog.forceInput("xxx");
        dialog.forceOutput();*/

        //eventSubscriptionIdStart = memory.subscribeToEvent("RearTactilTouched", "onTouchHead::(f)", this);
    }

    public void onTouchHead(Float value) throws InterruptedException, CallError, IOException, SonosControllerException, Exception {
        log(Constants.APP_NAME + " : Touch " + value);
        if (value == 1.0) {
            animationPlayer.run("animations/Stand/Waiting/AirGuitar_1");

            memory.unsubscribeToEvent(eventSubscriptionIdStart);

            startConcierge();

            if (Constants.Config.DEBUG) {
                memory.subscribeToEvent("RearTactilTouched", "onTouchEnd::(f)", this);
            }
        }
    }

    public void startConcierge() throws CallError, InterruptedException, IOException, SonosControllerException, Exception {
        boolean success;
        motion.wakeUp();
        //Start of tour - Move from door to switch cabinet
        loadTopic("/home/nao/Welcome.top");
        dialog.forceInput("xxx");
        dialog.forceOutput();
        unloadTopic();
        success = (boolean) motion.call("moveTo", 0f, 0f, new Float(Math.toRadians(180))).get();

        List<Float> old = motion.getRobotPosition(false);
        success = (boolean) motion.call("moveTo", 5.4f, 0f, 0f).get();
        log("startConcierge_\"moveTo\", 5.4f, 0f, 0f_" + success);
        System.out.println(success);

        success = (boolean) motion.call("moveTo", 0f, 0f, new Float(Math.toRadians(180))).get();

        //Kopf hoch
        loadTopicWithForceOutput("/home/nao/General.top");
        success = (boolean) motion.call("moveTo", 0f, 0f, new Float(Math.toRadians(-90))).get();

        //Move from switch cabinet to TV room
        old = motion.getRobotPosition(false);
        success = (boolean) motion.call("moveTo", 6f, 0f, 0f).get();
        log("startConcierge_\"moveTo\", 6f, 0f, 0f_" + success);
        checkDestination(success, old, motion.getRobotPosition(false), new float[]{5f, 0f, 0f});
        old = motion.getRobotPosition(false);
        success = navigation.navigateTo(3f, 0f);
        log("startConcierge_\"navigateTo\", 3f, 0f, 0f_" + success);
        checkDestination(success, old, motion.getRobotPosition(false), new float[]{3f, 0f, 0f});
        /*if (!success) {
                    old = motion.getRobotPosition(false);
                    success = navigation.navigateTo(2f, 0.5f);
                    System.out.println(success);
                    checkDestination(success, old, motion.getRobotPosition(false), new float[]{2f, 0.5f, 0f});
                }*/

        success = (boolean) motion.call("moveTo", 0f, 0f, new Float(Math.toRadians(180))).get();

        if (isFullConcierge) {
            tvRoom();
        }
    }

    public void tvRoom() throws Exception {
        log("tvRoom");
        boolean success;
        //Window open scene
        windowOpendID = memory.subscribeToEvent("WindowOpend", "onWindowOpend::(s)", this);
        windowClosedID = memory.subscribeToEvent("WindowClosed", "onWindowClosed::(s)", this);
        mQTTConnectionManager.subscribeToItems(Constants.MQTTTopics.Window.WINDOWS);
        loadTopic("/home/nao/TVRoom.top");
        dialog.forceOutput();
    }

    private void loadTopicWithForceOutput(String topicPath) {
        try {
            this.topic = dialog.loadTopic(topicPath);
            dialog.activateTopic(this.topic);
            dialog.subscribe(Constants.APP_NAME);
            dialog.forceOutput();

            dialog.unsubscribe(Constants.APP_NAME);
            dialog.deactivateTopic(this.topic);
            dialog.unloadTopic(this.topic);
        } catch (CallError | InterruptedException ex) {
            Logger.getLogger(BasicBehaviour.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void loadTopic(String topicPath) {
        try {
            this.topic = dialog.loadTopic(topicPath);
            dialog.activateTopic(this.topic);
            dialog.subscribe(Constants.APP_NAME);
        } catch (CallError | InterruptedException ex) {
            log(Level.SEVERE, ex);
            Logger.getLogger(BasicBehaviour.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void unloadTopic() {
        try {
            dialog.unsubscribe(Constants.APP_NAME);
            dialog.deactivateTopic(this.topic);
            dialog.unloadTopic(this.topic);
        } catch (CallError | InterruptedException ex) {
            log(Level.SEVERE, ex);
            Logger.getLogger(BasicBehaviour.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void runGamingScene() throws IOException, SonosControllerException, InterruptedException, CallError {

        log("runGamingScene");
        new Thread(new Runnable() {
            @Override
            public void run() {
                connectionManager.getRequest(Constants.Config.MOVIE_URL);
            }
        }).start();

        animationPlayer.run("animations/Stand/Waiting/DriveCar_1");
        animationPlayer.run("animations/Stand/Waiting/DriveCar_1");
    }

    public void onSubscribeMQTTTopic(String mqttTopic) throws CallError, InterruptedException {
        log("onSubscribeMQTTTopic: " + mqttTopic);
        mQTTConnectionManager.subscribeToItem(topic);
        /*String[] res = connectionManager.openHabGetRequest(mqttTopic, "state");
        logger.info("onSubscribeMQTTTopic_" + res[0] + " " + res[1]);
        if (res != null) {
            memory.insertData(res[0], res[1]);
        }*/
    }

    public void onGetValue(String mqttTopic) throws CallError, InterruptedException {
        String[] res = connectionManager.openHabGetRequest(mqttTopic, "state");
        log("onSubscribeMQTTTopic_" + res[0] + " " + res[1]);
        if (res != null) {
            memory.insertData(res[0], res[1]);
        }
    }

    public void onUnsubscribeMQTTTopic(String mqttTopic) {
        log("onUnsubscribeMQTTTopic: " + mqttTopic);
        mQTTConnectionManager.unsubscribeOfItem(mqttTopic);
    }

    public void onPublishMQTTMessage(String value) {
        String[] values = value.split(";");
        log("topic: " + values[0] + " payload: " + values[1]);
        mQTTConnectionManager.publishToItem(values[0], values[1]);
    }

    public void onWindowOpend(String item) throws CallError, InterruptedException {
        log("onWindowOpend: " + item);
        dialog.forceOutput();
        memory.unsubscribeToEvent(windowOpendID);
    }

    public void onWindowClosed(String item) throws CallError, InterruptedException, IOException, SonosControllerException, Exception {
        log("onWindowClosed: " + item);
        memory.unsubscribeToEvent(windowClosedID);
        dialog.forceOutput();

        resumeTvRoom();
    }

    public void onSpeechRecognitionOff(String state) throws IOException, SonosControllerException {
        log("onSpeechRecognitionOff: " + state);
        stateMachine(Constants.Steps.STEP_END);
        try {
            boolean isUrlLoaded = tabletService.loadUrl(Constants.Config.RATING_URL);
            log("goodby_isUrlLoaded: " + isUrlLoaded);
            boolean isShowWebview = tabletService.showWebview();
            log("goodby_isShowWebview: " + isShowWebview);
            loadTopic("/home/nao/Goodby.top");
            dialog.forceOutput();
            //animationPlayer.async().run("animations/Stand/Waiting/FunnyDancer_1");
            //lightShow();
            dialog.forceOutput();
            unloadTopic();

            sonos.setBass(0);
            sonos.setVolume(40);
            sonos.playUri("x-rincon-mp3radio://http://listen.technobase.fm/tunein-mp3-pls", "Technobase.fm");
        } catch (CallError | InterruptedException ex) {
            log(Level.SEVERE, ex);
        }

    }

    private void resumeTvRoom() throws CallError, InterruptedException, IOException, SonosControllerException, Exception {
        log("resumeTvRoom");
        boolean success;

        //mQTTConnectionManager.unsubscribeOfItems(Constants.MQTTTopics.Window.WINDOWS);
        //Gaming scene

        runGamingScene();
        dialog.forceOutput();
        dialog.forceOutput();
        System.out.println("Topic to unsubscribe");
        unloadTopic();
        List<Float> old = motion.getRobotPosition(false);
        success = navigation.navigateTo(2f, 0f);
        log("resumeTvRoom\"navigateTo\", 2f, 0f, 0f_" + success);
        checkDestination(success, old, motion.getRobotPosition(false), new float[]{2f, 0f, 0f});

        //Move from TV room to working room
        success = (boolean) motion.call("moveTo", 0f, 0f, new Float(Math.toRadians(-90))).get();

        old = motion.getRobotPosition(false);
        if (success) {
            success = (boolean) motion.call("moveTo", 2.5f, 0f, 0f).get();
            log("resumeTvRoom_if(success)_\"moveTo\", 2.5f, 0f, 0f_" + success);
        } else {
            success = navigation.navigateTo(2.5f, 0f);
            log("resumeTvRoom_else_\"navigateTo\", 2.5f, 0f, 0f_" + success);
            checkDestination(success, old, motion.getRobotPosition(false), new float[]{2.5f, 0f, 0f});
        }

        success = navigation.navigateTo(0f, 0f, new Float(Math.toRadians(-90)));

        old = motion.getRobotPosition(false);
        success = navigation.navigateTo(2.0f, 0f);
        log("resumeTvRoom_\"navigateTo\", 2f, 0f, 0f_" + success);
        checkDestination(success, old, motion.getRobotPosition(false), new float[]{2f, 0f, 0f});

        motion.call("moveTo", 0f, 0f, new Float(Math.toRadians(180))).get();

        if (isFullConcierge) {

            workingRoom();

        }

    }

    public void workingRoom() throws CallError, InterruptedException, IOException, SonosControllerException, Exception {
        boolean success;
        log("Workingroom");
        loadTopicWithForceOutput("/home/nao/WorkingRoom.top");
        List<Float> old = motion.getRobotPosition(false);

        success = (boolean) motion.call("moveTo", 2.7f, 0f, 0f).get();
        log("workingRoom_\"moveTo\", 2.7f, 0f, 0f_" + success);
        checkDestination(success, old, motion.getRobotPosition(false), new float[]{2.7f, 0f, 0f});

        success = navigation.navigateTo(0f, 0f, new Float(Math.toRadians(-90)));
        old = motion.getRobotPosition(false);
        success = navigation.navigateTo(2.2f, 0f);
        log("workingRoom_\"navigateTo\", 2.2f, 0f, 0f_" + success);
        checkDestination(success, old, motion.getRobotPosition(false), new float[]{2.2f, 0f, 0f});
        success = navigation.navigateTo(0f, 0f, new Float(Math.toRadians(180)));

        if (isFullConcierge) {
            bathRoom();
        }
    }

    public void bathRoom() throws CallError, InterruptedException, IOException, SonosControllerException, Exception {
        boolean success;
        log("Bathroom");
        loadTopic("/home/nao/Bathroom.top");
        dialog.forceOutput();
        dialog.forceOutput();
        dialog.forceOutput();
        unloadTopic();

        List<Float> old = motion.getRobotPosition(false);

        success = (boolean) motion.call("moveTo", 1f, 0f, 0f).get();
        log("bathroom_\"moveTo\", 1f, 0f, 0f_" + success);
        checkDestination(success, old, motion.getRobotPosition(false), new float[]{1f, 0f, 0f});
        success = (boolean) motion.call("moveTo", 0f, 0f, new Float(Math.toRadians(-90))).get();
        old = motion.getRobotPosition(false);
        success = navigation.navigateTo(3f, 0f);

        log("bathroom_\"navigateTo\", 2.8f, 0f, 0f_" + success);
        checkDestination(success, old, motion.getRobotPosition(false), new float[]{2.8f, 0f, 0f});
        success = (boolean) motion.call("moveTo", 0f, 0f, new Float(Math.toRadians(-90))).get();
        old = motion.getRobotPosition(false);
        success = navigation.navigateTo(2f, 0f);
        log("bathroom_\"navigateTo\", 2f, 0f, 0f_" + success);
        checkDestination(success, old, motion.getRobotPosition(false), new float[]{2f, 0f, 0f});
        success = navigation.navigateTo(0f, 0f, new Float(Math.toRadians(180)));

        if (isFullConcierge) {
            kitchen();
        }
    }

    public void kitchen() throws CallError, InterruptedException, IOException, SonosControllerException, Exception {
        boolean success;
        log("Kitchen");
        loadTopicWithForceOutput("/home/nao/Kitchen.top");

        success = (boolean) motion.call("moveTo", 1f, 0f, 0f).get();
        log("kitchen_\"moveTo\", 1f, 0f, 0f_" + success);
        success = (boolean) motion.call("moveTo", 0f, 0f, new Float(Math.toRadians(-90))).get();
        List<Float> old = motion.getRobotPosition(false);
        success = (boolean) motion.call("moveTo", 3f, 0f, 0f).get();
        log("kitchen_\"moveTo\", 3f, 0f, 0f_" + success);
        checkDestination(success, old, motion.getRobotPosition(false), new float[]{3f, 0f, 0f});
        success = (boolean) motion.call("moveTo", 0f, 0f, new Float(Math.toRadians(180))).get();

        if (isFullConcierge) {
            goodby();
        }
    }

    public void goodby() throws CallError, InterruptedException, IOException, SonosControllerException {
        log("goodby");
        //boolean isWifiConfig = tabletService.configureWifi("wpa", "SHLAB02-5", "91054319");
        //System.out.println("isWifiConfig: " + isWifiConfig);
        //boolean isConnectWifi = tabletService.connectWifi("SHLAB02-5");
        //System.out.println("isConnectWifi: " + isConnectWifi);
        loadTopic("/home/nao/Questions.top");

        log("ByeBye");
    }

    private float calcDistanceToWalk(float parameter) {
        if (parameter > 0.2) {
            if (parameter <= 3.0) {
                return parameter;
            } else {
                return 3.0f;
            }
        }
        return 0f;
    }

    public void checkDestination(boolean success, List<Float> oldPosition, List<Float> newPosition, float[] shouldGo) throws CallError, InterruptedException, Exception {

        if (success) {
            return;
        }

        List<Float> oldP = oldPosition;
        List<Float> newP = newPosition;
        float[] sGo = shouldGo;

        /*
        //old way
        float diffX = Math.abs(oldPosition.get(0) - newPosition.get(0));
        float diffY = Math.abs(oldPosition.get(1) - newPosition.get(1));
        float diffZ = Math.abs(oldPosition.get(2) - newPosition.get(2));

        success = false;
        int counter = 0;

        while (!success && counter <= 10) {
            if ((shouldGo[0] - diffX) > 0.2 || (shouldGo[1] - diffY) > 0.2 || (shouldGo[2] - diffZ) > 0.2) {
                float x = calcDistanceToWalk(shouldGo[0] - diffX);
                float y = calcDistanceToWalk(shouldGo[1] - diffY);
                float z = calcDistanceToWalk(shouldGo[2] - diffZ);
                System.out.println("corrigationVector: " + x + " " + y + " " + z);

                //motion.moveTo(x, y, z);
                success = navigation.navigateTo(x, y);

                pauseProgramm();
                counter++;
            }
        }*/
        success = false;
        int counter = 0;

        while (!success && counter <= 10) {
            float diffX = Math.abs(newP.get(0) - oldP.get(0));
            float diffY = Math.abs(newP.get(1) - oldP.get(1));
            float diffZ = Math.abs(newP.get(2) - oldP.get(2));
            oldP = motion.getRobotPosition(false);

            if ((sGo[0] - diffX) > 0.2 || (sGo[1] - diffY) > 0.2 || (sGo[2] - diffZ) > 0.2) {
                float x = calcDistanceToWalk(sGo[0] - diffX);
                float y = calcDistanceToWalk(sGo[1] - diffY);
                float z = calcDistanceToWalk(sGo[2] - diffZ);
                log("corrigationVector: " + x + " " + y + " " + z);

                //motion.moveTo(x, y, z);
                success = navigation.navigateTo(x, y);

                newP = motion.getRobotPosition(false);
                sGo[0] = x;
                sGo[1] = y;
                sGo[2] = z;

                pauseProgramm();
                counter++;
            }
        }
    }

    private void pauseProgramm() {
        pauseProgramm(3000L);
    }

    private void pauseProgramm(long pauseTime) {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < pauseTime) {
        }
    }

    private void lightShow() {
        try {
            mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Shutter.MAIN_SHUTTER_1, "DOWN");
            mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Shutter.MAIN_SHUTTER_2, "DOWN");
            mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Shutter.MAIN_SHUTTER_3, "DOWN");
            Thread.sleep(6000);

            sonos.setVolume(70);
            sonos.setBass(7);
            sonos.playUri("x-file-cifs://192.168.0.10/Medialib/Audio/Pepper/NEFFEX-Unstoppable.mp3", null);

            mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.TVRoom.HUE_1_COLOR, "10,100,100");
            mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.TVRoom.HUE_2_COLOR, "90,100,100");
            mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.TVRoom.HUE_3_COLOR, "100,100,100");
            mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.TVRoom.HUE_4_COLOR, "50,100,100");
            mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.TVRoom.HUE_5_COLOR, "20,100,100");
            mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.TVRoom.HUE_6_COLOR, "30,100,100");
            mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.TVRoom.HUE_1, "OFF");
            mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.TVRoom.HUE_2, "OFF");
            mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.TVRoom.HUE_3, "OFF");
            mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.TVRoom.HUE_4, "OFF");
            mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.TVRoom.HUE_5, "OFF");
            mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.TVRoom.HUE_6, "OFF");
            Thread.sleep(5000);
            mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.TVRoom.HUE_6_COLOR, "30,100,100");
            mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.TVRoom.HUE_5_COLOR, "20,100,100");
            mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.TVRoom.HUE_4_COLOR, "50,100,100");
            mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.TVRoom.HUE_3_COLOR, "100,100,100");
            mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.TVRoom.HUE_2_COLOR, "90,100,100");
            mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.TVRoom.HUE_1_COLOR, "10,100,100");
            mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.TVRoom.HUE_6, "OFF");
            mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.TVRoom.HUE_5, "OFF");
            mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.TVRoom.HUE_4, "OFF");
            mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.TVRoom.HUE_3, "OFF");
            mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.TVRoom.HUE_2, "OFF");
            mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.TVRoom.HUE_1, "OFF");
            Thread.sleep(5000);
            mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.TVRoom.HUE_1_COLOR, "10,100,100");
            mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.TVRoom.HUE_2_COLOR, "90,100,100");
            mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.TVRoom.HUE_3_COLOR, "100,100,100");
            mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.TVRoom.HUE_4_COLOR, "50,100,100");
            mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.TVRoom.HUE_5_COLOR, "20,100,100");
            mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.TVRoom.HUE_6_COLOR, "30,100,100");
            mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.TVRoom.HUE_1, "OFF");
            mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.TVRoom.HUE_2, "OFF");
            mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.TVRoom.HUE_3, "OFF");
            mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.TVRoom.HUE_4, "OFF");
            mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.TVRoom.HUE_5, "OFF");
            mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.TVRoom.HUE_6, "OFF");
            Thread.sleep(4000);

            Random random = new Random();
            int rand = random.nextInt(300);

            int maxLight = 300;
            int sleep = 30;

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    isPlaying = false;
                }
            }, 23000);

            while (isPlaying) {
                mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.TVRoom.HUE_1_COLOR, rand + ",100,100");
                Thread.sleep(sleep);
                mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.TVRoom.HUE_2, "OFF");
                Thread.sleep(sleep);
                rand = random.nextInt(maxLight);
                mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.TVRoom.HUE_4_COLOR, rand + ",100,100");
                Thread.sleep(sleep);
                mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.Doors.TVROOM_DOOR_1, "ON");
                Thread.sleep(sleep);
                rand = random.nextInt(maxLight);
                mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.Bathroom.HUE_2_COLOR, rand + ",100,100");
                Thread.sleep(sleep);
                mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.Bathroom.HUE_1, "OFF");
                Thread.sleep(sleep);
                mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Kitchen.RANGE_HOOD_LIGHT, "ON");
                Thread.sleep(sleep);
                mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.TVRoom.HUE_1, "OFF");
                Thread.sleep(sleep);
                rand = random.nextInt(maxLight);
                mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.Bathroom.HUE_6_COLOR, rand + ",100,100");
                Thread.sleep(sleep);
                rand = random.nextInt(maxLight);
                mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.TVRoom.HUE_5_COLOR, rand + ",100,100");
                Thread.sleep(sleep);
                mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.TVRoom.HUE_4, "OFF");
                Thread.sleep(sleep);
                rand = random.nextInt(maxLight);
                mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.TVRoom.HUE_3_COLOR, rand + ",100,100");
                Thread.sleep(sleep);
                mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.TVRoom.HUE_5, "OFF");
                Thread.sleep(sleep);
                mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.Doors.TVROOM_DOOR_2, "ON");
                Thread.sleep(sleep);
                rand = random.nextInt(maxLight);
                mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.Bathroom.HUE_4_COLOR, rand + ",100,100");
                Thread.sleep(sleep);
                mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.Bathroom.HUE_6, "OFF");
                Thread.sleep(sleep);
                mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.Doors.TVROOM_DOOR_1, "OFF");
                Thread.sleep(sleep);
                mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Kitchen.RANGE_HOOD_LIGHT, "OFF");
                Thread.sleep(sleep);
                rand = random.nextInt(maxLight);
                mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.Bathroom.HUE_5_COLOR, rand + ",100,100");
                Thread.sleep(sleep);
                mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.TVRoom.HUE_5, "OFF");
                Thread.sleep(sleep);
                rand = random.nextInt(maxLight);
                mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.TVRoom.HUE_2_COLOR, rand + ",100,100");
                Thread.sleep(sleep);
                mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.Bathroom.HUE_2, "OFF");
                Thread.sleep(sleep);
                rand = random.nextInt(maxLight);
                mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.Bathroom.HUE_1_COLOR, rand + ",100,100");
                Thread.sleep(sleep);
                mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.TVRoom.HUE_3, "OFF");
                Thread.sleep(sleep);
                rand = random.nextInt(maxLight);
                mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.Bathroom.HUE_5_COLOR, rand + ",100,100");
                Thread.sleep(sleep);
                mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.Doors.TVROOM_DOOR_2, "OFF");
                Thread.sleep(sleep);
                mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.TVRoom.HUE_5, "OFF");
                Thread.sleep(sleep);
                rand = random.nextInt(maxLight);
                mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.TVRoom.HUE_6_COLOR, rand + ",100,100");
                Thread.sleep(sleep);
                mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.Bathroom.HUE_4, "OFF");
                Thread.sleep(sleep);
                rand = random.nextInt(maxLight);
                mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.Bathroom.HUE_3_COLOR, rand + ",100,100");
            }

            mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.TVRoom.HUE_1, "OFF");
            mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.TVRoom.HUE_2, "OFF");
            mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.TVRoom.HUE_3, "OFF");
            mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.TVRoom.HUE_4, "OFF");
            mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.TVRoom.HUE_5, "OFF");
            mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.TVRoom.HUE_6, "OFF");
            mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.Bathroom.HUE_1, "OFF");
            mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.Bathroom.HUE_2, "OFF");
            mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.Bathroom.HUE_3, "OFF");
            mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.Bathroom.HUE_4, "OFF");
            mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.Bathroom.HUE_5, "OFF");
            mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.Bathroom.HUE_6, "OFF");
            mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.Doors.TVROOM_DOOR_1, "ON");
            mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.Doors.TVROOM_DOOR_2, "ON");
            mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Kitchen.RANGE_HOOD_LIGHT, "OFF");

            mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Shutter.MAIN_SHUTTER_1, "UP");
            mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Shutter.MAIN_SHUTTER_2, "UP");
            mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Shutter.MAIN_SHUTTER_3, "UP");
        } catch (InterruptedException | IOException | SonosControllerException ex) {
            log(Level.SEVERE, ex);
            Logger.getLogger(BasicBehaviour.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /*private void startRedBallTracker() {
        try {
            tracker.registerTarget("RedBall", 0.15f);
            tracker.setMode("Move");
            tracker.track("RedBall");
        } catch (CallError | InterruptedException ex) {
            Logger.getLogger(BasicBehaviour.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void stopRedBallTracker() {
        try {
            tracker.stopTracker();
            tracker.unregisterAllTargets();
        } catch (CallError | InterruptedException ex) {
            Logger.getLogger(BasicBehaviour.class.getName()).log(Level.SEVERE, null, ex);
        }
    }*/

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
                    topic = dialog.loadTopic("/home/nao/Questions.top");
                    dialog.activateTopic(topic);
                    dialog.subscribe(Constants.APP_NAME);
                    //dialog.forceInput("xxx");
                    dialog.forceOutput();
                    dialog.forceOutput();
                    System.out.println("Subscribe dialog");
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
//                    Object[] trajectory = new Object[3];
//                    trajectory[0] = "Composed";
//                    Object[] holonomic1 = new Object[4];
//                    holonomic1[0] = "Holonomic";
//
//                    Object[] line1 = new Object[2];
//                    line1[0] = "Line";
//                    line1[1] = new float[]{1.0f, 0.0f};
//
//                    holonomic1[1] = line1;
//                    holonomic1[2] = 0.0f;
//                    holonomic1[3] = 5.0f;
//
//                    trajectory[1] = holonomic1;
//
//                    Object[] holonomic2 = new Object[4];
//                    holonomic1[0] = "Holonomic";
//
//                    Object[] line2 = new Object[2];
//                    line1[0] = "Line";
//                    line1[1] = new float[]{-1.0f, 0.0f};
//
//                    holonomic1[1] = line2;
//                    holonomic1[2] = 0.0f;
//                    holonomic1[3] = 10.0f;
//
//                    trajectory[1] = holonomic1;
//                    trajectory[2] = holonomic2;
//                    
                    //Object obj[] = new Object[2];
                    //obj[0] = "Line";
                    //obj[1] = new float[]{1.0f, 0.0f};
//                    
//
//                    navigation.moveAlong(trajectory);
                    //navigation.moveAlong(obj);

                    //Arraylist<Object>
                    //checkDestination();
                    break;

                case Constants.Steps.STEP_END:
                    //memory.unsubscribeToEvent(speechRecognitionId);
                    //speechRecognition.pause(true);
                    //speechRecognition.unsubscribe(Constants.APP_NAME);
                    //speechRecognition.removeAllContext();
                    mQTTConnectionManager.disconnect();
                    dialog.unsubscribe(Constants.APP_NAME);
                    dialog.deactivateTopic(topic);
                    dialog.unloadTopic(topic);
                    //dialog.resetAll();
                    //speechRecognition.stop(0);
                    //speechRecognition.exit();
                    //motion.rest();
                    application.stop();
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
            try {
                tracker.stopTracker();
                tracker.unregisterAllTargets();
                stateMachine(Constants.Steps.STEP_END);

            } catch (CallError | InterruptedException ex) {
                Logger.getLogger(BasicBehaviour.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    public void onWordRecognizedForMoving(Object words) throws InterruptedException, CallError {
        //textToSpeech.say("Wort gefunden");
        speechRecognition.pause(true);
        awareness.pauseAwareness();

        String word = ((List<String>) words).get(0);
        System.out.println("Word " + word);

        //textToSpeech.say(word);
        /*if (word.equals("next")) {
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
        }*/
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

                connectionManager.openHabPostRequest("items/Multimediawand_HUE6_Toggle", "ON");
                connectionManager.openHabPostRequest("items/HMScheibentransparenz2_1_State", "OFF");
                restartSpeecheRecognition();
                break;

            case "licht aus":
                connectionManager.openHabPostRequest("items/Multimediawand_HUE6_Toggle", "OFF");
                connectionManager.openHabPostRequest("items/HMScheibentransparenz2_1_State", "ON");
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

    public void onPlayMusic(String title) throws IOException, SonosControllerException {
        System.out.println(Constants.APP_NAME + " : onPlayMusic " + title);
        sonos.setVolume(50);
        sonos.playUri("x-file-cifs://192.168.0.10/Medialib/Audio/Pepper/" + title, title);
    }

    @Override
    public void onSubscription(String item, String value) {
        String itemDescription = item.split("\\/")[3];

        System.out.println("onSubscription: " + itemDescription);

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
                        memory.raiseEvent("WindowOpend", itemDescription);
                        //animatedSpeech.say("Oh, ich sehe, ein Fenster ist geöffnet");
                    } else if (value.equals("CLOSED")) {
                        //animatedSpeech.say("Sehr gut, das Fenster ist wieder geschlossen.");
                        System.out.println("onSubscription: " + itemDescription + " " + value);
                        memory.raiseEvent("WindowClosed", itemDescription);
                        //mQTTConnectionManager.unsubscribeOfItems(Constants.MQTTTopics.Window.WINDOWS);
                    }
                } catch (CallError ex) {
                    Logger.getLogger(BasicBehaviour.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InterruptedException ex) {
                    Logger.getLogger(BasicBehaviour.class.getName()).log(Level.SEVERE, null, ex);
                }

                break;
            default:
        {
            try {
                memory.insertData(item, value);
            } catch (CallError | InterruptedException ex) {
                Logger.getLogger(BasicBehaviour.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        }
    }

    private void log(String message) {
        if (Constants.Config.DEBUG) {
            logger.info(message);
        }
    }

    private void log(Level loglevel, String message) {
        if (Constants.Config.DEBUG) {
            logger.log(loglevel, message);
        }
    }

    private void log(Level loglevel, Exception ex) {
        if (Constants.Config.DEBUG) {
            logger.log(loglevel, null, ex);
        }
    }

    public void setIsFullConcierge(boolean isFullConcierge) {
        this.isFullConcierge = isFullConcierge;
    }

    public boolean isIsFullConcierge() {
        return isFullConcierge;
    }

}

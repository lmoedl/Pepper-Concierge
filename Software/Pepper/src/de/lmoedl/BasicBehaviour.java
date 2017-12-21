/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.lmoedl;

import com.aldebaran.qi.Application;
import com.aldebaran.qi.CallError;
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
import com.aldebaran.qi.helper.proxies.ALPhotoCapture;
import com.aldebaran.qi.helper.proxies.ALSoundLocalization;
import com.aldebaran.qi.helper.proxies.ALSpeechRecognition;
import com.aldebaran.qi.helper.proxies.ALSystem;
import com.aldebaran.qi.helper.proxies.ALTextToSpeech;
import com.aldebaran.qi.helper.proxies.ALTracker;
import com.aldebaran.qi.helper.proxies.ALVideoDevice;
import com.vmichalak.sonoscontroller.SonosDevice;
import com.vmichalak.sonoscontroller.exception.SonosControllerException;
import de.lmoedl.interfaces.MQTTSubscriberCallbackInterface;
import java.io.IOException;
import java.nio.ByteBuffer;
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
    private ALTracker tracker;
    private ALAnimationPlayer animationPlayer;
    private ALNavigation navigation;
    private ALTabletService tabletService;
    private ALSystem system;
    private ALVideoDevice video;
    private ALPhotoCapture photoCapture;

    private ConnectionManager connectionManager;
    private MQTTConnectionManager mQTTConnectionManager;
    private SonosDevice sonos;

    private long eventSubscriptionIdStart = 0;
    private long windowOpendID = 0;
    private long windowClosedID = 0;
    //private long redballID = 0;
    
    private int topCamera = 0;
    private int resolution = 2; // 640 x 480
    private int colorspace = 11; // RGB
    private int frameRate = 10; // FPS
    private String moduleName;

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

        try {
            memory = new ALMemory(session);
            awareness = new ALBasicAwareness(session).async();
            motion = new ALMotion(session);
            textToSpeech = new ALTextToSpeech(session);
            animatedSpeech = new ALAnimatedSpeech(session);
            dialog = new ALDialog(session);
            tracker = new ALTracker(session);
            animationPlayer = new ALAnimationPlayer(session);
            connectionManager = new ConnectionManager();
            mQTTConnectionManager = new MQTTConnectionManager(this);
            navigation = new ALNavigation(session);
            tabletService = new ALTabletService(session);
            sonos = new SonosDevice(Constants.Config.SONOS_URL);
            system = new ALSystem(session);
            video = new ALVideoDevice(session);
            photoCapture = new ALPhotoCapture(session);
            
            config();
        } catch (Exception ex) {
            log(Level.SEVERE, ex);
        }
    }

    public void start() {
        try {
            /*try {
            //stateMachine(Constants.Steps.STEP_DIALOG);
            //startConcierge();
            //putHeadUp();
            goodby();
            } catch (CallError | InterruptedException | IOException | SonosControllerException ex) {
            Logger.getLogger(BasicBehaviour.class.getName()).log(Level.SEVERE, null, ex);
            }*/
            
            bathRoom();
        } catch (InterruptedException ex) {
            Logger.getLogger(BasicBehaviour.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(BasicBehaviour.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SonosControllerException ex) {
            Logger.getLogger(BasicBehaviour.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(BasicBehaviour.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void config() throws CallError, InterruptedException, Exception {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                disconnectAll();
                System.out.println("shutdownhook called");
            }
        }));

        sonos.setMute(false);
        textToSpeech.setLanguage(Constants.LANGUAGE);
        dialog.setLanguage(Constants.LANGUAGE);
        motion.setExternalCollisionProtectionEnabled("All", true);
        motion.setWalkArmsEnabled(Boolean.TRUE, Boolean.TRUE);

        tabletService.enableWifi();
        tabletService.hideWebview();
        tabletService.hideImage();

        motion.setOrthogonalSecurityDistance(0.15f);
        motion.wakeUp();

        //memory.subscribeToEvent("RearTactilTouched", "onTouchEnd::(f)", this);
        memory.subscribeToEvent("PlayMusic", "onPlayMusic::(s)", this);
        memory.subscribeToEvent("SubscribeMQTTTopic", "onSubscribeMQTTTopic::(s)", this);
        memory.subscribeToEvent("UnsubscribeMQTTTopic", "onUnsubscribeMQTTTopic::(s)", this);
        memory.subscribeToEvent("GetValue", "onGetValue::(s)", this);
        memory.subscribeToEvent("UnsubscribeMQTTTopic", "onUnsubscribeMQTTTopic::(s)", this);
        memory.subscribeToEvent("PublishMQTTMessage", "onPublishMQTTMessage::(s)", this);
        memory.subscribeToEvent("SpeechRecognitionOff", "onSpeechRecognitionOff::(s)", this);
        memory.subscribeToEvent("OpenUrl", "onOpenUrl::(s)", this);
        memory.subscribeToEvent("TakePicture", "onTakePicture::(s)", this);

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
        eventSubscriptionIdStart = memory.subscribeToEvent("RearTactilTouched", "onTouchHead::(f)", this);
        memory.subscribeToEvent("HandRightBackTouched", "onRightHandTouched::(f)", this);

        resetEnvironment();
    }

    public void resetEnvironment() {
        mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Shutter.INDOOR_SHUTTER_1, "UP");
        mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Shutter.INDOOR_SHUTTER_2, "UP");
        mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Shutter.INDOOR_SHUTTER_3, "UP");
        mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Shutter.INDOOR_SHUTTER_4, "UP");
        mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.Doors.TVROOM_DOOR_1, "ON");
        mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Lights.Doors.TVROOM_DOOR_2, "ON");
    }

    public void onTouchHead(Float value) throws InterruptedException, CallError, IOException, SonosControllerException, Exception {
        log(Constants.APP_NAME + " : Touch " + value);
        if (value == 1.0) {
            motion.wakeUp();
            animationPlayer.run("animations/Stand/Waiting/AirGuitar_1");

            if (Constants.Config.DEBUG && !Constants.Config.HEADLESS) {
                memory.unsubscribeToEvent(eventSubscriptionIdStart);
                memory.subscribeToEvent("RearTactilTouched", "onTouchEnd::(f)", this);
            }

            startConcierge();
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

        putHeadUp();
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
        putHeadUp();
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
        putHeadUp();
        try {
            boolean isUrlLoaded = tabletService.loadUrl(Constants.Config.RATING_URL);
            log("goodby_isUrlLoaded: " + isUrlLoaded);
            boolean isShowWebview = tabletService.showWebview();
            log("goodby_isShowWebview: " + isShowWebview);
            loadTopic("/home/nao/Goodby.top");
            dialog.forceOutput();

            //if (!Constants.Config.DEBUG) {
                //animationPlayer.run("animations/Stand/Waiting/FunnyDancer_1");
                lightShow();
            //}

            //if (Constants.Config.DEBUG) {
            //    System.out.println("resumeGoodby debug: " + Constants.Config.DEBUG);
                resumeGoodby();
            //}

            /*new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    resumeGoodby();
                }
            }, 40000);*/

        } catch (CallError | InterruptedException ex) {
            log(Level.SEVERE, ex);
        }

    }

    private void resumeGoodby() {
        try {
            dialog.forceOutput();
            unloadTopic();

            sonos.setBass(0);
            sonos.setVolume(40);
            sonos.playUri("x-rincon-mp3radio://http://listen.technobase.fm/tunein-mp3-pls", "Technobase.fm");
        } catch (CallError | InterruptedException | IOException | SonosControllerException ex) {
            log(Level.SEVERE, ex);
        }
    }

    private void resumeTvRoom() throws CallError, InterruptedException, IOException, SonosControllerException, Exception {
        log("resumeTvRoom");
        boolean success;

        mQTTConnectionManager.unsubscribeOfItems(Constants.MQTTTopics.Window.WINDOWS);
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
        putHeadUp();
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
        putHeadUp();
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
        putHeadUp();
        boolean success;
        log("Kitchen");
        loadTopicWithForceOutput("/home/nao/Kitchen.top");

        success = (boolean) motion.call("moveTo", 1f, 0f, 0f).get();
        log("kitchen_\"moveTo\", 1f, 0f, 0f_" + success);
        success = (boolean) motion.call("moveTo", 0f, 0f, new Float(Math.toRadians(-90))).get();
        List<Float> old = motion.getRobotPosition(false);
        success = (boolean) motion.call("moveTo", 2.5f, 0f, 0f).get();
        log("kitchen_\"moveTo\", 2.5f, 0f, 0f_" + success);
        checkDestination(success, old, motion.getRobotPosition(false), new float[]{2.5f, 0f, 0f});
        success = (boolean) motion.call("moveTo", 0f, 0f, new Float(Math.toRadians(180))).get();

        if (isFullConcierge) {
            goodby();
        }
    }

    public void goodby() throws CallError, InterruptedException, IOException, SonosControllerException {
        log("goodby");
        putHeadUp();
        //boolean isWifiConfig = tabletService.configureWifi("wpa", "SHLAB02-5", "91054319");
        //System.out.println("isWifiConfig: " + isWifiConfig);
        //boolean isConnectWifi = tabletService.connectWifi("SHLAB02-5");
        //System.out.println("isConnectWifi: " + isConnectWifi);
        loadTopic("/home/nao/Questions.top");
        dialog.forceOutput();

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
        /*try {
            Thread showThrad = new Thread(new Runnable() {
                @Override
                public void run() {*/

        try {
            mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Shutter.MAIN_SHUTTER_1, "DOWN");
            mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Shutter.MAIN_SHUTTER_2, "DOWN");
            mQTTConnectionManager.publishToItem(Constants.MQTTTopics.Shutter.MAIN_SHUTTER_3, "DOWN");
            Thread.sleep(6000);

            sonos.setMute(false);
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
            }, 20000);

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
            
            Thread.sleep(4000);
        } catch (InterruptedException | IOException | SonosControllerException ex) {
            log(Level.SEVERE, ex);
            Logger.getLogger(BasicBehaviour.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        
        resumeGoodby();

        //}
        /* });
            showThrad.start();
            showThrad.join();
            
            
        } catch (InterruptedException ex) {
            Logger.getLogger(BasicBehaviour.class.getName()).log(Level.SEVERE, null, ex);
        }*/
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
            switch (step) {
                case Constants.Steps.STEP_STARUP:
                    motion.wakeUp();

                    //Configure the awareness of the robot
                    //humanDetectedId = 
                    memory.subscribeToEvent("ALBasicAwareness/HumanTracked", "onHumanTracked::(i)", this);

                    textToSpeech.say("Hallo");
                    break;

                case Constants.Steps.STEP_DIALOG:
                    dialog.setLanguage(Constants.LANGUAGE);
                    //System.out.println(getClass().getClassLoader().getResource("TestDialog.top").getPath());
                    topic = dialog.loadTopic("/home/nao/Questions.top");
                    dialog.activateTopic(topic);
                    dialog.subscribe(Constants.APP_NAME);
                    //dialog.forceInput("xxx");
                    dialog.forceOutput();
                    //dialog.forceOutput();
                    System.out.println("Subscribe dialog");
                    break;

                case Constants.Steps.STEP_END:
                    //mQTTConnectionManager.disconnect();
                    dialog.unsubscribe(Constants.APP_NAME);
                    dialog.deactivateTopic(topic);
                    dialog.unloadTopic(topic);
                    //motion.rest();
                    //application.stop();
                    break;
            }
        } catch (Exception ex) {
            Logger.getLogger(BasicBehaviour.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void onTouchEnd(Float value) {
        System.out.println(Constants.APP_NAME + " : Touch " + value);
        if (value == 1.0) {
            //tracker.stopTracker();
            //tracker.unregisterAllTargets();
            stateMachine(Constants.Steps.STEP_END);
        }
    }

    public void onPlayMusic(String url) throws IOException, SonosControllerException {
        System.out.println(Constants.APP_NAME + " : onPlayMusic " + url);
        String[] splittedParts = url.split("/");
        sonos.setMute(false);
        sonos.setVolume(50);
        sonos.playUri(url, splittedParts[splittedParts.length - 1]);
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
                    } else if (value.equals("CLOSED")) {
                        System.out.println("onSubscription: " + itemDescription + " " + value);
                        memory.raiseEvent("WindowClosed", itemDescription);
                    }
                } catch (CallError ex) {
                    Logger.getLogger(BasicBehaviour.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InterruptedException ex) {
                    Logger.getLogger(BasicBehaviour.class.getName()).log(Level.SEVERE, null, ex);
                }

                break;
            default:

                try {
                    memory.insertData(item, value);
                } catch (CallError | InterruptedException ex) {
                    Logger.getLogger(BasicBehaviour.class.getName()).log(Level.SEVERE, null, ex);
                }

        }
    }

    public void onOpenUrl(String url) {
        try {
            boolean isUrlLoaded = tabletService.loadUrl(url);
            log("onOpenUrl_isUrlLoaded: " + isUrlLoaded);
            boolean isShowWebview = tabletService.showWebview();
            log("onOpenUrl_isShowWebview: " + isShowWebview);
        } catch (CallError | InterruptedException ex) {
            Logger.getLogger(BasicBehaviour.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void onRightHandTouched(Float touch) {
        if (touch == 1.0) {
            System.out.println("right hand touched");
            try {
                List<String> loadedTopics = dialog.getAllLoadedTopics();
                if (loadedTopics.isEmpty()) {
                    loadTopic("/home/nao/Questions.top");
                } else {
                    dialog.unsubscribe(Constants.APP_NAME);
                    dialog.deactivateTopic(topic);
                    dialog.unloadTopic(topic);
                }
            } catch (CallError | InterruptedException ex) {
                Logger.getLogger(BasicBehaviour.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void onTakePicture(String value){
        try {
            photoCapture.setResolution(resolution);
            photoCapture.setCameraID(topCamera);
            photoCapture.setPictureFormat("jpg");
            photoCapture.setColorSpace(colorspace);
            String pictureName = "Picture_" + new Date().getTime();
            List<Object> images = (List<Object>) photoCapture.takePicture("/home/nao/.local/share/PackageManager/apps/img/html", pictureName);
            log("obj: " + images.get(0));
            tabletService.showImage("http://198.18.0.1/apps/img/" + pictureName + ".jpg");
  
            /*try {
            byte[] rawData = takePicture();
            Picture picture = Util.toPicture(rawData);
            System.out.println("Picture filename: " + picture.getFilename());
            } catch (Exception ex) {
            Logger.getLogger(BasicBehaviour.class.getName()).log(Level.SEVERE, null, ex);
            }*/
        } catch (CallError ex) {
            Logger.getLogger(BasicBehaviour.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(BasicBehaviour.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    private byte[] takePicture() throws Exception {
        moduleName = video.subscribeCamera("demoAndroid", topCamera, resolution, colorspace, frameRate);
        System.out.format("subscribed with id: %s", moduleName);

        List<Object> image = (List<Object>) video.getImageRemote(moduleName);
        for (int i = 0; i<image.size(); i++){
            System.out.println(image.get(i));
        }
        ByteBuffer buffer = (ByteBuffer)image.get(6);
        byte[] rawData = buffer.array();
        video.unsubscribe(moduleName);
        boolean released = video.releaseImage(moduleName);
        System.out.println("released: " + released);
        return rawData;
}

    private void putHeadUp() {
        try {
            motion.angleInterpolationWithSpeed("Head", new ArrayList<>(Arrays.asList(0.0f, -0.3f)), 0.3f);
        } catch (CallError | InterruptedException ex) {
            Logger.getLogger(BasicBehaviour.class.getName()).log(Level.SEVERE, null, ex);
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
    
    public void rebootRobot(){
        try {
            system.reboot();
            System.exit(0);
        } catch (CallError | InterruptedException ex) {
            Logger.getLogger(BasicBehaviour.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void shutdownRobot(){
        try {
            system.shutdown();
            System.exit(0);
        } catch (CallError | InterruptedException ex) {
            Logger.getLogger(BasicBehaviour.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void disconnectAll(){
        mQTTConnectionManager.disconnect();
        application.stop();
    }

}

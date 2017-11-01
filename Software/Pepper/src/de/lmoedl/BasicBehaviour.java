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
import com.aldebaran.qi.helper.proxies.ALLogger;
import com.aldebaran.qi.helper.proxies.ALMemory;
import com.aldebaran.qi.helper.proxies.ALMotion;
import com.aldebaran.qi.helper.proxies.ALNotificationManager;
import com.aldebaran.qi.helper.proxies.ALSoundDetection;
import com.aldebaran.qi.helper.proxies.ALSoundLocalization;
import com.aldebaran.qi.helper.proxies.ALSpeechRecognition;
import com.aldebaran.qi.helper.proxies.ALTextToSpeech;
import com.aldebaran.qi.helper.proxies.ALTracker;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author lothar
 */
public class BasicBehaviour {

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

    private String currentState;
    
    private float x = 0f;
    private float y = 0f;
    private float teta = 0f;
    
    private long targetReachedId = 0;
    private long humanDetectedId = 0;
    private long humanDetectedDemoId = 0;
    

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
            
            config();
        } catch (Exception ex) {
            Logger.getLogger(BasicBehaviour.class.getName()).log(Level.SEVERE, null, ex);
        }

        stateMachine(Constants.Steps.STEP_STARUP);
    }
    
    private void config() throws CallError, InterruptedException, Exception{
        textToSpeech.setLanguage(Constants.LANGUAGE);
        motion.setExternalCollisionProtectionEnabled("All", true);
        motion.setWalkArmsEnabled(Boolean.TRUE, Boolean.TRUE);
        speechRecognition.pause(true);
        speechRecognition.setLanguage(Constants.LANGUAGE);
        speechRecognition.pause(false);
    }

    private void stateMachine(String step) {
        try {
            currentState = step;
            switch (step) {
                case Constants.Steps.STEP_STARUP:
                    motion.wakeUp();

                    //Configure the awareness of the robot
                    awareness.setEngagementMode(Constants.BasicAwareness.EngagementMode.SEMI_ENGAGED);
                    awareness.setTrackingMode(Constants.BasicAwareness.TrackingModes.MOVE_CONTEXTUALLY);
                    awareness.setStimulusDetectionEnabled(Constants.BasicAwareness.Stimulus.SOUND, true);
                    awareness.setStimulusDetectionEnabled(Constants.BasicAwareness.Stimulus.MOVEMENT, true);
                    awareness.setStimulusDetectionEnabled(Constants.BasicAwareness.Stimulus.PEOPLE, true);
                    awareness.setStimulusDetectionEnabled(Constants.BasicAwareness.Stimulus.TOUCH, true);

                    awareness.startAwareness();
                    animatedSpeech.say("Hallo i bims, 1 nicer Roboter vong Aussehen her.");
                    animatedSpeech.say("Ich bin so ein schlauer Hengst, ich kann dir sogar mit meinem Kopf folgen. Was sagst du dazu?");

                    memory.subscribeToEvent("RearTactilTouched", "onTouchEnd::(f)", this);
                    break;
                    
                    
                
                    
                case Constants.Steps.STEP_DEMO:
                    demo();
                                      
                    break;
                    
                    
                case Constants.Steps.STEP_END:
                    awareness.stopAwareness();
                    motion.rest();
                    speechRecognition.exit();
                    application.stop();
                    break;
            }
        } catch (Exception ex) {
            Logger.getLogger(BasicBehaviour.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void onTouchEnd(Float value) {
        System.out.println(Constants.APP_NAME + " : Touch " + value);
        if (value == 1.0) {
            //stateMachine(Constants.Steps.STEP_END);
            stateMachine(Constants.Steps.STEP_DEMO);
        }
    }
    
    
    
    public void demo() throws CallError, InterruptedException, Exception{
        //stateMachine(Constants.Steps.STEP_STARUP);
        humanDetectedDemoId = memory.subscribeToEvent("ALBasicAwareness/HumanTracked", "onHumanTrackedDemo::(i)", this);
            
        
        //String[] jointNames = new String[]{"LShoulderPitch", "LShoulderRoll", "LElbowYaw", "LElbowRoll"};
        //double[] arm1 = {Math.toRadians(-40), Math.toRadians(25), Math.toRadians(0), Math.toRadians(-40)};
                    
        //double[] arm2 = {-40, 25, 0, -40};
        //System.out.println(arm1);

    }
    
    public void onHumanTrackedDemo(Integer humanID) throws Exception {
        System.out.println("onHumanTracked: " + humanID);
        if (humanID >= 0) {
            System.out.println("onHumanTracked");

            try {
                awareness.pauseAwareness();
                memory.unsubscribeToEvent(humanDetectedDemoId);

                //textToSpeech.say("Mensch gefunden");
                animatedSpeech.say("Hallo i bims 1 dummer Roboter. ^startSound(Aldebaran/enu_ono_laugh_10)");
                animatedSpeech.say("Was kann ich für dich tun?");
                
                
                 ArrayList<String> words1 = new ArrayList<>();
                    words1.add("wo bist du");
                    words1.add("tanzen");
                    words1.add("ende");
   

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
    

    public void onWordRecognizedForMovingDemo(Object words) throws InterruptedException, CallError {

        speechRecognition.pause(true);
        awareness.pauseAwareness();

        String word = ((List<String>) words).get(0);
        System.out.println("Word " + word);

        if(word.equals("ende")){

            
        }else {
            switch(word){
                case "wo":
                case "bist":
                case "du":
                    animatedSpeech.say("^start(animations/Stand/Gestures/Hey_1) Hallöchen i bims hier ^wait(animations/Stand/Gestures/Hey_1)");
                    break;
                    
                case "tanzen":
                    animationPlayer.run("animations/Stand/Reactions/ShakeBody_1");
                    animationPlayer.run("animations/Stand/Waiting/FunnyDancer_1");
                    animationPlayer.run("animations/Stand/Waiting/Waddle_2");
                    animationPlayer.run("dialog_impossible_moves/animations/CrossArms");
                    
                    break;
                
                case "ende":
                    stateMachine(Constants.Steps.STEP_END);
                    break;
                
            }

        }

         animatedSpeech.async().say("Und was jetzt?");
         speechRecognition.pause(false);

    }

}

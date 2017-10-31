/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.lmoedl;

import com.aldebaran.qi.CallError;
import com.aldebaran.qi.Session;
import com.aldebaran.qi.helper.EventCallback;
import com.aldebaran.qi.helper.proxies.ALAnimatedSpeech;
import com.aldebaran.qi.helper.proxies.ALBasicAwareness;
import com.aldebaran.qi.helper.proxies.ALDialog;
import com.aldebaran.qi.helper.proxies.ALMemory;
import com.aldebaran.qi.helper.proxies.ALMotion;
import com.aldebaran.qi.helper.proxies.ALSpeechRecognition;
import com.aldebaran.qi.helper.proxies.ALTextToSpeech;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author lothar
 */
public class Speeche {
    private Session session;
    private ALMemory memory;
    private ALMotion motion;
    private ALTextToSpeech textToSpeech;
    private ALSpeechRecognition speechRecognition;
    
    public Speeche(Session session) {
        try {
            this.session = session;
            memory = new ALMemory(session);
            motion = new ALMotion(session);
            textToSpeech = new ALTextToSpeech(session);
            textToSpeech.setLanguage(Constants.LANGUAGE);
            speechRecognition = new  ALSpeechRecognition(session);
            speechRecognition.setLanguage(Constants.LANGUAGE);
        } catch (Exception ex) {
            Logger.getLogger(Speeche.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void detect() throws Exception{
        motion.wakeUp();
        memory.subscribeToEvent("MiddleTactilTouched", new EventCallback<Float>() {
                @Override
                public void onEvent(Float value) throws InterruptedException, CallError {
                    textToSpeech.say("Nicht anfassen");
                }
            });
        
        textToSpeech.say("Hallo hier bin ich");
        
//        try {
//            ArrayList<String> words1 = new ArrayList<>();
//            words1.add("geradeaus");
//            words1.add("links");
//            words1.add("rechts");
//            words1.add("stopp");
//            words1.add("schneller");
//            words1.add("langsamer");
//            words1.add("next");
//            
//            speechRecognition.pause(true);
//            speechRecognition.setVocabulary(words1, Boolean.FALSE);
//            speechRecognition.pause(false);
//            speechRecognition.subscribe(Constants.APP_NAME);
//            memory.subscribeToEvent("WordRecognized", new EventCallback<Object>() {
//                @Override
//                public void onEvent(Object t) throws InterruptedException, CallError {
//                    textToSpeech.say("Wort gefunden");
//                    String word = ((List<String>) t).get(0);
//                    System.out.println("Word " + word);
//        
//                    textToSpeech.say(word);
//                }
//            });
//            
//            
//            
//            textToSpeech.say("Wo soll ich hingehen?");
//        } catch (CallError ex) {
//            Logger.getLogger(Speeche.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (InterruptedException ex) {
//            Logger.getLogger(Speeche.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }
    
}

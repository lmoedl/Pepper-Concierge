/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.lmoedl;

/**
 *
 * @author lothar
 */
public class Constants {
    public static final String APP_NAME = "Pepper_Concierge";
    public static final String LANGUAGE = "German";
    
    class Start {
        public static final String ROBOT_URL = "pepper.local";
        public static final String ROBOT_PORT = "9559";
    }
    
    class Steps {
        public static final String STEP_STARUP = "STEP_STARTUP";
        public static final String STEP_MOVE_AROUND = "STEP_MOVE_AROUND";
        public static final String STEP_END = "STEP_END";
        public static final String STEP_FACERECOGNITION = "STEP_FACERECOGNITION";
        public static final String STEP_SOUNDLOCALIZATION = "STEP_SOUNDLOCALIZATION";
        public static final String STEP_COCHLOVIUS = "STEP_COCHLOVIUS";
    }
    
    class BasicAwareness {
        class Stimulus {
            public static final String SOUND = "Sound";
            public static final String MOVEMENT = "Movement";
            public static final String PEOPLE = "People";
            public static final String TOUCH = "Touch";
        }
        
        class EngagementMode {
            //(Default mode) when the robot is engaged with a user, it can be distracted by any stimulus, and engage with another person.
            public static final String UNENGAGED = "Unengaged";
            //as soon as the robot is engaged with a person, it stops listening to stimuli and stays engaged with the same person. If it loses the engaged person, it will listen to stimuli again and may engage with a different person.
            public static final String FULLY_ENGAGED = "FullyEngaged";
            //when the robot is engaged with a person, it keeps listening to the stimuli, and if it gets a stimulus, it will look in its direction, but it will always go back to the person it is engaged with. If it loses the person, it will listen to stimuli again and may engage with a different person.
            public static final String SEMI_ENGAGED = "SemiEngaged";
        }
        
        class TrackingModes {
            //the tracking only uses the head
            public static final String HEAD = "Head";
            // the tracking uses the head and the rotation of the body
            public static final String BODY_ROTATION = "BodyRotation";
            //the tracking uses the whole body, but doesn’t make it rotate
            public static final String WHOLE_BODY = "WholeBody";
            //the tracking uses the head and autonomously performs small moves such as approaching the tracked person, stepping backward, rotating, etc.
            public static final String MOVE_CONTEXTUALLY = "MoveContextually";
        }
         
    }
    
    
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.lmoedl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author lothar
 */
public class Constants {

    public static final String APP_NAME = "Pepper_Concierge";
    public static final String LANGUAGE = "German";

    public static class Start {

        public static final String ROBOT_URL = "pepper.local";
        public static final String ROBOT_PORT = "9559";
    }

    public static class Steps {

        public static final String STEP_STARUP = "STEP_STARTUP";
        public static final String STEP_MOVE_AROUND = "STEP_MOVE_AROUND";
        public static final String STEP_END = "STEP_END";
        public static final String STEP_FACERECOGNITION = "STEP_FACERECOGNITION";
        public static final String STEP_SOUNDLOCALIZATION = "STEP_SOUNDLOCALIZATION";
        public static final String STEP_COCHLOVIUS = "STEP_COCHLOVIUS";
        public static final String STEP_DIALOG = "STEP_DIALOG";
        public static final String STEP_MQTT = "STEP_MQTT";
        public static final String STEP_TRAJECTORY = "STEP_TRAJECTORY";
    }

    public static class BasicAwareness {

        public static class Stimulus {

            public static final String SOUND = "Sound";
            public static final String MOVEMENT = "Movement";
            public static final String PEOPLE = "People";
            public static final String TOUCH = "Touch";
        }

        public static class EngagementMode {

            //(Default mode) when the robot is engaged with a user, it can be distracted by any stimulus, and engage with another person.
            public static final String UNENGAGED = "Unengaged";
            //as soon as the robot is engaged with a person, it stops listening to stimuli and stays engaged with the same person. If it loses the engaged person, it will listen to stimuli again and may engage with a different person.
            public static final String FULLY_ENGAGED = "FullyEngaged";
            //when the robot is engaged with a person, it keeps listening to the stimuli, and if it gets a stimulus, it will look in its direction, but it will always go back to the person it is engaged with. If it loses the person, it will listen to stimuli again and may engage with a different person.
            public static final String SEMI_ENGAGED = "SemiEngaged";
        }

        public static class TrackingModes {

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

    public static class MQTTTopics {

        public static class Lights {

            public static class TVRoom {

                public static final String HUE_1 = "Multimediawand_HUE1_Toggle";
                public static final String HUE_2 = "Multimediawand_HUE2_Toggle";
                public static final String HUE_3 = "Multimediawand_HUE3_Toggle";
                public static final String HUE_4 = "Multimediawand_HUE4_Toggle";
                public static final String HUE_5 = "Multimediawand_HUE5_Toggle";
                public static final String HUE_6 = "Multimediawand_HUE6_Toggle";
                
                public static final String HUE_1_COLOR = "Multimediawand_HUE1_Color";
                public static final String HUE_2_COLOR = "Multimediawand_HUE2_Color";
                public static final String HUE_3_COLOR = "Multimediawand_HUE3_Color";
                public static final String HUE_4_COLOR = "Multimediawand_HUE4_Color";
                public static final String HUE_5_COLOR = "Multimediawand_HUE5_Color";
                public static final String HUE_6_COLOR = "Multimediawand_HUE6_Color";
            }

            public static class Bathroom {

                public static final String HUE_1 = "Bad_HUE1_Toggle";
                public static final String HUE_2 = "Bad_HUE2_Toggle";
                public static final String HUE_3 = "Bad_HUE3_Toggle";
                public static final String HUE_4 = "Bad_HUE4_Toggle";
                public static final String HUE_5 = "Bad_HUE5_Toggle";
                public static final String HUE_6 = "Bad_HUE6_Toggle";
                
                public static final String HUE_1_COLOR = "Bad_HUE1_Color";
                public static final String HUE_2_COLOR = "Bad_HUE2_Color";
                public static final String HUE_3_COLOR = "Bad_HUE3_Color";
                public static final String HUE_4_COLOR = "Bad_HUE4_Color";
                public static final String HUE_5_COLOR = "Bad_HUE5_Color";
                public static final String HUE_6_COLOR = "Bad_HUE6_Color";
            }

            public static class Doors {

                public static final String TVROOM_DOOR_1 = "HMScheibentransparenz1_1_State";
                public static final String TVROOM_DOOR_2 = "HMScheibentransparenz2_1_State";
            }
        }

        public static class Window {

            public static final String WINDOW_1 = "HMFS1_1_State";
            public static final String WINDOW_2 = "HMFS2_1_State";
            public static final String WINDOW_3 = "HMFS3_1_State";
            public static final String WINDOW_4 = "HMFS4_1_State";
            public static final String WINDOW_5 = "HMFS5_1_State";
            public static final String WINDOW_6 = "HMFS6_1_State";
            public static final String WINDOW_7 = "HMFS7_1_State";
            public static final String WINDOW_8 = "HMFS8_1_State";
            public static final String WINDOW_9 = "HMFS9_1_State";
            
            public static final List<String> WINDOWS = new ArrayList<String>(Arrays.asList(WINDOW_1, WINDOW_2, WINDOW_3, WINDOW_4, WINDOW_5, WINDOW_6, WINDOW_7, WINDOW_8, WINDOW_9));
        }
        
        public static class Shutter {
            public static final String MAIN_SHUTTER_1 = "SomfyHauptrollo_1_SomfyDeviceControl";
            public static final String MAIN_SHUTTER_2 = "SomfyHauptrollo_2_SomfyDeviceControl";
            public static final String MAIN_SHUTTER_3 = "SomfyHauptrollo_3_SomfyDeviceControl";
            public static final String INDOOR_SHUTTER_1 = "SomfyInnenjalousie_1_SomfyDeviceControl";
            public static final String INDOOR_SHUTTER_2 = "SomfyInnenjalousie_2_SomfyDeviceControl";
            public static final String INDOOR_SHUTTER_3 = "SomfyInnenjalousie_3_SomfyDeviceControl";
            public static final String INDOOR_SHUTTER_4 = "SomfyInnenjalousie_4_SomfyDeviceControl";
        }
        
        public static class Kitchen {
            public static final String RANGE_HOOD_LIGHT = "MieleHood_Switch";
            public static final String RANGE_HOOD_STATE = "MieleHood_Status";
            public static final String RANGE_HOOD_STOP = "MieleHood_Stop";
            public static final String RANGE_HOOD_STEP = "MieleHood_VentilationPower";
            
            public static final String REFRIGERATOR_COOLING_STATE = "MieleFridgeFreezer_FridgeStatus";
            public static final String REFRIGERATOR_TEMPERATURE = "MieleFridgeFreezer_CurrentTemperatureFridge";
            public static final String REFRIGERATOR_STATE = "MieleFridgeFreezer_Status";
            public static final String REFRIGERATOR_SUPERCOOL = "MieleFridgeFreezer_SuperCool";
            public static final String REFRIGERATOR_SUPERFREEZE = "MieleFridgeFreezer_SuperFreeze";
            public static final String REFRIGERATOR_SWITCH = "MieleFridgeFreezer_Switch";
        }
        
    }
}

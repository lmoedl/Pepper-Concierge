/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.lmoedl;

import com.aldebaran.qi.Application;
import com.aldebaran.qi.Session;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;



/**
 *
 * @author lothar
 */
public class Pepper {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new ConfigurationManager().loadConfigFile();
        
        String robotUrl = "tcp://" + Constants.Config.ROBOT_URL + ":" + Constants.Config.ROBOT_PORT;
        Application application = new Application(args, robotUrl);
        application.start();
        ConciergeController conciergeController = new ConciergeController(application);
        conciergeController.menu();
        /*BasicBehaviour basicBehaviour = new BasicBehaviour(application);
        basicBehaviour.start();*/
        
        application.run();
    }
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.lmoedl;

import com.aldebaran.qi.Application;
import com.aldebaran.qi.Session;
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
        String robotUrl = "tcp://" + Constants.Start.ROBOT_URL + ":" + Constants.Start.ROBOT_PORT;
        Application application = new Application(args, robotUrl);
        application.start();
        Session session = application.session();
        
        BasicBehaviour basicBehaviour = new BasicBehaviour(application);
        basicBehaviour.start();
        
        application.run();
    }
    
}

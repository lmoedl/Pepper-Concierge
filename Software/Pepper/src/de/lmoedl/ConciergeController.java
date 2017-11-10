/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.lmoedl;

import com.aldebaran.qi.Application;
import com.aldebaran.qi.Session;
import com.aldebaran.qi.helper.proxies.ALMemory;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author lothar
 */
public class ConciergeController {
    
    private Session session;
    private Application application;
    private ALMemory memory;

    public ConciergeController(Application application) {
        this.session = application.session();
        this.application = application;
        
        setup();
    }
    
    private void setup() {
        try {
            memory = new ALMemory(session);
        } catch (Exception ex) {
            Logger.getLogger(ConciergeController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
}

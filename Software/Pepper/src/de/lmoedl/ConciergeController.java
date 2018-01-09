/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.lmoedl;

import com.aldebaran.qi.Application;
import com.aldebaran.qi.Session;
import com.aldebaran.qi.helper.proxies.ALMemory;
import com.vmichalak.sonoscontroller.exception.SonosControllerException;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author lothar
 */
public class ConciergeController {

    private Session session;
    private Application application;
    private BasicBehaviour basicBehaviour;

    public ConciergeController(Application application) {
        this.session = application.session();
        this.application = application;

        setup();
    }

    private void setup() {
        basicBehaviour = new BasicBehaviour(application);
        //basicBehaviour.start();
    }

    public void menu() {
        
        if (Constants.Config.HEADLESS) {
            return;    
        }
        
        Scanner scanner = new Scanner(System.in);

        while (true) {
            try {
                int val = -1;
                printMenue();
                try {
                    val = scanner.nextInt();
                } catch (Exception e) {
                    System.err.println("Gib eine Zahl ein du Penner");
                }
                switch (val) {
                    case 0:
                        basicBehaviour.setIsFullConcierge(!basicBehaviour.isIsFullConcierge());
                        break;
                    case 1:
                        basicBehaviour.setIsFullConcierge(true);
                        basicBehaviour.startConcierge();
                        break;
                    case 2:
                        basicBehaviour.startConcierge();
                        break;
                    case 3:
                        basicBehaviour.tvRoom();
                        break;
                    case 4:
                        basicBehaviour.workingRoom();
                        break;
                    case 5:
                        basicBehaviour.bathRoom();
                        break;
                    case 6:
                        basicBehaviour.kitchen();
                        break;
                    case 7:
                        basicBehaviour.goodby();
                        break;
                    case 8:
                        basicBehaviour.resetEnvironment();
                        break;
                    case 9:
                        basicBehaviour.rebootRobot();
                        break;
                    case 10: 
                        basicBehaviour.shutdownRobot();
                        break;
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(ConciergeController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException | SonosControllerException ex) {
                Logger.getLogger(ConciergeController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(ConciergeController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void printMenue() {
        System.out.println("Bitte eine Zahl für die entsprechende Funktion eingeben");
        System.out.println("-------------------------------------------------------");
        System.out.println("0) Fortlaufende Tour ab ... starten: " + basicBehaviour.isIsFullConcierge());
        System.out.println("1) Komplette Tour starten");
        System.out.println("2) Begrüßung und allgemeine Informationen starten");
        System.out.println("3) TV Raum starten");
        System.out.println("4) Arbeitszimmer starten");
        System.out.println("5) Bad starten");
        System.out.println("6) Küche starten");
        System.out.println("7) Verabschiedung starten");
        System.out.println("8) Smart Home Labor auf Ausgangseinstellungen setzen");
        System.out.println("9) Reboot Pepper");
        System.out.println("10) Shutdown Pepper");
    }

}

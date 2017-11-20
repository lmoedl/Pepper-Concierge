/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.lmoedl;

import com.aldebaran.qi.Application;
import com.aldebaran.qi.Session;
import com.aldebaran.qi.helper.proxies.ALMemory;
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
    
    public void menu(){
        Scanner scanner = new Scanner(System.in);
        
        while (true){
            int val = -1;
            printMenue();
            try {
                val = scanner.nextInt();
            }catch (Exception e){
                System.err.println("Gib eine Zahl ein du Penner");
            } 
            switch(val){
                case 1:
                    
                break;
                case 2:
                    
                break;
                case 3:
                    
                break;
                case 4:
                    
                break;
                case 5:
                    
                break;
                case 6:
                    
                break;
                case 7:
                    
                break;
            }
        }
    }
    
    private void printMenue(){
        System.out.println("Bitte eine Zahl für die entsprechende Funktion eingeben");
        System.out.println("-------------------------------------------------------");
        System.out.println("1) Komplette Tour starten");
        System.out.println("2) Begrüßung und allgemeine Informationen starten");
        System.out.println("3) TV Raum starten");
        System.out.println("4) Arbeitszimmer starten");
        System.out.println("5) Bad starten");
        System.out.println("6) Küche starten");
        System.out.println("7) Verabschiedung starten");
    }
    
    
}

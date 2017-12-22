/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.lmoedl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author lothar
 */
public class ConfigurationManager {
    
    public void loadConfigFile(String fileName){
        String json = readConfigFile(fileName);
        if (!json.equals("")) {
            parseJson(json);
        }
    }
    
    public void loadConfigFile(){
        loadConfigFile("./concierge.conf");
    }
    
    private String readConfigFile(String fileName){
        String line = "";
        String result = "";
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            while ((line = br.readLine()) != null) {
                result += line;
            }
        } catch (IOException ex) {
            Logger.getLogger(ConfigurationManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    
    private void parseJson(String json){
        try {
            JSONObject object = new JSONObject(json);
            String pepperIP = object.getString("pepperIP");
            boolean headless = object.getBoolean("headless");
            boolean debug = object.getBoolean("debug");
            String movieURL = object.getString("movieUrl");
            String sonosIP = object.getString("sonosIP");
            String ratingUrl = object.getString("ratingUrl");
            
            Constants.Config.ROBOT_URL = pepperIP;
            Constants.Config.HEADLESS = headless;
            Constants.Config.DEBUG = debug;
            Constants.Config.MOVIE_URL = movieURL;
            Constants.Config.SONOS_URL = sonosIP;
            Constants.Config.RATING_URL = ratingUrl;
            
            //Write into log file
            //System.out.println("pepperIP: " + pepperIP + " headless: " + headless + " debug: " + debug + " movieURL: " + movieURL + " sonosIP:" + sonosIP);
            
        } catch (JSONException ex) {
            Logger.getLogger(ConfigurationManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
}

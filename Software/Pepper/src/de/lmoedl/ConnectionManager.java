/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.lmoedl;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author lothar
 */
public class ConnectionManager {
    private String basicUrl = "http://192.168.0.11:8080/rest/";

    public ConnectionManager(String basicUrl) {
        this.basicUrl = basicUrl;
    }

    public ConnectionManager() {
    }
    
    public void sendPostRequest(String path, String data){
        try {
            URL url = new URL(basicUrl + path);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Content-Type", "text/plain");
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.connect();
           
            
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(data.getBytes());
            
            InputStream inputStream = connection.getInputStream();
            System.out.println(connection.getResponseCode());
            
            inputStream.close();
            outputStream.close();
            connection.disconnect();
            
        } catch (MalformedURLException ex) {
            Logger.getLogger(ConnectionManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ConnectionManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    public String getBasicUrl() {
        return basicUrl;
    }

    public void setBasicUrl(String basicUrl) {
        this.basicUrl = basicUrl;
    }
    
    public static void switchOn(){
        new ConnectionManager().sendPostRequest("items/Multimediawand_HUE6_Toggle", "ON");
    }
}
